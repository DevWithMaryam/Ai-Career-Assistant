package com.maryam.aicareerassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.card.MaterialCardView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.model.ResumeAnalysis;
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.utils.JsonParser;
import com.maryam.aicareerassistant.viewmodel.CareerViewModel;

import java.util.List;

public class ResumeAnalysisFragment extends Fragment {

    private CareerViewModel careerViewModel;

    private Button btnAnalyze, btnNextCareer;
    private LinearLayout layoutLoading;
    private MaterialCardView cardScore, cardStrengths, cardWeaknesses, cardSuggestions;
    private TextView tvScore, tvStrengths, tvWeaknesses, tvSuggestions;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_resume_analysis, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Activity-scoped — has the resume text set by ResumeUploadFragment
        careerViewModel = new ViewModelProvider(requireActivity()).get(CareerViewModel.class);

        bindViews(view);
        observeAnalysis(view);

        btnAnalyze.setOnClickListener(v -> startAnalysis());
        btnNextCareer.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_resumeAnalysis_to_careerSelection));
    }

    private void bindViews(View view) {
        btnAnalyze = view.findViewById(R.id.btnAnalyze);
        btnNextCareer = view.findViewById(R.id.btnNextCareer);
        layoutLoading = view.findViewById(R.id.layoutLoading);
        cardScore = view.findViewById(R.id.cardScore);
        cardStrengths = view.findViewById(R.id.cardStrengths);
        cardWeaknesses = view.findViewById(R.id.cardWeaknesses);
        cardSuggestions = view.findViewById(R.id.cardSuggestions);
        tvScore = view.findViewById(R.id.tvScore);
        tvStrengths = view.findViewById(R.id.tvStrengths);
        tvWeaknesses = view.findViewById(R.id.tvWeaknesses);
        tvSuggestions = view.findViewById(R.id.tvSuggestions);
    }

    private void startAnalysis() {
        String resumeText = careerViewModel.getCurrentResumeText();
        if (resumeText == null || resumeText.trim().isEmpty()) {
            Toast.makeText(getContext(), R.string.error_no_resume_text, Toast.LENGTH_LONG).show();
            return;
        }
        careerViewModel.analyzeResume(resumeText);
    }

    private void observeAnalysis(View view) {
        careerViewModel.getAnalysisResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true);
                    break;
                case SUCCESS:
                    setLoading(false);
                    parseAndDisplay(resource.data);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void parseAndDisplay(String jsonData) {
        try {
            ResumeAnalysis analysis = JsonParser.parseResumeAnalysis(jsonData);
            tvScore.setText(String.valueOf(analysis.getScore()));
            cardScore.setVisibility(View.VISIBLE);
            tvStrengths.setText(formatList(analysis.getStrengths(), "✓"));
            cardStrengths.setVisibility(View.VISIBLE);
            tvWeaknesses.setText(formatList(analysis.getWeaknesses(), "✗"));
            cardWeaknesses.setVisibility(View.VISIBLE);
            tvSuggestions.setText(formatList(analysis.getSuggestions(), "→"));
            cardSuggestions.setVisibility(View.VISIBLE);
            btnNextCareer.setVisibility(View.VISIBLE);
            saveToFirestore(analysis);
        } catch (Exception e) {
            Toast.makeText(getContext(),
                    "Could not parse AI response. Please try again.", Toast.LENGTH_LONG).show();
        }
    }

    private String formatList(List<String> items, String bullet) {
        if (items == null || items.isEmpty()) return "None";
        StringBuilder sb = new StringBuilder();
        for (String item : items) sb.append(bullet).append(" ").append(item).append("\n\n");
        return sb.toString().trim();
    }

    private void saveToFirestore(ResumeAnalysis analysis) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS).document(uid)
                .collection(Constants.COLLECTION_ANALYSIS).add(analysis);
    }

    private void setLoading(boolean loading) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAnalyze.setEnabled(!loading);
        if (loading) {
            cardScore.setVisibility(View.GONE);
            cardStrengths.setVisibility(View.GONE);
            cardWeaknesses.setVisibility(View.GONE);
            cardSuggestions.setVisibility(View.GONE);
            btnNextCareer.setVisibility(View.GONE);
        }
    }
}