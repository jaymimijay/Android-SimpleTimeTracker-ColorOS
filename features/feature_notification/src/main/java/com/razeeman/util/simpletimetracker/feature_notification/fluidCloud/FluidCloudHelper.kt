package com.razeeman.util.simpletimetracker.feature_notification.fluidCloud

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FluidCloudHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        const val CHANNEL_ID = "fluid_cloud_timer_channel"
        const val NOTIFICATION_ID = 9001
    }

    private val notificationManager: NotificationManager by lazy {
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "计时器实时状态",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "在状态栏显示当前计时活动（支持 OPPO 流体云）"
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun startTimerLiveUpdate(activityName: String, startTime: Long) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
            showFallbackNotification(activityName, startTime)
            return
        }
        if (!notificationManager.canPostPromotedNotifications()) {
            showFallbackNotification(activityName, startTime)
            return
        }

        val progressStyle = Notification.ProgressStyle().setProgress(0)
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("正在追踪")
            .setContentText(activityName)
            .setSubText(formatElapsedTime(System.currentTimeMillis() - startTime))
            .setOngoing(true)
            .setColorized(true)
            .setColor(getActivityColor())
            .setStyle(progressStyle)
            .setContentIntent(getMainActivityPendingIntent())
            .setCategory(Notification.CATEGORY_PROGRESS)
            .build()

        if (notification.hasPromotableCharacteristics()) {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } else {
            showFallbackNotification(activityName, startTime)
        }
    }

    fun updateTimerLiveUpdate(activityName: String, startTime: Long, totalPlannedMinutes: Int? = null) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return

        val elapsedMs = System.currentTimeMillis() - startTime
        val elapsedMinutes = (elapsedMs / 60000).toInt()
        val progress = if (totalPlannedMinutes != null && totalPlannedMinutes > 0) {
            (elapsedMinutes * 100 / totalPlannedMinutes).coerceAtMost(100)
        } else {
            (elapsedMinutes % 60) * 100 / 60
        }

        val progressStyle = Notification.ProgressStyle().setProgress(progress)
        val notification = Notification.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("正在追踪")
            .setContentText(activityName)
            .setSubText(formatElapsedTime(elapsedMs))
            .setOngoing(true)
            .setColorized(true)
            .setColor(getActivityColor())
            .setStyle(progressStyle)
            .setContentIntent(getMainActivityPendingIntent())
            .setCategory(Notification.CATEGORY_PROGRESS)
            .build()

        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    fun endTimerLiveUpdate() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    private fun showFallbackNotification(activityName: String, startTime: Long) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setContentTitle("正在追踪: $activityName")
            .setContentText(formatElapsedTime(System.currentTimeMillis() - startTime))
            .setOngoing(true)
            .setContentIntent(getMainActivityPendingIntent())
            .build()
        notificationManager.notify(NOTIFICATION_ID, notification)
    }

    private fun getMainActivityPendingIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        } ?: Intent()
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun formatElapsedTime(elapsedMs: Long): String {
        val totalMinutes = elapsedMs / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分钟"
    }

    private fun getActivityColor(): Int {
        return Color.parseColor("#4CAF50")
    }
}
