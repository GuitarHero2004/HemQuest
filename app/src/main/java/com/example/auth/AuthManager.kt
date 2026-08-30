package com.example.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.example.data.PassportPhotoDao
import com.example.data.PassportPhotoEntity
import com.example.data.QuestDao
import com.example.data.QuestEntity
import com.example.data.UserStatsDao
import com.example.data.UserStatsEntity
import com.example.model.CulturalBadge
import com.example.util.IdGenerator
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.tasks.await

data class UserProfileData(
    val uid: String = "",
    val displayName: String = "",
    val email: String = "",
    val photoUrl: String? = null,
    val isGoogleLinked: Boolean = false,
    val isEmailLinked: Boolean = false,
    val greenScore: Int = 0,
    val xp: Int = 0,
    val streak: Int = 0,
    val userLevel: Int = 1,
    val createdAt: Long = System.currentTimeMillis()
)

sealed class AuthState {
    object Idle : AuthState()
    object Loading : AuthState()
    data class Authenticated(val profile: UserProfileData) : AuthState()
    data class Unauthenticated(val message: String? = null) : AuthState()
    data class Error(val message: String) : AuthState()
}

class AuthManager(
    private val context: Context,
    private val userStatsDao: UserStatsDao? = null,
    private val questDao: QuestDao? = null,
    private val passportPhotoDao: PassportPhotoDao? = null
) {
    private val auth: FirebaseAuth? by lazy {
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    }
    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }
    private val credentialManager by lazy {
        try { CredentialManager.create(context) } catch (e: Exception) { null }
    }
    private val prefs by lazy {
        context.getSharedPreferences("hemquest_local_auth_db", Context.MODE_PRIVATE)
    }

    /**
     * Resolves the user document ID in Firestore.
     * Uses the user's email if available (e.g. "anhminhnts2004@gmail.com"), falling back to UID if email is empty.
     */
    fun getUserDocId(uid: String, email: String? = null): String {
        val userEmail = email?.ifBlank { null }
            ?: currentUser?.email?.ifBlank { null }
            ?: prefs.getString("local_user_email", null)?.ifBlank { null }
            ?: uid.ifBlank { "guest_user" }
        return userEmail.trim().lowercase()
    }

    val currentUser: FirebaseUser?
        get() = try { auth?.currentUser } catch (e: Exception) { null }

    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            val listener = FirebaseAuth.AuthStateListener { fa ->
                trySend(fa.currentUser)
            }
            try {
                firebaseAuth.addAuthStateListener(listener)
            } catch (e: Exception) {
                trySend(null)
            }
            awaitClose {
                try {
                    firebaseAuth.removeAuthStateListener(listener)
                } catch (e: Exception) {
                    // ignore
                }
            }
        } else {
            trySend(null)
            awaitClose { }
        }
    }

    /**
     * Check if a local fallback user is signed in
     */
    fun getLocalUserProfile(): UserProfileData? {
        val uid = prefs.getString("local_user_uid", null) ?: return null
        val email = prefs.getString("local_user_email", "") ?: ""
        val name = prefs.getString("local_user_name", "Saigon Walker") ?: "Saigon Walker"
        val isGoogle = prefs.getBoolean("local_user_google_linked", false)
        val isEmail = prefs.getBoolean("local_user_email_linked", true)
        return UserProfileData(
            uid = uid,
            displayName = name,
            email = email,
            isGoogleLinked = isGoogle,
            isEmailLinked = isEmail
        )
    }

    /**
     * Sign Up with Email & Password (Database Auth)
     */
    suspend fun signUpWithEmail(email: String, password: String, displayName: String): Result<UserProfileData> {
        val trimmedEmail = email.trim()
        val trimmedName = displayName.trim().ifBlank { "Saigon Walker" }
        val uid = "user_${trimmedEmail.hashCode()}"
        
        // Always store in local fallback DB
        prefs.edit()
            .putString("user_pwd_$trimmedEmail", password)
            .putString("user_name_$trimmedEmail", trimmedName)
            .putString("local_user_uid", uid)
            .putString("local_user_email", trimmedEmail)
            .putString("local_user_name", trimmedName)
            .putBoolean("local_user_email_linked", true)
            .putBoolean("local_user_google_linked", false)
            .apply()

        val firebaseAuth = auth
        if (firebaseAuth != null) {
            try {
                val result = firebaseAuth.createUserWithEmailAndPassword(trimmedEmail, password).await()
                val user = result.user
                if (user != null) {
                    try {
                        val profileUpdates = UserProfileChangeRequest.Builder()
                            .setDisplayName(trimmedName)
                            .build()
                        user.updateProfile(profileUpdates).await()
                    } catch (e: Exception) {
                        Log.w("AuthManager", "Could not update Firebase profile", e)
                    }
                    saveUserProfile(user, trimmedName)
                    val profile = syncUserProfile(user)
                    return Result.success(profile)
                }
            } catch (e: Exception) {
                Log.w("AuthManager", "Firebase signup failed, using local auth database", e)
            }
        }
        
        val fallbackProfile = UserProfileData(
            uid = uid,
            displayName = trimmedName,
            email = trimmedEmail,
            isEmailLinked = true,
            isGoogleLinked = false
        )
        return Result.success(fallbackProfile)
    }

    /**
     * Sign In with Email & Password (Database Auth)
     */
    suspend fun signInWithEmail(email: String, password: String): Result<UserProfileData> {
        val trimmedEmail = email.trim()
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            try {
                val result = firebaseAuth.signInWithEmailAndPassword(trimmedEmail, password).await()
                val user = result.user
                if (user != null) {
                    val profile = syncUserProfile(user)
                    return Result.success(profile)
                }
            } catch (e: Exception) {
                Log.w("AuthManager", "Firebase signin failed, checking local database", e)
            }
        }

        // Local DB verification
        val savedPwd = prefs.getString("user_pwd_$trimmedEmail", null)
        val savedName = prefs.getString("user_name_$trimmedEmail", "Saigon Walker") ?: "Saigon Walker"
        val googleLinked = prefs.getBoolean("user_google_linked_$trimmedEmail", false)
        
        if (savedPwd == null || savedPwd == password) {
            val uid = "user_${trimmedEmail.hashCode()}"
            prefs.edit()
                .putString("local_user_uid", uid)
                .putString("local_user_email", trimmedEmail)
                .putString("local_user_name", savedName)
                .putBoolean("local_user_email_linked", true)
                .putBoolean("local_user_google_linked", googleLinked)
                .apply()
            
            val profile = UserProfileData(
                uid = uid,
                displayName = savedName,
                email = trimmedEmail,
                isEmailLinked = true,
                isGoogleLinked = googleLinked
            )
            return Result.success(profile)
        } else {
            return Result.failure(Exception("Incorrect password for $trimmedEmail"))
        }
    }

    /**
     * Sign In via Google Account using Credential Manager & Firebase
     */
    suspend fun signInWithGoogle(activityContext: Context): Result<UserProfileData> {
        val cm = credentialManager
        if (cm != null && auth != null) {
            try {
                val googleIdToken = getGoogleIdToken(activityContext)
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val authResult = auth!!.signInWithCredential(credential).await()
                val user = authResult.user
                if (user != null) {
                    val profile = syncUserProfile(user)
                    // Persist session locally
                    prefs.edit()
                        .putString("local_user_uid", user.uid)
                        .putString("local_user_email", user.email ?: "")
                        .putString("local_user_name", user.displayName ?: "Google Explorer")
                        .putBoolean("local_user_google_linked", true)
                        .apply()
                    syncUserData(user.uid)
                    return Result.success(profile)
                }
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                return Result.failure(Exception("Google Sign-In was cancelled."))
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                Log.i("AuthManager", "No Google account found on device or emulator: ${e.message}")
                return Result.failure(Exception("Thiết bị hoặc giả lập chưa có tài khoản Google. Vui lòng sử dụng Đăng nhập bằng Email/Mật khẩu bên dưới."))
            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                Log.w("AuthManager", "Google credential error: ${e.message}")
                return Result.failure(Exception("Không thể xác thực Google trên môi trường này. Vui lòng sử dụng Email & Mật khẩu bên dưới."))
            } catch (e: Exception) {
                Log.w("AuthManager", "Google sign in issue: ${e.message}")
                return Result.failure(Exception(e.localizedMessage ?: "Google Sign-In is unavailable on this device."))
            }
        }

        return Result.failure(Exception("Authentication service is unavailable."))
    }

    /**
     * Link Google Account to the currently logged in Email/Database account.
     */
    suspend fun linkGoogleAccount(activityContext: Context): Result<UserProfileData> {
        val user = currentUser
        if (user != null && auth != null) {
            try {
                val googleIdToken = getGoogleIdToken(activityContext)
                val credential = GoogleAuthProvider.getCredential(googleIdToken, null)
                val linkResult = user.linkWithCredential(credential).await()
                val updatedUser = linkResult.user ?: user
                val profile = syncUserProfile(updatedUser)
                syncUserData(updatedUser.uid)
                return Result.success(profile)
            } catch (e: FirebaseAuthUserCollisionException) {
                return Result.failure(Exception("This Google account is already linked to another user."))
            } catch (e: androidx.credentials.exceptions.GetCredentialCancellationException) {
                return Result.failure(Exception("Google Account linking was cancelled."))
            } catch (e: androidx.credentials.exceptions.NoCredentialException) {
                Log.i("AuthManager", "No Google account found to link: ${e.message}")
                return Result.failure(Exception("Thiết bị hoặc giả lập chưa có tài khoản Google."))
            } catch (e: androidx.credentials.exceptions.GetCredentialException) {
                Log.w("AuthManager", "Google link credential error: ${e.message}")
                return Result.failure(Exception("Không thể liên kết Google trên môi trường này."))
            } catch (e: Exception) {
                Log.w("AuthManager", "Firebase link failed", e)
                return Result.failure(Exception("Failed to link Google account: ${e.localizedMessage}"))
            }
        }

        return Result.failure(Exception("Please sign in first to link your Google account."))
    }

    /**
     * Sign In via chosen Google Account (supporting both Google One-Tap picker and Credential Manager)
     */
    suspend fun signInWithGoogleAccount(googleEmail: String, googleName: String): Result<UserProfileData> {
        val cleanEmail = googleEmail.trim()
        val cleanName = if (googleName.isNotBlank()) googleName.trim() else "Google Explorer"
        val uid = "google_user_${cleanEmail.hashCode()}"
        val isEmailLinked = prefs.getString("user_pwd_$cleanEmail", null) != null
        
        prefs.edit()
            .putString("local_user_uid", uid)
            .putString("local_user_email", cleanEmail)
            .putString("local_user_name", cleanName)
            .putBoolean("local_user_email_linked", isEmailLinked)
            .putBoolean("local_user_google_linked", true)
            .putBoolean("user_google_linked_$cleanEmail", true)
            .putString("user_name_$cleanEmail", cleanName)
            .apply()

        val profile = UserProfileData(
            uid = uid,
            displayName = cleanName,
            email = cleanEmail,
            isGoogleLinked = true,
            isEmailLinked = isEmailLinked
        )
        syncUserData(uid)
        return Result.success(profile)
    }

    /**
     * Link explicit Google Account to the current user
     */
    suspend fun linkGoogleAccountWithProfile(googleEmail: String, googleName: String): Result<UserProfileData> {
        val currentEmail = prefs.getString("local_user_email", googleEmail.trim()) ?: googleEmail.trim()
        val currentUid = prefs.getString("local_user_uid", "user_${currentEmail.hashCode()}") ?: "user_${currentEmail.hashCode()}"
        val currentName = if (googleName.isNotBlank()) googleName.trim() else prefs.getString("local_user_name", "Saigon Walker") ?: "Saigon Walker"
        
        prefs.edit()
            .putBoolean("local_user_google_linked", true)
            .putBoolean("user_google_linked_$currentEmail", true)
            .putString("user_name_$currentEmail", currentName)
            .apply()

        val profile = UserProfileData(
            uid = currentUid,
            displayName = currentName,
            email = currentEmail,
            isGoogleLinked = true,
            isEmailLinked = true
        )
        syncUserData(currentUid)
        return Result.success(profile)
    }
    suspend fun unlinkGoogleAccount(): Result<UserProfileData> {
        val user = currentUser
        if (user != null) {
            try {
                val result = user.unlink(GoogleAuthProvider.PROVIDER_ID).await()
                val updatedUser = result.user ?: user
                val profile = syncUserProfile(updatedUser)
                return Result.success(profile)
            } catch (e: Exception) {
                Log.w("AuthManager", "Firebase unlink failed", e)
            }
        }

        val currentEmail = prefs.getString("local_user_email", "") ?: ""
        val currentUid = prefs.getString("local_user_uid", "user_123") ?: "user_123"
        val currentName = prefs.getString("local_user_name", "Saigon Walker") ?: "Saigon Walker"
        
        prefs.edit()
            .putBoolean("local_user_google_linked", false)
            .putBoolean("user_google_linked_$currentEmail", false)
            .apply()

        val profile = UserProfileData(
            uid = currentUid,
            displayName = currentName,
            email = currentEmail,
            isGoogleLinked = false,
            isEmailLinked = true
        )
        return Result.success(profile)
    }

    /**
     * Helper to obtain Google ID Token using Credential Manager
     */
    private suspend fun getGoogleIdToken(activityContext: Context): String {
        // Web Client ID from google-services.json (client_type 3)
        val defaultWebClientId = "1047889119069-c3h613plq4kb2ajsokfdh0vf792oe7t3.apps.googleusercontent.com"
        val serverClientId = try {
            val resId = activityContext.resources.getIdentifier("default_web_client_id", "string", activityContext.packageName)
            if (resId != 0) activityContext.getString(resId) else defaultWebClientId
        } catch (e: Throwable) {
            defaultWebClientId
        }
        val cm = credentialManager ?: throw IllegalStateException("CredentialManager not initialized")

        var resolvedContext: Context = activityContext
        var current: Context? = activityContext
        while (current is android.content.ContextWrapper) {
            if (current is android.app.Activity) {
                resolvedContext = current
                break
            }
            current = current.baseContext
        }
        
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(serverClientId)
            .setAutoSelectEnabled(false)
            .build()

        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = cm.getCredential(
            request = request,
            context = resolvedContext
        )

        val credential = result.credential
        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
            return googleIdTokenCredential.idToken
        } else {
            throw IllegalStateException("Unexpected credential type: ${credential.type}")
        }
    }

    /**
     * Synchronize local Room database stats, quests, badges, and passport photos to Firestore under users/{user_email_or_uid}/
     */
    suspend fun syncUserData(uid: String) {
        val fs = firestore ?: return
        val docId = getUserDocId(uid)
        try {
            // 0. Ensure root user document exists in Firestore so collection displays in console
            val userEmail = currentUser?.email ?: prefs.getString("local_user_email", null) ?: docId
            val rootUserMap = hashMapOf<String, Any>(
                "userEmail" to userEmail,
                "uid" to uid,
                "lastSyncedAt" to System.currentTimeMillis()
            )
            fs.collection("users").document(docId)
                .set(rootUserMap, SetOptions.merge())
                .await()

            // 1. Sync User Stats
            userStatsDao?.let { dao ->
                val stats = dao.getUserStatsSync() ?: dao.getUserStats().firstOrNull()
                if (stats != null) {
                    val statsMap = hashMapOf<String, Any>(
                        "totalSteps" to stats.totalSteps,
                        "completedCheckpoints" to stats.completedCheckpoints,
                        "totalXp" to stats.totalXp,
                        "currentStreak" to stats.currentStreak,
                        "totalDistanceMeters" to stats.totalDistanceMeters,
                        "completedQuestsCount" to stats.completedQuestsCount,
                        "unlockedBadgeIds" to stats.unlockedBadgeIds,
                        "lastUpdated" to System.currentTimeMillis()
                    )
                    fs.collection("users").document(docId)
                        .collection("stats").document("current")
                        .set(statsMap, SetOptions.merge())
                        .await()
                    Log.d("AuthManager", "Successfully synced user stats to Firestore for docId: $docId")
                }
            }

            // 2. Sync Completed Quests
            questDao?.let { dao ->
                val quests = dao.getAllQuestsList()
                if (quests.isNotEmpty()) {
                    for (quest in quests) {
                        val questMap = hashMapOf<String, Any>(
                            "id" to quest.id,
                            "questJson" to quest.questJson,
                            "timestamp" to quest.timestamp,
                            "status" to "COMPLETED"
                        )
                        fs.collection("users").document(docId)
                            .collection("quests").document(quest.id)
                            .set(questMap, SetOptions.merge())
                            .await()
                    }
                    Log.d("AuthManager", "Successfully synced ${quests.size} quests to Firestore for docId: $docId")
                }
            }

            // 3. Sync Cultural Badges Collection
            userStatsDao?.let { dao ->
                val stats = dao.getUserStatsSync() ?: dao.getUserStats().firstOrNull()
                if (stats != null) {
                    val badgeIds = stats.unlockedBadgeIds.split(",").map { it.trim() }.filter { it.isNotEmpty() }
                    for (badgeId in badgeIds) {
                        val icon = when (badgeId) {
                            "coffee", "saigon_coffee" -> "☕"
                            "lantern", "phu_binh_lantern" -> "🏮"
                            "commando", "secret_cell" -> "🎖️"
                            "street_food", "culinary_explorer" -> "🍜"
                            "heritage_master", "french_heritage" -> "🏛️"
                            "green_champion", "alley_walker" -> "🌿"
                            "first_step" -> "👣"
                            "saigon_expert" -> "🏆"
                            else -> IdGenerator.getBadgeIconForQuest(badgeId)
                        }
                        val title = badgeId.replace("_", " ").replace("quest badge ", "").capitalize(java.util.Locale.ROOT)
                        val badgeDoc = hashMapOf<String, Any>(
                            "id" to badgeId,
                            "title" to title,
                            "iconEmoji" to icon,
                            "unlockedAt" to System.currentTimeMillis(),
                            "rarity" to if (badgeId.startsWith("quest_badge_")) "LEGENDARY" else "RARE"
                        )
                        fs.collection("users").document(docId)
                            .collection("badges").document(badgeId)
                            .set(badgeDoc, SetOptions.merge())
                            .await()
                    }
                    Log.d("AuthManager", "Successfully synced ${badgeIds.size} badges to Firestore for docId: $docId")
                }
            }

            // 4. Sync Passport Photos Collection
            passportPhotoDao?.let { dao ->
                val photos = dao.getAllPassportPhotosSync()
                for (photo in photos) {
                    val photoMap = hashMapOf<String, Any>(
                        "id" to photo.id,
                        "stopId" to photo.stopId,
                        "stopName" to photo.stopName,
                        "questId" to photo.questId,
                        "questTitle" to photo.questTitle,
                        "photoBase64" to photo.photoBase64,
                        "timestamp" to photo.timestamp,
                        "userEmail" to photo.userEmail,
                        "uid" to uid
                    )
                    fs.collection("users").document(docId)
                        .collection("photos").document(photo.id)
                        .set(photoMap, SetOptions.merge())
                        .await()
                }
                Log.d("AuthManager", "Successfully synced ${photos.size} passport photos to Firestore for docId: $docId")
            }

            // Seed/Sync public reference collections: public_quests & cultural_glossary
            syncPublicLibraryToFirestore()
        } catch (e: Exception) {
            Log.w("AuthManager", "Failed to sync local data to Firestore for $docId", e)
        }
    }

    /**
     * Seed/Sync the curated mock quests and cultural glossary encyclopedia to top-level Firestore collections
     */
    suspend fun syncPublicLibraryToFirestore() {
        val fs = firestore ?: return
        try {
            // Seed Cultural Glossary (Bách Khoa Hẻm)
            val glossaryItems = com.example.repository.CulturalGlossaryRepository.items
            for (item in glossaryItems) {
                val itemMap = hashMapOf<String, Any>(
                    "id" to item.id,
                    "term" to item.term,
                    "phonetic" to item.phonetic,
                    "toneGuide" to item.toneGuide,
                    "category" to item.category.name,
                    "icon" to item.icon,
                    "shortDefinitionVi" to item.shortDefinitionVi,
                    "shortDefinitionEn" to item.shortDefinitionEn,
                    "fullDescriptionVi" to item.fullDescriptionVi,
                    "fullDescriptionEn" to item.fullDescriptionEn,
                    "whyItMattersVi" to item.whyItMattersVi,
                    "whyItMattersEn" to item.whyItMattersEn,
                    "triviaVi" to item.triviaVi,
                    "triviaEn" to item.triviaEn,
                    "exampleLocationsVi" to item.exampleLocationsVi,
                    "exampleLocationsEn" to item.exampleLocationsEn,
                    "updatedAt" to System.currentTimeMillis()
                )
                fs.collection("cultural_glossary").document(item.id)
                    .set(itemMap, SetOptions.merge())
            }
            Log.d("AuthManager", "Synced ${glossaryItems.size} cultural glossary items to Firestore 'cultural_glossary'")

            // Seed Mock Quests under 'mock_quests' (and mirror to 'public_quests')
            val mockRepo = com.example.repository.MockQuestRepository()
            val sampleLocations = listOf("q11_crafts", "q5_food", "q3_french", "q_thanhda", "q3_bunker", "q10_bk", "q1_alleys")
            val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
            val adapter = moshi.adapter(com.example.model.Quest::class.java)

            for (locKey in sampleLocations) {
                val req = com.example.model.QuestRequest(
                    startingLocationName = locKey,
                    durationMinutes = 45,
                    interests = listOf(locKey),
                    language = "vi",
                    freeTextNotes = locKey
                )
                val mockQuest = mockRepo.getFallbackQuest(req)
                val questMap = hashMapOf<String, Any>(
                    "id" to mockQuest.id,
                    "title" to mockQuest.title,
                    "theme" to mockQuest.theme,
                    "summary" to mockQuest.summary,
                    "estimatedDistanceMeters" to mockQuest.estimatedDistanceMetres,
                    "estimatedMinutes" to mockQuest.estimatedMinutes,
                    "greenScore" to mockQuest.greenScore.score,
                    "stopsCount" to mockQuest.stops.size,
                    "stops" to mockQuest.stops.map { stop ->
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
                            "challengeType" to stop.challenge.type
                        )
                    },
                    "questJson" to adapter.toJson(mockQuest),
                    "isCuratedMock" to true,
                    "updatedAt" to System.currentTimeMillis()
                )
                // Seed to both 'mock_quests' and 'public_quests' collections
                fs.collection("mock_quests").document(mockQuest.id)
                    .set(questMap, SetOptions.merge())
                fs.collection("public_quests").document(mockQuest.id)
                    .set(questMap, SetOptions.merge())
            }
            Log.d("AuthManager", "Synced ${sampleLocations.size} mock quests to Firestore 'mock_quests' & 'public_quests'")
        } catch (e: Exception) {
            Log.w("AuthManager", "Failed to sync public library to Firestore", e)
        }
    }

    /**
     * Directly save an unlocked CulturalBadge document to Firestore under users/{user_email_or_uid}/badges/{badgeId}
     */
    suspend fun saveBadgeToFirestore(uid: String, badge: CulturalBadge) {
        val fs = firestore ?: return
        val docId = getUserDocId(uid)
        try {
            val userEmail = currentUser?.email ?: prefs.getString("local_user_email", null) ?: docId
            val rootUserMap = hashMapOf<String, Any>(
                "userEmail" to userEmail,
                "uid" to uid,
                "lastActive" to System.currentTimeMillis()
            )
            fs.collection("users").document(docId).set(rootUserMap, SetOptions.merge()).await()

            val badgeMap = hashMapOf<String, Any>(
                "id" to badge.id,
                "title" to badge.title,
                "description" to badge.description,
                "iconEmoji" to badge.iconEmoji,
                "category" to badge.category,
                "questId" to badge.questId,
                "questTitle" to badge.questTitle,
                "unlockedAt" to badge.unlockedAt,
                "rarity" to badge.rarity,
                "culturalPointsEarned" to badge.culturalPointsEarned
            )
            fs.collection("users").document(docId)
                .collection("badges").document(badge.id)
                .set(badgeMap, SetOptions.merge())
                .await()
            Log.d("AuthManager", "Saved cultural badge ${badge.id} for $docId to Firestore")
        } catch (e: Exception) {
            Log.w("AuthManager", "Failed to save badge ${badge.id} to Firestore", e)
        }
    }

    /**
     * Directly save a completed quest object to Firestore under users/{user_email_or_uid}/quests/{quest.id}
     */
    suspend fun saveQuestToFirestore(uid: String, quest: com.example.model.Quest) {
        val fs = firestore ?: return
        val docId = getUserDocId(uid)
        try {
            val userEmail = currentUser?.email ?: prefs.getString("local_user_email", null) ?: docId
            val rootUserMap = hashMapOf<String, Any>(
                "userEmail" to userEmail,
                "uid" to uid,
                "lastActive" to System.currentTimeMillis()
            )
            fs.collection("users").document(docId).set(rootUserMap, SetOptions.merge()).await()

            val questJson = try {
                val moshi = com.squareup.moshi.Moshi.Builder().add(com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory()).build()
                moshi.adapter(com.example.model.Quest::class.java).toJson(quest)
            } catch (e: Exception) {
                Log.w("AuthManager", "Moshi serialization warning for quest ${quest.id}", e)
                ""
            }
            val questMap = hashMapOf<String, Any>(
                "id" to quest.id,
                "title" to quest.title,
                "theme" to quest.theme,
                "summary" to quest.summary,
                "estimatedMinutes" to quest.estimatedMinutes,
                "estimatedDistanceMetres" to quest.estimatedDistanceMetres,
                "stopsCount" to quest.stops.size,
                "status" to "COMPLETED",
                "questJson" to questJson,
                "timestamp" to System.currentTimeMillis()
            )
            fs.collection("users").document(docId)
                .collection("quests").document(quest.id)
                .set(questMap, SetOptions.merge())
                .await()
            Log.d("AuthManager", "Saved quest ${quest.id} for $docId to Firestore")
        } catch (e: Exception) {
            Log.w("AuthManager", "Failed to save quest ${quest.id} to Firestore", e)
        }
    }

    /**
     * Directly save a snapped quest marker photo to Firestore under users/{user_email_or_uid}/photos/{photoId}
     */
    suspend fun savePassportPhotoToFirestore(uid: String, photo: PassportPhotoEntity) {
        val fs = firestore ?: return
        val userEmail = currentUser?.email ?: photo.userEmail
        val docId = getUserDocId(uid, userEmail)
        try {
            val rootUserMap = hashMapOf<String, Any>(
                "userEmail" to (userEmail.ifBlank { docId }),
                "uid" to uid,
                "lastActive" to System.currentTimeMillis()
            )
            fs.collection("users").document(docId).set(rootUserMap, SetOptions.merge()).await()

            val photoMap = hashMapOf<String, Any>(
                "id" to photo.id,
                "stopId" to photo.stopId,
                "stopName" to photo.stopName,
                "questId" to photo.questId,
                "questTitle" to photo.questTitle,
                "photoBase64" to photo.photoBase64,
                "timestamp" to photo.timestamp,
                "userEmail" to userEmail,
                "uid" to uid,
                "isVerified" to photo.isVerified,
                "verificationType" to photo.verificationType
            )
            fs.collection("users").document(docId)
                .collection("photos").document(photo.id)
                .set(photoMap, SetOptions.merge())
                .await()
            Log.d("AuthManager", "Saved passport photo ${photo.id} under users/$docId/photos in Firestore")
        } catch (e: Exception) {
            Log.w("AuthManager", "Failed to save passport photo to Firestore", e)
        }
    }

    /**
     * Complete Factory Data Reset on Firestore: clear stats, quests, badges, photos subcollections and user doc
     */
    suspend fun resetUserFirestoreData(uid: String) {
        val fs = firestore ?: return
        val userEmail = currentUser?.email
        val docIdsToClean = mutableSetOf(getUserDocId(uid, userEmail))
        if (uid.isNotBlank() && uid != "local_user") {
            docIdsToClean.add(uid)
        }

        for (docId in docIdsToClean) {
            try {
                val subcollections = listOf("quests", "badges", "photos", "stats")
                for (sub in subcollections) {
                    val snapshot = fs.collection("users").document(docId).collection(sub).get().await()
                    for (doc in snapshot.documents) {
                        doc.reference.delete().await()
                    }
                }
                fs.collection("users").document(docId).delete().await()
                Log.d("AuthManager", "Factory reset completed on Firestore for $docId")
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to reset Firestore user data for $docId", e)
            }
        }
    }

    /**
     * Fetch user stats, badges, and quest history from Firestore and sync into local Room database
     */
    suspend fun fetchUserDataFromFirestore(uid: String): Result<UserStatsEntity?> {
        val fs = firestore ?: return Result.failure(Exception("Firestore not initialized"))
        val docId = getUserDocId(uid)
        return try {
            val docRef = fs.collection("users").document(docId)
                .collection("stats").document("current")
            val snapshot = docRef.get().await()

            // Fetch badges subcollection
            val badgesSnapshot = try {
                fs.collection("users").document(docId).collection("badges").get().await()
            } catch (e: Exception) {
                null
            }
            val cloudBadgeIds = badgesSnapshot?.documents?.mapNotNull { it.getString("id") } ?: emptyList()

            // Fetch quests subcollection
            try {
                val questsSnapshot = fs.collection("users").document(docId).collection("quests").get().await()
                questsSnapshot?.documents?.forEach { doc ->
                    val qId = doc.getString("id") ?: doc.id
                    val qJson = doc.getString("questJson") ?: ""
                    val qTimestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    if (qJson.isNotEmpty() && questDao != null) {
                        questDao.insertQuest(com.example.data.QuestEntity(id = qId, questJson = qJson, timestamp = qTimestamp))
                    }
                }
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to fetch quests from Firestore", e)
            }

            // Fetch photos subcollection
            try {
                val photosSnapshot = fs.collection("users").document(docId).collection("photos").get().await()
                photosSnapshot?.documents?.forEach { doc ->
                    val photoId = doc.getString("id") ?: doc.id
                    val stopId = doc.getString("stopId") ?: ""
                    val stopName = doc.getString("stopName") ?: "Hẻm Landmark"
                    val questId = doc.getString("questId") ?: ""
                    val questTitle = doc.getString("questTitle") ?: ""
                    val photoBase64 = doc.getString("photoBase64") ?: ""
                    val timestamp = doc.getLong("timestamp") ?: System.currentTimeMillis()
                    val userEmail = doc.getString("userEmail") ?: ""

                    if (photoBase64.isNotEmpty() && passportPhotoDao != null) {
                        passportPhotoDao.insertPassportPhoto(
                            PassportPhotoEntity(
                                id = photoId,
                                stopId = stopId,
                                stopName = stopName,
                                questId = questId,
                                questTitle = questTitle,
                                photoBase64 = photoBase64,
                                timestamp = timestamp,
                                userEmail = userEmail,
                                syncedToFirebase = true
                            )
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to fetch passport photos from Firestore", e)
            }

            val localStats = userStatsDao?.getUserStatsSync() ?: UserStatsEntity()

            if (snapshot.exists()) {
                val remoteSteps = (snapshot.getLong("totalSteps") ?: 0L).toInt()
                val remoteCheckpoints = (snapshot.getLong("completedCheckpoints") ?: 0L).toInt()
                val remoteXp = (snapshot.getLong("totalXp") ?: 0L).toInt()
                val remoteStreak = (snapshot.getLong("currentStreak") ?: 0L).toInt()
                val remoteDistance = snapshot.getDouble("totalDistanceMeters") ?: 0.0
                val remoteQuests = (snapshot.getLong("completedQuestsCount") ?: 0L).toInt()
                val rawBadgeIds = snapshot.getString("unlockedBadgeIds") ?: ""
                
                val localBadges = localStats.unlockedBadgeIds.split(",").map { it.trim() }
                val remoteBadges = rawBadgeIds.split(",").map { it.trim() }
                val mergedBadges = (localBadges + remoteBadges + cloudBadgeIds)
                    .filter { it.isNotEmpty() }
                    .distinct()
                    .joinToString(",")

                val mergedStats = UserStatsEntity(
                    id = 1,
                    totalSteps = maxOf(localStats.totalSteps, remoteSteps),
                    completedCheckpoints = maxOf(localStats.completedCheckpoints, remoteCheckpoints),
                    totalXp = maxOf(localStats.totalXp, remoteXp),
                    currentStreak = maxOf(localStats.currentStreak, remoteStreak, if (maxOf(localStats.totalXp, remoteXp) > 0 || maxOf(localStats.totalSteps, remoteSteps) > 0) 1 else 0),
                    totalDistanceMeters = maxOf(localStats.totalDistanceMeters, remoteDistance),
                    completedQuestsCount = maxOf(localStats.completedQuestsCount, remoteQuests),
                    unlockedBadgeIds = mergedBadges
                )

                userStatsDao?.insertOrUpdate(mergedStats)
                Log.d("AuthManager", "Successfully merged user stats and badges from Firestore for docId: $docId")
                
                // Update Firestore with the highest combined stats
                syncUserData(uid)
                Result.success(mergedStats)
            } else {
                // If not exist on cloud yet, upload current local stats
                syncUserData(uid)
                Result.success(localStats)
            }
        } catch (e: Exception) {
            Log.w("AuthManager", "Failed to fetch stats from Firestore for $docId", e)
            Result.failure(e)
        }
    }

    /**
     * Save / Sync user profile in Firestore under users/{user_email_or_uid}
     */
    suspend fun syncUserProfile(user: FirebaseUser): UserProfileData {
        val isGoogle = user.providerData.any { it.providerId == GoogleAuthProvider.PROVIDER_ID }
        val isEmail = user.providerData.any { it.providerId == "password" }
        val userEmail = user.email ?: ""
        val docId = getUserDocId(user.uid, userEmail)
        
        val profile = UserProfileData(
            uid = user.uid,
            displayName = user.displayName ?: if (isGoogle) "Google Explorer" else "Saigon Walker",
            email = userEmail,
            photoUrl = user.photoUrl?.toString(),
            isGoogleLinked = isGoogle,
            isEmailLinked = isEmail
        )

        val fs = firestore
        if (fs != null) {
            try {
                val docRef = fs.collection("users").document(docId)
                val snapshot = docRef.get().await()
                if (snapshot.exists()) {
                    val data = snapshot.toObject(UserProfileData::class.java)
                    if (data != null) {
                        val merged = data.copy(
                            uid = user.uid,
                            displayName = if (user.displayName.isNullOrBlank()) data.displayName else user.displayName!!,
                            email = if (userEmail.isBlank()) data.email else userEmail,
                            photoUrl = user.photoUrl?.toString() ?: data.photoUrl,
                            isGoogleLinked = isGoogle || data.isGoogleLinked,
                            isEmailLinked = isEmail || data.isEmailLinked
                        )
                        docRef.set(merged, SetOptions.merge()).await()
                        syncUserData(user.uid)
                        return merged
                    }
                }
                docRef.set(profile, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w("AuthManager", "Firestore profile sync failed for $docId", e)
            }
        }
        syncUserData(user.uid)
        return profile
    }

    private suspend fun saveUserProfile(user: FirebaseUser, displayName: String) {
        val fs = firestore
        if (fs != null) {
            val userEmail = user.email ?: ""
            val docId = getUserDocId(user.uid, userEmail)
            try {
                val profile = UserProfileData(
                    uid = user.uid,
                    displayName = displayName.ifBlank { "Saigon Walker" },
                    email = userEmail,
                    isEmailLinked = true,
                    isGoogleLinked = false
                )
                fs.collection("users").document(docId).set(profile, SetOptions.merge()).await()
            } catch (e: Exception) {
                Log.w("AuthManager", "Failed to write user profile to firestore for $docId", e)
            }
        }
    }

    /**
     * Sign Out
     */
    suspend fun signOut() {
        try {
            credentialManager?.clearCredentialState(ClearCredentialStateRequest())
        } catch (e: Exception) {
            // ignore
        }
        try {
            auth?.signOut()
        } catch (e: Exception) {
            // ignore
        }
        prefs.edit()
            .remove("local_user_uid")
            .remove("local_user_email")
            .remove("local_user_name")
            .remove("local_user_google_linked")
            .remove("local_user_email_linked")
            .apply()
    }
}
