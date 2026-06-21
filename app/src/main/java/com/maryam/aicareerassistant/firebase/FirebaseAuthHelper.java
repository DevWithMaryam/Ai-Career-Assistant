package com.maryam.aicareerassistant.firebase;

import androidx.annotation.NonNull;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

/**
 * Thin wrapper around FirebaseAuth SDK — isolates raw Firebase calls
 * from the Repository layer (cleaner separation of concerns).
 */
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
                    if (result.getUser() != null) {
                        listener.onSuccess(result.getUser());
                    } else {
                        listener.onFailure("Signup failed. Please try again.");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(mapError(e)));
    }

    public void login(String email, String password, @NonNull AuthListener listener) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(result -> {
                    if (result.getUser() != null) {
                        listener.onSuccess(result.getUser());
                    } else {
                        listener.onFailure("Login failed. Please try again.");
                    }
                })
                .addOnFailureListener(e -> listener.onFailure(mapError(e)));
    }

    public void sendPasswordResetEmail(String email, @NonNull AuthListener listener) {
        firebaseAuth.sendPasswordResetEmail(email)
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

        if (msg.contains("password is invalid") || msg.contains("no user record")) {
            return "Invalid email or password.";
        } else if (msg.contains("email address is already in use")) {
            return "This email is already registered. Try logging in instead.";
        } else if (msg.contains("badly formatted")) {
            return "Please enter a valid email address.";
        } else if (msg.contains("network error")) {
            return "Network error. Please check your internet connection.";
        }
        return msg;
    }
}