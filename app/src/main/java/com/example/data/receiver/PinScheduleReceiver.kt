package com.example.data.receiver

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity
import com.example.data.database.AppDatabase
import com.example.data.model.PinPost
import com.example.data.repository.PinRepository
import com.example.domain.repository.IPinRepository
import com.example.domain.usecase.GetPostsUseCase
import com.example.domain.usecase.PublishPostUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PinScheduleReceiver : BroadcastReceiver() {

    private val notificationChannelId = "pinterest_automation_reminders"

    override fun onReceive(context: Context, intent: Intent) {
        val postId = intent.getIntExtra("PIN_POST_ID", -1)
        if (postId == -1) return

        createNotificationChannel(context)

        // Run coroutine on background IO dispatcher
        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getDatabase(context)
                val repository: IPinRepository = PinRepository(context, db.pinPostDao())
                val getPostsUseCase = GetPostsUseCase(repository)
                val publishPostUseCase = PublishPostUseCase(repository)

                val post = getPostsUseCase.getById(postId)

                if (post != null && post.status == "SCHEDULED") {
                    // Send an immediate "Posting automated Pin..." warning/reminder
                    sendPushNotification(
                        context,
                        postId,
                        "Automating Pin Post...",
                        "Publishing scheduled pin: \"${post.title}\""
                    )

                    // Execute posting
                    val result = publishPostUseCase(post)

                    if (result.isSuccess) {
                        sendPushNotification(
                            context,
                            postId + 10000, // Offset notification ID
                            "Pin Published Successfully!",
                            "Your Pin \"${post.title}\" is now live on board \"${post.boardName}\"."
                        )
                    } else {
                        val errMessage = result.exceptionOrNull()?.localizedMessage ?: "Unknown error"
                        sendPushNotification(
                            context,
                            postId + 20000,
                            "Scheduled Pin Failed",
                            "Could not publish \"${post.title}\": $errMessage"
                        )
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Pinterest Automator Alarms"
            val descriptionText = "Notifications for automatic Pinterest postings and scheduled task success reports."
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(notificationChannelId, name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun sendPushNotification(context: Context, id: Int, title: String, message: String) {
        val openAppIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            id,
            openAppIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(context, notificationChannelId)
            .setSmallIcon(android.R.drawable.ic_menu_today) // Standard calendar reminder icon
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        try {
            with(NotificationManagerCompat.from(context)) {
                notify(id, builder.build())
            }
        } catch (se: SecurityException) {
            // Notification permission might not be granted yet. Still failed silently but safely.
            se.printStackTrace()
        }
    }
}
