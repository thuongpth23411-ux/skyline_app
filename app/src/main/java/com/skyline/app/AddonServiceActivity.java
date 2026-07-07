package com.skyline.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.skyline.app.network.Flight;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class AddonServiceActivity extends AppCompatActivity {

    private Flight flight;
    private final Gson gson = new Gson();
    private double baseFarePrice, addonPrice = 0;
    private String fareType;
    private TextView txtTotalPrice;

    private static final int REQUEST_CODE_SEAT = 100;
    private static final int REQUEST_CODE_BAGGAGE = 101;
    private String selectedSeat = "";
    private int baggage10 = 0, baggage23 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_service);

        String json = getIntent().getStringExtra("flight_json");
        if (json != null) {
            flight = gson.fromJson(json, Flight.class);
        }
        baseFarePrice = getIntent().getDoubleExtra("totalPrice", 0);
        fareType = getIntent().getStringExtra("fareType");

        if (flight == null) {
            finish();
            return;
        }

        initViews();
        updateUI();
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

        findViewById(R.id.itemSeat).setOnClickListener(v -> {
            Intent intent = new Intent(this, SeatSelectionActivity.class);
            intent.putExtra("flight_json", gson.toJson(flight));
            intent.putExtra("initialSeat", selectedSeat);
            intent.putExtra("fareType", fareType);
            startActivityForResult(intent, REQUEST_CODE_SEAT);
        });

        findViewById(R.id.itemBaggage).setOnClickListener(v -> {
            Intent intent = new Intent(this, BaggageSelectionActivity.class);
            intent.putExtra("flight_json", gson.toJson(flight));
            intent.putExtra("initialB10", baggage10);
            intent.putExtra("initialB23", baggage23);
            intent.putExtra("fareType", fareType); // Thêm dòng này
            startActivityForResult(intent, REQUEST_CODE_BAGGAGE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, requestCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_SEAT) {
                selectedSeat = data.getStringExtra("selectedSeat");
                TextView tvSelectSeat = findViewById(R.id.tvSelectSeat);
                if (tvSelectSeat != null) {
                    tvSelectSeat.setText(selectedSeat != null && !selectedSeat.isEmpty() ? selectedSeat : "Chọn");
                }
            } else if (requestCode == REQUEST_CODE_BAGGAGE) {
                baggage10 = data.getIntExtra("baggage10", 0);
                baggage23 = data.getIntExtra("baggage23", 0);
                addonPrice = data.getDoubleExtra("baggagePrice", 0);
                
                TextView tvSelectBaggage = findViewById(R.id.tvSelectBaggage);
                if (tvSelectBaggage != null) {
                    if (baggage10 > 0 || baggage23 > 0) {
                        tvSelectBaggage.setText("+" + (baggage10 * 10 + baggage23 * 23) + "KG");
                    } else {
                        tvSelectBaggage.setText("Chọn");
                    }
                }
                updateTotalPrice();
            }
        }
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
        setTextSafe(R.id.tvFlightNumberHeader, "CHUYẾN BAY " + flight.getFlightNumber());
        setTextSafe(R.id.tvDepCode, flight.getDepartureAirport().getCode());
        setTextSafe(R.id.tvArrCode, flight.getArrivalAirport().getCode());
        setTextSafe(R.id.tvDepAirport, cleanAirportName(flight.getDepartureAirport().getName()));
        setTextSafe(R.id.tvArrAirport, cleanAirportName(flight.getArrivalAirport().getName()));

        Date dDate = parseIsoDate(flight.getDepartureAt());
        Date aDate = parseIsoDate(flight.getArrivalAt());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN"));
        timeFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        if (dDate != null) {
            setTextSafe(R.id.tvDepTime, timeFormat.format(dDate));
            setTextSafe(R.id.tvDepDate, dateFormat.format(dDate));
        }
        if (aDate != null) {
            setTextSafe(R.id.tvArrTime, timeFormat.format(aDate));
            setTextSafe(R.id.tvArrDate, dateFormat.format(aDate));
        }
        
        int durationMinutes = flight.getDuration();
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
        return name.toUpperCase()
                .replace("SÂN BAY QUỐC TẾ ", "")
                .replace("SÂN BAY ", "")
                .replace("AIRPORT", "")
                .trim();
    }

    private void updateTotalPrice() {
        if (txtTotalPrice != null) {
            DecimalFormat df = new DecimalFormat("#,###");
            txtTotalPrice.setText(df.format(baseFarePrice + addonPrice) + " VND");
        }
    }

    private void goNext() {
        Intent intent = new Intent(this, ConfirmPaymentActivity.class);
        intent.putExtra("flightNumber", flight.getFlightNumber());
        intent.putExtra("totalPrice", baseFarePrice + addonPrice);
        startActivity(intent);
    }
}