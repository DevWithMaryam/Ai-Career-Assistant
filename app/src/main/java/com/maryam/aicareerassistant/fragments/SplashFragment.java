package com.maryam.aicareerassistant.fragments;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.viewmodel.AuthViewModel;

/**
 * Shows the app logo briefly, then routes to Home (if already logged in) or Login.
 */
public class SplashFragment extends Fragment {

    private static final long SPLASH_DELAY_MS = 1500;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        AuthViewModel authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isAdded()) return;

            if (authViewModel.isUserLoggedIn()) {
                Navigation.findNavController(view).navigate(R.id.action_splash_to_home);
            } else {
                Navigation.findNavController(view).navigate(R.id.action_splash_to_login);
            }
        }, SPLASH_DELAY_MS);
    }
}