package com.traveler.traveljournal.ui;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.traveler.traveljournal.Constants;
import com.traveler.traveljournal.Validation;
import com.traveler.traveljournal.ValidationResponse;
import com.traveler.traveljournal.databinding.ActivityRegisterBinding;
import com.traveler.traveljournal.model.User;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;

    private FirebaseAuth mAuth;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        firestore = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        binding.registerBtn.setOnClickListener(view -> {
            String username = Objects.requireNonNull(binding.registerUsernameEdt.getText()).toString();
            String email = Objects.requireNonNull(binding.registerEmailEdt.getText()).toString();
            String password = Objects.requireNonNull(binding.registerPasswordEdt.getText()).toString();
            createUserAccount(username, email, password);
        });

        binding.registerHaveAccountBtn.setOnClickListener(view -> {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

    }

    private void createUserAccount(String username, String email, String password) {
        Validation validation = new Validation();
        ValidationResponse usernameResponse = validation.validateUsername(username);
        ValidationResponse emailResponse = validation.validateEmail(email);
        ValidationResponse passwordResponse = validation.validatePassword(password);

        if (!usernameResponse.getValid() || !emailResponse.getValid() || !passwordResponse.getValid()) {
            binding.registerUsernameLayout.setError(usernameResponse.getMessage());
            binding.registerEmailLayout.setError(emailResponse.getMessage());
            binding.registerPasswordLayout.setError(passwordResponse.getMessage());
            return;
        }

        binding.registerBtn.setVisibility(View.INVISIBLE);
        binding.registerProgressBar.setVisibility(View.VISIBLE);
        registerFirebaseUser(username, email, password);


    }

    private void registerFirebaseUser(String username, String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    // Registered successfully
                    FirebaseUser firebaseUser = authResult.getUser();
                    if (firebaseUser == null) return;
                    User newUser = new User(firebaseUser.getUid(), username, email);
                    DocumentReference document = firestore.collection(Constants.USER_COLLECTION).document(firebaseUser.getUid());

                    document.set(newUser)
                            .addOnCompleteListener(task -> {
                                binding.registerBtn.setVisibility(View.VISIBLE);
                                binding.registerProgressBar.setVisibility(View.GONE);

                                if (task.isSuccessful()) {
                                    // User information saved
                                    Intent intent = new Intent(this, MainActivity.class);
                                    intent.putExtra("user", newUser);
                                    startActivity(intent);
                                    finish();
                                } else {
                                    // Something went wrong!
                                    Toast.makeText(this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            });

                })
                .addOnFailureListener(e -> {
                    // Registration failed
                    binding.registerBtn.setVisibility(View.VISIBLE);
                    binding.registerProgressBar.setVisibility(View.GONE);

                    Toast.makeText(this, e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


}


















