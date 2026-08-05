package com.maryam.aicareerassistant.repository;

import androidx.lifecycle.MutableLiveData;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maryam.aicareerassistant.firebase.FirebaseAuthHelper;
import com.maryam.aicareerassistant.model.User;
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.utils.Resource;
import com.maryam.aicareerassistant.viewmodel.AuthViewModel;

public class AuthRepository {

    private final FirebaseAuthHelper authHelper;
    private final FirebaseFirestore firestore;

    public AuthRepository() {
        authHelper = new FirebaseAuthHelper();
        firestore = FirebaseFirestore.getInstance();
    }

    public void login(String email, String password,
                      MutableLiveData<Resource<FirebaseUser>> result) {
        result.setValue(Resource.loading());
        authHelper.login(email, password, new FirebaseAuthHelper.AuthListener() {
            @Override
            public void onSuccess(FirebaseUser user) {
                result.setValue(Resource.success(user));
            }
            @Override
            public void onFailure(String errorMessage) {
                // Pass special code so ViewModel/UI can show resend option
                result.setValue(Resource.error(errorMessage));
            }
        });
    }

    public void signup(String name, String email, String password,
                       MutableLiveData<Resource<FirebaseUser>> result) {
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
                        .addOnSuccessListener(unused ->
                                result.setValue(Resource.success(firebaseUser)))
                        .addOnFailureListener(e ->
                                result.setValue(Resource.error(
                                        "Account created but profile setup failed.")));
            }
            @Override
            public void onFailure(String errorMessage) {
                result.setValue(Resource.error(errorMessage));
            }
        });
    }

    public void sendPasswordResetEmail(String email,
                                       MutableLiveData<Resource<Void>> result) {
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

    public void resendVerificationEmail(MutableLiveData<Resource<Void>> result) {
        result.setValue(Resource.loading());
        authHelper.resendVerificationEmail(new FirebaseAuthHelper.AuthListener() {
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

    public void logout() { authHelper.logout(); }

    public FirebaseUser getCurrentUser() { return authHelper.getCurrentUser(); }

    public void verifySession(AuthViewModel.SessionCallback callback) {
        FirebaseUser user = authHelper.getCurrentUser();
        if (user == null) { callback.onInvalid(); return; }
        user.reload()
                .addOnSuccessListener(unused -> {
                    if (user.isEmailVerified()) callback.onValid();
                    else callback.onInvalid();
                })
                .addOnFailureListener(e -> callback.onInvalid());
    }
}