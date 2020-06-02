package com.example.foregroundtest

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val alarmIntent = Intent(context, LocationService::class.java).apply {
            action = intent?.action
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context?.startForegroundService(alarmIntent)
        } else {
            context?.startService(alarmIntent)
        }
    }
}