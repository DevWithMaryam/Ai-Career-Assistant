package com.maryam.aicareerassistant.viewmodel;

import android.app.Application;
import android.net.Uri;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.maryam.aicareerassistant.repository.ResumeRepository;
import com.maryam.aicareerassistant.utils.PdfExtractor;
import com.maryam.aicareerassistant.utils.Resource;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ResumeViewModel extends AndroidViewModel {

    private final ResumeRepository resumeRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Integer> uploadProgress = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> uploadResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> existingResumeResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> extractionResult = new MutableLiveData<>();

    private String extractedResumeText = null;

    public ResumeViewModel(@NonNull Application application) {
        super(application);
        resumeRepository = new ResumeRepository();
    }

    public LiveData<Integer> getUploadProgress() { return uploadProgress; }
    public LiveData<Resource<String>> getUploadResult() { return uploadResult; }
    public LiveData<Resource<String>> getExistingResumeResult() { return existingResumeResult; }
    public LiveData<Resource<String>> getExtractionResult() { return extractionResult; }

    public String getExtractedResumeText() { return extractedResumeText; }

    public void uploadResume(Uri fileUri) {
        resumeRepository.uploadResume(fileUri, uploadProgress, uploadResult);
    }

    public void checkExistingResume() {
        resumeRepository.fetchExistingResumeUrl(existingResumeResult);
    }

    public void extractTextFromPdf(Uri pdfUri) {
        extractionResult.setValue(Resource.loading());

        executor.execute(() -> {
            PdfExtractor.extractText(
                    getApplication().getContentResolver(),
                    pdfUri,
                    new PdfExtractor.ExtractionCallback() {
                        @Override
                        public void onSuccess(String text) {
                            extractedResumeText = text;
                            extractionResult.postValue(Resource.success(text));
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            extractionResult.postValue(Resource.error(errorMessage));
                        }
                    }
            );
        });
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}