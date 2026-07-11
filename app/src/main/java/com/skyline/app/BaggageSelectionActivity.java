package com.skyline.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.gson.Gson;
import com.skyline.app.network.Flight;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class BaggageSelectionActivity extends AppCompatActivity {

    private Flight flight;
    private final Gson gson = new Gson();
    private TextView txtQty10, txtQty23, txtTotalPrice;
    private MaterialCardView btnMinus10, btnPlus10, btnMinus23, btnPlus23;

    private int qty10 = 0;
    private int qty23 = 0;

    private static final int PRICE_10KG = 200000;
    private static final int PRICE_23KG = 450000;
    private static final int MAX_TOTAL_BAGGAGE = 2;
    private String fareType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_baggage_selection);

        initData();
        initViews();
        updateFlightInfo();
        updateIncludedBaggage(); // Cập nhật hành lý đã bao gồm theo hạng ghế
        startPlaneAnimation();
        updateUI();
    }

    private void initData() {
        Intent intent = getIntent();
        String json = intent.getStringExtra("flight_json");
        if (json != null) {
            flight = gson.fromJson(json, Flight.class);
        }
        qty10 = intent.getIntExtra("initialB10", 0);
        qty23 = intent.getIntExtra("initialB23", 0);
        fareType = intent.getStringExtra("fareType");
    }

    private void initViews() {
        txtQty10 = findViewById(R.id.txtQty10);
        txtQty23 = findViewById(R.id.txtQty23);
        txtTotalPrice = findViewById(R.id.txtTotalPrice);

        btnMinus10 = findViewById(R.id.btnMinus10);
        btnPlus10 = findViewById(R.id.btnPlus10);
        btnMinus23 = findViewById(R.id.btnMinus23);
        btnPlus23 = findViewById(R.id.btnPlus23);

        findViewById(R.id.btnClose).setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("baggage10", 0);
            resultIntent.putExtra("baggage23", 0);
            resultIntent.putExtra("baggagePrice", 0.0);
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        btnMinus10.setOnClickListener(v -> {
            if (qty10 > 0) {
                qty10--;
                updateUI();
            }
        });

        btnPlus10.setOnClickListener(v -> {
            if (qty10 + qty23 < MAX_TOTAL_BAGGAGE) {
                qty10++;
                updateUI();
            } else {
                showMaxToast();
            }
        });

        btnMinus23.setOnClickListener(v -> {
            if (qty23 > 0) {
                qty23--;
                updateUI();
            }
        });

        btnPlus23.setOnClickListener(v -> {
            if (qty10 + qty23 < MAX_TOTAL_BAGGAGE) {
                qty23++;
                updateUI();
            } else {
                showMaxToast();
            }
        });

        findViewById(R.id.btnViewSelectedBaggage).setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("baggage10", qty10);
            resultIntent.putExtra("baggage23", qty23);
            resultIntent.putExtra("baggagePrice", (double)(qty10 * PRICE_10KG + qty23 * PRICE_23KG));
            setResult(RESULT_OK, resultIntent);
            finish();
        });
    }

    private void updateIncludedBaggage() {
        TextView tvCarryOn = findViewById(R.id.tvIncludedCarryOn);
        TextView tvChecked = findViewById(R.id.tvIncludedChecked);
        
        if (tvCarryOn == null || tvChecked == null) return;

        if ("Business".equalsIgnoreCase(fareType)) {
            tvCarryOn.setText("Hành lý xách tay: 01 kiện 12kg/người");
            tvChecked.setText("Hành lý ký gửi: 01 kiện 32kg/người");
        } else {
            // Mặc định là Economy
            tvCarryOn.setText("Hành lý xách tay: 01 kiện 7kg/người");
            tvChecked.setText("Hành lý ký gửi: 01 kiện 23kg/người");
        }
    }

    private void updateUI() {
        txtQty10.setText(String.valueOf(qty10));
        txtQty23.setText(String.valueOf(qty23));

        if (qty10 > 0) {
            btnMinus10.setCardBackgroundColor(ContextCompat.getColor(this, R.color.skyline_blue_dark));
        } else {
            btnMinus10.setCardBackgroundColor(Color.parseColor("#E0E3E8"));
        }

        if (qty23 > 0) {
            btnMinus23.setCardBackgroundColor(ContextCompat.getColor(this, R.color.skyline_blue_dark));
        } else {
            btnMinus23.setCardBackgroundColor(Color.parseColor("#E0E3E8"));
        }

        int totalQty = qty10 + qty23;
        int activeColor = ContextCompat.getColor(this, R.color.skyline_blue_dark);
        int disabledColor = Color.parseColor("#E0E3E8");

        btnPlus10.setCardBackgroundColor(totalQty < MAX_TOTAL_BAGGAGE ? activeColor : disabledColor);
        btnPlus23.setCardBackgroundColor(totalQty < MAX_TOTAL_BAGGAGE ? activeColor : disabledColor);

        int total = qty10 * PRICE_10KG + qty23 * PRICE_23KG;
        DecimalFormat df = new DecimalFormat("#,###");
        txtTotalPrice.setText(df.format(total) + " VND");
    }

    private void showMaxToast() {
        Toast.makeText(this, "Mỗi hành khách chỉ được mua tối đa 2 gói hành lý bổ sung", Toast.LENGTH_SHORT).show();
    }

    private void updateFlightInfo() {
        if (flight == null) return;
        
        TextView tvFlightHeader = findViewById(R.id.tvFlightNumberHeader);
        if (tvFlightHeader != null) {
            tvFlightHeader.setText("CHUYẾN BAY " + flight.getFlightNumber());
        }

        setTextSafe(R.id.tvDepCode, flight.getDepartureAirport().getCode());
        setTextSafe(R.id.tvArrCode, flight.getArrivalAirport().getCode());
        setTextSafe(R.id.tvDepAirport, cleanAirportName(flight.getDepartureAirport().getName()));
        setTextSafe(R.id.tvArrAirport, cleanAirportName(flight.getArrivalAirport().getName()));

        Date dDate = parseIsoDate(flight.getDepartureAt());
        Date aDate = parseIsoDate(flight.getArrivalAt());
        SimpleDateFormat timeF = new SimpleDateFormat("HH:mm", Locale.US);
        SimpleDateFormat dateF = new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN"));
        timeF.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateF.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (dDate != null) {
            setTextSafe(R.id.tvDepTime, timeF.format(dDate));
            setTextSafe(R.id.tvDepDate, dateF.format(dDate));
        }
        if (aDate != null) {
            setTextSafe(R.id.tvArrTime, timeF.format(aDate));
            setTextSafe(R.id.tvArrDate, dateF.format(aDate));
        }
        if (flight.getDuration() > 0) {
            setTextSafe(R.id.tvDuration, (flight.getDuration()/60) + "g " + (flight.getDuration()%60) + "p");
        }
    }

    private void startPlaneAnimation() {
        View imgPlane = findViewById(R.id.imgPlane);
        if (imgPlane != null) {
            ObjectAnimator animator = ObjectAnimator.ofFloat(imgPlane, "translationY", -15f, 15f);
            animator.setDuration(2000);
            animator.setRepeatMode(ValueAnimator.REVERSE);
            animator.setRepeatCount(ValueAnimator.INFINITE);
            animator.setInterpolator(new AccelerateDecelerateInterpolator());
            animator.start();
        }
    }

    private void setTextSafe(int viewId, String text) {
        TextView tv = findViewById(viewId);
        if (tv != null && text != null) tv.setText(text);
    }

    private Date parseIsoDate(String iso) {
        if (iso == null) return null;
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
        for (String p : patterns) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(p, Locale.US);
                f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return f.parse(iso);
            } catch (Exception ignored) {}
        }
        return null;
    }

    private String cleanAirportName(String name) {
        if (name == null) return "";
        return name.toUpperCase().replace("SÂN BAY QUỐC TẾ ", "").replace("SÂN BAY ", "").trim();
    }
}