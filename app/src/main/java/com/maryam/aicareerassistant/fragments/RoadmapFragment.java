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
import com.maryam.aicareerassistant.viewmodel.CareerViewModel;

import org.json.JSONArray;
import org.json.JSONObject;

public class RoadmapFragment extends Fragment {

    private CareerViewModel careerViewModel;
    private Button btnGenerateRoadmap, btnNextInterview;
    private LinearLayout layoutLoading, containerWeeks;
    private TextView tvRoadmapCareer;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_roadmap, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        careerViewModel = new ViewModelProvider(requireActivity()).get(CareerViewModel.class);

        tvRoadmapCareer = view.findViewById(R.id.tvRoadmapCareer);
        btnGenerateRoadmap = view.findViewById(R.id.btnGenerateRoadmap);
        layoutLoading = view.findViewById(R.id.layoutLoading);
        containerWeeks = view.findViewById(R.id.containerWeeks);
        btnNextInterview = view.findViewById(R.id.btnNextInterview);

        String career = careerViewModel.getCurrentTargetCareer();
        if (career != null) tvRoadmapCareer.setText("Target: " + career);

        btnGenerateRoadmap.setOnClickListener(v -> careerViewModel.getRoadmap());

        btnNextInterview.setOnClickListener(v ->
                Navigation.findNavController(view).navigate(R.id.action_roadmap_to_interview));

        careerViewModel.getRoadmapResult().observe(getViewLifecycleOwner(), resource -> {
            if (resource == null) return;
            switch (resource.status) {
                case LOADING:
                    setLoading(true); break;
                case SUCCESS:
                    setLoading(false);
                    displayRoadmap(resource.data);
                    break;
                case ERROR:
                    setLoading(false);
                    Toast.makeText(getContext(), resource.message, Toast.LENGTH_LONG).show();
                    break;
            }
        });
    }

    private void displayRoadmap(String json) {
        try {
            String clean = json.replaceAll("```json", "").replaceAll("```", "").trim();
            JSONObject obj = new JSONObject(clean);
            JSONArray weeks = obj.getJSONArray("weeks");

            containerWeeks.removeAllViews();
            int[] weekColors = {
                    R.color.card_indigo, R.color.accent, R.color.card_purple, R.color.card_orange
            };

            for (int i = 0; i < weeks.length(); i++) {
                JSONObject week = weeks.getJSONObject(i);
                int weekNum = week.optInt("week", i + 1);
                String focus = week.optString("focus", "");
                JSONArray tasks = week.optJSONArray("tasks");
                JSONArray resources = week.optJSONArray("resources");

                addWeekCard(weekNum, focus, tasks, resources,
                        weekColors[i % weekColors.length]);
            }

            containerWeeks.setVisibility(View.VISIBLE);
            btnNextInterview.setVisibility(View.VISIBLE);
            saveToFirestore(json);

        } catch (Exception e) {
            Toast.makeText(getContext(), "Could not parse roadmap. Try again.", Toast.LENGTH_LONG).show();
        }
    }

    private void addWeekCard(int weekNum, String focus, JSONArray tasks,
                             JSONArray resources, int colorRes) {
        if (!isAdded() || getContext() == null) return;

        MaterialCardView card = new MaterialCardView(requireContext());
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        cardParams.setMargins(0, 0, 0, 32);
        card.setLayoutParams(cardParams);
        card.setRadius(40f);
        card.setCardElevation(6f);
        card.setCardBackgroundColor(requireContext().getColor(R.color.surface));

        LinearLayout inner = new LinearLayout(requireContext());
        inner.setOrientation(LinearLayout.VERTICAL);
        inner.setPadding(48, 40, 48, 40);

        // Week header
        TextView tvWeek = new TextView(requireContext());
        tvWeek.setText("Week " + weekNum);
        tvWeek.setTextSize(18f);
        tvWeek.setTextColor(requireContext().getColor(colorRes));
        tvWeek.setTypeface(null, android.graphics.Typeface.BOLD);
        inner.addView(tvWeek);

        // Focus
        TextView tvFocus = new TextView(requireContext());
        tvFocus.setText(focus);
        tvFocus.setTextSize(14f);
        tvFocus.setTextColor(requireContext().getColor(R.color.text_primary));
        tvFocus.setPadding(0, 8, 0, 16);
        inner.addView(tvFocus);

        // Tasks
        if (tasks != null && tasks.length() > 0) {
            TextView tvTasksHeader = new TextView(requireContext());
            tvTasksHeader.setText("📌 Tasks");
            tvTasksHeader.setTextSize(13f);
            tvTasksHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            tvTasksHeader.setTextColor(requireContext().getColor(R.color.text_secondary));
            inner.addView(tvTasksHeader);

            StringBuilder tasksSb = new StringBuilder();
            for (int j = 0; j < tasks.length(); j++) {
                tasksSb.append("• ").append(tasks.optString(j)).append("\n");
            }
            TextView tvTasks = new TextView(requireContext());
            tvTasks.setText(tasksSb.toString().trim());
            tvTasks.setTextSize(13f);
            tvTasks.setTextColor(requireContext().getColor(R.color.text_primary));
            tvTasks.setPadding(0, 4, 0, 12);
            inner.addView(tvTasks);
        }

        // Resources
        if (resources != null && resources.length() > 0) {
            TextView tvResHeader = new TextView(requireContext());
            tvResHeader.setText("📚 Resources");
            tvResHeader.setTextSize(13f);
            tvResHeader.setTypeface(null, android.graphics.Typeface.BOLD);
            tvResHeader.setTextColor(requireContext().getColor(R.color.text_secondary));
            inner.addView(tvResHeader);

            StringBuilder resSb = new StringBuilder();
            for (int j = 0; j < resources.length(); j++) {
                resSb.append("→ ").append(resources.optString(j)).append("\n");
            }
            TextView tvRes = new TextView(requireContext());
            tvRes.setText(resSb.toString().trim());
            tvRes.setTextSize(13f);
            tvRes.setTextColor(requireContext().getColor(R.color.text_primary));
            tvRes.setPadding(0, 4, 0, 0);
            inner.addView(tvRes);
        }

        card.addView(inner);
        containerWeeks.addView(card);
    }

    private void saveToFirestore(String json) {
        if (FirebaseAuth.getInstance().getCurrentUser() == null) return;
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        java.util.Map<String, Object> data = new java.util.HashMap<>();
        data.put("roadmapJson", json);
        data.put("career", careerViewModel.getCurrentTargetCareer());
        data.put("timestamp", System.currentTimeMillis());
        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS).document(uid)
                .collection(Constants.COLLECTION_ROADMAPS).add(data);
    }

    private void setLoading(boolean loading) {
        layoutLoading.setVisibility(loading ? View.VISIBLE : View.GONE);
        btnGenerateRoadmap.setEnabled(!loading);
        if (loading) {
            containerWeeks.setVisibility(View.GONE);
            btnNextInterview.setVisibility(View.GONE);
        }
    }
}