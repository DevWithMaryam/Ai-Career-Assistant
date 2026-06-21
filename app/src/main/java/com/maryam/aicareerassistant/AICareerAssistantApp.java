package com.maryam.aicareerassistant;

import android.app.Application;
import com.google.firebase.FirebaseApp;

/**
 * Application class — initializes Firebase once at app startup.
 */
public class AICareerAssistantApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
    }
}