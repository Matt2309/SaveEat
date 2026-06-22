package com.mattiamularoni.saveeat.features.stats.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE user_id = :userId LIMIT 1")
    fun observe(userId: String): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE user_id = :userId LIMIT 1")
    suspend fun getByUserId(userId: String): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: UserStatsEntity)
}
