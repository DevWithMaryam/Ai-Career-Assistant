package com.maryam.aicareerassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.viewmodel.AuthViewModel;

public class LoginFragment extends Fragment {

    private AuthViewModel authViewModel;
    private TextInputEditText etEmail, etPassword;
    private Button btnLogin;
    private ProgressBar progressBar;
    private TextView tvForgotPassword, tvSignupLink;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_login, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etEmail = view.findViewById(R.id.etEmail);
        etPassword = view.findViewById(R.id.etPassword);
        btnLogin = view.findViewById(R.id.btnLogin);
        progressBar = view.findViewById(R.id.progressBar);
        tvForgotPassword = view.findViewById(R.id.tvForgotPassword);
        tvSignupLink = view.findViewById(R.id.tvSignupLink);

        btnLogin.setOnClickListener(v -> attemptLogin());

        tvForgotPassword.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_login_to_forgotPassword));

        tvSignupLink.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_login_to_signup));

        observeAuthResult(view);
        observeResendResult();
    }

    private void attemptLogin() {
        String email = etEmail.getText() != null
                ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null
                ? etPassword.getText().toString().trim() : "";
        authViewModel.login(email, password);
    }

    private void observeAuthResult(View view) {
        authViewModel.getAuthResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;

                case SUCCESS:
                    setLoading(false);
                    Navigation.findNavController(view)
                            .navigate(R.id.action_login_to_home);
                    break;

                case ERROR:
                    setLoading(false);
                    // Special case: email not verified
                    if ("EMAIL_NOT_VERIFIED".equals(resource.message)) {
                        showVerificationDialog();
                    } else {
                        Toast.makeText(getContext(),
                                resource.message, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        });
    }

    private void observeResendResult() {
        authViewModel.getResendVerificationResult().observe(
                getViewLifecycleOwner(), resource -> {
                    if (resource == null) return;
                    if (resource.status == com.maryam.aicareerassistant.utils.Resource.Status.SUCCESS) {
                        Toast.makeText(getContext(),
                                "Verification email sent! Check your inbox.",
                                Toast.LENGTH_LONG).show();
                    } else if (resource.status ==
                            com.maryam.aicareerassistant.utils.Resource.Status.ERROR) {
                        Toast.makeText(getContext(),
                                resource.message, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void showVerificationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Email Not Verified")
                .setMessage("Please verify your email before logging in.\n\n"
                        + "Check your inbox for the verification link we sent when you signed up.")
                .setPositiveButton("Resend Email", (dialog, which) ->
                        authViewModel.resendVerificationEmail())
                .setNegativeButton("OK", null)
                .show();
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!isLoading);
    }
}