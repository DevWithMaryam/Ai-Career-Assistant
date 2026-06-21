package com.maryam.aicareerassistant.activities;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.maryam.aicareerassistant.R;

/**
 * Single Activity that hosts all Fragments via Navigation Component.
 * All screens (Splash, Login, Signup, Home, etc.) live inside this
 * Activity as Fragments managed by NavHostFragment.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // Navigation is handled automatically via NavHostFragment in XML
    }
}