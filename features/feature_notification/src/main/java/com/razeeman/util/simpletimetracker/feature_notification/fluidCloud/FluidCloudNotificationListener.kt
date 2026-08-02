package com.razeeman.util.simpletimetracker.feature_notification.fluidCloud

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * 流体云通知监听器
 *
 * 原理：监听 SimpleTimeTracker 自己发送的"正在计时"通知，
 * 当它显示 ongoing 通知时，同步发送流体云 Live Update；
 * 当通知被移除时，同步结束流体云。
 *
 * 优点：不依赖广播，不修改现有代码，只要 SimpleTimeTracker 有通知就能触发。
 * 缺点：需要用户在系统设置中手动开启"通知访问权限"。
 */
class FluidCloudNotificationListener : NotificationListenerService() {

    private var fluidCloudHelper: FluidCloudHelper? = null

    override fun onCreate() {
        super.onCreate()
        fluidCloudHelper = FluidCloudHelper(this)
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        // 只监听 SimpleTimeTracker 自己的通知
        if (sbn.packageName != packageName) return

        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        // SimpleTimeTracker 的计时通知是 ongoing（不可滑动移除）
        // 检测到 ongoing 通知时，启动流体云
        if (notification.flags and Notification.FLAG_ONGOING_EVENT != 0) {
            val activityName = title.ifEmpty { text.ifEmpty { "正在计时" } }
            fluidCloudHelper?.createChannel()
            fluidCloudHelper?.startTimerLiveUpdate(activityName, System.currentTimeMillis())
        }
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName != packageName) return

        // ongoing 通知被移除时（停止计时），结束流体云
        if (sbn.notification.flags and Notification.FLAG_ONGOING_EVENT != 0) {
            fluidCloudHelper?.endTimerLiveUpdate()
        }
    }
}
