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
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.viewmodel.AuthViewModel;

/**
 * Sends a Firebase password-reset email to the given address.
 */
public class ForgotPasswordFragment extends Fragment {

    private AuthViewModel authViewModel;

    private TextInputEditText etEmail;
    private Button btnSendReset;
    private ProgressBar progressBar;
    private TextView tvBackToLogin;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_forgot_password, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        etEmail = view.findViewById(R.id.etEmail);
        btnSendReset = view.findViewById(R.id.btnSendReset);
        progressBar = view.findViewById(R.id.progressBar);
        tvBackToLogin = view.findViewById(R.id.tvBackToLogin);

        btnSendReset.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            authViewModel.sendPasswordResetEmail(email);
        });

        tvBackToLogin.setOnClickListener(v ->
                Navigation.findNavController(view).popBackStack());

        observeResetResult();
    }

    private void observeResetResult() {
        authViewModel.getResetPasswordResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    Toast.makeText(getContext(), getString(R.string.reset_email_sent), Toast.LENGTH_LONG).show();
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void setLoading(boolean isLoading) {
        progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
        btnSendReset.setEnabled(!isLoading);
    }
}