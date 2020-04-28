package com.test.testfcmmessaging

import android.app.AppOpsManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.Task
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
   // val baseActivity = this
    var token:String? = ""
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    override fun onResume() {
        super.onResume()
       try{
           val  b= intent.extras
           if(b?.containsKey("test")!!){
               Toast.makeText(baseContext, b?.getString("test"), Toast.LENGTH_LONG).show()
           }

       }catch (e:java.lang.Exception){

       }

    }
    fun init(){
        fcmInit()

        btnSend.setOnClickListener(object : View.OnClickListener{
            override fun onClick(v: View?) {
                val name = txtName.text
                if(name.isEmpty()){
                    Toast.makeText(baseContext,"name is empty" , Toast.LENGTH_LONG).show()
                    return
                }
                addTopic("test" , object :afterTopicComplete{
                    override fun callBack(result: Boolean) {
                        if(!result){
                            return
                        }
                        val i = Intent()

                        i.putExtra("loginName",name.toString())
                        i.setClass(baseContext, ChatRoomActivity::class.java)
                        startActivityForResult(i , 1)
                    }
                })

            }
        })

       if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.LOLLIPOP){
           if(needPermissionForBlocking(baseContext)){
               startActivity(Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS));
           }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        deleteTopic("test",object :afterTopicComplete{
            override fun callBack(result: Boolean) {

            }
        })
    }

    fun fcmInit(){
        FirebaseApp.initializeApp(this)

        var fcm = FirebaseInstanceId.getInstance()
        fcm.instanceId
            .addOnCompleteListener(object:OnCompleteListener<InstanceIdResult>{
                override fun onComplete(task: Task<InstanceIdResult>) {
                    if (!task.isSuccessful) {
                        Log.w("FCM Error", "getInstanceId failed", task.exception)

                    }

                    // Get new Instance ID token
                    token = task.result?.token
                    Log.d("FCM Complete", token)
                  //  Toast.makeText(baseContext, token, Toast.LENGTH_SHORT).show()
                }
            })
            .addOnFailureListener(object : OnFailureListener{
                override fun onFailure(task: Exception) {
                    Toast.makeText(baseContext, task.toString() , Toast.LENGTH_SHORT).show()
                    Log.w("FCM Failure", task.toString())
                }
            })

    }


    interface afterTopicComplete{
        open fun callBack(result:Boolean)
    }

    fun addTopic(name:String , cb:afterTopicComplete){
        FirebaseMessaging.getInstance().subscribeToTopic(name)
            .addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(t: Task<Void>) {
                    if(!t.isSuccessful){
                        Log.w("Topic Error","Subscribe failure")
                        Toast.makeText(baseContext, "Subscribe failure" , Toast.LENGTH_SHORT).show()
                    }
                    cb.callBack(t.isSuccessful)
                }
            })
    }

    fun deleteTopic(name:String, cb:afterTopicComplete){
        FirebaseMessaging.getInstance().unsubscribeFromTopic(name)
            .addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(t: Task<Void>) {
                    if(!t.isSuccessful){
                        Log.w("Topic Error","Unsubscribe failure")
                        Toast.makeText(baseContext, "Unsubscribe failure" , Toast.LENGTH_SHORT).show()
                    }
                    cb.callBack(t.isSuccessful)
                }
            })
    }

      fun needPermissionForBlocking(context: Context):Boolean {
        try {
            var packageManager = context.getPackageManager();
            var applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
            var appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager;
            var mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, applicationInfo.uid, applicationInfo.packageName)
            return  (mode != AppOpsManager.MODE_ALLOWED);
        } catch (e: PackageManager.NameNotFoundException ) {
            return true;
        }
    }


}
