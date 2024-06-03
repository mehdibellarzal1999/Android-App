package com.traveler.traveljournal.ui;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;

import com.google.android.material.chip.ChipGroup;
import com.traveler.traveljournal.Constants;
import com.traveler.traveljournal.R;
import com.traveler.traveljournal.databinding.ActivitySettingsBinding;

import java.util.List;
import java.util.Locale;

public class SettingsActivity extends AppCompatActivity {

    private ActivitySettingsBinding binding;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        initSharedPreferences();
        binding.settingsBackBtn.setOnClickListener(view -> finish());

        binding.settingsChipsGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.settings_french_chip) {
                setLanguage("fr");
                editor.putString("language", "fr");

            } else {
                setLanguage("en");
                editor.putString("language", "en");
            }
            editor.apply();
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
            finish();

        });
    }
    private void setLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);

        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }
    private void initSharedPreferences() {
        sp = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        editor = sp.edit();
        String currentLanguage = sp.getString("language","en");
        if (currentLanguage.equals("fr")) {
            binding.settingsFrenchChip.setChecked(true);
        } else  {
            binding.settingsEnglishChip.setChecked(true);
        }
        setLanguage(currentLanguage);

    }


}