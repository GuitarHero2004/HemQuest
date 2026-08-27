package com.example.repository

import android.content.Context
import android.util.Log
import com.example.auth.UserProfileData
import com.example.data.UserStatsDao
import com.example.data.UserStatsEntity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

/**
 * UserAuthRepository / Service
 * 
 * Actively monitors Firebase Auth state changes and automatically ensures that a complete
 * user profile document exists in the 'users' Firestore collection upon first sign-in or subsequent sign-ins.
 */
class UserAuthRepository(
    private val context: Context,
    private val userStatsDao: UserStatsDao? = null,
    private val externalScope: CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
) {
    private val auth: FirebaseAuth? by lazy {
        try { FirebaseAuth.getInstance() } catch (e: Exception) { null }
    }
    
    private val firestore: FirebaseFirestore? by lazy {
        try { FirebaseFirestore.getInstance() } catch (e: Exception) { null }
    }

    private val prefs by lazy {
        context.getSharedPreferences("hemquest_local_auth_db", Context.MODE_PRIVATE)
    }

    companion object {
        private const val TAG = "UserAuthRepository"
        const val USERS_COLLECTION = "users"
        const val STATS_SUBCOLLECTION = "stats"
        const val CURRENT_STATS_DOC = "current"
    }

    /**
     * Cold Flow emitting FirebaseUser updates when authentication state changes.
     */
    val authStateFlow: Flow<FirebaseUser?> = callbackFlow {
        val firebaseAuth = auth
        if (firebaseAuth != null) {
            val listener = FirebaseAuth.AuthStateListener { fa ->
                trySend(fa.currentUser)
            }
            try {
                firebaseAuth.addAuthStateListener(listener)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to attach AuthStateListener", e)
                trySend(null)
            }
            awaitClose {
                try {
                    firebaseAuth.removeAuthStateListener(listener)
                } catch (e: Exception) {
                    // Ignore during teardown
                }
            }
        } else {
            trySend(null)
            awaitClose { }
        }
    }

    init {
        // Start listening to auth state changes immediately to initialize user document on sign-in
        startAuthLifecycleObserver()
    }

    /**
     * Resolves the Firestore document ID for a given user.
     * Uses email if available, otherwise falls back to UID.
     */
    fun resolveUserDocId(user: FirebaseUser? = auth?.currentUser): String {
        val email = user?.email?.ifBlank { null }
            ?: prefs.getString("local_user_email", null)?.ifBlank { null }
            ?: user?.uid?.ifBlank { null }
            ?: "anhminhnts2004@gmail.com"
        return email.trim().lowercase()
    }

    /**
     * Listens for Firebase Auth sign-in events and triggers document creation in Firestore.
     */
    private fun startAuthLifecycleObserver() {
        externalScope.launch {
            authStateFlow.distinctUntilChanged().collect { user ->
                if (user != null) {
                    Log.d(TAG, "User signed in: ${user.uid} (${user.email}). Initializing Firestore document...")
                    initializeUserDocumentOnSignIn(user)
                } else {
                    Log.d(TAG, "User signed out or unauthenticated.")
                }
            }
        }
    }

    /**
     * Creates or updates the user root document in the 'users' Firestore collection
     * and sets up initial baseline stats if it's the first time signing in.
     */
    suspend fun initializeUserDocumentOnSignIn(user: FirebaseUser): Boolean {
        val fs = firestore ?: run {
            Log.w(TAG, "FirebaseFirestore instance unavailable")
            return false
        }

        val docId = resolveUserDocId(user)
        val userEmail = user.email ?: prefs.getString("local_user_email", "") ?: docId
        val displayName = user.displayName ?: prefs.getString("local_user_name", "Explorer") ?: "Explorer"
        val photoUrl = user.photoUrl?.toString() ?: ""

        try {
            val userDocRef = fs.collection(USERS_COLLECTION).document(docId)
            val snapshot = userDocRef.get().await()

            val isFirstSignIn = !snapshot.exists()
            val now = System.currentTimeMillis()

            val userMap = hashMapOf<String, Any>(
                "uid" to user.uid,
                "userEmail" to userEmail,
                "displayName" to displayName,
                "photoUrl" to photoUrl,
                "lastActive" to now,
                "lastSyncedAt" to now,
                "provider" to if (user.email != null) "GOOGLE" else "GUEST"
            )

            if (isFirstSignIn) {
                userMap["createdAt"] = now
                userMap["accountStatus"] = "ACTIVE"
                Log.d(TAG, "First sign-in detected for $docId. Creating new root user document...")
            }

            // Write root user document (using merge to preserve any existing extra fields)
            userDocRef.set(userMap, SetOptions.merge()).await()

            // Ensure baseline stats subcollection exists
            val statsDocRef = userDocRef.collection(STATS_SUBCOLLECTION).document(CURRENT_STATS_DOC)
            val statsSnapshot = statsDocRef.get().await()

            if (!statsSnapshot.exists()) {
                val localStats = userStatsDao?.getUserStatsSync() ?: UserStatsEntity(id = 1)
                val initialStats = hashMapOf<String, Any>(
                    "totalSteps" to localStats.totalSteps,
                    "completedCheckpoints" to localStats.completedCheckpoints,
                    "totalXp" to localStats.totalXp,
                    "currentStreak" to localStats.currentStreak,
                    "totalDistanceMeters" to localStats.totalDistanceMeters,
                    "completedQuestsCount" to localStats.completedQuestsCount,
                    "unlockedBadgeIds" to localStats.unlockedBadgeIds,
                    "lastUpdated" to now
                )
                statsDocRef.set(initialStats, SetOptions.merge()).await()
                Log.d(TAG, "Initialized default stats subcollection for $docId")
            }

            Log.i(TAG, "Successfully initialized Firestore 'users' document for $docId")
            return true
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing user document in Firestore for $docId", e)
            return false
        }
    }

    /**
     * Manual trigger to initialize or sync user document immediately.
     */
    fun triggerUserInitialization(user: FirebaseUser? = auth?.currentUser) {
        if (user != null) {
            externalScope.launch {
                initializeUserDocumentOnSignIn(user)
            }
        }
    }
}
