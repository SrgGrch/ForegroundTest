package com.example.foregroundtest

import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data
        TestObject.messageReceived?.invoke(data["revive"] ?: "")
        //todo startService

        val intent = Intent(this, LocationService::class.java).apply {
            action = "RESTART"
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent)
        } else {
            startService(intent)
        }
    }

    override fun onNewToken(token: String) {
        val prefs = getSharedPreferences("Loc", Context.MODE_PRIVATE)

        prefs.edit()
            .putString("Token", token)
            .apply()

        Log.i("NOTIFICATION_TOKEN", token)

        TestObject.onNewToken?.invoke(token)
    }
}