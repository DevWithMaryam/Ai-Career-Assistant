package com.maryam.aicareerassistant.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.maryam.aicareerassistant.R;
import com.maryam.aicareerassistant.utils.Constants;
import com.maryam.aicareerassistant.viewmodel.CareerViewModel;

public class ProfileFragment extends Fragment {

    private CareerViewModel careerViewModel;
    private TextInputEditText etName;
    private TextView tvProfileName, tvProfileEmail, tvCurrentCareer;
    private Button btnUpdateProfile, btnChangeCareer, btnUploadNewResume;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        careerViewModel = new ViewModelProvider(requireActivity()).get(CareerViewModel.class);

        tvProfileName = view.findViewById(R.id.tvProfileName);
        tvProfileEmail = view.findViewById(R.id.tvProfileEmail);
        tvCurrentCareer = view.findViewById(R.id.tvCurrentCareer);
        etName = view.findViewById(R.id.etName);
        btnUpdateProfile = view.findViewById(R.id.btnUpdateProfile);
        btnChangeCareer = view.findViewById(R.id.btnChangeCareer);
        btnUploadNewResume = view.findViewById(R.id.btnUploadNewResume);

        loadUserData();

        btnUpdateProfile.setOnClickListener(v -> updateName());

        btnChangeCareer.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_profile_to_careerSelection));

        btnUploadNewResume.setOnClickListener(v ->
                Navigation.findNavController(view)
                        .navigate(R.id.action_profile_to_resumeUpload));
    }

    private void loadUserData() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        tvProfileEmail.setText(user.getEmail());

        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .get()
                .addOnSuccessListener(snapshot -> {
                    String name = snapshot.getString("name");
                    String career = snapshot.getString("targetCareer");

                    if (name != null) {
                        tvProfileName.setText(name);
                        etName.setText(name);
                    }

                    if (career != null && !career.isEmpty()) {
                        tvCurrentCareer.setText(career);
                    } else {
                        tvCurrentCareer.setText(getString(R.string.not_set));
                    }
                });
    }

    private void updateName() {
        String name = etName.getText() != null ? etName.getText().toString().trim() : "";
        if (name.isEmpty()) {
            Toast.makeText(getContext(), "Name cannot be empty.", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) return;

        FirebaseFirestore.getInstance()
                .collection(Constants.COLLECTION_USERS)
                .document(user.getUid())
                .update("name", name)
                .addOnSuccessListener(unused -> {
                    tvProfileName.setText(name);
                    Toast.makeText(getContext(),
                            getString(R.string.profile_updated), Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(),
                                "Update failed: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }
}