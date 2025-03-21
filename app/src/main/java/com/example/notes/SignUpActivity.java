package com.example.notes;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.HashMap;
import java.util.Map;

public class SignUpActivity extends AppCompatActivity {
    private EditText emailInput, passwordInput, confirmPasswordInput;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        mAuth = FirebaseAuth.getInstance();

        emailInput = findViewById(R.id.email_input);
        passwordInput = findViewById(R.id.password_input);
        confirmPasswordInput = findViewById(R.id.confirm_password_input);
        Button signUpButton = findViewById(R.id.sign_up_button);
        TextView signInLink = findViewById(R.id.sign_in_link);

        signUpButton.setOnClickListener(v -> signUp());
        signInLink.setOnClickListener(v -> {
            startActivity(new Intent(this, SignInActivity.class));
            finish();
        });
    }

    private void signUp() {
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseMessaging.getInstance().getToken()
                                .addOnCompleteListener(tokenTask -> {
                                    if (tokenTask.isSuccessful()) {
                                        String token = tokenTask.getResult();
                                        String userId = mAuth.getCurrentUser().getUid();

                                        Map<String, Object> userData = new HashMap<>();
                                        userData.put("email", email);
                                        userData.put("fcmToken", token);
                                        userData.put("createdAt", FieldValue.serverTimestamp());

                                        FirebaseFirestore.getInstance()
                                                .collection("users")
                                                .document(userId)
                                                .set(userData)
                                                .addOnSuccessListener(aVoid -> {
                                                    startActivity(new Intent(this, MainActivity.class));
                                                    finish();
                                                })
                                                .addOnFailureListener(
                                                        e -> Toast.makeText(this, "Error creating user profile",
                                                                Toast.LENGTH_SHORT).show());
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Sign up failed: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
