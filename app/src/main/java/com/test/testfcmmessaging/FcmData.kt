package com.test.testfcmmessaging

data class FcmData(var to:String,var data:Notification)

data class Notification(var title:String ,var body:String )

data class ChatInfo(var user:String , var message:String)