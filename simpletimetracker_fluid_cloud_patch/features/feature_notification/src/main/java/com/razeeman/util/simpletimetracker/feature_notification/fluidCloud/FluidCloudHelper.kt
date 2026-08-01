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

/**
 * OPPO 流体云 / Android Live Updates 辅助类
 * 
 * 原理：Android 16+ 的 Promoted Notification（ProgressStyle）会被 ColorOS 16+
 * 自动识别并渲染为流体云胶囊/卡片。旧设备回退到普通持续通知。
 */
@Singleton
class FluidCloudHelper @Inject constructor(
    @ApplicationContext private val context: Context,
    private val notificationManager: NotificationManager
) {
    companion object {
        const val CHANNEL_ID = "fluid_cloud_timer_channel"
        const val NOTIFICATION_ID = 9001
    }

    /** 创建通知渠道 */
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

    /** 
     * 启动计时器 Live Update（流体云）
     * @param activityName 活动名称
     * @param startTime 计时开始时间戳（System.currentTimeMillis()）
     */
    fun startTimerLiveUpdate(
        activityName: String,
        startTime: Long
    ) {
        // Android 16 以下使用普通持续通知
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
            showFallbackNotification(activityName, startTime)
            return
        }

        // 检查是否有权限发布 Promoted Notification
        if (!notificationManager.canPostPromotedNotifications()) {
            showFallbackNotification(activityName, startTime)
            return
        }

        val progressStyle = Notification.ProgressStyle()
            .setProgress(0)

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

    /**
     * 更新计时器 Live Update（例如每分钟调用一次）
     * @param activityName 活动名称
     * @param startTime 计时开始时间戳
     * @param totalPlannedMinutes 计划时长（分钟），用于显示进度条，可为 null
     */
    fun updateTimerLiveUpdate(
        activityName: String,
        startTime: Long,
        totalPlannedMinutes: Int? = null
    ) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) return

        val elapsedMs = System.currentTimeMillis() - startTime
        val elapsedMinutes = (elapsedMs / 60000).toInt()

        val progress = if (totalPlannedMinutes != null && totalPlannedMinutes > 0) {
            (elapsedMinutes * 100 / totalPlannedMinutes).coerceAtMost(100)
        } else {
            (elapsedMinutes % 60) * 100 / 60
        }

        val progressStyle = Notification.ProgressStyle()
            .setProgress(progress)

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

    /** 结束计时器 Live Update */
    fun endTimerLiveUpdate() {
        notificationManager.cancel(NOTIFICATION_ID)
    }

    /** 检查是否支持 Live Update */
    fun isLiveUpdateSupported(): Boolean {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.BAKLAVA &&
            notificationManager.canPostPromotedNotifications()
    }

    /** Android 16 以下回退通知 */
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

    /** 获取主界面 PendingIntent */
    private fun getMainActivityPendingIntent(): PendingIntent {
        val intent = context.packageManager.getLaunchIntentForPackage(context.packageName)?.apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        } ?: Intent()
        return PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    /** 格式化已用时间 */
    private fun formatElapsedTime(elapsedMs: Long): String {
        val totalMinutes = elapsedMs / 60000
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return if (hours > 0) "${hours}小时${minutes}分钟" else "${minutes}分钟"
    }

    /** 获取活动颜色（可扩展为根据活动类型返回不同颜色） */
    private fun getActivityColor(): Int {
        return Color.parseColor("#4CAF50")
    }
}
