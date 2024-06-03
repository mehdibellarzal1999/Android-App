package com.traveler.traveljournal.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.traveler.traveljournal.Constants;
import com.traveler.traveljournal.Validation;
import com.traveler.traveljournal.ValidationResponse;
import com.traveler.traveljournal.databinding.ActivityLoginBinding;
import com.traveler.traveljournal.model.User;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        firestore = FirebaseFirestore.getInstance();

        binding.loginBtn.setOnClickListener(view -> {
            String email = Objects.requireNonNull(binding.loginEmailEdt.getText()).toString();
            String password = Objects.requireNonNull(binding.loginPasswordEdt.getText()).toString();

            signInUser(email, password);
        });

        binding.loginNoAccountBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
            finish();
        });
    }

    private void signInUser(String email, String password) {
        Validation validation = new Validation();
        ValidationResponse emailResponse = validation.validateEmail(email);
        ValidationResponse passwordResponse = validation.validatePassword(password);

        if (!emailResponse.getValid() || !passwordResponse.getValid()) {
            binding.loginEmailLayout.setError(emailResponse.getMessage());
            binding.loginPasswordLayout.setError(passwordResponse.getMessage());
            return;
        }

        binding.loginBtn.setVisibility(View.INVISIBLE);
        binding.loginProgressBar.setVisibility(View.VISIBLE);
        signInFirebaseUser(email, password);
    }

    private void signInFirebaseUser(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    binding.loginBtn.setVisibility(View.VISIBLE);
                    binding.loginProgressBar.setVisibility(View.GONE);

                    if (task.isSuccessful()) {
                        // Signed in successfully
                        binding.loginBtn.setVisibility(View.VISIBLE);
                        binding.loginProgressBar.setVisibility(View.GONE);
                        FirebaseUser firebaseUser = task.getResult().getUser();
                        if (firebaseUser == null) return;
                        firestore.collection(Constants.USER_COLLECTION)
                                .document(firebaseUser.getUid())
                                .get()
                                .addOnSuccessListener(documentSnapshot -> {
                                    User user = documentSnapshot.toObject(User.class);
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.putExtra("user", user);
                                    startActivity(intent);
                                    finish();
                                })
                                .addOnFailureListener(e -> {
                                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                    } else {
                        // Signing failed!
                        Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }



















}