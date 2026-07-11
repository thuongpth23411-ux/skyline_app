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
import com.google.gson.Gson;
import com.skyline.app.network.Flight;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class FareSelectionActivity extends AppCompatActivity {
    
    private Flight flight;
    private final Gson gson = new Gson();
    
    private boolean isRoundTrip = false;
    private boolean isSelectingReturn = false;
    private String returnDateStr;
    private String outboundFlightJson;
    private double outboundFarePrice;
    private String outboundFareType;
    private String fromName, toName;
    private int adults, children;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fare_selection);

        Intent intent = getIntent();
        String json = intent.getStringExtra("flight_json");
        if (json != null) {
            flight = gson.fromJson(json, Flight.class);
        }

        // Nhận thông tin khứ hồi
        isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);
        isSelectingReturn = intent.getBooleanExtra("isSelectingReturn", false);
        returnDateStr = intent.getStringExtra("returnDate");
        fromName = intent.getStringExtra("fromName");
        toName = intent.getStringExtra("toName");
        adults = intent.getIntExtra("adults", 1);
        children = intent.getIntExtra("children", 0);
        
        if (isSelectingReturn) {
            outboundFlightJson = intent.getStringExtra("outbound_flight");
            outboundFarePrice = intent.getDoubleExtra("outbound_fare_price", 0);
            outboundFareType = intent.getStringExtra("outbound_fare_type");
        }

        if (flight == null) {
            finish();
            return;
        }

        initViews();
        updateFlightInfo();
        startPlaneAnimation();
        setupFares();
    }

    private void initViews() {
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnClose).setOnClickListener(v -> {
            Intent intent = new Intent(this, HomeActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void updateFlightInfo() {
        TextView tvFlightHeader = findViewById(R.id.tvFlightNumberHeader);
        TextView tvSubtitleHeader = findViewById(R.id.tvSubtitleHeader);
        TextView tvDepCode = findViewById(R.id.tvDepCode);
        TextView tvDepTime = findViewById(R.id.tvDepTime);
        TextView tvDepDate = findViewById(R.id.tvDepDate);
        TextView tvDepAirport = findViewById(R.id.tvDepAirport);
        TextView tvArrCode = findViewById(R.id.tvArrCode);
        TextView tvArrTime = findViewById(R.id.tvArrTime);
        TextView tvArrDate = findViewById(R.id.tvArrDate);
        TextView tvArrAirport = findViewById(R.id.tvArrAirport);
        TextView tvDuration = findViewById(R.id.tvDuration);

        if (tvFlightHeader != null) {
            tvFlightHeader.setText(isRoundTrip ? "CHUYẾN BAY KHỨ HỒI" : "CHUYẾN BAY MỘT CHIỀU");
        }
        TextView tvFlightNum = findViewById(R.id.tvFlightNumber);
        if (tvFlightNum != null) tvFlightNum.setText(flight.getFlightNumber());
        if (tvSubtitleHeader != null) {
            tvSubtitleHeader.setText(isRoundTrip ? (isSelectingReturn ? "CHỌN HẠNG VÉ LƯỢT VỀ" : "CHỌN HẠNG VÉ LƯỢT ĐI") : "CHỌN HẠNG VÉ");
        }

        if (tvDepCode != null) tvDepCode.setText(flight.getDepartureAirport().getCode());
        if (tvArrCode != null) tvArrCode.setText(flight.getArrivalAirport().getCode());
        
        if (tvDepAirport != null) tvDepAirport.setText(cleanAirportName(flight.getDepartureAirport().getName()));
        if (tvArrAirport != null) tvArrAirport.setText(cleanAirportName(flight.getArrivalAirport().getName()));

        Date dDate = parseIsoDate(flight.getDepartureAt());
        Date aDate = parseIsoDate(flight.getArrivalAt());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN"));
        timeFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        if (dDate != null) {
            if (tvDepTime != null) tvDepTime.setText(timeFormat.format(dDate));
            if (tvDepDate != null) tvDepDate.setText(dateFormat.format(dDate));
        }
        if (aDate != null) {
            if (tvArrTime != null) tvArrTime.setText(timeFormat.format(aDate));
            if (tvArrDate != null) tvArrDate.setText(dateFormat.format(aDate));
        }
        
        int durationMinutes = flight.getDuration();
        if (durationMinutes > 0) {
            int h = durationMinutes / 60;
            int m = durationMinutes % 60;
            if (tvDuration != null) tvDuration.setText(h + "g " + m + "p");
        }
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

        // API trả về giá đã bao gồm thuế và phí
        double baseE = flight.getBasePrice();
        double baseB = flight.getBasePrice() * 2.0;

        List<Flight.PriceOption> options = flight.getPriceOptions();
        if (options != null) {
            for (Flight.PriceOption opt : options) {
                if ("ECONOMY".equalsIgnoreCase(opt.getType())) baseE = opt.getPrice();
                if ("BUSINESS".equalsIgnoreCase(opt.getType())) baseB = opt.getPrice();
            }
        }

        double totalE = baseE;
        double totalB = baseB;

        DecimalFormat df = new DecimalFormat("#,###");

        View cardEconomy = findViewById(R.id.cardEconomy);
        if (cardEconomy != null) {
            setupFareDetails(cardEconomy, "Phổ thông", "TIẾT KIỆM & TIỆN LỢI", df.format(totalE) + " VND", blue);
            setupBenefit(cardEconomy, R.id.benefitCarryOn, R.drawable.ic_baggage_small, "HÀNH LÝ XÁCH TAY", "01 kiện 7kg", blue);
            setupBenefit(cardEconomy, R.id.benefitChecked, R.drawable.ic_baggage_big, "HÀNH LÝ KÝ GỬI", "01 kiện 23kg", blue);
            setupBenefit(cardEconomy, R.id.benefitChange, R.drawable.ic_swap, "ĐỔI VÉ", "300.000 VND + chênh lệch", blue);
            setupBenefit(cardEconomy, R.id.benefitRefund, R.drawable.ic_ticket, "HOÀN VÉ", "Phí từ 800.000 VND", blue);
            setupBenefit(cardEconomy, R.id.benefitSeat, R.drawable.ic_seat, "CHỌN CHỖ", "Miễn phí", blue);

            final double finalBaseE = baseE;
            cardEconomy.findViewById(R.id.btnSelectFare).setOnClickListener(v -> navigateToAddon("Economy", finalBaseE));
        }

        View cardBusiness = findViewById(R.id.cardBusiness);
        if (cardBusiness != null) {
            setupFareDetails(cardBusiness, "Thương gia", "LINH HOẠT TỐI ĐA", df.format(totalB) + " VND", teal);
            setupBenefit(cardBusiness, R.id.benefitCarryOn, R.drawable.ic_baggage_small, "HÀNH LÝ XÁCH TAY", "01 kiện 12kg", teal);
            setupBenefit(cardBusiness, R.id.benefitChecked, R.drawable.ic_baggage_big, "HÀNH LÝ KÝ GỬI", "01 kiện 32kg", teal);
            setupBenefit(cardBusiness, R.id.benefitChange, R.drawable.ic_swap, "ĐỔI VÉ", "Miễn phí đổi + chênh lệch", teal);
            setupBenefit(cardBusiness, R.id.benefitRefund, R.drawable.ic_ticket, "HOÀN VÉ", "Phí từ 500.000 VND", teal);
            setupBenefit(cardBusiness, R.id.benefitSeat, R.drawable.ic_seat, "CHỌN CHỖ", "Miễn phí", teal);

            final double finalBaseB = baseB;
            cardBusiness.findViewById(R.id.btnSelectFare).setOnClickListener(v -> navigateToAddon("Business", finalBaseB));
        }
    }

    private void navigateToAddon(String fareType, double price) {
        if (isRoundTrip && !isSelectingReturn) {
            // ĐI TIẾP ĐẾN CHỌN CHUYẾN BAY VỀ
            Intent intent = new Intent(this, FlightResultsActivity.class);
            intent.putExtra("fromCode", flight.getArrivalAirport().getCode());
            intent.putExtra("toCode", flight.getDepartureAirport().getCode());
            intent.putExtra("fromName", toName); // Đảo ngược tên thành phố
            intent.putExtra("toName", fromName);
            intent.putExtra("date", returnDateStr);
            
            intent.putExtra("isRoundTrip", true);
            intent.putExtra("isSelectingReturn", true);
            intent.putExtra("returnDate", returnDateStr);
            
            // TRUYỀN TIẾP SỐ LƯỢNG HÀNH KHÁCH KHI CHỌN LƯỢT VỀ
            intent.putExtra("adults", adults);
            intent.putExtra("children", children);
            
            // Lưu thông tin lượt đi
            intent.putExtra("outbound_flight", gson.toJson(flight));
            intent.putExtra("outbound_fare_price", price);
            intent.putExtra("outbound_fare_type", fareType);
            
            startActivity(intent);
        } else {
            // ĐI ĐẾN DỊCH VỤ BỔ SUNG
            Intent intent = new Intent(this, AddonServiceActivity.class);
            intent.putExtra("isRoundTrip", isRoundTrip);
            intent.putExtra("adults", adults);
            intent.putExtra("children", children);
            
            if (isRoundTrip) {
                // Đã chọn xong cả 2 lượt
                intent.putExtra("flight_json", outboundFlightJson); // Lượt đi
                intent.putExtra("fareType", outboundFareType);
                intent.putExtra("totalPrice", outboundFarePrice);
                
                intent.putExtra("return_flight_json", gson.toJson(flight)); // Lượt về (flight hiện tại)
                intent.putExtra("returnFareType", fareType);
                intent.putExtra("returnTotalPrice", price);
            } else {
                // Một chiều
                intent.putExtra("flight_json", gson.toJson(flight));
                intent.putExtra("fareType", fareType);
                intent.putExtra("totalPrice", price);
            }
            startActivity(intent);
        }
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

    private Date parseIsoDate(String isoString) {
        if (isoString == null) return null;
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
        for (String pattern : patterns) {
            try {
                SimpleDateFormat format = new SimpleDateFormat(pattern, Locale.US);
                if (pattern.contains("Z") || pattern.contains("XXX")) {
                    format.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                }
                return format.parse(isoString);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String cleanAirportName(String name) {
        if (name == null) return "";
        return name.replaceAll("(?i)(SÂN BAY QUỐC TẾ |SÂN BAY |AIRPORT )", "").trim().toUpperCase();
    }
}