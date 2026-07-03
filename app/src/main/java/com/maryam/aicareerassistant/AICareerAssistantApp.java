package com.maryam.aicareerassistant;

import android.app.Application;

import com.google.firebase.FirebaseApp;
import com.maryam.aicareerassistant.utils.PdfExtractor;

/**
 * Application class — initializes Firebase and PDFBox once at app startup.
 */
public class AICareerAssistantApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FirebaseApp.initializeApp(this);
        PdfExtractor.init(this); // PDFBox needs one-time initialization
    }
}