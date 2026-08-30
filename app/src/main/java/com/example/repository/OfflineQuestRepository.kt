package com.example.repository

import android.util.Log
import com.example.data.QuestDao
import com.example.data.QuestEntity
import com.example.model.Quest
import com.example.model.QuestRequest
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineQuestRepository(
    private val remoteRepository: GeminiQuestRepository,
    private val questDao: QuestDao
) {
    private val moshi = Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    private val questAdapter = moshi.adapter(Quest::class.java)

    fun getAllSavedQuests(): Flow<List<Quest>> {
        return questDao.getAllQuests().map { entities ->
            entities.mapNotNull { entity ->
                try {
                    questAdapter.fromJson(entity.questJson)
                } catch (e: Exception) {
                    Log.e("OfflineQuestRepository", "Failed to parse quest json for id ${entity.id}", e)
                    null
                }
            }
        }
    }

    suspend fun getQuestById(id: String): Quest? {
        return try {
            questDao.getQuestById(id)?.questJson?.let { questAdapter.fromJson(it) }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun saveQuestLocally(quest: Quest) {
        try {
            val json = questAdapter.toJson(quest)
            questDao.insertQuest(QuestEntity(id = quest.id, questJson = json))
            Log.d("OfflineQuestRepository", "Saved quest ${quest.id} to local DB")
        } catch (e: Exception) {
            Log.e("OfflineQuestRepository", "Failed to save quest locally", e)
        }
    }

    suspend fun pullAndCacheMockQuestsFromFirestore(): List<Quest> {
        return try {
            val quests = remoteRepository.fetchMockQuestsFromFirestore()
            for (quest in quests) {
                saveQuestLocally(quest)
            }
            quests
        } catch (e: Exception) {
            Log.w("OfflineQuestRepository", "Error syncing Firestore mock quests: ${e.message}", e)
            emptyList()
        }
    }

    suspend fun getOrFetchQuest(request: QuestRequest): Quest {
        return try {
            val quest = remoteRepository.generateQuest(request)
            saveQuestLocally(quest)
            quest
        } catch (e: Exception) {
            Log.e("OfflineQuestRepository", "Failed to fetch new quest, might be offline. Checking cache...", e)
            val cached = questDao.getAllQuests().map { entities ->
                entities.mapNotNull { entity -> questAdapter.fromJson(entity.questJson) }
            }
            throw e // Throw for now or return a cached one if we want to fallback
        }
    }

    suspend fun verifyPhoto(
        bitmap: android.graphics.Bitmap,
        stopName: String,
        vietnameseName: String = "",
        challengePrompt: String,
        category: String = "",
        story: String = "",
        whySelected: String = "",
        culturalTip: String = "",
        successGuidance: String = "",
        questTheme: String = "",
        targetLatitude: Double? = null,
        targetLongitude: Double? = null,
        userLatitude: Double? = null,
        userLongitude: Double? = null,
        distanceMeters: Int? = null,
        language: String = "en"
    ): com.example.model.PhotoVerificationResult {
        return remoteRepository.verifyPhoto(
            bitmap = bitmap,
            stopName = stopName,
            vietnameseName = vietnameseName,
            challengePrompt = challengePrompt,
            category = category,
            story = story,
            whySelected = whySelected,
            culturalTip = culturalTip,
            successGuidance = successGuidance,
            questTheme = questTheme,
            targetLatitude = targetLatitude,
            targetLongitude = targetLongitude,
            userLatitude = userLatitude,
            userLongitude = userLongitude,
            distanceMeters = distanceMeters,
            language = language
        )
    }
}
