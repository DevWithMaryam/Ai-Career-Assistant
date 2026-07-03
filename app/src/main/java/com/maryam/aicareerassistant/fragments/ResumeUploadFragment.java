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
 * Lets the user pick a PDF resume (via Storage Access Framework),
 * upload it to Firebase Storage, and save the download link to Firestore.
 */
public class ResumeUploadFragment extends Fragment {

    private ResumeViewModel resumeViewModel;

    private MaterialCardView cardExistingResume;
    private Button btnViewExistingResume;
    private MaterialCardView cardPicker;
    private ImageView ivPdfIcon;
    private TextView tvFileName;
    private TextView tvFileSize;
    private Button btnUpload;
    private LinearLayout progressContainer;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;

    private Uri selectedFileUri = null;
    private String currentResumeUrl = null;

    private final ActivityResultLauncher<String[]> pdfPickerLauncher =
            registerForActivityResult(new ActivityResultContracts.OpenDocument(), this::handlePickedFile);

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resume_upload, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

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
        ivPdfIcon = view.findViewById(R.id.ivPdfIcon);
        tvFileName = view.findViewById(R.id.tvFileName);
        tvFileSize = view.findViewById(R.id.tvFileSize);
        btnUpload = view.findViewById(R.id.btnUpload);
        progressContainer = view.findViewById(R.id.progressContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
    }

    private void setupListeners() {
        cardPicker.setOnClickListener(v -> pdfPickerLauncher.launch(new String[]{"application/pdf"}));

        btnUpload.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                resumeViewModel.uploadResume(selectedFileUri);
            }
        });

        btnViewExistingResume.setOnClickListener(v -> openResumeUrl(currentResumeUrl));
    }

    private void handlePickedFile(Uri uri) {
        if (uri == null || !isAdded()) return;

        long sizeBytes = FileUtils.getFileSize(requireContext().getContentResolver(), uri);

        if (sizeBytes > FileUtils.MAX_FILE_SIZE_BYTES) {
            Toast.makeText(getContext(), R.string.error_file_too_large, Toast.LENGTH_LONG).show();
            return;
        }

        selectedFileUri = uri;
        String fileName = FileUtils.getFileName(requireContext().getContentResolver(), uri);

        tvFileName.setText(fileName);
        tvFileSize.setText(FileUtils.formatFileSize(sizeBytes));
        tvFileSize.setVisibility(View.VISIBLE);

        btnUpload.setEnabled(true);
    }

    private void observeViewModel() {
        resumeViewModel.getUploadProgress().observe(getViewLifecycleOwner(), percent -> {
            if (percent == null) return;
            progressBar.setProgress(percent);
            tvProgressPercent.setText(percent + "%");
        });

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
                    Toast.makeText(getContext(), R.string.resume_upload_success, Toast.LENGTH_LONG).show();
                    break;
                case ERROR:
                    setUploading(false);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });

        resumeViewModel.getExistingResumeResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.status == com.maryam.aicareerassistant.utils.Resource.Status.SUCCESS) {
                String url = resource.data;
                if (url != null && !url.isEmpty()) {
                    currentResumeUrl = url;
                    showExistingResumeCard(true);
                } else {
                    showExistingResumeCard(false);
                }
            }
            // LOADING / ERROR on this silent background check don't need UI feedback
        });
    }

    private void showExistingResumeCard(boolean show) {
        cardExistingResume.setVisibility(show ? View.VISIBLE : View.GONE);
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