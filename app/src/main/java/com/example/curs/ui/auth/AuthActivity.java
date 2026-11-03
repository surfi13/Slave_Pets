package com.example.curs.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.lifecycle.ViewModelProvider;

import com.example.curs.ui.main.MainActivity;
import com.example.curs.R;
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

        Toolbar toolbar = findViewById(R.id.toolbarAuth);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        // Проверяем, авторизован ли уже пользователь
        try {
            if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
                goToMain();
                return;
            }
        } catch (Exception e) {
            // Игнорируем ошибку, если нет сети
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
            String name = "Пользователь";
            viewModel.authenticate(name, email, password);
        });

        tvToggleMode.setOnClickListener(v -> viewModel.toggleMode());
    }

    private void observeViewModel() {
        viewModel.getIsRegisterMode().observe(this, isRegister -> {
            if (isRegister != null) {
                btnAction.setText(isRegister ? "Зарегистрироваться" : "Войти");
                tvToggleMode.setText(isRegister ? "Уже есть аккаунт? Войти" : "Нет аккаунта? Зарегистрироваться");
            }
        });

        viewModel.getIsLoading().observe(this, isLoading -> {
            if (isLoading != null) {
                progressBar.setVisibility(isLoading ? View.VISIBLE : View.GONE);
                btnAction.setEnabled(!isLoading);
            }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_auth, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.action_info) {
            showProjectInfoDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showProjectInfoDialog() {
        new AlertDialog.Builder(this)
                .setTitle("О курсовом проекте")
                .setMessage("Тема: Разработка мобильного приложения...\n\nВыполнил: Ваше Имя")
                .setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void goToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish(); // Закрываем экран авторизации
    }
}