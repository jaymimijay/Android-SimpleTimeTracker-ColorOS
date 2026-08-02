package com.razeeman.util.simpletimetracker.feature_notification.fluidCloud

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class FluidCloudBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val fluidCloudHelper = FluidCloudHelper(context)

        when (intent.action) {
            "com.razeeman.util.simpletimetracker.ACTION_START_ACTIVITY" -> {
                val activityName = intent.getStringExtra("activity_name")
                    ?: intent.getStringExtra("name")
                    ?: "正在计时"
                fluidCloudHelper.createChannel()
                fluidCloudHelper.startTimerLiveUpdate(activityName, System.currentTimeMillis())
            }
            "com.razeeman.util.simpletimetracker.ACTION_STOP_ACTIVITY",
            "com.razeeman.util.simpletimetracker.ACTION_STOP_ALL_ACTIVITIES" -> {
                fluidCloudHelper.endTimerLiveUpdate()
            }
        }
    }
}
