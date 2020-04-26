package com.test.testfcmmessaging

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.FirebaseApp
import com.google.firebase.iid.FirebaseInstanceId
import com.google.firebase.iid.InstanceIdResult
import kotlinx.android.synthetic.main.activity_main.*
import java.lang.Exception

class MainActivity : AppCompatActivity() {
   // val baseActivity = this
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        init()
    }

    fun init(){
        FirebaseApp.initializeApp(this)

        var fcm = FirebaseInstanceId.getInstance()
        fcm.instanceId.addOnSuccessListener( object: OnSuccessListener<InstanceIdResult>{
            override fun onSuccess(task : InstanceIdResult?) {
                if(task == null){
                    Log.w("FCM Error", "getInstanceId failed")
                }

                val token = task?.token
                txtV.text = token
                Toast.makeText(baseContext, token , Toast.LENGTH_SHORT).show()
                Log.w("FCM Success", token)
            }
        })
            .addOnFailureListener(object : OnFailureListener{
                override fun onFailure(task: Exception) {
                    Toast.makeText(baseContext, task.toString() , Toast.LENGTH_SHORT).show()
                }
            })
    }
}
