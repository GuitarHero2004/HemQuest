package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStats(): Flow<UserStatsEntity?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStatsSync(): UserStatsEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrUpdate(stats: UserStatsEntity)

    @Query("UPDATE user_stats SET totalSteps = totalSteps + :steps WHERE id = 1")
    suspend fun addSteps(steps: Int)

    @Query("UPDATE user_stats SET completedCheckpoints = completedCheckpoints + 1 WHERE id = 1")
    suspend fun incrementCheckpoints()
    
    @Query("UPDATE user_stats SET totalXp = totalXp + :xp WHERE id = 1")
    suspend fun addXp(xp: Int)

    @Query("UPDATE user_stats SET totalDistanceMeters = totalDistanceMeters + :meters WHERE id = 1")
    suspend fun addDistance(meters: Double)

    @Query("UPDATE user_stats SET completedQuestsCount = completedQuestsCount + 1 WHERE id = 1")
    suspend fun incrementCompletedQuests()

    @Query("UPDATE user_stats SET unlockedBadgeIds = :badgeIds WHERE id = 1")
    suspend fun updateUnlockedBadges(badgeIds: String)

    @Query("DELETE FROM user_stats")
    suspend fun deleteAllUserStats()
}
