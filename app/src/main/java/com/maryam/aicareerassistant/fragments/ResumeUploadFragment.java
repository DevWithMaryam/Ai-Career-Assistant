package com.maryam.aicareerassistant.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.utils.FileUtils;
import com.maryam.aicareerassistant.viewmodel.CareerViewModel;
import com.maryam.aicareerassistant.viewmodel.ResumeViewModel;

public class ResumeUploadFragment extends Fragment {

    private ResumeViewModel resumeViewModel;
    private CareerViewModel careerViewModel;

    private MaterialCardView cardExistingResume;
    private Button btnViewExistingResume;
    private MaterialCardView cardPicker;
    private TextView tvFileName, tvFileSize, tvExtractionStatus;
    private Button btnUpload;
    private LinearLayout progressContainer;
    private ProgressBar progressBar;
    private TextView tvProgressPercent;

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

        resumeViewModel = new ViewModelProvider(this).get(ResumeViewModel.class);
        // CareerViewModel is activity-scoped — survives navigation
        careerViewModel = new ViewModelProvider(requireActivity()).get(CareerViewModel.class);

        bindViews(view);
        setupListeners();
        observeViewModel(view);
        resumeViewModel.checkExistingResume();
    }

    private void bindViews(View view) {
        cardExistingResume = view.findViewById(R.id.cardExistingResume);
        btnViewExistingResume = view.findViewById(R.id.btnViewExistingResume);
        cardPicker = view.findViewById(R.id.cardPicker);
        tvFileName = view.findViewById(R.id.tvFileName);
        tvFileSize = view.findViewById(R.id.tvFileSize);
        tvExtractionStatus = view.findViewById(R.id.tvExtractionStatus);
        btnUpload = view.findViewById(R.id.btnUpload);
        progressContainer = view.findViewById(R.id.progressContainer);
        progressBar = view.findViewById(R.id.progressBar);
        tvProgressPercent = view.findViewById(R.id.tvProgressPercent);
    }

    private void setupListeners() {
        cardPicker.setOnClickListener(v ->
                pdfPickerLauncher.launch(new String[]{"application/pdf"}));

        btnUpload.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                resumeViewModel.extractTextFromPdf(selectedFileUri);
            }
        });

        btnViewExistingResume.setOnClickListener(v -> {
            if (currentResumeUrl != null && !currentResumeUrl.isEmpty()) {
                android.content.Intent intent = new android.content.Intent(
                        android.content.Intent.ACTION_VIEW,
                        Uri.parse(currentResumeUrl));
                startActivity(intent);
            }
        });
    }

    private void handlePickedFile(Uri uri) {
        if (uri == null || !isAdded()) return;
        long sizeBytes = FileUtils.getFileSize(requireContext().getContentResolver(), uri);
        if (sizeBytes > FileUtils.MAX_FILE_SIZE_BYTES) {
            Toast.makeText(getContext(), R.string.error_file_too_large, Toast.LENGTH_LONG).show();
            return;
        }
        selectedFileUri = uri;
        tvFileName.setText(FileUtils.getFileName(requireContext().getContentResolver(), uri));
        tvFileSize.setText(FileUtils.formatFileSize(sizeBytes));
        tvFileSize.setVisibility(View.VISIBLE);
        tvExtractionStatus.setVisibility(View.GONE);
        btnUpload.setEnabled(true);
    }

    private void observeViewModel(View view) {

        resumeViewModel.getExtractionResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    btnUpload.setEnabled(false);
                    cardPicker.setEnabled(false);
                    tvExtractionStatus.setText(R.string.extracting_text);
                    tvExtractionStatus.setVisibility(View.VISIBLE);
                    break;

                case SUCCESS:
                    // Save extracted text to shared CareerViewModel
                    careerViewModel.setCurrentResumeText(resource.data);
                    tvExtractionStatus.setText(R.string.extraction_success);
                    // Navigate safely
                    if (isAdded() && getView() != null) {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_resumeUpload_to_resumeAnalysis);
                    }
                    break;

                case ERROR:
                    btnUpload.setEnabled(true);
                    cardPicker.setEnabled(true);
                    tvExtractionStatus.setVisibility(View.GONE);
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
                    cardExistingResume.setVisibility(View.VISIBLE);
                }
            }
        });
    }
}