package com.maryam.aicareerassistant.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maryam.aicareerassistant.firebase.FirebaseAuthHelper;
import com.maryam.aicareerassistant.model.User;
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.utils.Resource;
import com.maryam.aicareerassistant.viewmodel.AuthViewModel;

/**
 * Single source of truth for authentication. ViewModel never talks to
 * Firebase directly — always through this Repository.
 */
public class AuthRepository {

    private final FirebaseAuthHelper authHelper;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        authHelper = new FirebaseAuthHelper();
        firestore = FirebaseFirestore.getInstance();
    }

    public void login(String email, String password, MutableLiveData<Resource<FirebaseUser>> result) {
        result.setValue(Resource.loading());
        authHelper.login(email, password, new FirebaseAuthHelper.AuthListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                result.setValue(Resource.success(user));
            }

            @Override
            public void onFailure(String errorMessage) {
                result.setValue(Resource.error(errorMessage));
            }
        });
    }

    public void signup(String name, String email, String password, MutableLiveData<Resource<FirebaseUser>> result) {
        result.setValue(Resource.loading());
        authHelper.signUp(email, password, new FirebaseAuthHelper.AuthListener() {
            @Override
            public void onSuccess(FirebaseUser firebaseUser) {
                if (firebaseUser == null) {
                    result.setValue(Resource.error("Signup failed. Please try again."));
                    return;
                }
                User user = new User(firebaseUser.getUid(), name, email);
                firestore.collection(Constants.COLLECTION_USERS)
                        .document(firebaseUser.getUid())
                        .set(user.toMap())
                        .addOnSuccessListener(unused -> result.setValue(Resource.success(firebaseUser)))
                        .addOnFailureListener(e -> result.setValue(
                                Resource.error("Account created but profile setup failed: " + e.getMessage())));
            }

            @Override
            public void onFailure(String errorMessage) {
                result.setValue(Resource.error(errorMessage));
            }
        });
    }

    public void sendPasswordResetEmail(String email, MutableLiveData<Resource<Void>> result) {
        result.setValue(Resource.loading());
        authHelper.sendPasswordResetEmail(email, new FirebaseAuthHelper.AuthListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                result.setValue(Resource.success(null));
            }

            @Override
            public void onFailure(String errorMessage) {
                result.setValue(Resource.error(errorMessage));
            }
        });
    }

    public void logout() {
        authHelper.logout();
    }

    public FirebaseUser getCurrentUser() {
        return authHelper.getCurrentUser();
    }

    /**
     * Asks Firebase to reload the current user's data from its servers.
     * Fails if the account was deleted, disabled, or the session token
     * was revoked — which getCurrentUser() alone cannot detect.
     */
    public void verifySession(AuthViewModel.SessionCallback callback) {
        FirebaseUser user = authHelper.getCurrentUser();
        if (user == null) {
            callback.onInvalid();
            return;
        }

        user.reload()
                .addOnSuccessListener(unused -> callback.onValid())
                .addOnFailureListener(e -> callback.onInvalid());
    }
}