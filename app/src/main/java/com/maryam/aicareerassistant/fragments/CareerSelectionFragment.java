package com.maryam.aicareerassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.viewmodel.CareerViewModel;

public class CareerSelectionFragment extends Fragment {

    private CareerViewModel careerViewModel;

    private MaterialCardView cardAndroid, cardFrontend, cardBackend,
            cardData, cardCustom;
    private ImageView checkAndroid, checkFrontend, checkBackend,
            checkData, checkCustom;
    private TextInputLayout tilCustomCareer;
    private TextInputEditText etCustomCareer;
    private Button btnConfirmCareer;

    private String selectedCareer = null;
    private boolean isCustomSelected = false;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_career_selection, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        careerViewModel = new ViewModelProvider(requireActivity()).get(CareerViewModel.class);

        bindViews(view);
        restorePreviousSelection();
        setupCardClicks();

        btnConfirmCareer.setOnClickListener(v -> confirmSelection(view));
    }

    private void bindViews(View view) {
        cardAndroid = view.findViewById(R.id.cardAndroid);
        cardFrontend = view.findViewById(R.id.cardFrontend);
        cardBackend = view.findViewById(R.id.cardBackend);
        cardData = view.findViewById(R.id.cardData);
        cardCustom = view.findViewById(R.id.cardCustom);

        checkAndroid = view.findViewById(R.id.checkAndroid);
        checkFrontend = view.findViewById(R.id.checkFrontend);
        checkBackend = view.findViewById(R.id.checkBackend);
        checkData = view.findViewById(R.id.checkData);
        checkCustom = view.findViewById(R.id.checkCustom);

        tilCustomCareer = view.findViewById(R.id.tilCustomCareer);
        etCustomCareer = view.findViewById(R.id.etCustomCareer);
        btnConfirmCareer = view.findViewById(R.id.btnConfirmCareer);
    }

    private void restorePreviousSelection() {
        String prev = careerViewModel.getCurrentTargetCareer();
        if (prev == null) return;

        if (prev.equals(Constants.CAREER_ANDROID) ||
                prev.equals(Constants.CAREER_FRONTEND) ||
                prev.equals(Constants.CAREER_BACKEND) ||
                prev.equals(Constants.CAREER_DATA)) {
            selectedCareer = prev;
            highlightSelected(prev);
        } else {
            // Was a custom career
            isCustomSelected = true;
            selectedCareer = prev;
            highlightCustom();
            etCustomCareer.setText(prev);
        }
        btnConfirmCareer.setEnabled(true);
    }

    private void setupCardClicks() {
        cardAndroid.setOnClickListener(v -> selectPreset(Constants.CAREER_ANDROID));
        cardFrontend.setOnClickListener(v -> selectPreset(Constants.CAREER_FRONTEND));
        cardBackend.setOnClickListener(v -> selectPreset(Constants.CAREER_BACKEND));
        cardData.setOnClickListener(v -> selectPreset(Constants.CAREER_DATA));

        cardCustom.setOnClickListener(v -> {
            isCustomSelected = true;
            selectedCareer = null; // Will be read from text field
            highlightCustom();
            btnConfirmCareer.setEnabled(true);
        });
    }

    private void selectPreset(String career) {
        isCustomSelected = false;
        selectedCareer = career;
        highlightSelected(career);
        tilCustomCareer.setVisibility(View.GONE);
        btnConfirmCareer.setEnabled(true);
    }

    private void highlightSelected(String career) {
        resetAllCards();
        int color = requireContext().getColor(R.color.primary);

        if (career.equals(Constants.CAREER_ANDROID)) {
            cardAndroid.setStrokeColor(color);
            checkAndroid.setVisibility(View.VISIBLE);
        } else if (career.equals(Constants.CAREER_FRONTEND)) {
            cardFrontend.setStrokeColor(color);
            checkFrontend.setVisibility(View.VISIBLE);
        } else if (career.equals(Constants.CAREER_BACKEND)) {
            cardBackend.setStrokeColor(color);
            checkBackend.setVisibility(View.VISIBLE);
        } else if (career.equals(Constants.CAREER_DATA)) {
            cardData.setStrokeColor(color);
            checkData.setVisibility(View.VISIBLE);
        }
    }

    private void highlightCustom() {
        resetAllCards();
        int color = requireContext().getColor(R.color.primary);
        cardCustom.setStrokeColor(color);
        checkCustom.setVisibility(View.VISIBLE);
        tilCustomCareer.setVisibility(View.VISIBLE);
    }

    private void resetAllCards() {
        int defaultColor = requireContext().getColor(R.color.primary_light);
        int surfaceColor = requireContext().getColor(R.color.surface);

        cardAndroid.setStrokeColor(defaultColor);
        cardAndroid.setCardBackgroundColor(surfaceColor);
        cardFrontend.setStrokeColor(defaultColor);
        cardFrontend.setCardBackgroundColor(surfaceColor);
        cardBackend.setStrokeColor(defaultColor);
        cardBackend.setCardBackgroundColor(surfaceColor);
        cardData.setStrokeColor(defaultColor);
        cardData.setCardBackgroundColor(surfaceColor);
        cardCustom.setStrokeColor(defaultColor);
        cardCustom.setCardBackgroundColor(surfaceColor);

        checkAndroid.setVisibility(View.GONE);
        checkFrontend.setVisibility(View.GONE);
        checkBackend.setVisibility(View.GONE);
        checkData.setVisibility(View.GONE);
        checkCustom.setVisibility(View.GONE);
    }

    private void confirmSelection(View view) {
        String career;

        if (isCustomSelected) {
            // Read from text field
            String customText = etCustomCareer.getText() != null
                    ? etCustomCareer.getText().toString().trim() : "";
            if (customText.isEmpty()) {
                Toast.makeText(getContext(),
                        R.string.error_enter_career, Toast.LENGTH_SHORT).show();
                etCustomCareer.requestFocus();
                return;
            }
            career = customText;
        } else {
            if (selectedCareer == null) return;
            career = selectedCareer;
        }

        // Save in ViewModel session
        careerViewModel.setCurrentTargetCareer(career);

        // Persist in Firestore
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;
        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .update("targetCareer", career);
        }

        Toast.makeText(getContext(),
                getString(R.string.career_selected, career),
                Toast.LENGTH_SHORT).show();

        Navigation.findNavController(view)
                .navigate(R.id.action_careerSelection_to_skillGap);
    }
}