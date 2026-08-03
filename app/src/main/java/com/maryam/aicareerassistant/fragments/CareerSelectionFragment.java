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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.viewmodel.CareerViewModel;

/**
 * Lets the user pick their target career. Selection is:
 *  1. Saved in CareerViewModel (for the current session — Skill Gap, Roadmap, etc.)
 *  2. Persisted in Firestore users/{uid}.targetCareer
 */
public class CareerSelectionFragment extends Fragment {

    private CareerViewModel careerViewModel;

    private MaterialCardView cardAndroid, cardFrontend, cardBackend, cardData;
    private ImageView checkAndroid, checkFrontend, checkBackend, checkData;
    private Button btnConfirmCareer;

    private String selectedCareer = null;

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

        checkAndroid = view.findViewById(R.id.checkAndroid);
        checkFrontend = view.findViewById(R.id.checkFrontend);
        checkBackend = view.findViewById(R.id.checkBackend);
        checkData = view.findViewById(R.id.checkData);

        btnConfirmCareer = view.findViewById(R.id.btnConfirmCareer);
    }

    private void restorePreviousSelection() {
        // If user already chose a career earlier in this session, restore it
        String prev = careerViewModel.getCurrentTargetCareer();
        if (prev != null) {
            selectedCareer = prev;
            highlightSelected(prev);
            btnConfirmCareer.setEnabled(true);
        }
    }

    private void setupCardClicks() {
        cardAndroid.setOnClickListener(v -> selectCareer(Constants.CAREER_ANDROID));
        cardFrontend.setOnClickListener(v -> selectCareer(Constants.CAREER_FRONTEND));
        cardBackend.setOnClickListener(v -> selectCareer(Constants.CAREER_BACKEND));
        cardData.setOnClickListener(v -> selectCareer(Constants.CAREER_DATA));
    }

    private void selectCareer(String career) {
        selectedCareer = career;
        highlightSelected(career);
        btnConfirmCareer.setEnabled(true);
    }

    private void highlightSelected(String career) {
        // Reset all cards
        resetCard(cardAndroid, checkAndroid);
        resetCard(cardFrontend, checkFrontend);
        resetCard(cardBackend, checkBackend);
        resetCard(cardData, checkData);

        // Highlight selected
        int selectedColor = requireContext().getColor(R.color.primary);
        int selectedStroke = requireContext().getColor(R.color.primary);

        if (career.equals(Constants.CAREER_ANDROID)) {
            cardAndroid.setStrokeColor(selectedColor);
            cardAndroid.setCardBackgroundColor(
                    requireContext().getColor(R.color.primary_light) & 0x20FFFFFF
                            | 0xFFF5F6FF);
            checkAndroid.setVisibility(View.VISIBLE);
        } else if (career.equals(Constants.CAREER_FRONTEND)) {
            cardFrontend.setStrokeColor(selectedColor);
            checkFrontend.setVisibility(View.VISIBLE);
        } else if (career.equals(Constants.CAREER_BACKEND)) {
            cardBackend.setStrokeColor(selectedColor);
            checkBackend.setVisibility(View.VISIBLE);
        } else if (career.equals(Constants.CAREER_DATA)) {
            cardData.setStrokeColor(selectedColor);
            checkData.setVisibility(View.VISIBLE);
        }
    }

    private void resetCard(MaterialCardView card, ImageView check) {
        card.setStrokeColor(requireContext().getColor(R.color.primary_light));
        card.setCardBackgroundColor(requireContext().getColor(R.color.surface));
        check.setVisibility(View.GONE);
    }

    private void confirmSelection(View view) {
        if (selectedCareer == null) return;

        // Save in ViewModel (session)
        careerViewModel.setCurrentTargetCareer(selectedCareer);

        // Persist in Firestore
        String uid = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (uid != null) {
            FirebaseFirestore.getInstance()
                    .collection(Constants.COLLECTION_USERS)
                    .document(uid)
                    .update("targetCareer", selectedCareer)
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(),
                                    "Saved locally, sync failed: " + e.getMessage(),
                                    Toast.LENGTH_SHORT).show());
        }

        Toast.makeText(getContext(),
                getString(R.string.career_selected, selectedCareer),
                Toast.LENGTH_SHORT).show();

        Navigation.findNavController(view)
                .navigate(R.id.action_careerSelection_to_skillGap);
    }
}