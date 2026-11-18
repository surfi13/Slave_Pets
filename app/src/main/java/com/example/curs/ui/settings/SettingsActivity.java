package com.example.curs.ui.settings;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import com.example.curs.R;
import com.example.curs.ui.BaseActivity;
import com.example.curs.ui.auth.AuthActivity;

public class SettingsActivity extends BaseActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        Toolbar toolbar = findViewById(R.id.toolbarSettings);
        setSupportActionBar(toolbar);
        toolbar.setNavigationOnClickListener(v -> finish());

        prefs = getSharedPreferences("Settings", MODE_PRIVATE);
        RadioGroup rgLang = findViewById(R.id.rgLanguage);
        RadioGroup rgTheme = findViewById(R.id.rgTheme);

        // --- ЯЗЫК ---
        String currentLang = prefs.getString("language", "ru");
        if (currentLang.equals("en")) {
            rgLang.check(R.id.rbEnglish);
        } else if (currentLang.equals("uk")) {
            rgLang.check(R.id.rbUkrainian);
        } else {
            rgLang.check(R.id.rbRussian);
        }

        rgLang.setOnCheckedChangeListener((group, checkedId) -> {
            String newLang = "ru";
            if (checkedId == R.id.rbEnglish) newLang = "en";
            else if (checkedId == R.id.rbUkrainian) newLang = "uk";

            prefs.edit().putString("language", newLang).apply();
            restartApp(); // Перезапуск, чтобы язык применился
        });

        // --- ТЕМА ---
        int currentTheme = prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) rgTheme.check(R.id.rbDark);
        else if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) rgTheme.check(R.id.rbLight);
        else rgTheme.check(R.id.rbSystem);

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbDark) mode = AppCompatDelegate.MODE_NIGHT_YES;
            else if (checkedId == R.id.rbLight) mode = AppCompatDelegate.MODE_NIGHT_NO;
            else mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            prefs.edit().putInt("theme", mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode); // Мгновенно применяем тему
        });
    }

    private void restartApp() {
        Intent intent = new Intent(this, AuthActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        overridePendingTransition(0, 0);
    }
}