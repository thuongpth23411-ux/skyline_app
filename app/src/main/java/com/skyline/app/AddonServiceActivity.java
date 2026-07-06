package com.skyline.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddonServiceActivity extends AppCompatActivity {

    private String flightNumber, fromCode, toCode, fromName, toName, departureTime, arrivalTime;
    private int durationMinutes;
    private double totalPrice;
    private TextView txtTotalPrice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_service);

        initData();
        initViews();
        updateUI();
    }

    private void initData() {
        Intent intent = getIntent();
        flightNumber = intent.getStringExtra("flightNumber");
        fromCode = intent.getStringExtra("fromCode");
        toCode = intent.getStringExtra("toCode");
        fromName = intent.getStringExtra("fromName");
        toName = intent.getStringExtra("toName");
        departureTime = intent.getStringExtra("departureTime");
        arrivalTime = intent.getStringExtra("arrivalTime");
        durationMinutes = intent.getIntExtra("duration", 0);
        totalPrice = intent.getDoubleExtra("totalPrice", 0);
    }

    private void initViews() {
        txtTotalPrice = findViewById(R.id.txtTotalPrice);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        View btnClose = findViewById(R.id.btnClose);
        if (btnClose != null) {
            btnClose.setOnClickListener(v -> {
                Intent intent = new Intent(this, HomeActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                finish();
            });
        }

        findViewById(R.id.btnContinue).setOnClickListener(v -> goNext());

        findViewById(R.id.itemSeat).setOnClickListener(v ->
                Toast.makeText(this, "Tính năng chọn ghế đang phát triển", Toast.LENGTH_SHORT).show()
        );

        findViewById(R.id.itemBaggage).setOnClickListener(v ->
                Toast.makeText(this, "Tính năng mua hành lý đang phát triển", Toast.LENGTH_SHORT).show()
        );
    }

    private void updateUI() {
        updateFlightInfo();
        startPlaneAnimation();
        updateTotalPrice();
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

    private void updateFlightInfo() {
        setTextSafe(R.id.tvFlightNumberHeader, "CHUYẾN BAY " + (flightNumber != null ? flightNumber : ""));
        setTextSafe(R.id.tvDepCode, fromCode);
        setTextSafe(R.id.tvArrCode, toCode);
        setTextSafe(R.id.tvDepAirport, cleanAirportName(fromName));
        setTextSafe(R.id.tvArrAirport, cleanAirportName(toName));

        Date dDate = parseIsoDate(departureTime);
        Date aDate = parseIsoDate(arrivalTime);

        if (dDate != null) {
            setTextSafe(R.id.tvDepTime, new SimpleDateFormat("HH:mm", Locale.US).format(dDate));
            setTextSafe(R.id.tvDepDate, new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN")).format(dDate));
        }
        if (aDate != null) {
            setTextSafe(R.id.tvArrTime, new SimpleDateFormat("HH:mm", Locale.US).format(aDate));
            setTextSafe(R.id.tvArrDate, new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN")).format(aDate));
        }
        
        if (durationMinutes > 0) {
            int h = durationMinutes / 60;
            int m = durationMinutes % 60;
            setTextSafe(R.id.tvDuration, h + "h " + m + "m");
        }
    }

    private void setTextSafe(int viewId, String text) {
        TextView textView = findViewById(viewId);
        if (textView != null && text != null) {
            textView.setText(text);
        }
    }

    private Date parseIsoDate(String isoString) {
        if (isoString == null) return null;
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
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

    private void updateTotalPrice() {
        if (txtTotalPrice != null) {
            DecimalFormat df = new DecimalFormat("#,###");
            txtTotalPrice.setText(df.format(totalPrice) + " VND");
        }
    }

    private void goNext() {
        Intent intent = new Intent(this, ConfirmPaymentActivity.class);
        intent.putExtra("flightNumber", flightNumber);
        intent.putExtra("totalPrice", totalPrice);
        startActivity(intent);
    }
}