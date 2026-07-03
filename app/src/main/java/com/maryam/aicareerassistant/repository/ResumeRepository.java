package com.maryam.aicareerassistant.repository;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maryam.aicareerassistant.firebase.StorageHelper;
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.utils.Resource;

/**
 * Single source of truth for resume upload + retrieval.
 * Coordinates Firebase Storage (file) and Firestore (resumeUrl reference).
 */
public class ResumeRepository {

    private final StorageHelper storageHelper;
    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    public ResumeRepository() {
        storageHelper = new StorageHelper();
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    public void uploadResume(Uri fileUri,
                             MutableLiveData<Integer> progress,
                             MutableLiveData<Resource<String>> result) {

        if (firebaseAuth.getCurrentUser() == null) {
            result.setValue(Resource.error("You must be logged in to upload a resume."));
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        result.setValue(Resource.loading());
        progress.setValue(0);

        storageHelper.uploadResume(userId, fileUri, new StorageHelper.UploadListener() {
            @Override
            public void onProgress(int percent) {
                progress.setValue(percent);
            }

            @Override
            public void onSuccess(String downloadUrl) {
                firestore.collection(Constants.COLLECTION_USERS)
                        .document(userId)
                        .update("resumeUrl", downloadUrl)
                        .addOnSuccessListener(unused -> result.setValue(Resource.success(downloadUrl)))
                        .addOnFailureListener(e -> result.setValue(
                                Resource.error("Resume uploaded but failed to save link: " + e.getMessage())));
            }

            @Override
            public void onFailure(String errorMessage) {
                result.setValue(Resource.error(errorMessage));
            }
        });
    }

    public void fetchExistingResumeUrl(MutableLiveData<Resource<String>> result) {
        if (firebaseAuth.getCurrentUser() == null) {
            result.setValue(Resource.error("You must be logged in."));
            return;
        }

        String userId = firebaseAuth.getCurrentUser().getUid();
        result.setValue(Resource.loading());

        firestore.collection(Constants.COLLECTION_USERS)
                .document(userId)
                .get()
                .addOnSuccessListener(snapshot -> {
                    String url = snapshot.getString("resumeUrl");
                    result.setValue(Resource.success(url)); // url may be null -> no resume yet
                })
                .addOnFailureListener(e -> result.setValue(
                        Resource.error("Failed to check existing resume: " + e.getMessage())));
    }
}