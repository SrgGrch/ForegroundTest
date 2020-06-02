package com.example.foregroundtest

import android.content.Context
import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.notification
        TestObject.messageReceived?.invoke(data?.title ?: "")
        //todo startService

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