package com.example.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface QuestDao {
    @Query("SELECT * FROM quests ORDER BY timestamp DESC")
    fun getAllQuests(): Flow<List<QuestEntity>>
    
    @Query("SELECT * FROM quests WHERE id = :id LIMIT 1")
    suspend fun getQuestById(id: String): QuestEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuest(quest: QuestEntity)

    @Query("DELETE FROM quests WHERE id = :id")
    suspend fun deleteQuestById(id: String)

    @Query("DELETE FROM quests")
    suspend fun deleteAllQuests()
}
