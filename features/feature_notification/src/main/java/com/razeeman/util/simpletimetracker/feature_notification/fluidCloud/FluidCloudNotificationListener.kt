package com.razeeman.util.simpletimetracker.feature_notification.fluidCloud

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.widget.Toast

class FluidCloudNotificationListener : NotificationListenerService() {

    private var fluidCloudHelper: FluidCloudHelper? = null

    override fun onCreate() {
        super.onCreate()
        fluidCloudHelper = FluidCloudHelper(this)
        Toast.makeText(this, "流体云服务已启动", Toast.LENGTH_SHORT).show()
    }

    override fun onNotificationPosted(sbn: StatusBarNotification) {
        if (sbn.packageName != packageName) return

        val notification = sbn.notification
        val extras = notification.extras
        val title = extras.getString(Notification.EXTRA_TITLE) ?: ""
        val text = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString() ?: ""

        Toast.makeText(this, "收到通知: $title", Toast.LENGTH_SHORT).show()

        val activityName = title.ifEmpty { text.ifEmpty { "正在计时" } }
        fluidCloudHelper?.createChannel()
        fluidCloudHelper?.startTimerLiveUpdate(activityName, System.currentTimeMillis())
    }

    override fun onNotificationRemoved(sbn: StatusBarNotification) {
        if (sbn.packageName != packageName) return

        Toast.makeText(this, "通知已移除，结束流体云", Toast.LENGTH_SHORT).show()
        fluidCloudHelper?.endTimerLiveUpdate()
    }

    override fun onListenerConnected() {
        super.onListenerConnected()
        Toast.makeText(this, "通知监听器已连接", Toast.LENGTH_SHORT).show()
    }
}
