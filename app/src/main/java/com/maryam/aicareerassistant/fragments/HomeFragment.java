package com.maryam.aicareerassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

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

/**
 * Home Dashboard — entry point after login. Shows navigation cards
 * for every major feature of the app.
 */
public class HomeFragment extends Fragment {

    private AuthViewModel authViewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
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

        List<DashboardItem> items = buildDashboardItems();

        DashboardAdapter adapter = new DashboardAdapter(items, this::onCardClicked);
        rvDashboard.setAdapter(adapter);
    }

    private List<DashboardItem> buildDashboardItems() {
        List<DashboardItem> items = new ArrayList<>();

        items.add(new DashboardItem(
                "resume_analysis",
                getString(R.string.dashboard_card_resume_title),
                getString(R.string.dashboard_card_resume_subtitle),
                R.drawable.ic_resume,
                R.color.card_indigo));

        items.add(new DashboardItem(
                "skill_gap",
                getString(R.string.dashboard_card_skillgap_title),
                getString(R.string.dashboard_card_skillgap_subtitle),
                R.drawable.ic_skill_gap,
                R.color.accent));

        items.add(new DashboardItem(
                "roadmap",
                getString(R.string.dashboard_card_roadmap_title),
                getString(R.string.dashboard_card_roadmap_subtitle),
                R.drawable.ic_roadmap,
                R.color.card_purple));

        items.add(new DashboardItem(
                "interview",
                getString(R.string.dashboard_card_interview_title),
                getString(R.string.dashboard_card_interview_subtitle),
                R.drawable.ic_interview,
                R.color.card_orange));

        items.add(new DashboardItem(
                "history",
                getString(R.string.dashboard_card_history_title),
                getString(R.string.dashboard_card_history_subtitle),
                R.drawable.ic_history,
                R.color.card_blue));

        items.add(new DashboardItem(
                "profile",
                getString(R.string.dashboard_card_profile_title),
                getString(R.string.dashboard_card_profile_subtitle),
                R.drawable.ic_profile,
                R.color.success));

        return items;
    }

    private void onCardClicked(DashboardItem item) {
        // Each module below is built in a later phase. For now we show a
        // friendly "coming soon" message instead of crashing on a missing
        // navigation destination. As each phase is completed, replace the
        // Toast with Navigation.findNavController(...).navigate(R.id...).
        switch (item.getKey()) {
            case "resume_analysis":
            case "skill_gap":
            case "roadmap":
            case "interview":
            case "history":
            case "profile":
                Toast.makeText(getContext(), R.string.coming_soon, Toast.LENGTH_SHORT).show();
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