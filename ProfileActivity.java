package com.example.expensify;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

public class ProfileActivity extends AppCompatActivity {

    private TextInputEditText editTextName, editTextEmail;
    private Button buttonSaveProfile;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Profile");
        }

        // Initialize views
        editTextName = findViewById(R.id.editTextName);
        editTextEmail = findViewById(R.id.editTextEmail);
        buttonSaveProfile = findViewById(R.id.buttonSaveProfile);

        // Load user data
        loadUserData();

        // Set up save button
        buttonSaveProfile.setOnClickListener(v -> saveUserProfile());
    }

    private void loadUserData() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String displayName = user.getDisplayName();
            String email = user.getEmail();

            if (displayName != null && !displayName.isEmpty()) {
                editTextName.setText(displayName);
            }

            if (email != null && !email.isEmpty()) {
                editTextEmail.setText(email);
                // Email can't be changed in Firebase without reauthentication
                editTextEmail.setEnabled(false);
            }
        }
    }

    private void saveUserProfile() {
        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            String newName = editTextName.getText().toString().trim();

            if (newName.isEmpty()) {
                Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                    .setDisplayName(newName)
                    .build();

            user.updateProfile(profileUpdates)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(ProfileActivity.this,
                                    "Profile updated successfully", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ProfileActivity.this,
                                    "Failed to update profile", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}