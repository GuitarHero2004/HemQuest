package com.example.ui

import android.graphics.Bitmap
import android.location.Location
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.auth.AuthManager
import com.example.data.PassportPhotoDao
import com.example.data.PassportPhotoEntity
import com.example.data.QuestDao
import com.example.data.UserStatsDao
import com.example.data.UserStatsEntity
import com.example.model.CulturalBadge
import com.example.util.IdGenerator
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

data class LiveLocationState(
    val latitude: Double = 10.7741,
    val longitude: Double = 106.7028,
    val accuracyMeters: Float = 6.5f,
    val districtName: String = "Phường Sài Gòn, TP. Hồ Chí Minh",
    val sessionDistanceMeters: Double = 0.0,
    val sessionSteps: Int = 0,
    val isGpsActive: Boolean = true,
    val sessionStartTimeMillis: Long = System.currentTimeMillis()
)

data class FirestoreSyncState(
    val isSyncing: Boolean = false,
    val isSynced: Boolean = true,
    val lastSyncedTimeMillis: Long = System.currentTimeMillis(),
    val errorMessage: String? = null
)

class UserStatsViewModel(
    private val userStatsDao: UserStatsDao,
    private val questDao: QuestDao? = null,
    private val authManager: AuthManager? = null,
    private val notificationManager: com.example.util.AppNotificationManager? = null,
    private val passportPhotoDao: PassportPhotoDao? = null
) : ViewModel() {

    val userStats: StateFlow<UserStatsEntity> = userStatsDao.getUserStats()
        .map { it ?: UserStatsEntity() }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = UserStatsEntity()
        )

    val passportPhotos: StateFlow<List<PassportPhotoEntity>> = (passportPhotoDao?.getAllPassportPhotos()
        ?: MutableStateFlow(emptyList()))
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    private val _locationState = MutableStateFlow(LiveLocationState())
    val locationState: StateFlow<LiveLocationState> = _locationState.asStateFlow()

    private val _syncState = MutableStateFlow(FirestoreSyncState())
    val syncState: StateFlow<FirestoreSyncState> = _syncState.asStateFlow()

    private var previousLocation: Location? = null

    init {
        // Initialize if empty
        viewModelScope.launch {
            if (userStatsDao.getUserStatsSync() == null) {
                userStatsDao.insertOrUpdate(UserStatsEntity())
            }
            val uid = authManager?.currentUser?.uid ?: "local_user"
            if (authManager != null) {
                try {
                    authManager.syncUserData(uid)
                } catch (e: Exception) {
                    Log.w("UserStatsViewModel", "Initial Firestore sync error", e)
                }
            }
        }
    }

    /**
     * Factory reset: Clear all user stats, delete cached quests in local database, delete photos, and wipe Firestore
     */
    fun resetAllUserData(onComplete: (() -> Unit)? = null) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userStatsDao.deleteAllUserStats()
            } catch (e: Exception) {
                Log.w("UserStatsViewModel", "Failed to delete user stats table", e)
            }
            userStatsDao.insertOrUpdate(UserStatsEntity(id = 1))
            try {
                questDao?.deleteAllQuests()
            } catch (e: Exception) {
                Log.w("UserStatsViewModel", "Failed to clear quests", e)
            }
            try {
                passportPhotoDao?.deleteAllPassportPhotos()
            } catch (e: Exception) {
                Log.w("UserStatsViewModel", "Failed to clear passport photos", e)
            }
            val uid = authManager?.currentUser?.uid ?: "local_user"
            if (authManager != null) {
                try {
                    authManager.resetUserFirestoreData(uid)
                } catch (e: Exception) {
                    Log.w("UserStatsViewModel", "Failed to reset Firestore user data", e)
                }
            }
            previousLocation = null
            _locationState.update { LiveLocationState() }
            _syncState.update { FirestoreSyncState(isSyncing = false, isSynced = true) }
            withContext(Dispatchers.Main) {
                onComplete?.invoke()
            }
        }
    }

    /**
     * Clear active in-memory session states during Sign Out (without deleting cloud/account data).
     * Ensures clean slate for next account login without state crossover.
     */
    fun clearSessionOnSignOut() {
        previousLocation = null
        _locationState.update { LiveLocationState() }
        _syncState.update { FirestoreSyncState(isSyncing = false, isSynced = false) }
        viewModelScope.launch(Dispatchers.IO) {
            try {
                userStatsDao.deleteAllUserStats()
                userStatsDao.insertOrUpdate(UserStatsEntity(id = 1))
            } catch (e: Exception) {
                Log.w("UserStatsViewModel", "Failed to reset local session table", e)
            }
        }
    }

    /**
     * Process real-time location ping from GPS since app opened
     */
    fun onLocationUpdate(newLocation: Location?) {
        if (newLocation == null) return

        val prev = previousLocation
        val deltaDistance = if (prev != null) {
            val dist = prev.distanceTo(newLocation)
            // Filter out GPS jump spikes over 150m in a single update
            if (dist in 0.5f..150.0f) dist.toDouble() else 0.0
        } else {
            0.0
        }
        previousLocation = newLocation

        val detectedDistrict = detectDistrict(newLocation.latitude, newLocation.longitude)

        _locationState.update { current ->
            val newSessionDist = current.sessionDistanceMeters + deltaDistance
            val newSessionSteps = (newSessionDist / 0.75).toInt()
            current.copy(
                latitude = newLocation.latitude,
                longitude = newLocation.longitude,
                accuracyMeters = if (newLocation.hasAccuracy()) newLocation.accuracy else 5.0f,
                districtName = detectedDistrict,
                sessionDistanceMeters = newSessionDist,
                sessionSteps = newSessionSteps,
                isGpsActive = true
            )
        }

        // If moved significantly, add to database cumulative stats
        if (deltaDistance > 5.0) {
            viewModelScope.launch {
                val deltaSteps = (deltaDistance / 0.75).toInt()
                if (deltaSteps > 0) {
                    userStatsDao.addSteps(deltaSteps)
                }
                userStatsDao.addDistance(deltaDistance)
            }
        }
    }

    private fun detectDistrict(lat: Double, lng: Double): String {
        return when {
            lat in 10.740..10.765 && lng in 106.645..106.675 -> "Khu Di Sản Chợ Lớn (Phường Chợ Lớn)"
            lat in 10.815..10.845 && lng in 106.715..106.745 -> "Khu Bán Đảo Thanh Đa (Phường Thanh Đa)"
            lat in 10.765..10.785 && lng in 106.675..106.692 -> "Khu Hẻm Bàn Cờ (Phường Bàn Cờ, Quận 3)"
            lat in 10.785..10.800 && lng in 106.685..106.700 -> "Khu Di Sản Tân Định (Phường Tân Định, Quận 1)"
            lat in 10.765..10.785 && lng in 106.692..106.715 -> "Khu Trung Tâm Di Sản (Phường Sài Gòn, Quận 1)"
            lat in 10.750..10.780 && lng in 106.620..106.650 -> "Làng Lồng Đèn Phú Bình (Phường Hòa Bình, Q.11)"
            lat in 10.775..10.795 && lng in 106.680..106.705 -> "Khu Biệt Thự Cổ (Phường Xuân Hòa, Quận 3)"
            lat in 10.790..10.810 && lng in 106.675..106.690 -> "Khu Hẻm Ẩm Thực Phú Nhuận (Phường Đức Nhuận)"
            lat in 10.750..10.770 && lng in 106.630..106.655 -> "Khu Chợ Thủ Công (Phường Minh Phụng, Q.11)"
            lat in 10.760..10.780 && lng in 106.640..106.660 -> "Khu Di Tích Cổ Tự (Phường Bình Thới, Q.11)"
            else -> "TP. Hồ Chí Minh, Việt Nam"
        }
    }

    fun onStepDetected(steps: Int = 1) {
        if (steps <= 0) return
        val deltaDistance = steps * 0.75
        _locationState.update { current ->
            val newSessionDist = current.sessionDistanceMeters + deltaDistance
            val newSessionSteps = current.sessionSteps + steps
            current.copy(
                sessionDistanceMeters = newSessionDist,
                sessionSteps = newSessionSteps
            )
        }
        viewModelScope.launch {
            val oldStats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
            userStatsDao.addSteps(steps)
            userStatsDao.addDistance(deltaDistance)
            if (oldStats.currentStreak <= 0 && steps > 0) {
                userStatsDao.updateStreak(1)
            }
        }
    }

    fun addSteps(steps: Int) {
        onStepDetected(steps)
    }

    fun incrementCheckpoints() {
        viewModelScope.launch {
            val oldStats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
            val oldLevel = com.example.util.QuestLevelUtils.calculateLevelInfo(oldStats.totalXp).level

            userStatsDao.incrementCheckpoints()
            userStatsDao.addXp(50)

            val newStats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
            val newLevelInfo = com.example.util.QuestLevelUtils.calculateLevelInfo(newStats.totalXp)

            if (newLevelInfo.level > oldLevel) {
                notificationManager?.triggerXpNotification(
                    title = "🎉 THĂNG CẤP LEVEL ${newLevelInfo.level}!",
                    message = "Chúc mừng! Bạn đạt '${newLevelInfo.titleVi}' và nhận +50 XP từ chặng!",
                    xpAmount = 50,
                    isLevelUp = true,
                    iconEmoji = "🎉"
                )
            } else {
                notificationManager?.triggerXpNotification(
                    title = "📍 Hoàn Thành Chặng!",
                    message = "Bạn nhận +50 Quest XP! Tiếp tục tiến bước lên Cấp ${newLevelInfo.level + 1}.",
                    xpAmount = 50,
                    isLevelUp = false,
                    iconEmoji = "📍"
                )
            }
        }
    }

    fun addXp(xp: Int) {
        viewModelScope.launch {
            val oldStats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
            val oldLevel = com.example.util.QuestLevelUtils.calculateLevelInfo(oldStats.totalXp).level

            userStatsDao.addXp(xp)

            val newStats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
            val newLevelInfo = com.example.util.QuestLevelUtils.calculateLevelInfo(newStats.totalXp)

            if (newLevelInfo.level > oldLevel) {
                notificationManager?.triggerXpNotification(
                    title = "🎉 THĂNG CẤP LEVEL ${newLevelInfo.level}!",
                    message = "Chúc mừng! Bạn mở khóa danh hiệu '${newLevelInfo.titleVi}' với +$xp Quest XP!",
                    xpAmount = xp,
                    isLevelUp = true,
                    iconEmoji = "🏆"
                )
            } else {
                notificationManager?.triggerXpNotification(
                    title = "⚡ Nhận +$xp Quest XP!",
                    message = "Tích lũy ${newLevelInfo.currentLevelXp}/${newLevelInfo.requiredLevelXp} XP để đạt Cấp ${newLevelInfo.level + 1}.",
                    xpAmount = xp,
                    isLevelUp = false,
                    iconEmoji = "⚡"
                )
            }
        }
    }

    fun completeQuest(
        xpReward: Int = 120,
        distanceMeters: Double = 1200.0,
        questTitle: String? = null,
        questCategory: String? = null,
        questId: String? = null,
        currentUid: String? = null
    ) {
        viewModelScope.launch {
            val oldStats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
            val oldLevel = com.example.util.QuestLevelUtils.calculateLevelInfo(oldStats.totalXp).level

            userStatsDao.incrementCompletedQuests()
            userStatsDao.addXp(xpReward)
            userStatsDao.addDistance(distanceMeters)
            userStatsDao.addSteps((distanceMeters / 0.75).toInt())

            val stats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
            val newLevelInfo = com.example.util.QuestLevelUtils.calculateLevelInfo(stats.totalXp)

            if (newLevelInfo.level > oldLevel) {
                notificationManager?.triggerXpNotification(
                    title = "🎉 THĂNG CẤP MỚI LEVEL ${newLevelInfo.level}!",
                    message = "Hoàn thành chuyến đi bộ! Đạt danh hiệu '${newLevelInfo.titleVi}' (+$xpReward Quest XP)!",
                    xpAmount = xpReward,
                    isLevelUp = true,
                    iconEmoji = "👑"
                )
            } else {
                notificationManager?.triggerXpNotification(
                    title = "🚶 Hoàn Thành Chuyến Đi Bộ!",
                    message = "Bạn đã hoàn thành '${questTitle ?: "Chuyến đi di sản"}' và nhận +$xpReward Quest XP!",
                    xpAmount = xpReward,
                    isLevelUp = false,
                    iconEmoji = "🌟"
                )
            }

            val existingBadges = stats.unlockedBadgeIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()

            val newCount = stats.completedQuestsCount + 1
            if (newCount >= 1) existingBadges.add("first_step")
            if (newCount >= 3) existingBadges.add("alley_walker")
            if (newCount >= 5) existingBadges.add("heritage_master")
            if (newCount >= 10) existingBadges.add("saigon_expert")

            var culturalBadge: com.example.model.CulturalBadge? = null
            if (!questTitle.isNullOrBlank()) {
                val dynamicBadgeKey = com.example.util.IdGenerator.generateQuestBadgeId(questTitle)
                existingBadges.add(dynamicBadgeKey)

                val uniqueIcon = com.example.util.IdGenerator.getBadgeIconForQuest(questTitle, questCategory)
                val rarity = com.example.util.IdGenerator.getBadgeRarityForQuest(4, distanceMeters.toInt())
                culturalBadge = com.example.model.CulturalBadge(
                    id = dynamicBadgeKey,
                    title = questTitle,
                    description = "Huy hiệu vinh danh hoàn thành hành trình đi bộ di sản \"$questTitle\"",
                    iconEmoji = uniqueIcon,
                    category = questCategory ?: "CULTURE",
                    questId = questId ?: dynamicBadgeKey.replace("quest_badge_", "quest_"),
                    questTitle = questTitle,
                    unlockedAt = System.currentTimeMillis(),
                    rarity = rarity,
                    culturalPointsEarned = xpReward
                )
            }

            val updatedBadgeString = existingBadges.joinToString(",")
            userStatsDao.updateUnlockedBadges(updatedBadgeString)

            val targetUid = currentUid ?: authManager?.currentUser?.uid ?: "local_user"
            if (authManager != null) {
                culturalBadge?.let { badge ->
                    authManager.saveBadgeToFirestore(targetUid, badge)
                }
                authManager.syncUserData(targetUid)
            }
        }
    }

    fun saveCompletedQuest(quest: com.example.model.Quest) {
        viewModelScope.launch(kotlinx.coroutines.Dispatchers.IO) {
            try {
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                val json = moshi.adapter(com.example.model.Quest::class.java).toJson(quest)
                questDao?.insertQuest(com.example.data.QuestEntity(id = quest.id, questJson = json, timestamp = System.currentTimeMillis()))
                
                val targetUid = authManager?.currentUser?.uid ?: "local_user"
                if (authManager != null) {
                    authManager.saveQuestToFirestore(targetUid, quest)
                    authManager.syncUserData(targetUid)
                }
                Log.d("UserStatsViewModel", "Successfully saved completed quest ${quest.id} locally & to Firestore")
            } catch (e: Exception) {
                Log.e("UserStatsViewModel", "Failed to save completed quest", e)
            }
        }
    }

    fun unlockBadge(badgeId: String, currentUid: String? = null) {
        viewModelScope.launch {
            val stats = userStatsDao.getUserStatsSync() ?: UserStatsEntity()
            val existingBadges = stats.unlockedBadgeIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }.toMutableSet()
            if (!existingBadges.contains(badgeId)) {
                existingBadges.add(badgeId)
                val updatedBadgeString = existingBadges.joinToString(",")
                userStatsDao.updateUnlockedBadges(updatedBadgeString)
                userStatsDao.addXp(100) // Bonus XP for unlocking badge

                // Sync to Firestore if authenticated
                if (currentUid != null && authManager != null) {
                    authManager.syncUserData(currentUid)
                }
            }
        }
    }

    /**
     * Fetch latest user stats & quest progress directly from Firestore
     */
    fun fetchFromFirestore(uid: String) {
        if (authManager == null) return
        viewModelScope.launch {
            _syncState.update { it.copy(isSyncing = true, errorMessage = null) }
            val result = authManager.fetchUserDataFromFirestore(uid)
            result.onSuccess {
                _syncState.update {
                    it.copy(
                        isSyncing = false,
                        isSynced = true,
                        lastSyncedTimeMillis = System.currentTimeMillis(),
                        errorMessage = null
                    )
                }
            }.onFailure { error ->
                _syncState.update {
                    it.copy(
                        isSyncing = false,
                        errorMessage = error.localizedMessage ?: "Failed to fetch from Firestore"
                    )
                }
            }
        }
    }

    /**
     * Manually trigger push sync to Firestore
     */
    fun syncToFirestore(uid: String) {
        if (authManager == null) return
        viewModelScope.launch {
            _syncState.update { it.copy(isSyncing = true, errorMessage = null) }
            try {
                authManager.syncUserData(uid)
                authManager.syncPublicLibraryToFirestore()
                _syncState.update {
                    it.copy(
                        isSyncing = false,
                        isSynced = true,
                        lastSyncedTimeMillis = System.currentTimeMillis(),
                        errorMessage = null
                    )
                }
            } catch (e: Exception) {
                _syncState.update {
                    it.copy(
                        isSyncing = false,
                        errorMessage = e.localizedMessage ?: "Sync error"
                    )
                }
            }
        }
    }

    /**
     * Explicitly seed the 'mock_quests' and 'cultural_glossary' collections into Firestore
     */
    fun seedMockQuestsAndGlossary(onComplete: ((Boolean) -> Unit)? = null) {
        if (authManager == null) {
            onComplete?.invoke(false)
            return
        }
        viewModelScope.launch {
            try {
                authManager.syncPublicLibraryToFirestore()
                onComplete?.invoke(true)
            } catch (e: Exception) {
                Log.e("UserStatsViewModel", "Failed to seed mock quests and glossary", e)
                onComplete?.invoke(false)
            }
        }
    }

    /**
     * Store a snapped quest marker photo into Room database & Firebase Firestore
     */
    fun savePassportPhoto(
        stopId: String,
        stopName: String,
        questId: String = "",
        questTitle: String = "",
        bitmap: Bitmap,
        verificationType: String = "AI_VERIFIED"
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val byteArrayOutputStream = ByteArrayOutputStream()
                bitmap.compress(Bitmap.CompressFormat.JPEG, 75, byteArrayOutputStream)
                val base64String = Base64.encodeToString(byteArrayOutputStream.toByteArray(), Base64.NO_WRAP)

                val userEmail = authManager?.currentUser?.email ?: ""
                val uid = authManager?.currentUser?.uid ?: "local_user"
                val photoId = "photo_${stopId.replace(" ", "_")}_${System.currentTimeMillis()}"

                val entity = PassportPhotoEntity(
                    id = photoId,
                    stopId = stopId,
                    stopName = stopName,
                    questId = questId,
                    questTitle = questTitle,
                    photoBase64 = base64String,
                    timestamp = System.currentTimeMillis(),
                    userEmail = userEmail,
                    syncedToFirebase = true,
                    isVerified = true,
                    verificationType = verificationType
                )

                passportPhotoDao?.insertPassportPhoto(entity)
                authManager?.savePassportPhotoToFirestore(uid, entity)

                notificationManager?.triggerXpNotification(
                    title = "📸 Đã lưu ảnh con dấu vào Sổ Passport!",
                    message = "Ảnh chụp tại $stopName đã được lưu vào Sổ Passport Hẻm và đồng bộ Firestore.",
                    xpAmount = 50,
                    iconEmoji = "📸"
                )
            } catch (e: Exception) {
                android.util.Log.w("UserStatsViewModel", "Error saving passport photo", e)
            }
        }
    }

    val allQuestsFlow: StateFlow<List<com.example.model.Quest>> = (questDao?.getAllQuests() ?: kotlinx.coroutines.flow.flowOf(emptyList()))
        .map { entities ->
            val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
            val questAdapter = moshi.adapter(com.example.model.Quest::class.java)
            entities.mapNotNull {
                try {
                    questAdapter.fromJson(it.questJson)
                } catch (e: Exception) {
                    null
                }
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}

