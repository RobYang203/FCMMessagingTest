package com.test.testfcmmessaging

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log

class FcmReceiver:BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Log.w("FCM Receive","onReceive")
        intent?.setClass(context as Context , ChatRoomActivity::class.java)
        context?.startActivity(intent)
    }
}