package com.example.minipay;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.util.ArrayList;
import java.util.List;

public class DashboardActivity extends AppCompatActivity {

    // ── Shared app state ──────────────────────────────────────────
    public static double             balance      = 12500.00;
    public static List<Transaction>  transactions = new ArrayList<>();
    public static String             userName     = "John";

    // ── Views ─────────────────────────────────────────────────────
    TextView              tvWelcome, tvBalance;
    MaterialButton        sendBtn, receiveBtn, historyBtn, scanBtn, topUpBtn;
    BottomNavigationView  bottomNav;
    LinearLayout          recentContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        DataManager.loadData(this);

        setContentView(R.layout.activity_dashboard);

        // Read user name passed from Login
        String email = getIntent().getStringExtra("user_email");
        if (email != null) {
            // Derive display name from email prefix
            String namePart = email.split("@")[0];
            userName = namePart.substring(0, 1).toUpperCase() + namePart.substring(1);
        }

        bindViews();
        setupClickListeners();
        setupBottomNav();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshUI();
    }

    // ── Bind ──────────────────────────────────────────────────────
    private void bindViews() {
        tvWelcome       = findViewById(R.id.welcomeText);
        tvBalance       = findViewById(R.id.balanceText);
        sendBtn         = findViewById(R.id.sendBtn);
        receiveBtn      = findViewById(R.id.receiveBtn);
        historyBtn      = findViewById(R.id.historyBtn);
        scanBtn         = findViewById(R.id.scanBtn);
        topUpBtn        = findViewById(R.id.topUpBtn);
        recentContainer = findViewById(R.id.recentTransactionContainer);
        bottomNav       = findViewById(R.id.bottomNav);
    }

    // ── Listeners ─────────────────────────────────────────────────
    private void setupClickListeners() {
        sendBtn.setOnClickListener(v ->
                startActivity(new Intent(this, SelectUserActivity.class)));

        receiveBtn.setOnClickListener(v ->
                showReceiveDialog());

        historyBtn.setOnClickListener(v ->
                startActivity(new Intent(this, HistoryActivity.class)));

        scanBtn.setOnClickListener(v ->
                startActivity(new Intent(this, ScanQRActivity.class)));

        topUpBtn.setOnClickListener(v -> showTopUpDialog());
    }

    private void setupBottomNav() {
        bottomNav.setSelectedItemId(R.id.home);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home)    { return true; }
            if (id == R.id.send)    { startActivity(new Intent(this, SelectUserActivity.class)); return true; }
            if (id == R.id.history) { startActivity(new Intent(this, HistoryActivity.class));   return true; }
            if (id == R.id.profile) { startActivity(new Intent(this, ProfileActivity.class));   return true; }
            return false;
        });
    }

    // ── UI Refresh ────────────────────────────────────────────────
    private void refreshUI() {
        tvWelcome.setText("Hi, " + userName + "! 👋");
        tvBalance.setText("₹" + String.format("%,.2f", balance));
        buildRecentTransactions();
    }

    private void buildRecentTransactions() {
        recentContainer.removeAllViews();

        // Show all recent transactions (removed the limit of 5 to show "all" as requested)
        if (transactions.isEmpty()) {
            addEmptyState();
            return;
        }

        for (int i = 0; i < transactions.size(); i++) {
            Transaction tx = transactions.get(i);
            View row = buildTransactionRow(tx);
            recentContainer.addView(row);

            // Divider (not after last)
            if (i < transactions.size() - 1) {
                View divider = new View(this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT, 1);
                lp.setMarginStart(dpToPx(54)); // align with text column
                divider.setLayoutParams(lp);
                divider.setBackgroundColor(ContextCompat.getColor(this, R.color.divider));
                recentContainer.addView(divider);
            }
        }
    }

    private View buildTransactionRow(Transaction tx) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(android.view.Gravity.CENTER_VERTICAL);
        row.setPadding(dpToPx(16), dpToPx(14), dpToPx(16), dpToPx(14));

        // Icon circle
        TextView icon = new TextView(this);
        icon.setText(tx.isSent() ? "↑" : "↓");
        icon.setTextSize(16);
        icon.setTextColor(Color.WHITE);
        icon.setGravity(Gravity.CENTER);
        icon.setBackgroundResource(tx.isSent()
                ? R.drawable.icon_sent_bg
                : R.drawable.icon_received_bg);
        LinearLayout.LayoutParams iconLp = new LinearLayout.LayoutParams(dpToPx(40), dpToPx(40));
        iconLp.setMarginEnd(dpToPx(14));
        icon.setLayoutParams(iconLp);

        // Text column
        LinearLayout textCol = new LinearLayout(this);
        textCol.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textLp = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f);
        textCol.setLayoutParams(textLp);

        TextView title = new TextView(this);
        title.setText(tx.getTitle());
        title.setTextSize(14);
        title.setTextColor(ContextCompat.getColor(this, R.color.text_dark));
        title.setTypeface(null, android.graphics.Typeface.BOLD);

        TextView date = new TextView(this);
        date.setText(tx.getDate());
        date.setTextSize(12);
        date.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        date.setPadding(0, dpToPx(2), 0, 0);

        textCol.addView(title);
        textCol.addView(date);

        // Amount
        TextView amount = new TextView(this);
        amount.setText(tx.getFormattedAmount());
        amount.setTextSize(14);
        amount.setTypeface(null, android.graphics.Typeface.BOLD);
        amount.setTextColor(ContextCompat.getColor(this, tx.isSent()
                ? R.color.red_debit
                : R.color.green_credit));

        row.addView(icon);
        row.addView(textCol);
        row.addView(amount);
        return row;
    }

    private void addEmptyState() {
        TextView empty = new TextView(this);
        empty.setText("No transactions yet.\nSend money to get started! 💸");
        empty.setGravity(Gravity.CENTER);
        empty.setTextSize(14);
        empty.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        empty.setPadding(dpToPx(24), dpToPx(32), dpToPx(24), dpToPx(32));
        recentContainer.addView(empty);
    }

    private void showTopUpDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter amount to add");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setPadding(48, 24, 48, 8);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Add Funds to Wallet")
                .setView(input)
                .setPositiveButton("Top Up", (dialog, which) -> {
                    String amtStr = input.getText().toString().trim();
                    if (!amtStr.isEmpty()) {
                        double amt = Double.parseDouble(amtStr);
                        balance += amt;
                        
                        String date = new java.text.SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(new java.util.Date());
                        transactions.add(0, new Transaction("Wallet Top-Up", amt, date, Transaction.TYPE_RECEIVED));
                        
                        DataManager.saveData(this);
                        refreshUI();
                        android.widget.Toast.makeText(this, "Successfully added ₹" + amtStr, android.widget.Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showReceiveDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint("Enter amount (leave blank for open QR)");
        input.setInputType(android.text.InputType.TYPE_CLASS_NUMBER | android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setPadding(48, 24, 48, 8);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Request Money")
                .setMessage("Specify an amount to receive:")
                .setView(input)
                .setPositiveButton("Next", (dialog, which) -> {
                    String amtStr = input.getText().toString().trim();
                    showQrDialog(amtStr);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showQrDialog(String amount) {
        String upiId = userName.toLowerCase().replaceAll("\\s+", ".") + "@minipay";
        String phone = "+91 98765 00000";
        String qrData = upiId;
        if (!amount.isEmpty()) {
            qrData = "upi://pay?pa=" + upiId + "&pn=" + userName + "&am=" + amount;
        }
        
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER);
        layout.setPadding(dpToPx(24), dpToPx(24), dpToPx(24), dpToPx(16));

        android.widget.ImageView qrImageView = new android.widget.ImageView(this);
        try {
            com.journeyapps.barcodescanner.BarcodeEncoder barcodeEncoder = new com.journeyapps.barcodescanner.BarcodeEncoder();
            android.graphics.Bitmap bitmap = barcodeEncoder.encodeBitmap(qrData, com.google.zxing.BarcodeFormat.QR_CODE, 600, 600);
            qrImageView.setImageBitmap(bitmap);
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        LinearLayout.LayoutParams qrLp = new LinearLayout.LayoutParams(dpToPx(200), dpToPx(200));
        qrLp.setMargins(0, 0, 0, dpToPx(16));
        qrImageView.setLayoutParams(qrLp);

        TextView details = new TextView(this);
        String text = "UPI ID: " + upiId + "\nPhone: " + phone;
        if (!amount.isEmpty()) text += "\nRequested Amount: ₹" + amount;
        text += "\n\nScan this QR code to pay.";
        details.setText(text);
        details.setGravity(Gravity.CENTER);
        details.setTextSize(14);
        details.setTextColor(ContextCompat.getColor(this, R.color.text_dark));

        layout.addView(qrImageView);
        layout.addView(details);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("Your QR Code")
                .setView(layout)
                .setPositiveButton("Done", null)
                .show();
    }

    // ── Helpers ───────────────────────────────────────────────────
    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}