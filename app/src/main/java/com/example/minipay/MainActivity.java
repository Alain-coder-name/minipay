package com.example.minipay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity {

    TextInputEditText etEmail, etPassword;
    MaterialButton loginBtn, registerBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences prefs = getSharedPreferences("minipay_prefs", MODE_PRIVATE);
        if (prefs.getBoolean("is_logged_in", false)) {
            String savedEmail = prefs.getString("user_email", "");
            Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
            intent.putExtra("user_email", savedEmail);
            startActivity(intent);
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        etEmail    = findViewById(R.id.username);
        etPassword = findViewById(R.id.password);
        loginBtn   = findViewById(R.id.loginBtn);
        registerBtn = findViewById(R.id.registerBtn);

        loginBtn.setOnClickListener(v -> attemptLogin());

        // Launch Registration Page
        registerBtn.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Forgot password link
        findViewById(R.id.forgotPassword).setOnClickListener(v ->
                Toast.makeText(this, "Password reset link sent!", Toast.LENGTH_SHORT).show());
    }

    private void attemptLogin() {
        String email = etEmail.getText().toString().trim();
        String pass  = etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            etEmail.setError("Please enter your email");
            etEmail.requestFocus();
            return;
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            etEmail.setError("Enter a valid email");
            etEmail.requestFocus();
            return;
        }
        if (TextUtils.isEmpty(pass)) {
            etPassword.setError("Please enter your password");
            etPassword.requestFocus();
            return;
        }
        if (pass.length() < 4) {
            etPassword.setError("Password too short");
            etPassword.requestFocus();
            return;
        }

        // ✅ Login success — pass user name to dashboard
        SharedPreferences prefs = getSharedPreferences("minipay_prefs", MODE_PRIVATE);
        prefs.edit().putBoolean("is_logged_in", true).putString("user_email", email).apply();

        Toast.makeText(this, "Login Successful! Welcome back.", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(MainActivity.this, DashboardActivity.class);
        intent.putExtra("user_email", email);
        startActivity(intent);
        finish(); // remove login from back stack
    }
}
