package com.test.testfcmmessaging

import android.app.Activity
import android.app.ProgressDialog
import android.content.*
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.Gravity
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_chatroom.*
import okhttp3.*
import java.io.IOException


class ChatRoomActivity: AppCompatActivity() {
    private var appCount = 0
    var name:String =""
    val H = object :Handler(){
        override fun handleMessage(msg: Message) {
            super.handleMessage(msg)
            addChat(msg.obj.toString(),msg.arg2)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chatroom)

        init()

    }

    override fun onRestart() {
        super.onRestart()
        appCount =0
    }

    override fun onStart() {
        super.onStart()
        appCount =0
    }
    override fun onResume() {
        super.onResume()
        appCount =0
        writeConfig(appCount)
    }
    override fun onStop() {
        super.onStop()
        appCount++
        writeConfig(appCount)

    }

    override fun onPause() {
        super.onPause()
        appCount++
        Log.w("ChatRoomActivity",appCount.toString())
    }

    fun init(){
        setToobar()
        name = intent.getStringExtra("loginName")
        callFCM("system" , "$name is coming..."  ,"test")
       // addChat("$name is coming..." , Gravity.CENTER)
        registerBroadcast()
        btnSend.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                if(txtChat.text.isEmpty()){
                    return
                }
                callFCM(name , txtChat.text.toString(),"test")
                txtChat.setText("")
               // addChat("$name say: ${txtChat.text.toString()}" , Gravity.RIGHT)
            }
        })

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if(item.itemId == android.R.id.home){
            callFCM("system" , "$name is left...", "test",object:afterFCNSend{
                override fun callBack() {
                    intent.putExtra("action",1)
                    setResult(Activity.RESULT_OK , intent)
                    finish()
                }
            })
        }
        return super.onOptionsItemSelected(item)
    }

    fun setToobar(){
        tb.title = "Chat Room Test"
        setSupportActionBar(tb)
        supportActionBar?.setHomeButtonEnabled(true)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }
    interface afterFCNSend{
        open fun callBack()
    }

    fun callFCM(name:String, chat:String, topic: String, cb: afterFCNSend? = null){
        val loading = ProgressDialog.show(this , "connecting..." , "Please wait..." ,true)
        val oh = OkHttpClient()

        val json = createJsonInfo(name ,chat ,topic)
        Log.w("chatroom send msg" , json)
        val request = createRequest(json ,getString(R.string.fcmSendUrl),getString(R.string.fcmApiKey))

        val call = oh.newCall(request)
        call.enqueue(object: Callback {
            override fun onResponse(call: Call, response: Response) {
                val responseStr = response!!.body()!!.string()
                loading.dismiss()

                Log.w("Request Success" , responseStr)
                if(cb == null){
                    return
                }
                cb.callBack()
            }

            override fun onFailure(call: Call, e: IOException) {
                loading.dismiss()

                Log.w("Request Failure" , e.toString())
                if(cb == null){
                    return
                }
                cb.callBack()
            }
        })
    }

    fun createRequest(json:String, url:String , apiKey:String):Request{
        val JSONType = MediaType.parse("application/json; charset=utf-8;")
        val body = RequestBody.create(JSONType,json)
        return Request.Builder()
            .addHeader("Authorization","key=${apiKey}")
            .url(url)
            .post(body)
            .build()
    }
    fun createJsonInfo(name:String , chat:String, topic: String):String{
        val cInfo = ChatInfo(name , chat)

        val g = Gson()
        val msg = g.toJson(cInfo)
        var n = Notification(name,msg)
        var data  = FcmData("/topics/${topic}", n)

        return g.toJson(data)
    }
    fun addChat(chat:String , position:Int){
        val tmpChat = TextView(baseContext)
        tmpChat.text = chat
        tmpChat.gravity = position
        chatList.addView(tmpChat)
    }

    private fun registerBroadcast() {
        val lb = LocalBroadcastManager.getInstance(this)
        val filter: IntentFilter = IntentFilter(FCMWork.Tag)
        val receiver: BroadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val action = intent.action
                if(!FCMWork.Tag.equals(action)){
                    return
                }
                val user = intent.getStringExtra("user")
                val chat = "${user}:  ${intent.getStringExtra("message")}"

                val handlerMsg = Message();
                handlerMsg.obj = chat;
                handlerMsg.arg2 = if(user == name){
                    Gravity.RIGHT
                }else if(user == "system"){
                    Gravity.CENTER
                }else{
                    Gravity.LEFT
                }
                H.sendMessage(handlerMsg)
            }
        }
        lb.registerReceiver(receiver, filter)
    }

    companion object{
         fun isApplicationForground(myApplication:ChatRoomActivity):Boolean{
            return myApplication.appCount > 0
        }
    }

    private fun writeConfig(backgroundCount: Int) {
        Log.d("[Notify]backgroundCount", backgroundCount.toString())
        val pref: SharedPreferences = baseContext.getSharedPreferences("config", Context.MODE_PRIVATE)
        pref.edit()
            .putInt("backgroundCount", backgroundCount)
            .commit()
    }
}