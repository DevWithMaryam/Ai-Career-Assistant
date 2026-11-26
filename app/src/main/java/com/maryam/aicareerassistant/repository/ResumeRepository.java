package com.maryam.aicareerassistant.repository;

import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.utils.Resource;

import java.util.HashMap;
import java.util.Map;

/**
 * Stores the extracted resume TEXT in Firestore (users/{uid}.resumeText).
 * No Firebase Storage needed, so it works on the free Spark plan.
 * Later, a Storage-based upload can be added here without touching
 * ViewModels or Fragments.
 */
public class ResumeRepository {

    private static final String TAG = "ResumeRepository";

    // Firestore documents are limited to 1 MB, so we cap the saved text.
    private static final int MAX_TEXT_LENGTH = 60000;

    private static final String FIELD_TEXT = "resumeText";
    private static final String FIELD_FILE_NAME = "resumeFileName";
    private static final String FIELD_UPDATED_AT = "resumeUpdatedAt";

    private final FirebaseFirestore firestore;
    private final FirebaseAuth firebaseAuth;

    public ResumeRepository() {
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
    }

    /**
     * Saves resume text in the background. Fire-and-forget on purpose:
     * the UI must not wait on the network to continue.
     */
    public void saveResumeText(String text, String fileName) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null || text == null || text.trim().isEmpty()) return;

        String trimmed = text.length() > MAX_TEXT_LENGTH
                ? text.substring(0, MAX_TEXT_LENGTH) : text;

        Map<String, Object> data = new HashMap<>();
        data.put(FIELD_TEXT, trimmed);
        data.put(FIELD_FILE_NAME, fileName != null ? fileName : "resume.pdf");
        data.put(FIELD_UPDATED_AT, System.currentTimeMillis());

        firestore.collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .set(data, SetOptions.merge())
                .addOnFailureListener(e ->
                        Log.e(TAG, "Failed to save resume text: " + e.getMessage()));
    }

    /**
     * Loads the previously saved resume text (null/empty if none).
     */
    public void fetchSavedResumeText(MutableLiveData<Resource<String>> result) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user == null) {
            result.setValue(Resource.error("You must be logged in."));
            return;
        }

        result.setValue(Resource.loading());

        firestore.collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot ->
                        result.setValue(Resource.success(snapshot.getString(FIELD_TEXT))))
                .addOnFailureListener(e ->
                        result.setValue(Resource.error(
                                "Failed to check saved resume: " + e.getMessage())));
    }
}