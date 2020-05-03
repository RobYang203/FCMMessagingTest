package com.test.testfcmmessaging


import android.content.Intent
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.Observer
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.Data
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import java.util.*

class FCMService: FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.w("FCM Message onNewToken" , token)
    }

    override fun onMessageReceived(rm: RemoteMessage) {
        super.onMessageReceived(rm)
        val d = rm.data
        val workData = Data.Builder()
            .putString("json" , d?.get("body"))
            .build()

        val msgWork = OneTimeWorkRequest.Builder(FCMWork::class.java)
            .setInputData(workData)
            .build()

        WorkManager.getInstance().enqueue(msgWork)


        Log.w("FCM Message Received" , "title: ${d?.get("title")}, body: ${d?.get("body")}")

    }


}