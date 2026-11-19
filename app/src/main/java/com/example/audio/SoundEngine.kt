package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.sin

class SoundEngine(context: Context) {
    private val appContext = context.applicationContext
    private val sampleRate = 22050

    // Dedicated single-thread executor for zero-latency non-blocking SFX & haptics
    private val sfxExecutor = Executors.newSingleThreadExecutor { r ->
        Thread(r, "magic_sfx_thread").apply { isDaemon = true }
    }

    @Volatile
    private var musicEnabled = true

    @Volatile
    private var soundEnabled = true

    @Volatile
    private var vibrationEnabled = true

    @Volatile
    private var appInForeground = true

    @Volatile
    private var gameplayPaused = false

    private val released = AtomicBoolean(false)
    private var musicThread: Thread? = null
    private val musicLock = Any()

    private val vibrator: Vibrator? by lazy {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val manager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                manager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
        } catch (_: Exception) {
            null
        }
    }

    // Reusable static AudioTrack instances initialized off the UI thread
    @Volatile private var pickupTrack: AudioTrack? = null
    @Volatile private var correctTrack: AudioTrack? = null
    @Volatile private var wrongTrack: AudioTrack? = null
    @Volatile private var levelCompleteTrack: AudioTrack? = null
    @Volatile private var buttonTapTrack: AudioTrack? = null
    @Volatile private var coinTrack: AudioTrack? = null

    init {
        sfxExecutor.execute {
            if (released.get()) return@execute
            pickupTrack = buildStaticTrack(synthToneSequence(listOf(523.25 to 0.055, 659.25 to 0.06), amplitude = 0.25))
            correctTrack = buildStaticTrack(synthToneSequence(listOf(587.33 to 0.07, 783.99 to 0.08, 1046.50 to 0.14), amplitude = 0.30))
            wrongTrack = buildStaticTrack(synthWrongBoing())
            levelCompleteTrack = buildStaticTrack(
                synthToneSequence(
                    listOf(523.25 to 0.09, 659.25 to 0.09, 783.99 to 0.09, 1046.50 to 0.14, 1318.51 to 0.24),
                    amplitude = 0.34
                )
            )
            buttonTapTrack = buildStaticTrack(synthToneSequence(listOf(620.0 to 0.04), amplitude = 0.18))
            coinTrack = buildStaticTrack(synthToneSequence(listOf(987.77 to 0.07, 1318.51 to 0.15), amplitude = 0.28))
        }
    }

    fun updateSettings(music: Boolean, sound: Boolean, vibration: Boolean) {
        musicEnabled = music
        soundEnabled = sound
        vibrationEnabled = vibration
        syncMusicPlayback()
    }

    fun setAppForeground(foreground: Boolean) {
        appInForeground = foreground
        syncMusicPlayback()
    }

    fun setGameplayPaused(paused: Boolean) {
        gameplayPaused = paused
        syncMusicPlayback()
    }

    private fun shouldPlayMusic(): Boolean =
        !released.get() && musicEnabled && appInForeground && !gameplayPaused

    private fun syncMusicPlayback() {
        synchronized(musicLock) {
            if (shouldPlayMusic()) {
                if (musicThread == null || musicThread?.isAlive != true) {
                    val thread = Thread({ runMusicStreamLoop() }, "magic_music_stream").apply {
                        isDaemon = true
                    }
                    musicThread = thread
                    thread.start()
                }
            } else {
                musicThread?.interrupt()
                musicThread = null
            }
        }
    }

    private fun runMusicStreamLoop() {
        val minBufBytes = AudioTrack.getMinBufferSize(
            sampleRate,
            AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        ).coerceAtLeast(4096)

        var streamTrack: AudioTrack? = null
        try {
            streamTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(minBufBytes * 2)
                .setTransferMode(AudioTrack.MODE_STREAM)
                .build()

            streamTrack.play()

            // Pre-synthesize the 16-beat marimba loop once (zero per-note allocations!)
            val notes = doubleArrayOf(
                523.25, 659.25, 783.99, 659.25,
                587.33, 659.25, 523.25, 392.00,
                440.00, 523.25, 659.25, 783.99,
                659.25, 587.33, 523.25, 0.0
            )
            val prebuiltBeats = Array(notes.size) { i ->
                val freq = notes[i]
                if (freq > 0.0) {
                    synthMarimbaBeat(freq, totalSec = 0.35, noteSec = 0.28, amplitude = 0.075)
                } else {
                    ShortArray((sampleRate * 0.35).toInt())
                }
            }

            var idx = 0
            while (shouldPlayMusic() && !Thread.currentThread().isInterrupted) {
                val beat = prebuiltBeats[idx % prebuiltBeats.size]
                val written = streamTrack.write(beat, 0, beat.size)
                if (written < 0) break
                idx++
            }
        } catch (_: Throwable) {
            // Safe fallback on headless or audio-less devices
        } finally {
            try {
                streamTrack?.pause()
                streamTrack?.flush()
                streamTrack?.stop()
                streamTrack?.release()
            } catch (_: Throwable) {
            }
        }
    }

    fun playPickup() {
        if (soundEnabled) replayStaticTrack { pickupTrack }
        vibrate(14)
    }

    fun playCorrect() {
        if (soundEnabled) replayStaticTrack { correctTrack }
        vibrate(24)
    }

    fun playWrong() {
        if (soundEnabled) replayStaticTrack { wrongTrack }
        vibrate(55)
    }

    fun playLevelComplete() {
        if (soundEnabled) replayStaticTrack { levelCompleteTrack }
        vibrate(65)
    }

    fun playButtonTap() {
        if (soundEnabled) replayStaticTrack { buttonTapTrack }
        vibrate(10)
    }

    fun playCoinReward() {
        if (soundEnabled) replayStaticTrack { coinTrack }
        vibrate(30)
    }

    fun vibrate(durationMs: Long) {
        if (!vibrationEnabled || released.get()) return
        sfxExecutor.execute {
            try {
                val vib = vibrator ?: return@execute
                if (!vib.hasVibrator()) return@execute
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    vib.vibrate(VibrationEffect.createOneShot(durationMs, VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vib.vibrate(durationMs)
                }
            } catch (_: Exception) {
            }
        }
    }

    private inline fun replayStaticTrack(crossinline trackProvider: () -> AudioTrack?) {
        if (released.get()) return
        sfxExecutor.execute {
            if (released.get()) return@execute
            val track = trackProvider() ?: return@execute
            try {
                if (track.playState == AudioTrack.PLAYSTATE_PLAYING) {
                    track.pause()
                }
                track.flush()
                track.reloadStaticData()
                track.play()
            } catch (_: Exception) {
            }
        }
    }

    private fun buildStaticTrack(samples: ShortArray): AudioTrack? {
        return try {
            val byteCount = samples.size * 2
            AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(byteCount)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()
                .apply {
                    write(samples, 0, samples.size)
                }
        } catch (_: Exception) {
            null
        }
    }

    private fun synthMarimbaBeat(
        freq: Double,
        totalSec: Double,
        noteSec: Double,
        amplitude: Double
    ): ShortArray {
        val totalSamples = (sampleRate * totalSec).toInt()
        val noteSamples = (sampleRate * noteSec).toInt().coerceAtMost(totalSamples)
        val out = ShortArray(totalSamples)
        for (i in 0 until noteSamples) {
            val t = i.toDouble() / sampleRate
            val env = exp(-t * 9.5) * (1.0 - exp(-t * 140.0))
            val wave = sin(2.0 * PI * freq * t) + 0.25 * sin(2.0 * PI * (freq * 2.0) * t)
            val v = (wave * env * amplitude * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
            out[i] = v.toShort()
        }
        return out
    }

    private fun synthToneSequence(notes: List<Pair<Double, Double>>, amplitude: Double): ShortArray {
        val chunks = notes.map { (freq, dur) ->
            val len = (sampleRate * dur).toInt()
            ShortArray(len) { i ->
                val t = i.toDouble() / sampleRate
                val env = exp(-t * 11.0) * (1.0 - exp(-t * 180.0))
                val wave = sin(2.0 * PI * freq * t) + 0.2 * sin(2.0 * PI * freq * 2.0 * t)
                (wave * env * amplitude * Short.MAX_VALUE)
                    .toInt()
                    .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                    .toShort()
            }
        }
        val total = chunks.sumOf { it.size }
        val result = ShortArray(total)
        var offset = 0
        chunks.forEach { chunk ->
            chunk.copyInto(result, offset)
            offset += chunk.size
        }
        return result
    }

    private fun synthWrongBoing(): ShortArray {
        val dur = 0.24
        val len = (sampleRate * dur).toInt()
        val out = ShortArray(len)
        for (i in 0 until len) {
            val t = i.toDouble() / sampleRate
            val freq = 260.0 - (t / dur) * 110.0 + 18.0 * sin(2.0 * PI * 24.0 * t)
            val env = exp(-t * 8.0) * (1.0 - exp(-t * 120.0))
            val wave = sin(2.0 * PI * freq * t) + 0.35 * sin(2.0 * PI * freq * 1.5 * t)
            out[i] = (wave * env * 0.28 * Short.MAX_VALUE)
                .toInt()
                .coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt())
                .toShort()
        }
        return out
    }

    fun release() {
        if (released.compareAndSet(false, true)) {
            syncMusicPlayback()
            sfxExecutor.execute {
                listOf(pickupTrack, correctTrack, wrongTrack, levelCompleteTrack, buttonTapTrack, coinTrack)
                    .forEach { track ->
                        try {
                            track?.stop()
                            track?.release()
                        } catch (_: Exception) {
                        }
                    }
            }
            sfxExecutor.shutdown()
        }
    }
}
