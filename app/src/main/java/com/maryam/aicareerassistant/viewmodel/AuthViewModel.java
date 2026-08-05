package com.maryam.aicareerassistant.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseUser;
import com.maryam.aicareerassistant.repository.AuthRepository;
import com.maryam.aicareerassistant.utils.Resource;
import com.maryam.aicareerassistant.utils.Validator;

public class AuthViewModel extends ViewModel {

    private final AuthRepository authRepository;

    private final MutableLiveData<Resource<FirebaseUser>> authResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> resetPasswordResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<Void>> resendVerificationResult = new MutableLiveData<>();

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    public LiveData<Resource<FirebaseUser>> getAuthResult() { return authResult; }
    public LiveData<Resource<Void>> getResetPasswordResult() { return resetPasswordResult; }
    public LiveData<Resource<Void>> getResendVerificationResult() { return resendVerificationResult; }

    public void login(String email, String password) {
        if (!Validator.isValidEmail(email)) {
            authResult.setValue(Resource.error("Please enter a valid email address."));
            return;
        }
        if (!Validator.isValidPassword(password)) {
            authResult.setValue(Resource.error("Password must be at least 6 characters."));
            return;
        }
        authRepository.login(email, password, authResult);
    }

    public void signup(String name, String email, String password, String confirmPassword) {
        if (!Validator.isValidName(name)) {
            authResult.setValue(Resource.error("Please enter your name."));
            return;
        }
        if (!Validator.isValidEmail(email)) {
            authResult.setValue(Resource.error("Please enter a valid email address."));
            return;
        }
        if (!Validator.isValidPassword(password)) {
            authResult.setValue(Resource.error("Password must be at least 6 characters."));
            return;
        }
        if (!Validator.passwordsMatch(password, confirmPassword)) {
            authResult.setValue(Resource.error("Passwords do not match."));
            return;
        }
        authRepository.signup(name, email, password, authResult);
    }

    public void sendPasswordResetEmail(String email) {
        if (!Validator.isValidEmail(email)) {
            resetPasswordResult.setValue(
                    Resource.error("Please enter a valid email address."));
            return;
        }
        authRepository.sendPasswordResetEmail(email, resetPasswordResult);
    }

    public void resendVerificationEmail() {
        authRepository.resendVerificationEmail(resendVerificationResult);
    }

    public boolean isUserLoggedIn() {
        return authRepository.getCurrentUser() != null;
    }

    public void logout() { authRepository.logout(); }

    public FirebaseUser getCurrentUserOrNull() {
        return authRepository.getCurrentUser();
    }

    public interface SessionCallback {
        void onValid();
        void onInvalid();
    }

    public void verifySession(SessionCallback callback) {
        authRepository.verifySession(callback);
    }
}