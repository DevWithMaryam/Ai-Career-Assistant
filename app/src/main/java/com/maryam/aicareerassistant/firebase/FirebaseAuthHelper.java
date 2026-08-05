package com.maryam.aicareerassistant.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class FirebaseAuthHelper {

    private final FirebaseAuth firebaseAuth;

    public FirebaseAuthHelper() {
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public interface AuthListener {
        void onSuccess(FirebaseUser user);
        void onFailure(String errorMessage);
    }

    public void signUp(String email, String password, @NonNull AuthListener listener) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user != null) {
                        // Send verification email immediately after signup
                        user.sendEmailVerification()
                                .addOnSuccessListener(unused -> listener.onSuccess(user))
                                .addOnFailureListener(e -> listener.onSuccess(user));
                        // Even if verification email fails to send, account is created
                    } else {
                        listener.onFailure("Signup failed. Please try again.");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(mapError(e)));
    }

    public void login(String email, String password, @NonNull AuthListener listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    FirebaseUser user = result.getUser();
                    if (user == null) {
                        listener.onFailure("Login failed. Please try again.");
                        return;
                    }
                    // Check email verification
                    if (!user.isEmailVerified()) {
                        firebaseAuth.signOut(); // Don't allow unverified users in
                        listener.onFailure("EMAIL_NOT_VERIFIED");
                        return;
                    }
                    listener.onSuccess(user);
                })
                .addOnFailureListener(e -> listener.onFailure(mapError(e)));
    }

    public void sendPasswordResetEmail(String email, @NonNull AuthListener listener) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnSuccessListener(unused -> listener.onSuccess(null))
                .addOnFailureListener(e -> listener.onFailure(mapError(e)));
    }

    public void resendVerificationEmail(@NonNull AuthListener listener) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            listener.onFailure("No user found. Please sign up again.");
            return;
        }
        user.sendEmailVerification()
                .addOnSuccessListener(unused -> listener.onSuccess(null))
                .addOnFailureListener(e -> listener.onFailure(mapError(e)));
    }

    public void logout() {
        firebaseAuth.signOut();
    }

    public FirebaseUser getCurrentUser() {
        return firebaseAuth.getCurrentUser();
    }

    private String mapError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "Something went wrong. Please try again.";
        if (msg.contains("password is invalid") || msg.contains("no user record"))
            return "Invalid email or password.";
        if (msg.contains("email address is already in use"))
            return "This email is already registered. Try logging in instead.";
        if (msg.contains("badly formatted"))
            return "Please enter a valid email address.";
        if (msg.contains("network error"))
            return "Network error. Please check your internet connection.";
        return msg;
    }
}