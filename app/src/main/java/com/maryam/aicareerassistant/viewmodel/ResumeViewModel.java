package com.maryam.aicareerassistant.viewmodel;

import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.maryam.aicareerassistant.repository.ResumeRepository;
import com.maryam.aicareerassistant.utils.Resource;

public class ResumeViewModel extends ViewModel {

    private final ResumeRepository resumeRepository;

    private final MutableLiveData<Integer> uploadProgress = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> uploadResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> existingResumeResult = new MutableLiveData<>();

    public ResumeViewModel() {
        resumeRepository = new ResumeRepository();
    }

    public LiveData<Integer> getUploadProgress() {
        return uploadProgress;
    }

    public LiveData<Resource<String>> getUploadResult() {
        return uploadResult;
    }

    public LiveData<Resource<String>> getExistingResumeResult() {
        return existingResumeResult;
    }

    public void uploadResume(Uri fileUri) {
        resumeRepository.uploadResume(fileUri, uploadProgress, uploadResult);
    }

    public void checkExistingResume() {
        resumeRepository.fetchExistingResumeUrl(existingResumeResult);
    }
}