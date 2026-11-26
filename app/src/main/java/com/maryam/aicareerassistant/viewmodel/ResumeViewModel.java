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

/**
 * Handles: PDF -> text extraction (background thread) and
 * saving / loading the resume text via ResumeRepository.
 */
public class ResumeViewModel extends AndroidViewModel {

    private final ResumeRepository resumeRepository;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final MutableLiveData<Resource<String>> processResult = new MutableLiveData<>();
    private final MutableLiveData<Resource<String>> savedResumeResult = new MutableLiveData<>();

    private String extractedResumeText = null;

    public ResumeViewModel(@NonNull Application application) {
        super(application);
        resumeRepository = new ResumeRepository();
    }

    public LiveData<Resource<String>> getProcessResult() { return processResult; }

    public LiveData<Resource<String>> getSavedResumeResult() { return savedResumeResult; }

    public String getExtractedResumeText() { return extractedResumeText; }

    /**
     * Extracts text from the picked PDF, saves it to Firestore in the
     * background, and posts the text to processResult.
     */
    public void processResume(Uri pdfUri, String fileName) {
        processResult.setValue(Resource.loading());

        executor.execute(() -> PdfExtractor.extractText(
                getApplication().getContentResolver(),
                pdfUri,
                new PdfExtractor.ExtractionCallback() {
                    @Override
                    public void onSuccess(String text) {
                        extractedResumeText = text;
                        resumeRepository.saveResumeText(text, fileName);
                        processResult.postValue(Resource.success(text));
                    }

                    @Override
                    public void onFailure(String errorMessage) {
                        processResult.postValue(Resource.error(errorMessage));
                    }
                }));
    }

    /**
     * Call after the UI has handled SUCCESS/ERROR, so coming back to the
     * screen does not trigger the same navigation or toast again.
     */
    public void clearProcessResult() {
        processResult.setValue(null);
    }

    public void loadSavedResume() {
        resumeRepository.fetchSavedResumeText(savedResumeResult);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        executor.shutdown();
    }
}