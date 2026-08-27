package com.example.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.MainActivity
import com.example.R
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class InAppNotification(
    val id: String = System.currentTimeMillis().toString(),
    val title: String,
    val message: String,
    val iconEmoji: String = "⚡",
    val isLevelUp: Boolean = false,
    val xpEarned: Int = 0,
    val timestamp: Long = System.currentTimeMillis()
)

class AppNotificationManager(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager

    private val _currentInAppNotification = MutableStateFlow<InAppNotification?>(null)
    val currentInAppNotification: StateFlow<InAppNotification?> = _currentInAppNotification.asStateFlow()

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Quest XP & Level Up Notifications",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Notifications for walking XP rewards, quest completions, and level advancement."
            }
            notificationManager?.createNotificationChannel(channel)
        }
    }

    fun triggerXpNotification(
        title: String,
        message: String,
        xpAmount: Int,
        isLevelUp: Boolean = false,
        iconEmoji: String = if (isLevelUp) "🎉" else "⚡"
    ) {
        // 1. Show In-App Banner Popup
        val notification = InAppNotification(
            title = title,
            message = message,
            iconEmoji = iconEmoji,
            isLevelUp = isLevelUp,
            xpEarned = xpAmount
        )
        _currentInAppNotification.update { notification }

        // 2. Trigger Android System Push Notification
        try {
            val intent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                context,
                0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            val builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)

            val notificationId = (System.currentTimeMillis() % 10000).toInt()
            notificationManager?.notify(notificationId, builder.build())
        } catch (e: Exception) {
            // System notification permission might be off or missing
        }
    }

    fun dismissInAppNotification() {
        _currentInAppNotification.update { null }
    }

    companion object {
        const val CHANNEL_ID = "quest_xp_channel"

        @Volatile
        private var INSTANCE: AppNotificationManager? = null

        fun getInstance(context: Context): AppNotificationManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AppNotificationManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
}
