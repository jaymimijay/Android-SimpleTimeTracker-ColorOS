package com.razeeman.util.simpletimetracker.feature_notification.fluidCloud

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

/**
 * 流体云广播接收器
 * 
 * 监听 SimpleTimeTracker 的计时广播事件，自动启动/更新/结束流体云 Live Update。
 * 无需修改现有 Activity/ViewModel/Service 代码，零侵入集成。
 */
@AndroidEntryPoint
class FluidCloudBroadcastReceiver : BroadcastReceiver() {

    @Inject
    lateinit var fluidCloudHelper: FluidCloudHelper

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            // 开始某项活动计时
            "com.razeeman.util.simpletimetracker.ACTION_START_ACTIVITY" -> {
                val activityName = intent.getStringExtra("activity_name")
                    ?: intent.getStringExtra("name")
                    ?: "正在计时"

                fluidCloudHelper.createChannel()
                fluidCloudHelper.startTimerLiveUpdate(
                    activityName = activityName,
                    startTime = System.currentTimeMillis()
                )
            }

            // 停止某项活动计时
            "com.razeeman.util.simpletimetracker.ACTION_STOP_ACTIVITY" -> {
                fluidCloudHelper.endTimerLiveUpdate()
            }

            // 停止所有活动计时
            "com.razeeman.util.simpletimetracker.ACTION_STOP_ALL_ACTIVITIES" -> {
                fluidCloudHelper.endTimerLiveUpdate()
            }
        }
    }
}
