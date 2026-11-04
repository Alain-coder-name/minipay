package com.example.minipay;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import java.io.File;
import java.io.FileOutputStream;

public class ReceiptActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        Intent intent = getIntent();
        String title = intent.getStringExtra("title");
        String amount = intent.getStringExtra("amount");
        String date = intent.getStringExtra("date");
        int type = intent.getIntExtra("type", Transaction.TYPE_SENT);

        TextView tvAmount = findViewById(R.id.receiptAmount);
        TextView tvName = findViewById(R.id.receiptName);
        TextView tvDate = findViewById(R.id.receiptDate);
        TextView tvType = findViewById(R.id.receiptType);

        tvAmount.setText(amount);
        tvName.setText(title.replace("Sent to ", "")
                           .replace("Received from ", "")
                           .replace("Requested from ", ""));
        tvDate.setText(date);
        
        if (type == Transaction.TYPE_SENT) {
            tvType.setText("Sent");
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.red_debit));
        } else if (type == Transaction.TYPE_REQUESTED) {
            tvType.setText("Requested");
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.text_gray));
        } else {
            tvType.setText("Received");
            tvAmount.setTextColor(ContextCompat.getColor(this, R.color.green_credit));
        }

        findViewById(R.id.closeBtn).setOnClickListener(v -> finish());
        findViewById(R.id.shareBtn).setOnClickListener(v -> shareReceipt());
    }

    private void shareReceipt() {
        View receiptCard = findViewById(R.id.receiptCard);
        Bitmap bitmap = Bitmap.createBitmap(receiptCard.getWidth(), receiptCard.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        receiptCard.draw(canvas);
        
        try {
            File cachePath = new File(getCacheDir(), "images");
            cachePath.mkdirs();
            FileOutputStream stream = new FileOutputStream(cachePath + "/receipt.png");
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            File imagePath = new File(getCacheDir(), "images");
            File newFile = new File(imagePath, "receipt.png");
            Uri contentUri = FileProvider.getUriForFile(this, getPackageName() + ".fileprovider", newFile);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, "Share Receipt"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to share receipt", Toast.LENGTH_SHORT).show();
        }
    }
}
