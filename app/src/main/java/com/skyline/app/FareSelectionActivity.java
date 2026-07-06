package com.skyline.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class FareSelectionActivity extends AppCompatActivity {
    
    private String flightNumber, fromCode, toCode, fromName, toName, departureTime, arrivalTime;
    private int durationMinutes;
    private double basePrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_selection);

        // Get data from intent
        flightNumber = getIntent().getStringExtra("flightNumber");
        fromCode = getIntent().getStringExtra("fromCode");
        toCode = getIntent().getStringExtra("toCode");
        fromName = getIntent().getStringExtra("fromName");
        toName = getIntent().getStringExtra("toName");
        departureTime = getIntent().getStringExtra("departureTime");
        arrivalTime = getIntent().getStringExtra("arrivalTime");
        durationMinutes = getIntent().getIntExtra("duration", 0);
        basePrice = getIntent().getDoubleExtra("basePrice", 0);

        View btnBack = findViewById(R.id.btnBack);
        View btnClose = findViewById(R.id.btnClose);
        
        if (btnBack != null) btnBack.setOnClickListener(v -> finish());
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                Intent intent = new Intent(FareSelectionActivity.this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        updateFlightInfo();
        startPlaneAnimation();
        setupFares();
    }

    private void updateFlightInfo() {
        TextView tvFlightHeader = findViewById(R.id.tvFlightNumberHeader);
        TextView tvDepCode = findViewById(R.id.tvDepCode);
        TextView tvDepTime = findViewById(R.id.tvDepTime);
        TextView tvDepDate = findViewById(R.id.tvDepDate);
        TextView tvDepAirport = findViewById(R.id.tvDepAirport);
        TextView tvArrCode = findViewById(R.id.tvArrCode);
        TextView tvArrTime = findViewById(R.id.tvArrTime);
        TextView tvArrDate = findViewById(R.id.tvArrDate);
        TextView tvArrAirport = findViewById(R.id.tvArrAirport);
        TextView tvDuration = findViewById(R.id.tvDuration);

        if (flightNumber != null) tvFlightHeader.setText("CHUYẾN BAY " + flightNumber);
        if (fromCode != null) tvDepCode.setText(fromCode);
        if (toCode != null) tvArrCode.setText(toCode);
        
        if (fromName != null) tvDepAirport.setText(cleanAirportName(fromName));
        if (toName != null) tvArrAirport.setText(cleanAirportName(toName));

        // Format dates
        Date dDate = parseIsoDate(departureTime);
        Date aDate = parseIsoDate(arrivalTime);

        if (dDate != null) {
            tvDepTime.setText(new SimpleDateFormat("HH:mm", Locale.US).format(dDate));
            tvDepDate.setText(new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN")).format(dDate));
        } else {
            tvDepTime.setText("--:--");
            tvDepDate.setText("---");
        }

        if (aDate != null) {
            tvArrTime.setText(new SimpleDateFormat("HH:mm", Locale.US).format(aDate));
            tvArrDate.setText(new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN")).format(aDate));
        } else {
            tvArrTime.setText("--:--");
            tvArrDate.setText("---");
        }
        
        if (durationMinutes > 0) {
            int h = durationMinutes / 60;
            int m = durationMinutes % 60;
            tvDuration.setText(h + "h " + m + "m");
        } else if (dDate != null && aDate != null) {
            long diff = aDate.getTime() - dDate.getTime();
            long totalMinutes = diff / (60 * 1000);
            long h = totalMinutes / 60;
            long m = totalMinutes % 60;
            tvDuration.setText(h + "h " + m + "m");
        } else {
            tvDuration.setText("--h --m");
        }
    }

    private Date parseIsoDate(String isoString) {
        if (isoString == null) return null;
        String[] patterns = {
            "yyyy-MM-dd'T'HH:mm:ssXXX",
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss",
            "yyyy-MM-dd HH:mm:ss"
        };
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                return format.parse(isoString);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String cleanAirportName(String name) {
        if (name == null) return "";
        return name.toUpperCase()
                .replace("SÂN BAY QUỐC TẾ ", "")
                .replace("SÂN BAY ", "")
                .replace("AIRPORT", "")
                .trim();
    }

    private void startPlaneAnimation() {
        ImageView imgPlane = findViewById(R.id.imgPlane);
        if (imgPlane != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(imgPlane, "translationY", -15f, 15f);
            animator.setDuration(2000);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
        }
    }

    private void setupFares() {
        int blue = ContextCompat.getColor(this, R.color.skyline_blue);
        int teal = ContextCompat.getColor(this, R.color.skyline_teal);

        DecimalFormat df = new DecimalFormat("#,###");
        String economyPrice = df.format(basePrice) + " VND";
        String businessPrice = df.format(basePrice * 2.0) + " VND";

        View cardEconomy = findViewById(R.id.cardEconomy);
        if (cardEconomy != null) {
            setupFareDetails(cardEconomy, "Phổ thông", "TIẾT KIỆM & TIỆN LỢI", economyPrice, blue);
            setupBenefit(cardEconomy, R.id.benefitCarryOn, R.drawable.ic_baggage_small, "HÀNH LÝ XÁCH TAY", "01 kiện 7kg", blue);
            setupBenefit(cardEconomy, R.id.benefitChecked, R.drawable.ic_baggage_big, "HÀNH LÝ KÝ GỬI", "01 kiện 23kg", blue);
            setupBenefit(cardEconomy, R.id.benefitChange, R.drawable.ic_swap, "ĐỔI VÉ", "300.000 VND + chênh lệch", blue);
            setupBenefit(cardEconomy, R.id.benefitRefund, R.drawable.ic_ticket, "HOÀN VÉ", "Phí từ 800.000 VND", blue);
            setupBenefit(cardEconomy, R.id.benefitSeat, R.drawable.ic_seat, "CHỌN CHỖ", "Miễn phí", blue);

            cardEconomy.findViewById(R.id.btnSelectFare).setOnClickListener(v -> navigateToPayment("Economy", basePrice));
        }

        View cardBusiness = findViewById(R.id.cardBusiness);
        if (cardBusiness != null) {
            setupFareDetails(cardBusiness, "Thương gia", "LINH HOẠT TỐI ĐA", businessPrice, teal);
            setupBenefit(cardBusiness, R.id.benefitCarryOn, R.drawable.ic_baggage_small, "HÀNH LÝ XÁCH TAY", "01 kiện 12kg", teal);
            setupBenefit(cardBusiness, R.id.benefitChecked, R.drawable.ic_baggage_big, "HÀNH LÝ KÝ GỬI", "01 kiện 32kg", teal);
            setupBenefit(cardBusiness, R.id.benefitChange, R.drawable.ic_swap, "ĐỔI VÉ", "Miễn phí đổi + chênh lệch", teal);
            setupBenefit(cardBusiness, R.id.benefitRefund, R.drawable.ic_ticket, "HOÀN VÉ", "Phí từ 500.000 VND", teal);
            setupBenefit(cardBusiness, R.id.benefitSeat, R.drawable.ic_seat, "CHỌN CHỖ", "Miễn phí", teal);

            cardBusiness.findViewById(R.id.btnSelectFare).setOnClickListener(v -> navigateToPayment("Business", basePrice * 2.0));
        }
    }

    private void navigateToPayment(String fareType, double price) {
        Intent intent = new Intent(this, AddonServiceActivity.class);
        intent.putExtra("flightNumber", flightNumber);
        intent.putExtra("fromCode", fromCode);
        intent.putExtra("toCode", toCode);
        intent.putExtra("fromName", fromName);
        intent.putExtra("toName", toName);
        intent.putExtra("departureTime", departureTime);
        intent.putExtra("arrivalTime", arrivalTime);
        intent.putExtra("duration", durationMinutes);
        intent.putExtra("fareType", fareType);
        intent.putExtra("totalPrice", price);
        startActivity(intent);
    }

    private void setupFareDetails(View fareView, String title, String subtitle, String price, int themeColor) {
        TextView tvTitle = fareView.findViewById(R.id.tvFareTitle);
        TextView tvSubtitle = fareView.findViewById(R.id.tvFareSubtitle);
        TextView tvPrice = fareView.findViewById(R.id.tvFarePrice);
        View btnSelect = fareView.findViewById(R.id.btnSelectFare);
        MaterialCardView card = (MaterialCardView) fareView;

        if (tvTitle != null) {
            tvTitle.setText(title);
            tvTitle.setTextColor(themeColor);
        }
        if (tvSubtitle != null) tvSubtitle.setText(subtitle);
        if (tvPrice != null) tvPrice.setText(price);
        if (btnSelect != null) {
            btnSelect.setBackgroundTintList(ColorStateList.valueOf(themeColor));
        }
        card.setStrokeColor(themeColor);
    }

    private void setupBenefit(View parent, int includeId, int iconRes, String label, String value, int themeColor) {
        View benefitView = parent.findViewById(includeId);
        if (benefitView == null) return;

        ImageView icon = benefitView.findViewById(R.id.imgBenefitIcon);
        TextView tvTitle = benefitView.findViewById(R.id.tvBenefitTitle);
        TextView tvDesc = benefitView.findViewById(R.id.tvBenefitDesc);

        if (icon != null) {
            icon.setImageResource(iconRes);
            icon.setImageTintList(ColorStateList.valueOf(themeColor));
        }
        if (tvTitle != null) tvTitle.setText(label.toUpperCase());
        if (tvDesc != null) {
            tvDesc.setText(value);
            if (value.contains("Miễn phí") || value.contains("Có thu phí")) {
                tvDesc.setTextColor(themeColor);
            } else {
                tvDesc.setTextColor(ContextCompat.getColor(this, R.color.skyline_text));
            }
        }
    }
}