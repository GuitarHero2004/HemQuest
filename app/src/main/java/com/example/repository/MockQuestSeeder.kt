package com.example.repository

import android.util.Log
import com.example.model.Quest
import com.example.model.QuestRequest
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.SetOptions
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

/**
 * MockQuestSeeder
 *
 * Checks if the 'mock_quests' Firestore collection is empty on app startup.
 * If empty, seeds curated sample heritage quests such as 'Bách Khoa Hẻm' (HCMUT Student Quests),
 * 'Ký Ức Thanh Đa', 'Làng Lồng Đèn Phú Bình', 'Hẻm Biệt Thự Cổ Pháp', 'Ẩm Thực Chợ Lớn',
 * and 'Biệt Động Sài Gòn' to ensure rich initial content for new users.
 */
class MockQuestSeeder(
    private val firestore: FirebaseFirestore? = try { FirebaseFirestore.getInstance() } catch (e: Exception) { null },
    private val auth: FirebaseAuth? = try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
) {

    private val moshi: Moshi by lazy {
        Moshi.Builder().add(KotlinJsonAdapterFactory()).build()
    }

    private val questAdapter by lazy {
        moshi.adapter(Quest::class.java)
    }

    private val mockQuestRepo by lazy {
        MockQuestRepository()
    }

    /**
     * Executes check on app startup. If 'mock_quests' is empty, populates it with curated heritage paths.
     *
     * @return true if seeding took place, false if already populated, unauthenticated, or unavailable.
     */
    suspend fun seedIfNeeded(): Boolean = withContext(Dispatchers.IO) {
        seederMutex.withLock {
            if (hasAlreadyCheckedAndSeeded) {
                return@withContext false
            }

            val fs = firestore
            if (fs == null) {
                Log.d(TAG, "FirebaseFirestore instance unavailable, relying on offline MockQuestRepository.")
                return@withContext false
            }

            try {
                // Check if mock_quests collection contains any documents
                val snapshot = fs.collection("mock_quests").limit(1).get().await()
                if (!snapshot.isEmpty) {
                    Log.d(TAG, "'mock_quests' Firestore collection already has data. Skipping seeder.")
                    hasAlreadyCheckedAndSeeded = true
                    return@withContext false
                }

                Log.i(TAG, "'mock_quests' Firestore collection is empty. Populating initial heritage paths (Bách Khoa Hẻm, Thanh Đa, Phú Bình, Chợ Lớn, ...)...")

                val curatedPathKeys = listOf(
                    "q10_bk",        // Bách Khoa Sài Gòn & Hẻm Sinh Viên (HCMUT Heritage)
                    "q_thanhda",      // Ký Ức Thanh Đa: Hẻm Bờ Sông & Cư Xá Cũ
                    "q11_crafts",     // Làng Lồng Đèn Phú Bình & Xưởng Thủ Công
                    "q3_french",      // Hẻm Biệt Thự Cổ & Cà Phê Nắng Sớm
                    "q5_food",        // Hẻm Ẩm Thực & Hội Quán Chợ Lớn
                    "q3_bunker",      // Biệt Động Sài Gòn & Hầm Bí Mật
                    "q1_alleys"       // Cà Phê Vợt & Hẻm Di Sản Sài Gòn
                )

                var seededCount = 0
                for (key in curatedPathKeys) {
                    val req = QuestRequest(
                        startingLocationName = key,
                        durationMinutes = 45,
                        interests = listOf(key),
                        language = "vi",
                        freeTextNotes = key
                    )

                    val quest = mockQuestRepo.getFallbackQuest(req)
                    val questJson = questAdapter.toJson(quest)

                    val questDocumentMap = hashMapOf<String, Any>(
                        "id" to quest.id,
                        "title" to quest.title,
                        "theme" to quest.theme,
                        "summary" to quest.summary,
                        "estimatedDistanceMeters" to quest.estimatedDistanceMetres,
                        "estimatedMinutes" to quest.estimatedMinutes,
                        "greenScore" to quest.greenScore.score,
                        "stopsCount" to quest.stops.size,
                        "stops" to quest.stops.map { stop ->
                            hashMapOf<String, Any>(
                                "id" to stop.id,
                                "placeId" to stop.placeId,
                                "name" to stop.name,
                                "category" to stop.category,
                                "latitude" to stop.latitude,
                                "longitude" to stop.longitude,
                                "whySelected" to stop.whySelected,
                                "story" to stop.story,
                                "factReference" to stop.factReference,
                                "challengePrompt" to stop.challenge.prompt,
                                "challengeType" to stop.challenge.type,
                                "photos" to stop.photos
                            )
                        },
                        "questJson" to questJson,
                        "isCuratedMock" to true,
                        "categoryKey" to key,
                        "createdAt" to System.currentTimeMillis(),
                        "updatedAt" to System.currentTimeMillis()
                    )

                    // Write to both 'mock_quests' and mirror to 'public_quests'
                    fs.collection("mock_quests").document(quest.id)
                        .set(questDocumentMap, SetOptions.merge())
                        .await()

                    fs.collection("public_quests").document(quest.id)
                        .set(questDocumentMap, SetOptions.merge())
                        .await()

                    seededCount++
                    Log.d(TAG, "Seeded quest '${quest.title}' [ID: ${quest.id}] to 'mock_quests'")
                }

                Log.i(TAG, "MockQuestSeeder completed successfully. Seeded $seededCount heritage quests into Firestore.")
                hasAlreadyCheckedAndSeeded = true
                true
            } catch (e: FirebaseFirestoreException) {
                // Handled gracefully when unauthenticated or restricted by Firestore security rules
                if (e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                    Log.d(TAG, "Firestore 'mock_quests' read/write access restricted (${e.message}). Using local offline quests.")
                } else {
                    Log.w(TAG, "Firestore access notice during MockQuestSeeder: ${e.code} (${e.message})")
                }
                hasAlreadyCheckedAndSeeded = true
                false
            } catch (e: Exception) {
                Log.w(TAG, "Notice executing MockQuestSeeder: ${e.message}. Using local offline quests.")
                hasAlreadyCheckedAndSeeded = true
                false
            }
        }
    }

    companion object {
        private const val TAG = "MockQuestSeeder"
        private val seederMutex = Mutex()
        @Volatile
        private var hasAlreadyCheckedAndSeeded = false

        /**
         * Convenience static method to trigger seeding in the background.
         */
        suspend fun seedIfNeeded(): Boolean {
            return MockQuestSeeder().seedIfNeeded()
        }
    }
}
