package com.example.minipay;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class RegisterActivity extends AppCompatActivity {

    TextInputEditText etName, etEmail, etPassword;
    MaterialButton btnRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etName = findViewById(R.id.regName);
        etEmail = findViewById(R.id.regEmail);
        etPassword = findViewById(R.id.regPassword);
        btnRegister = findViewById(R.id.btnDoRegister);

        btnRegister.setOnClickListener(v -> attemptRegistration());

        findViewById(R.id.tvBackToLogin).setOnClickListener(v -> finish());
    }

    private void attemptRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String pass = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            etName.setError("Name is required");
            return;
        }
        if (TextUtils.isEmpty(email) || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Valid email is required");
            return;
        }
        if (TextUtils.isEmpty(pass) || pass.length() < 6) {
            etPassword.setError("Password must be at least 6 characters");
            return;
        }

        // Mock registration - in a real app, you'd save this to a database
        Toast.makeText(this, "Registration successful!", Toast.LENGTH_SHORT).show();
        
        // Go to login screen
        Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(intent);
        finish();
    }
}
