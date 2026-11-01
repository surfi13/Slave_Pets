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

        // Исправление ошибки 1:
        if (com.google.firebase.auth.FirebaseAuth.getInstance().getCurrentUser() != null) {
            goToMain();
            return;
        }

        initViews();
        setupListeners();
        observeViewModel();
    }

    private void setupListeners() {
        btnAction.setOnClickListener(v -> {
            String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
            String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";

            // Исправление ошибки 2: передаем 3 аргумента
            String name = "Пользователь";
            viewModel.authenticate(name, email, password);
        });

        tvToggleMode.setOnClickListener(v -> viewModel.toggleMode());
    }

    private void initViews() {
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnAction = findViewById(R.id.btnLoginRegister);
        tvToggleMode = findViewById(R.id.tvToggleAuthMode);
        progressBar = findViewById(R.id.progressBar);
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
                .setMessage("Тема: Разработка мобильного приложения для учёта домашних питомцев с напоминаниями о вакцинации\n\n" +
                        "Выполнил: Сало Артём Ришатович\n" +
                        "Группа: 3пк1\n" +
                        "Руководитель: Егурнова Елена Николаевна\n\n" +
                        "ГАПОУ ОКЭИ, 2026 г.")
                .setPositiveButton("Закрыть", (dialog, which) -> dialog.dismiss())
                .setIcon(android.R.drawable.ic_dialog_info)
                .show();
    }

    private void goToMain() {
        Intent intent = new Intent(AuthActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }
}
