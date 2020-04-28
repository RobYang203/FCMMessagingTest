package com.test.testfcmmessaging

import android.R
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.RingtoneManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.work.ListenableWorker
import androidx.work.Worker
import androidx.work.WorkerParameters
import com.google.gson.Gson
import java.util.*


class FCMWork (appContext: Context, workerParams: WorkerParameters) : Worker(appContext, workerParams) {
    val context:Context = appContext

    companion object{
       const val Tag = "FCMWork"
    }
    override fun doWork(): ListenableWorker.Result {
        val json = inputData.getString("json")

        val g = Gson()
        val d = g.fromJson(json,ChatInfo::class.java)


        //i.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
        if(!isTopActivity()){
            showNotification(d.user,d.message,context)
        }
        sendToChatRoom(d.user , d.message)

        Log.w("Work Info" , json)
        return ListenableWorker.Result.success()
    }

    fun isTopActivity():Boolean{
        val ts = System.currentTimeMillis()
        val manager = applicationContext.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
        val usageList:List<UsageStats> = manager.queryUsageStats(UsageStatsManager.INTERVAL_BEST,ts - 1000 , ts)
        val backgroundCount: Int =
            applicationContext.getSharedPreferences("config", MODE_PRIVATE)
                .getInt("backgroundCount", 0)
        if(backgroundCount == 0){
            return true
        }
        if((usageList == null || usageList.isEmpty()) ){
            return false
        }

        Collections.sort(usageList , RecentUseComparator())
        return usageList[0].packageName == applicationContext.packageName
    }

    fun sendToChatRoom(user:String , msg:String){
        val li = Intent()
        li.action = Tag
        li.putExtra("user",user)
        li.putExtra("message",msg)
        LocalBroadcastManager.getInstance(applicationContext).sendBroadcastSync(li)
    }
    var i = 0
     fun showNotification(
        title: String,
        body: String,
        context: Context
    ) {
        val intent = Intent()
        intent.setClass( context, FcmReceiver::class.java)
        intent.putExtra("test","tesdddst")
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED
        intent.action = "com.test.ACTION.Notification"
        val pendingIntent = PendingIntent.getBroadcast(
            applicationContext,
            i++ /* Request code */,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val ChannelId = "test"
        val ChannelName = "chatroome"
        val TextStyle = NotificationCompat.BigTextStyle().bigText(body)
        val BigIcon =
            BitmapFactory.decodeResource(context.resources, R.drawable.alert_dark_frame)
        val defaultSoundUri: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val notification: Notification? = NotificationCompat.Builder(context, ChannelId)
            .setSmallIcon(R.drawable.alert_dark_frame)
            .setLargeIcon(BigIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setSound(defaultSoundUri)
            .setContentIntent(pendingIntent)
            .setStyle(TextStyle)
            .setAutoCancel(true)
            .build()
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val ImportanceLevel = NotificationManager.IMPORTANCE_HIGH
        if (Build.VERSION.SDK_INT >= 26) {
            val channel =
                NotificationChannel(ChannelId, ChannelName, ImportanceLevel)
            notificationManager.createNotificationChannel(channel)
        }
        notificationManager.notify(i /* ID of notification */, notification)
    }
}