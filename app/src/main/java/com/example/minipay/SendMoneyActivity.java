package com.example.minipay;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Random;

public class SendMoneyActivity extends AppCompatActivity {

    TextView tvReceiverName, tvReceiverPhone, tvAvatar, tvBalance;
    EditText etAmount, etNote;
    Button   sendBtn;

    private String receiverName;
    private String receiverPhone;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_money);

        // Read contact details from SelectUserActivity
        receiverName  = getIntent().getStringExtra("receiver_name");
        receiverPhone = getIntent().getStringExtra("receiver_phone");
        if (receiverName  == null) receiverName  = "User";
        if (receiverPhone == null) receiverPhone = "";

        bindViews();
        populateReceiverInfo();
        updateBalanceDisplay();

        sendBtn.setOnClickListener(v -> initiateTransaction());
        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private void bindViews() {
        tvReceiverName  = findViewById(R.id.receiverName);
        tvReceiverPhone = findViewById(R.id.receiverNumber);
        tvAvatar        = findViewById(R.id.receiverAvatar);
        tvBalance       = findViewById(R.id.balanceText);
        etAmount        = findViewById(R.id.amount);
        etNote          = findViewById(R.id.note);
        sendBtn         = findViewById(R.id.sendBtn);
    }

    private void populateReceiverInfo() {
        tvReceiverName.setText(receiverName);
        tvReceiverPhone.setText(receiverPhone);

        // Build initials for avatar
        String[] parts = receiverName.trim().split(" ");
        String initials = parts.length >= 2
                ? "" + parts[0].charAt(0) + parts[parts.length - 1].charAt(0)
                : receiverName.substring(0, Math.min(2, receiverName.length()));
        tvAvatar.setText(initials.toUpperCase());
    }

    private void updateBalanceDisplay() {
        tvBalance.setText("Available: ₹" + String.format("%,.2f", DashboardActivity.balance));
    }

    private void initiateTransaction() {
        String amtStr = etAmount.getText().toString().trim();
        
        // ── Basic Validations ──────────────────────────────────────
        if (TextUtils.isEmpty(amtStr)) {
            etAmount.setError("Enter an amount");
            return;
        }

        double amt;
        try {
            amt = Double.parseDouble(amtStr);
        } catch (NumberFormatException e) {
            etAmount.setError("Invalid amount");
            return;
        }

        if (amt <= 0) {
            etAmount.setError("Amount must be positive");
            return;
        }

        if (amt > DashboardActivity.balance) {
            Toast.makeText(this, "❌ Insufficient balance", Toast.LENGTH_SHORT).show();
            return;
        }

        // ── Generate and Show OTP ──────────────────────────────────
        String generatedOtp = String.format("%04d", new Random().nextInt(10000));
        showOtpDialog(generatedOtp, amt);
    }

    private void showOtpDialog(String correctOtp, double amt) {
        EditText otpInput = new EditText(this);
        otpInput.setHint("Enter 4-digit OTP");
        otpInput.setInputType(android.text.InputType.TYPE_CLASS_NUMBER);
        otpInput.setPadding(60, 40, 60, 40);
        otpInput.setGravity(android.view.Gravity.CENTER);

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("OTP Verification")
                .setMessage("A verification code has been sent to your registered mobile number.\n\n(Demo OTP: " + correctOtp + ")")
                .setView(otpInput)
                .setCancelable(false)
                .setPositiveButton("Verify & Pay", (dialog, which) -> {
                    String userOtp = otpInput.getText().toString().trim();
                    if (userOtp.equals(correctOtp)) {
                        processPayment(amt);
                    } else {
                        Toast.makeText(this, "❌ Invalid OTP. Transaction cancelled.", Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void processPayment(double amt) {
        String note = etNote.getText().toString().trim();
        
        // ── Process payment ────────────────────────────────────────
        DashboardActivity.balance -= amt;

        String date = new SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(new Date());
        String txTitle = "Sent to " + receiverName;
        if (!note.isEmpty()) txTitle += " (" + note + ")";

        Transaction tx = new Transaction(txTitle, amt, date, Transaction.TYPE_SENT);
        DashboardActivity.transactions.add(0, tx); // newest first
        
        // Save state persistently
        DataManager.saveData(this);

        // ── Feedback ───────────────────────────────────────────────
        Toast.makeText(this,
                "✅ ₹" + String.format("%,.2f", amt) + " sent successfully!",
                Toast.LENGTH_LONG).show();

        sendBtn.setEnabled(false);
        sendBtn.setText("Success");
        sendBtn.postDelayed(this::finish, 1000);
    }
}