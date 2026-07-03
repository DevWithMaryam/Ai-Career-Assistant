package com.maryam.aicareerassistant.fragments;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.material.card.MaterialCardView;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.utils.FileUtils;
import com.maryam.aicareerassistant.viewmodel.ResumeViewModel;

/**
 * Lets the user pick a PDF resume, extract its text locally,
 * and (when Firebase Storage is enabled) upload to cloud storage.
 */
public class ResumeUploadFragment extends Fragment {

    private ResumeViewModel resumeViewModel;

    private MaterialCardView cardExistingResume;
    private Button btnViewExistingResume;
    private MaterialCardView cardPicker;
    private TextView tvFileName;
    private TextView tvFileSize;
    private Button btnUpload;
    private LinearLayout progressContainer;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;
    private TextView tvExtractionStatus;

    private Uri selectedFileUri = null;
    private String currentResumeUrl = null;

    private final ActivityResultLauncher<String[]> pdfPickerLauncher =
            registerForActivityResult(
                    new ActivityResultContracts.OpenDocument(),
                    this::handlePickedFile);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resume_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // AndroidViewModel requires ViewModelProvider with default factory
        resumeViewModel = new ViewModelProvider(this).get(ResumeViewModel.class);

        bindViews(view);
        setupListeners();
        observeViewModel();

        resumeViewModel.checkExistingResume();
    }

    private void bindViews(View view) {
        cardExistingResume = view.findViewById(R.id.cardExistingResume);
        btnViewExistingResume = view.findViewById(R.id.btnViewExistingResume);
        cardPicker = view.findViewById(R.id.cardPicker);
        tvFileName = view.findViewById(R.id.tvFileName);
        tvFileSize = view.findViewById(R.id.tvFileSize);
        btnUpload = view.findViewById(R.id.btnUpload);
        progressContainer = view.findViewById(R.id.progressContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
        tvExtractionStatus = view.findViewById(R.id.tvExtractionStatus);
    }

    private void setupListeners() {
        cardPicker.setOnClickListener(v ->
                pdfPickerLauncher.launch(new String[]{"application/pdf"}));

        btnUpload.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                // Step 1: Extract text first
                resumeViewModel.extractTextFromPdf(selectedFileUri);
            }
        });

        btnViewExistingResume.setOnClickListener(v -> openResumeUrl(currentResumeUrl));
    }

    private void handlePickedFile(Uri uri) {
        if (uri == null || !isAdded()) return;

        long sizeBytes = FileUtils.getFileSize(
                requireContext().getContentResolver(), uri);

        if (sizeBytes > FileUtils.MAX_FILE_SIZE_BYTES) {
            Toast.makeText(getContext(),
                    R.string.error_file_too_large, Toast.LENGTH_LONG).show();
            return;
        }

        selectedFileUri = uri;
        String fileName = FileUtils.getFileName(
                requireContext().getContentResolver(), uri);

        tvFileName.setText(fileName);
        tvFileSize.setText(FileUtils.formatFileSize(sizeBytes));
        tvFileSize.setVisibility(View.VISIBLE);
        tvExtractionStatus.setVisibility(View.GONE);

        btnUpload.setEnabled(true);
    }

    private void observeViewModel() {

        // --- PDF Extraction ---
        resumeViewModel.getExtractionResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setExtracting(true);
                    tvExtractionStatus.setText(R.string.extracting_text);
                    tvExtractionStatus.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    setExtracting(false);
                    tvExtractionStatus.setText(R.string.extraction_success);

                    // Step 2: Now upload to Firebase Storage
                    // (if Storage not enabled yet, this shows a toast error
                    //  but extraction itself is already done & text is stored in ViewModel)
                    if (selectedFileUri != null) {
                        resumeViewModel.uploadResume(selectedFileUri);
                    }
                    break;

                case ERROR:
                    setExtracting(false);
                    tvExtractionStatus.setVisibility(View.GONE);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        // --- Upload Progress ---
        resumeViewModel.getUploadProgress().observe(getViewLifecycleOwner(), percent -> {
            if (percent == null) return;
            progressBar.setProgress(percent);
            tvProgressPercent.setText(percent + "%");
        });

        // --- Upload Result ---
        resumeViewModel.getUploadResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setUploading(true);
                    break;

                case SUCCESS:
                    setUploading(false);
                    currentResumeUrl = resource.data;
                    showExistingResumeCard(true);
                    Toast.makeText(getContext(),
                            R.string.resume_upload_success, Toast.LENGTH_LONG).show();
                    break;

                case ERROR:
                    setUploading(false);
                    // Storage not enabled yet — extraction already done,
                    // show a specific message instead of raw Firebase error
                    if (resource.message != null &&
                            resource.message.contains("Permission")) {
                        Toast.makeText(getContext(),
                                R.string.storage_not_enabled_yet, Toast.LENGTH_LONG).show();
                    } else {
                        Toast.makeText(getContext(),
                                resource.message, Toast.LENGTH_LONG).show();
                    }
                    break;
            }
        });

        // --- Existing Resume check ---
        resumeViewModel.getExistingResumeResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            if (resource.status == com.maryam.aicareerassistant.utils.Resource.Status.SUCCESS) {
                String url = resource.data;
                if (url != null && !url.isEmpty()) {
                    currentResumeUrl = url;
                    showExistingResumeCard(true);
                }
            }
        });
    }

    private void showExistingResumeCard(boolean show) {
        cardExistingResume.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setExtracting(boolean extracting) {
        btnUpload.setEnabled(!extracting);
        cardPicker.setEnabled(!extracting);
    }

    private void setUploading(boolean uploading) {
        btnUpload.setEnabled(!uploading && selectedFileUri != null);
        cardPicker.setEnabled(!uploading);
        progressContainer.setVisibility(uploading ? View.VISIBLE : View.GONE);
        if (!uploading) {
            progressBar.setProgress(0);
            tvProgressPercent.setText("0%");
        }
    }

    private void openResumeUrl(String url) {
        if (url == null || url.isEmpty() || !isAdded()) return;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        startActivity(intent);
    }
}