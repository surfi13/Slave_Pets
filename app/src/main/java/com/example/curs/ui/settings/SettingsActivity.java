package com.example.curs.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import com.example.curs.R;
import com.example.curs.utils.LocaleHelper;

public class SettingsActivity extends AppCompatActivity {

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefs = getSharedPreferences("Settings", Context.MODE_PRIVATE);
        setupLanguageSettings();
        setupThemeSettings();
    }

    private void setupLanguageSettings() {
        RadioGroup rgLanguage = findViewById(R.id.rgLanguage);
        RadioButton rbRussian = findViewById(R.id.rbRussian);
        RadioButton rbEnglish = findViewById(R.id.rbEnglish);

        String currentLang = prefs.getString("My_Lang", "ru");
        if (currentLang.equals("en")) rbEnglish.setChecked(true);
        else rbRussian.setChecked(true);

        rgLanguage.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbEnglish) {
                LocaleHelper.setLocale(this, "en");
                prefs.edit().putString("My_Lang", "en").apply();
                recreate();
            } else {
                LocaleHelper.setLocale(this, "ru");
                prefs.edit().putString("My_Lang", "ru").apply();
                recreate();
            }
        });
    }

    private void setupThemeSettings() {
        RadioGroup rgTheme = findViewById(R.id.rgTheme);
        RadioButton rbLight = findViewById(R.id.rbLight);
        RadioButton rbDark = findViewById(R.id.rbDark);
        RadioButton rbSystem = findViewById(R.id.rbSystem);

        int currentTheme = prefs.getInt("My_Theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (currentTheme == AppCompatDelegate.MODE_NIGHT_NO) rbLight.setChecked(true);
        else if (currentTheme == AppCompatDelegate.MODE_NIGHT_YES) rbDark.setChecked(true);
        else rbSystem.setChecked(true);

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbLight) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                prefs.edit().putInt("My_Theme", AppCompatDelegate.MODE_NIGHT_NO).apply();
            } else if (checkedId == R.id.rbDark) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                prefs.edit().putInt("My_Theme", AppCompatDelegate.MODE_NIGHT_YES).apply();
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                prefs.edit().putInt("My_Theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply();
            }
        });
    }
}