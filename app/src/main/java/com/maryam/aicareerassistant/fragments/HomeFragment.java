package com.maryam.aicareerassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseUser;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.adapter.DashboardAdapter;
import com.maryam.aicareerassistant.model.DashboardItem;
import com.maryam.aicareerassistant.viewmodel.AuthViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setupHeader(view);
        setupDashboardGrid(view);
        setupLogout(view);
    }

    private void setupHeader(View view) {
        TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
        FirebaseUser user = authViewModel.getCurrentUserOrNull();
        if (user != null && user.getEmail() != null) {
            tvUserEmail.setText(user.getEmail());
        }
    }

    private void setupDashboardGrid(View view) {
        RecyclerView rvDashboard = view.findViewById(R.id.rvDashboard);
        rvDashboard.setLayoutManager(new GridLayoutManager(getContext(), 2));
        List<DashboardItem> items = new ArrayList<>();
        items.add(new DashboardItem("resume_analysis",
                getString(R.string.dashboard_card_resume_title),
                getString(R.string.dashboard_card_resume_subtitle),
                R.drawable.ic_resume, R.color.card_indigo));
        items.add(new DashboardItem("skill_gap",
                getString(R.string.dashboard_card_skillgap_title),
                getString(R.string.dashboard_card_skillgap_subtitle),
                R.drawable.ic_skill_gap, R.color.accent));
        items.add(new DashboardItem("roadmap",
                getString(R.string.dashboard_card_roadmap_title),
                getString(R.string.dashboard_card_roadmap_subtitle),
                R.drawable.ic_roadmap, R.color.card_purple));
        items.add(new DashboardItem("interview",
                getString(R.string.dashboard_card_interview_title),
                getString(R.string.dashboard_card_interview_subtitle),
                R.drawable.ic_interview, R.color.card_orange));
        items.add(new DashboardItem("history",
                getString(R.string.dashboard_card_history_title),
                getString(R.string.dashboard_card_history_subtitle),
                R.drawable.ic_history, R.color.card_blue));
        items.add(new DashboardItem("profile",
                getString(R.string.dashboard_card_profile_title),
                getString(R.string.dashboard_card_profile_subtitle),
                R.drawable.ic_profile, R.color.success));

        rvDashboard.setAdapter(new DashboardAdapter(items,
                item -> onCardClicked(view, item)));
    }

    private void onCardClicked(View view, DashboardItem item) {
        switch (item.getKey()) {
            case "resume_analysis":
                Navigation.findNavController(view)
                        .navigate(R.id.action_home_to_resumeUpload);
                break;
            case "skill_gap":
                Navigation.findNavController(view)
                        .navigate(R.id.action_home_to_careerSelection);
                break;
            case "roadmap":
                Navigation.findNavController(view)
                        .navigate(R.id.action_home_to_roadmap);
                break;
            case "interview":
                Navigation.findNavController(view)
                        .navigate(R.id.action_home_to_interview);
                break;
            case "history":
                Navigation.findNavController(view)
                        .navigate(R.id.action_home_to_history);
                break;
            case "profile":
                Navigation.findNavController(view)
                        .navigate(R.id.action_home_to_profile);
                break;
        }
    }

    private void setupLogout(View view) {
        ImageButton btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            authViewModel.logout();
            Navigation.findNavController(view).navigate(R.id.action_home_to_login);
        });
    }
}