package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Language
import androidx.compose.material.icons.rounded.MusicNote
import androidx.compose.material.icons.rounded.OfflineBolt
import androidx.compose.material.icons.rounded.Restore
import androidx.compose.material.icons.rounded.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.PlayerPrefs
import com.example.localization.LocalizedStrings
import com.example.model.AppLanguage
import com.example.ui.components.CandyButton
import com.example.ui.components.RoundIconPillButton
import com.example.ui.theme.BerryPink
import com.example.ui.theme.BerryPinkSoft
import com.example.ui.theme.CreamBackground
import com.example.ui.theme.CreamBorder
import com.example.ui.theme.CreamSurface
import com.example.ui.theme.DeepCocoa
import com.example.ui.theme.MagicPurple
import com.example.ui.theme.MagicPurpleDark
import com.example.ui.theme.MagicPurpleSoft
import com.example.ui.theme.MintGreen
import com.example.ui.theme.MintGreenDark
import com.example.ui.theme.MintGreenSoft
import com.example.ui.theme.SkyBlue
import com.example.ui.theme.SkyBlueSoft
import com.example.ui.theme.SoftCocoa
import com.example.ui.theme.TangerineOrange
import com.example.ui.theme.TangerineOrangeDark
import com.example.ui.theme.TangerineOrangeSoft
import kotlinx.coroutines.delay

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    prefs: PlayerPrefs,
    strings: LocalizedStrings,
    restoredEventCounter: Int,
    onToggleMusic: (Boolean) -> Unit,
    onToggleSound: (Boolean) -> Unit,
    onToggleVibration: (Boolean) -> Unit,
    onSelectLanguage: (AppLanguage) -> Unit,
    onRestoreDefaults: () -> Unit,
    onBack: () -> Unit
) {
    BackHandler(onBack = onBack)

    var showRestoredToast by remember { mutableStateOf(false) }
    LaunchedEffect(restoredEventCounter) {
        if (restoredEventCounter > 0) {
            showRestoredToast = true
            delay(2000L)
            showRestoredToast = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CreamBackground)
            .windowInsetsPadding(WindowInsets.safeDrawing)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 560.dp)
                .align(Alignment.TopCenter)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RoundIconPillButton(
                    icon = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = strings.homeButton,
                    backgroundColor = MagicPurpleSoft,
                    iconTint = MagicPurpleDark,
                    onClick = onBack,
                    modifier = Modifier.testTag("settings_back")
                )
                Spacer(modifier = Modifier.width(14.dp))
                Text(
                    text = strings.settingsTitle,
                    style = MaterialTheme.typography.headlineLarge,
                    color = DeepCocoa
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            SettingsToggleCard(
                icon = Icons.Rounded.MusicNote,
                iconBg = MagicPurpleSoft,
                iconTint = MagicPurple,
                title = strings.musicLabel,
                statusText = if (prefs.musicEnabled) strings.onLabel else strings.offLabel,
                checked = prefs.musicEnabled,
                onCheckedChange = onToggleMusic,
                testTag = "music_switch"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsToggleCard(
                icon = Icons.AutoMirrored.Rounded.VolumeUp,
                iconBg = BerryPinkSoft,
                iconTint = BerryPink,
                title = strings.soundLabel,
                statusText = if (prefs.soundEnabled) strings.onLabel else strings.offLabel,
                checked = prefs.soundEnabled,
                onCheckedChange = onToggleSound,
                testTag = "sound_switch"
            )

            Spacer(modifier = Modifier.height(12.dp))

            SettingsToggleCard(
                icon = Icons.Rounded.Vibration,
                iconBg = TangerineOrangeSoft,
                iconTint = TangerineOrange,
                title = strings.vibrationLabel,
                statusText = if (prefs.vibrationEnabled) strings.onLabel else strings.offLabel,
                checked = prefs.vibrationEnabled,
                onCheckedChange = onToggleVibration,
                testTag = "vibration_switch"
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Language Selection Card
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .shadow(4.dp, RoundedCornerShape(24.dp))
                    .clip(RoundedCornerShape(24.dp))
                    .background(CreamSurface)
                    .border(2.dp, CreamBorder, RoundedCornerShape(24.dp))
                    .padding(16.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SkyBlueSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Language,
                            contentDescription = null,
                            tint = SkyBlue,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = strings.languageLabel,
                        style = MaterialTheme.typography.titleLarge,
                        color = DeepCocoa
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppLanguage.entries.forEach { lang ->
                        val selected = prefs.language == lang
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(if (selected) MagicPurple else CreamBackground)
                                .border(
                                    width = 1.5.dp,
                                    color = if (selected) MagicPurpleDark else CreamBorder,
                                    shape = RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectLanguage(lang) }
                                .padding(horizontal = 14.dp, vertical = 10.dp)
                                .testTag("language_option_${lang.code}")
                        ) {
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.labelLarge,
                                color = if (selected) Color.White else DeepCocoa
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Restore Defaults Button
            CandyButton(
                onClick = onRestoreDefaults,
                containerColor = TangerineOrange,
                shadowColor = TangerineOrangeDark,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("restore_button")
            ) {
                Icon(
                    imageVector = Icons.Rounded.Restore,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = strings.restoreButton,
                    style = MaterialTheme.typography.labelLarge,
                    color = Color.White
                )
            }

            AnimatedVisibility(visible = showRestoredToast) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 10.dp)
                        .clip(CircleShape)
                        .background(MintGreenSoft)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.restoredMessage,
                        style = MaterialTheme.typography.labelLarge,
                        color = MintGreenDark
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Offline Storage Notice Card
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(MintGreenSoft)
                    .border(1.5.dp, MintGreen.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
                    .padding(14.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.OfflineBolt,
                    contentDescription = null,
                    tint = MintGreen,
                    modifier = Modifier.size(26.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = strings.offlineNote,
                    style = MaterialTheme.typography.bodyMedium,
                    color = DeepCocoa,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleCard(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    statusText: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(22.dp))
            .clip(RoundedCornerShape(22.dp))
            .background(CreamSurface)
            .border(2.dp, CreamBorder, RoundedCornerShape(22.dp))
            .clickable { onCheckedChange(!checked) }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(iconBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = DeepCocoa
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.labelMedium,
                    color = SoftCocoa
                )
            }
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = MintGreen,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color(0xFFD6CEC4)
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}
