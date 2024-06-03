package com.traveler.traveljournal.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.traveler.traveljournal.Constants;
import com.traveler.traveljournal.R;
import com.traveler.traveljournal.model.User;

import java.util.Locale;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();
        initSharedPreferences();
        new Handler().postDelayed(() -> {
            FirebaseUser firebaseUser = mAuth.getCurrentUser();
            if (firebaseUser != null) {
                firestore.collection(Constants.USER_COLLECTION).document(firebaseUser.getUid()).get()
                        .addOnCompleteListener(task -> {
                            Intent intent;
                            if (task.isSuccessful()) {
                                User user = task.getResult().toObject(User.class);
                                intent = new Intent(SplashActivity.this, MainActivity.class);
                                intent.putExtra("user", user);
                            } else {
                                intent = new Intent(SplashActivity.this, LoginActivity.class);
                            }
                            startActivity(intent);
                            finish();
                        });
            } else {
                Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }


        }, 3000);

    }

    private void initSharedPreferences() {
        sp = getSharedPreferences(Constants.SHARED_PREFERENCES_FILE, Context.MODE_PRIVATE);
        editor = sp.edit();
        String currentLanguage = sp.getString("language","en");
        setLanguage(currentLanguage);

    }
    private void setLanguage(String languageCode) {
        Locale locale = new Locale(languageCode);
        Locale.setDefault(locale);
        Resources resources = getResources();
        Configuration configuration = resources.getConfiguration();
        configuration.setLocale(locale);
        resources.updateConfiguration(configuration, resources.getDisplayMetrics());
    }

}