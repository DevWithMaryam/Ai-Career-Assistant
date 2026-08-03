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
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.utils.JsonParser;
import com.maryam.aicareerassistant.viewmodel.CareerViewModel;

import java.util.List;

public class SkillGapFragment extends Fragment {

    private CareerViewModel careerViewModel;
    private Button btnAnalyzeGap, btnNextRoadmap;
    private LinearLayout layoutLoading;
    private MaterialCardView cardExisting, cardMissing, cardPriority;
    private TextView tvTargetCareer, tvExistingSkills, tvMissingSkills, tvPrioritySkills;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_skill_gap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        careerViewModel = new ViewModelProvider(requireActivity()).get(CareerViewModel.class);

        tvTargetCareer = view.findViewById(R.id.tvTargetCareer);
        btnAnalyzeGap = view.findViewById(R.id.btnAnalyzeGap);
        layoutLoading = view.findViewById(R.id.layoutLoading);
        cardExisting = view.findViewById(R.id.cardExisting);
        cardMissing = view.findViewById(R.id.cardMissing);
        cardPriority = view.findViewById(R.id.cardPriority);
        tvExistingSkills = view.findViewById(R.id.tvExistingSkills);
        tvMissingSkills = view.findViewById(R.id.tvMissingSkills);
        tvPrioritySkills = view.findViewById(R.id.tvPrioritySkills);
        btnNextRoadmap = view.findViewById(R.id.btnNextRoadmap);

        String career = careerViewModel.getCurrentTargetCareer();
        if (career != null) tvTargetCareer.setText("Target: " + career);

        btnAnalyzeGap.setOnClickListener(v -> {
            if (career == null) {
                Toast.makeText(getContext(), "Please select a target career first.", Toast.LENGTH_SHORT).show();
                return;
            }
            careerViewModel.getSkillGap(career);
        });

        btnNextRoadmap.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_skillGap_to_roadmap));

        careerViewModel.getSkillGapResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true); break;
                case SUCCESS:
                    setLoading(false);
                    displaySkillGap(resource.data);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void displaySkillGap(String json) {
        try {
            List<String> existing = JsonParser.parseStringList(json, "existing_skills");
            List<String> missing = JsonParser.parseStringList(json, "missing_skills");
            List<String> priority = JsonParser.parseStringList(json, "priority_skills");

            // Save missing skills for roadmap generation
            careerViewModel.setCurrentMissingSkills(String.join(", ", missing));

            tvExistingSkills.setText(formatList(existing, "✓"));
            cardExisting.setVisibility(View.VISIBLE);

            tvMissingSkills.setText(formatList(missing, "✗"));
            cardMissing.setVisibility(View.VISIBLE);

            tvPrioritySkills.setText(formatList(priority, "⚡"));
            cardPriority.setVisibility(View.VISIBLE);

            btnNextRoadmap.setVisibility(View.VISIBLE);

            saveToFirestore(json);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not parse response. Try again.", Toast.LENGTH_LONG).show();
        }
    }

    private String formatList(List<String> items, String bullet) {
        if (items == null || items.isEmpty()) return "None detected";
        StringBuilder sb = new StringBuilder();
        for (String item : items) sb.append(bullet).append(" ").append(item).append("\n\n");
        return sb.toString().trim();
    }

    private void saveToFirestore(String json) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("skillGapJson", json);
        data.put("career", careerViewModel.getCurrentTargetCareer());
        data.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS).document(uid)
                .collection("skillGaps").add(data);
    }

    private void setLoading(boolean loading) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnAnalyzeGap.setEnabled(!loading);
        if (loading) {
            cardExisting.setVisibility(View.GONE);
            cardMissing.setVisibility(View.GONE);
            cardPriority.setVisibility(View.GONE);
            btnNextRoadmap.setVisibility(View.GONE);
        }
    }
}