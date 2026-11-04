package com.example.minipay;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.materialswitch.MaterialSwitch;

public class ProfileActivity extends AppCompatActivity {

    private static final String PREFS_NAME  = "minipay_prefs";
    private static final String KEY_DARK    = "dark_mode";

    TextView       tvName, tvEmail, tvAvatar, tvSentCount, tvBalance;
    MaterialSwitch darkModeSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvName        = findViewById(R.id.profileName);
        tvEmail       = findViewById(R.id.profileEmail);
        tvAvatar      = findViewById(R.id.profileAvatar);
        tvSentCount   = findViewById(R.id.sentCount);
        tvBalance     = findViewById(R.id.balanceStat);
        darkModeSwitch = findViewById(R.id.darkModeSwitch);

        loadProfile();
        initDarkModeSwitch();

        // Settings options
        findViewById(R.id.editProfileOption).setOnClickListener(v ->
                showEditProfileDialog());

        findViewById(R.id.securityOption).setOnClickListener(v -> showSecurityDialog());

        findViewById(R.id.notifOption).setOnClickListener(v -> showNotificationsDialog());

        // Back button
        android.view.View backBtn = findViewById(R.id.backBtn);
        if (backBtn != null) {
            backBtn.setOnClickListener(v -> finish());
        }

        // Logout — clear back stack back to Login
        findViewById(R.id.logoutBtn).setOnClickListener(v -> {
            SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
            prefs.edit().putBoolean("is_logged_in", false).remove("user_email").apply();

            Intent intent = new Intent(this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadProfile();
    }

    // ── Profile Data ──────────────────────────────────────────────
    private void loadProfile() {
        String name  = DashboardActivity.userName;
        // UPI ID uses the same rule as the receive dialog in Dashboard
        String upiId = name.toLowerCase().replaceAll("\\s+", ".") + "@minipay";

        // Build initials (1st letter if single word, else first letters of first+last)
        String[] parts   = name.trim().split(" ");
        String initials  = parts.length >= 2
                ? ("" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)).toUpperCase()
                : name.substring(0, Math.min(2, name.length())).toUpperCase();

        tvAvatar.setText(initials);
        tvName.setText(name);
        tvEmail.setText(upiId);   // show UPI ID as the sub-heading in the header

        // Stats
        long sentCount = DashboardActivity.transactions.stream()
                .filter(Transaction::isSent).count();
        tvSentCount.setText(String.valueOf(sentCount));
        tvBalance.setText("₹" + String.format("%,.0f", DashboardActivity.balance));
    }

    // ── Dark Mode ─────────────────────────────────────────────────
    private void initDarkModeSwitch() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean isDark = prefs.getBoolean(KEY_DARK, false);

        // Reflect current state without triggering the listener
        darkModeSwitch.setChecked(isDark);

        darkModeSwitch.setOnCheckedChangeListener((btn, isChecked) -> {
            // Persist preference
            prefs.edit().putBoolean(KEY_DARK, isChecked).apply();

            // Apply mode — recreate restores the activity correctly
            AppCompatDelegate.setDefaultNightMode(
                    isChecked
                            ? AppCompatDelegate.MODE_NIGHT_YES
                            : AppCompatDelegate.MODE_NIGHT_NO);
        });
    }

    // ── Edit Profile Dialog ───────────────────────────────────────
    private void showEditProfileDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter your name");
        input.setText(DashboardActivity.userName);
        input.setPadding(48, 24, 48, 8);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Edit Display Name")
                .setView(input)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        DashboardActivity.userName = newName;
                        loadProfile();
                        Toast.makeText(this, "Name updated!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
    // ── Security Settings Dialog ──────────────────────────────────
    private void showSecurityDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean pinEnabled = prefs.getBoolean("pin_enabled", false);
        boolean bioEnabled = prefs.getBoolean("bio_enabled", false);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 32, 64, 16);

        com.google.android.material.materialswitch.MaterialSwitch pinSwitch = new com.google.android.material.materialswitch.MaterialSwitch(this);
        pinSwitch.setText("Require PIN for transfers");
        pinSwitch.setChecked(pinEnabled);
        pinSwitch.setPadding(0, 16, 0, 16);

        com.google.android.material.materialswitch.MaterialSwitch bioSwitch = new com.google.android.material.materialswitch.MaterialSwitch(this);
        bioSwitch.setText("Enable Biometric Login");
        bioSwitch.setChecked(bioEnabled);
        bioSwitch.setPadding(0, 16, 0, 16);

        layout.addView(pinSwitch);
        layout.addView(bioSwitch);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Security & Privacy")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    prefs.edit()
                            .putBoolean("pin_enabled", pinSwitch.isChecked())
                            .putBoolean("bio_enabled", bioSwitch.isChecked())
                            .apply();
                    Toast.makeText(this, "Security settings updated", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    // ── Notifications Settings Dialog ─────────────────────────────
    private void showNotificationsDialog() {
        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        boolean paymentNotifs = prefs.getBoolean("notif_payments", true);
        boolean promoNotifs = prefs.getBoolean("notif_promos", false);

        android.widget.LinearLayout layout = new android.widget.LinearLayout(this);
        layout.setOrientation(android.widget.LinearLayout.VERTICAL);
        layout.setPadding(64, 32, 64, 16);

        com.google.android.material.materialswitch.MaterialSwitch paymentSwitch = new com.google.android.material.materialswitch.MaterialSwitch(this);
        paymentSwitch.setText("Payment Alerts");
        paymentSwitch.setChecked(paymentNotifs);
        paymentSwitch.setPadding(0, 16, 0, 16);

        com.google.android.material.materialswitch.MaterialSwitch promoSwitch = new com.google.android.material.materialswitch.MaterialSwitch(this);
        promoSwitch.setText("Promotional Offers");
        promoSwitch.setChecked(promoNotifs);
        promoSwitch.setPadding(0, 16, 0, 16);

        layout.addView(paymentSwitch);
        layout.addView(promoSwitch);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Notifications")
                .setView(layout)
                .setPositiveButton("Save", (dialog, which) -> {
                    prefs.edit()
                            .putBoolean("notif_payments", paymentSwitch.isChecked())
                            .putBoolean("notif_promos", promoSwitch.isChecked())
                            .apply();
                    Toast.makeText(this, "Notification preferences saved", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
