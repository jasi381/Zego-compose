package com.jasmeet.myapplication

import android.app.Application
import com.google.firebase.Firebase
import com.google.firebase.FirebaseApp
import com.google.firebase.initialize
import com.onesignal.OneSignal
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp
class MainApp : Application() {


    val ONESIGNAL_APP_ID = "59375227-1705-45e5-a44f-bcaf820a928d"

    override fun onCreate() {
        super.onCreate()
        Firebase.initialize(context = this)
        OneSignal.initWithContext(this, ONESIGNAL_APP_ID)

    }


}