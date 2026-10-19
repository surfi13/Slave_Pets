package com.example.curs;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;


import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class AuthActivity extends AppCompatActivity {

    private AuthViewModel viewModel;
    private TextInputEditText etEmail, etPassword;
    private MaterialButton btnAction;
    private TextView tvToggleMode;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        viewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        if (viewModel.isUserLoggedIn()) {
            goToMain();
            return;
        }

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnAction = findViewById(R.id.btnLoginRegister);
        tvToggleMode = findViewById(R.id.tvToggleAuthMode);
        progressBar = findViewById(R.id.progressBar);
    }

    private void setupListeners() {
        btnAction.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
            viewModel.authenticate(email, password);
        });

        tvToggleMode.setOnClickListener(v -> viewModel.toggleMode());
    }

    private void observeViewModel() {
        viewModel.getIsRegisterMode().observe(this, isRegister -> {
            if (isRegister) {
                btnAction.setText("Зарегистрироваться");
                tvToggleMode.setText("Уже есть аккаунт? Войти");
            } else {
                btnAction.setText("Войти");
                tvToggleMode.setText("Нет аккаунта? Зарегистрироваться");
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
            btnAction.setEnabled(!isLoading);
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) {
                Toast.makeText(this, error, Toast.LENGTH_LONG).show();
            }
        });

        viewModel.getIsAuthSuccessful().observe(this, isSuccess -> {
            if (Boolean.TRUE.equals(isSuccess)) {
                goToMain();
            }
        });
    }

    private void goToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
