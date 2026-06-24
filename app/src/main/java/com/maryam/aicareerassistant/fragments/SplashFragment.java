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

import com.google.firebase.auth.FirebaseUser;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.viewmodel.AuthViewModel;

/**
 * Splash screen — shows briefly, then decides where to route the user:
 *  - No cached session  -> Login
 *  - Cached session, but Firebase confirms it's still valid -> Home
 *  - Cached session, but account was deleted/disabled server-side -> Login (force logout)
 */
public class SplashFragment extends Fragment {

    private static final long MIN_SPLASH_DELAY_MS = 1200;

    private AuthViewModel authViewModel;
    private boolean navigated = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_splash, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // Ensure splash is visible for at least MIN_SPLASH_DELAY_MS,
        // even if the session check finishes instantly.
        new Handler(Looper.getMainLooper()).postDelayed(this::checkSession, MIN_SPLASH_DELAY_MS);
    }

    private void checkSession() {
        if (!isAdded()) return;

        FirebaseUser cachedUser = authViewModel.getCurrentUserOrNull();

        if (cachedUser == null) {
            // No session at all -> Login
            goToLogin();
            return;
        }

        // We have a cached session. Verify it's still valid with Firebase servers
        // (catches deleted/disabled accounts, revoked tokens, etc.)
        authViewModel.verifySession(new AuthViewModel.SessionCallback() {
            @Override
            public void onValid() {
                goToHome();
            }

            @Override
            public void onInvalid() {
                authViewModel.logout();
                goToLogin();
            }
        });
    }

    private void goToHome() {
        if (navigated || !isAdded()) return;
        navigated = true;
        Navigation.findNavController(requireView()).navigate(R.id.action_splash_to_home);
    }

    private void goToLogin() {
        if (navigated || !isAdded()) return;
        navigated = true;
        Navigation.findNavController(requireView()).navigate(R.id.action_splash_to_login);
    }
}