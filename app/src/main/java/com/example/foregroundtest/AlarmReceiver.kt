package com.example.foregroundtest

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build

class AlarmReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        val alarmIntent = Intent(context, LocationService::class.java).apply {
            action = intent?.action
        }

        context?.let {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(alarmIntent)
            } else {
                context.startService(alarmIntent)
            }

            val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent = Intent(context, AlarmReceiver::class.java).apply {
                action = "RESTART"
            }.let {
                PendingIntent.getBroadcast(context, 0, it, 0)
            }


            alarmManager.setAlarmClock(
                AlarmManager.AlarmClockInfo(
                    System.currentTimeMillis() + 60 * 1000,
                    alarmIntent
                ),
                alarmIntent
            )
        }
    }
}