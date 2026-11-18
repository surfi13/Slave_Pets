package com.example.curs.ui.settings;

import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;

import com.example.curs.R;

import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

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

        String lang = prefs.getString("lang", "ru");
        rgLang.check(lang.equals("en") ? R.id.rbEnglish : R.id.rbRussian);

        int theme = prefs.getInt("theme", AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        if (theme == AppCompatDelegate.MODE_NIGHT_YES) rgTheme.check(R.id.rbDark);
        else if (theme == AppCompatDelegate.MODE_NIGHT_NO) rgTheme.check(R.id.rbLight);
        else rgTheme.check(R.id.rbSystem);

        rgLang.setOnCheckedChangeListener((group, checkedId) -> {
            String newLang = checkedId == R.id.rbEnglish ? "en" : "ru";
            prefs.edit().putString("lang", newLang).apply();
            setLocale(newLang);
            recreate();
        });

        rgTheme.setOnCheckedChangeListener((group, checkedId) -> {
            int mode;
            if (checkedId == R.id.rbDark) mode = AppCompatDelegate.MODE_NIGHT_YES;
            else if (checkedId == R.id.rbLight) mode = AppCompatDelegate.MODE_NIGHT_NO;
            else mode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

            prefs.edit().putInt("theme", mode).apply();
            AppCompatDelegate.setDefaultNightMode(mode);
        });
    }

    private void setLocale(String lang) {
        Locale locale = new Locale(lang);
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.setLocale(locale);
        getResources().updateConfiguration(config, getResources().getDisplayMetrics());
    }
}