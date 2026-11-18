package com.example.curs.ui.profile;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.curs.R;
import com.example.curs.ui.auth.AuthActivity;
import com.example.curs.ui.settings.SettingsActivity;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class ProfileFragment extends Fragment {

    private ProfileViewModel viewModel;
    private TextInputEditText etName, etEmail;
    private MaterialButton btnSave;
    private ProgressBar progressBar;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profile, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProfileViewModel.class);

        etName = view.findViewById(R.id.etName);
        etEmail = view.findViewById(R.id.etEmail);
        btnSave = view.findViewById(R.id.btnSaveProfile);
        progressBar = view.findViewById(R.id.progressBarProfile);

        btnSave.setOnClickListener(v -> {
            String name = etName.getText() != null ? etName.getText().toString().trim() : "";
            viewModel.saveProfile(name);
        });

        view.findViewById(R.id.btnChangePassword).setOnClickListener(v ->
                new ChangePasswordDialog().show(getParentFragmentManager(), "change_pw"));

        view.findViewById(R.id.btnSettings).setOnClickListener(v ->
                startActivity(new Intent(requireContext(), SettingsActivity.class)));

        view.findViewById(R.id.btnLogoutProfile).setOnClickListener(v -> {
            viewModel.logout();
            Intent intent = new Intent(requireContext(), AuthActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        observe();
    }

    private void observe() {
        viewModel.getUser().observe(getViewLifecycleOwner(), user -> {
            if (user != null) {
                etName.setText(user.name);
                etEmail.setText(user.email);
            }
        });
        viewModel.getIsLoading().observe(getViewLifecycleOwner(), loading ->
                progressBar.setVisibility(loading ? ProgressBar.VISIBLE : ProgressBar.GONE));
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), err -> {
            if (err != null) Toast.makeText(requireContext(), err, Toast.LENGTH_LONG).show();
        });
        viewModel.getIsSaved().observe(getViewLifecycleOwner(), saved -> {
            if (Boolean.TRUE.equals(saved))
                Toast.makeText(requireContext(), "Профиль сохранён", Toast.LENGTH_SHORT).show();
        });
    }
}