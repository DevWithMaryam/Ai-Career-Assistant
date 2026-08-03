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

public class InterviewFragment extends Fragment {

    private CareerViewModel careerViewModel;
    private Button btnGenerateInterview, btnGoHome;
    private LinearLayout layoutLoading;
    private MaterialCardView cardTechnical, cardHr, cardResumeBased;
    private TextView tvInterviewCareer, tvTechnical, tvHr, tvResumeBased;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_interview, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        careerViewModel = new ViewModelProvider(requireActivity()).get(CareerViewModel.class);

        tvInterviewCareer = view.findViewById(R.id.tvInterviewCareer);
        btnGenerateInterview = view.findViewById(R.id.btnGenerateInterview);
        layoutLoading = view.findViewById(R.id.layoutLoading);
        cardTechnical = view.findViewById(R.id.cardTechnical);
        cardHr = view.findViewById(R.id.cardHr);
        cardResumeBased = view.findViewById(R.id.cardResumeBased);
        tvTechnical = view.findViewById(R.id.tvTechnical);
        tvHr = view.findViewById(R.id.tvHr);
        tvResumeBased = view.findViewById(R.id.tvResumeBased);
        btnGoHome = view.findViewById(R.id.btnGoHome);

        String career = careerViewModel.getCurrentTargetCareer();
        if (career != null) tvInterviewCareer.setText("Target: " + career);

        btnGenerateInterview.setOnClickListener(v -> careerViewModel.getInterviewQuestions());

        btnGoHome.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_interview_to_home));

        careerViewModel.getInterviewResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true); break;
                case SUCCESS:
                    setLoading(false);
                    displayQuestions(resource.data);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void displayQuestions(String json) {
        try {
            List<String> technical = JsonParser.parseStringList(json, "technical_questions");
            List<String> hr = JsonParser.parseStringList(json, "hr_questions");
            List<String> resumeBased = JsonParser.parseStringList(json, "resume_based_questions");

            tvTechnical.setText(formatNumbered(technical));
            cardTechnical.setVisibility(View.VISIBLE);

            tvHr.setText(formatNumbered(hr));
            cardHr.setVisibility(View.VISIBLE);

            tvResumeBased.setText(formatNumbered(resumeBased));
            cardResumeBased.setVisibility(View.VISIBLE);

            btnGoHome.setVisibility(View.VISIBLE);
            saveToFirestore(json);
        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not parse questions. Try again.", Toast.LENGTH_LONG).show();
        }
    }

    private String formatNumbered(List<String> items) {
        if (items == null || items.isEmpty()) return "None generated";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < items.size(); i++) {
            sb.append(i + 1).append(". ").append(items.get(i)).append("\n\n");
        }
        return sb.toString().trim();
    }

    private void saveToFirestore(String json) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("interviewJson", json);
        data.put("career", careerViewModel.getCurrentTargetCareer());
        data.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS).document(uid)
                .collection(Constants.COLLECTION_INTERVIEWS).add(data);
    }

    private void setLoading(boolean loading) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerateInterview.setEnabled(!loading);
        if (loading) {
            cardTechnical.setVisibility(View.GONE);
            cardHr.setVisibility(View.GONE);
            cardResumeBased.setVisibility(View.GONE);
            btnGoHome.setVisibility(View.GONE);
        }
    }
}