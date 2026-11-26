package com.maryam.aicareerassistant.fragments;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.utils.FileUtils;
import com.maryam.aicareerassistant.utils.Resource;
import com.maryam.aicareerassistant.viewmodel.CareerViewModel;
import com.maryam.aicareerassistant.viewmodel.ResumeViewModel;

/**
 * Lets the user pick a PDF resume. The text is extracted locally and saved
 * to Firestore (no Firebase Storage needed). Returning users can reuse
 * their saved resume with one tap.
 */
public class ResumeUploadFragment extends Fragment {

    private ResumeViewModel resumeViewModel;
    private CareerViewModel careerViewModel;

    private MaterialCardView cardExistingResume;
    private MaterialCardView cardPicker;
    private Button btnUseSaved;
    private Button btnUpload;
    private TextView tvFileName;
    private TextView tvFileSize;
    private TextView tvExtractionStatus;

    private Uri selectedFileUri = null;
    private String selectedFileName = null;
    private String savedResumeText = null;

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
        // Activity-scoped: shares resume text with the next screens
        careerViewModel = new ViewModelProvider(requireActivity()).get(CareerViewModel.class);

        bindViews(view);
        setupListeners();
        observeViewModel();

        resumeViewModel.loadSavedResume();
    }

    private void bindViews(View view) {
        cardExistingResume = view.findViewById(R.id.cardExistingResume);
        cardPicker = view.findViewById(R.id.cardPicker);
        btnUseSaved = view.findViewById(R.id.btnViewExistingResume);
        btnUpload = view.findViewById(R.id.btnUpload);
        tvFileName = view.findViewById(R.id.tvFileName);
        tvFileSize = view.findViewById(R.id.tvFileSize);
        tvExtractionStatus = view.findViewById(R.id.tvExtractionStatus);

        btnUseSaved.setText(R.string.use_saved_resume);
    }

    private void setupListeners() {
        cardPicker.setOnClickListener(v ->
                pdfPickerLauncher.launch(new String[]{"application/pdf"}));

        btnUpload.setOnClickListener(v -> {
            if (selectedFileUri != null) {
                resumeViewModel.processResume(selectedFileUri, selectedFileName);
            }
        });

        btnUseSaved.setOnClickListener(v -> {
            if (savedResumeText == null || savedResumeText.trim().isEmpty()) return;
            careerViewModel.setCurrentResumeText(savedResumeText);
            goToAnalysis();
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
        selectedFileName = FileUtils.getFileName(requireContext().getContentResolver(), uri);

        tvFileName.setText(selectedFileName);
        tvFileSize.setText(FileUtils.formatFileSize(sizeBytes));
        tvFileSize.setVisibility(View.VISIBLE);
        tvExtractionStatus.setVisibility(View.GONE);

        btnUpload.setEnabled(true);
    }

    private void observeViewModel() {

        resumeViewModel.getProcessResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            switch (resource.status) {
                case LOADING:
                    setBusy(true);
                    break;

                case SUCCESS:
                    setBusy(false);
                    resumeViewModel.clearProcessResult();
                    careerViewModel.setCurrentResumeText(resource.data);
                    goToAnalysis();
                    break;

                case ERROR:
                    setBusy(false);
                    tvExtractionStatus.setVisibility(View.GONE);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    resumeViewModel.clearProcessResult();
                    break;
            }
        });

        resumeViewModel.getSavedResumeResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;

            if (resource.status == Resource.Status.SUCCESS) {
                savedResumeText = resource.data;
                boolean hasSaved = savedResumeText != null
                        && !savedResumeText.trim().isEmpty();
                cardExistingResume.setVisibility(hasSaved ? View.VISIBLE : View.GONE);
            }
            // LOADING / ERROR on this silent background check need no UI
        });
    }

    private void setBusy(boolean busy) {
        btnUpload.setEnabled(!busy && selectedFileUri != null);
        cardPicker.setEnabled(!busy);
        btnUseSaved.setEnabled(!busy);

        if (busy) {
            tvExtractionStatus.setText(R.string.extracting_text);
            tvExtractionStatus.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Navigates only if we are still on this screen (prevents a crash
     * from double taps or a late callback).
     */
    private void goToAnalysis() {
        if (!isAdded() || getView() == null) return;

        NavController navController = Navigation.findNavController(requireView());
        NavDestination current = navController.getCurrentDestination();
        if (current != null && current.getId() == R.id.resumeUploadFragment) {
            navController.navigate(R.id.action_resumeUpload_to_resumeAnalysis);
        }
    }
}