package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface LevelProgressDao {
    @Query("SELECT * FROM level_progress ORDER BY levelNumber ASC")
    fun observeAllProgress(): Flow<List<LevelProgressEntity>>

    @Query("SELECT * FROM level_progress WHERE levelNumber = :levelNumber LIMIT 1")
    suspend fun getLevelProgress(levelNumber: Int): LevelProgressEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(progress: LevelProgressEntity)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAllIgnore(items: List<LevelProgressEntity>)
}
