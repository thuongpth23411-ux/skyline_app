package com.skyline.app;

import android.graphics.Bitmap;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.skyline.app.databinding.ActivityMemberQrBinding;
import com.skyline.app.utils.QrGenerator;
import com.skyline.app.utils.SessionManager;

public class MemberQrActivity extends AppCompatActivity {
    private ActivityMemberQrBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMemberQrBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SessionManager sm = new SessionManager(this);
        String name = sm.getUserName();
        String code = sm.getMemberCode();
        String rank = getIntent().getStringExtra("RANK");

        binding.tvFullName.setText(name.toUpperCase());
        binding.tvMemberCode.setText(code);
        binding.tvRank.setText(rank != null ? rank.toUpperCase() : "ĐỒNG");

        binding.btnClose.setOnClickListener(v -> finish());

        // Generate Codes
        if (code != null && !code.isEmpty()) {
            Bitmap qr = QrGenerator.generateQrCode(code, 600);
            if (qr != null) binding.imgQrLarge.setImageBitmap(qr);

            Bitmap barcode = QrGenerator.generateBarcode(code, 1000, 300);
            if (barcode != null) binding.imgBarcode.setImageBitmap(barcode);
        }
    }
}
