package com.maryam.aicareerassistant.firebase;

import android.net.Uri;

import androidx.annotation.NonNull;

import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

/**
 * Thin wrapper around Firebase Storage SDK — keeps raw Storage calls
 * out of the Repository layer.
 * this activity is not use in project because upgrade
 * project. firebase storage not use because pricing plan not intrigate with my bank account.
 *
 */
public class StorageHelper {

    private final FirebaseStorage storage;

    public interface UploadListener {
        void onProgress(int percent);
        void onSuccess(String downloadUrl);
        void onFailure(String errorMessage);
    }

    public StorageHelper() {
        storage = FirebaseStorage.getInstance();
    }

    public void uploadResume(String userId, Uri fileUri, @NonNull UploadListener listener) {
        StorageReference resumeRef = storage.getReference()
                .child("resumes")
                .child(userId)
                .child("resume.pdf");

        UploadTask uploadTask = resumeRef.putFile(fileUri);

        uploadTask.addOnProgressListener(snapshot -> {
            if (snapshot.getTotalByteCount() > 0) {
                int percent = (int) (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                listener.onProgress(percent);
            }
        });

        uploadTask.continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException() != null ? task.getException() : new Exception("Upload failed");
                    }
                    return resumeRef.getDownloadUrl();
                }).addOnSuccessListener(uri -> listener.onSuccess(uri.toString()))
                .addOnFailureListener(e -> listener.onFailure(mapError(e)));
    }

    private String mapError(Exception e) {
        String msg = e.getMessage();
        if (msg == null) return "Upload failed. Please try again.";
        if (msg.toLowerCase().contains("permission")) {
            return "Permission denied. Please check your account and try again.";
        }
        if (msg.toLowerCase().contains("network")) {
            return "Network error. Please check your internet connection.";
        }
        return "Upload failed: " + msg;
    }
}