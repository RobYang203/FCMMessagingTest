package com.test.testfcmmessaging


import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FCMService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.w("FCM Message onNewToken" , token)
    }

    override fun onMessageReceived(rm: RemoteMessage) {
        super.onMessageReceived(rm)
        val d = rm.notification
        Log.w("FCM Message Received" , "title: ${d?.title} body: ${d?.body}")
    }
}