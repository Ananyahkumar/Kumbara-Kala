package com.example.kumbarakala

import android.app.Application
import android.util.Log
import com.google.firebase.FirebaseApp

class KumbaraKalaApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        val app = FirebaseApp.initializeApp(this)
        if (app == null) {
            Log.w(
                "KumbaraKalaApp",
                "Firebase not configured. Add app/google-services.json for authentication."
            )
        }
    }
}
