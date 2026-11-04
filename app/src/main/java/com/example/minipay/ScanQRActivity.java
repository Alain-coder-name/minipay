package com.example.minipay;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.journeyapps.barcodescanner.BarcodeCallback;
import com.journeyapps.barcodescanner.BarcodeResult;
import com.journeyapps.barcodescanner.DecoratedBarcodeView;

public class ScanQRActivity extends AppCompatActivity {
    private DecoratedBarcodeView barcodeView;
    private boolean isPermissionRequested = false;

    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    barcodeView.resume();
                } else {
                    Toast.makeText(this, "Camera permission is required to scan QR codes", Toast.LENGTH_LONG).show();
                    finish();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_qr);

        barcodeView = findViewById(R.id.barcode_scanner);
        barcodeView.decodeContinuous(callback);

        findViewById(R.id.backBtn).setOnClickListener(v -> finish());
    }

    private final BarcodeCallback callback = new BarcodeCallback() {
        @Override
        public void barcodeResult(BarcodeResult result) {
            if (result.getText() != null) {
                barcodeView.pause();
                
                String scannedText = result.getText();
                Intent intent = new Intent(ScanQRActivity.this, SendMoneyActivity.class);
                
                // Set the receiver name to 'Scanned User' and phone/ID to the scanned text
                intent.putExtra("receiver_name", "Scanned User");
                intent.putExtra("receiver_phone", scannedText);
                
                startActivity(intent);
                finish(); // Close the scanner
            }
        }
    };

    @Override
    protected void onResume() {
        super.onResume();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            barcodeView.resume();
        } else if (!isPermissionRequested) {
            isPermissionRequested = true;
            requestPermissionLauncher.launch(Manifest.permission.CAMERA);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        barcodeView.pause();
    }
}
