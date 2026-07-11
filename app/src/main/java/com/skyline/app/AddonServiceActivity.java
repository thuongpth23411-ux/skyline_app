package com.skyline.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.skyline.app.network.Flight;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddonServiceActivity extends AppCompatActivity {

    private Flight flight, returnFlight;
    private final Gson gson = new Gson();
    private double baseFarePrice, returnBasePrice;
    private double taxesAndFees = 450000, returnTaxesAndFees = 450000;
    private String fareType, returnFareType;
    private int adults, children;
    private TextView txtTotalPrice;

    private boolean isRoundTrip = false;

    private static final int REQUEST_CODE_SEAT = 100;
    private static final int REQUEST_CODE_BAGGAGE = 101;
    private static final int REQUEST_CODE_SEAT_RETURN = 200;
    private static final int REQUEST_CODE_BAGGAGE_RETURN = 201;

    // Quản lý dữ liệu theo từng hành khách (Lượt đi)
    private final List<String> selectedSeats = new ArrayList<>();
    private final List<Integer> baggage10s = new ArrayList<>();
    private final List<Integer> baggage23s = new ArrayList<>();
    
    // Quản lý dữ liệu theo từng hành khách (Lượt về)
    private final List<String> returnSelectedSeats = new ArrayList<>();
    private final List<Integer> returnB10s = new ArrayList<>();
    private final List<Integer> returnB23s = new ArrayList<>();

    private int currentTargetPassengerIndex = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_service);

        Intent intent = getIntent();
        isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);
        adults = intent.getIntExtra("adults", 1);
        children = intent.getIntExtra("children", 0);

        String json = intent.getStringExtra("flight_json");
        if (json != null) flight = gson.fromJson(json, Flight.class);
        baseFarePrice = intent.getDoubleExtra("totalPrice", 0);
        fareType = intent.getStringExtra("fareType");

        if (isRoundTrip) {
            String retJson = intent.getStringExtra("return_flight_json");
            if (retJson != null) returnFlight = gson.fromJson(retJson, Flight.class);
            returnBasePrice = intent.getDoubleExtra("returnTotalPrice", 0);
            returnFareType = intent.getStringExtra("returnFareType");
        }

        initPassengerData();
        initViews();
        updateUI();
    }

    private void initPassengerData() {
        int totalPax = adults + children; // Trẻ em cũng có ghế và hành lý
        for (int i = 0; i < totalPax; i++) {
            selectedSeats.add("");
            baggage10s.add(0);
            baggage23s.add(0);
            if (isRoundTrip) {
                returnSelectedSeats.add("");
                returnB10s.add(0);
                returnB23s.add(0);
            }
        }
    }

    private void initViews() {
        txtTotalPrice = findViewById(R.id.txtTotalPrice);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnContinue).setOnClickListener(v -> goNext());
        
        setupPassengerCards();
    }

    private void setupPassengerCards() {
        int totalPax = adults + children;
        boolean isSinglePaxOneWay = (totalPax == 1 && !isRoundTrip);

        // UI 1: Danh sách cũ cho 1 người 1 chiều
        findViewById(R.id.layoutSinglePaxOneWay).setVisibility(isSinglePaxOneWay ? View.VISIBLE : View.GONE);
        if (isSinglePaxOneWay) {
            setupSinglePaxOneWayEvents();
            findViewById(R.id.layoutMultiPaxOrRoundTrip).setVisibility(View.GONE);
            return;
        }

        // UI 2: Dạng thẻ cho nhiều người hoặc khứ hồi
        findViewById(R.id.layoutMultiPaxOrRoundTrip).setVisibility(View.VISIBLE);
        LinearLayout containerOut = findViewById(R.id.containerOutbound);
        containerOut.removeAllViews();
        for (int i = 0; i < totalPax; i++) {
            containerOut.addView(createPassengerCard(i, false, containerOut));
        }

        if (isRoundTrip) {
            findViewById(R.id.layoutReturnSection).setVisibility(View.VISIBLE);
            LinearLayout containerRet = findViewById(R.id.containerReturn);
            containerRet.removeAllViews();
            for (int i = 0; i < totalPax; i++) {
                containerRet.addView(createPassengerCard(i, true, containerRet));
            }
        } else {
            findViewById(R.id.layoutReturnSection).setVisibility(View.GONE);
        }
    }

    private void setupSinglePaxOneWayEvents() {
        TextView tvSeat = findViewById(R.id.tvSelectSeatSingle);
        TextView tvBag = findViewById(R.id.tvSelectBaggageSingle);

        // Hiển thị dữ liệu hiện tại
        String seatVal = selectedSeats.get(0);
        tvSeat.setText(seatVal.isEmpty() ? "Chọn" : seatVal);
        if (!seatVal.isEmpty()) {
            tvSeat.setTextColor(ContextCompat.getColor(this, "Business".equalsIgnoreCase(fareType) ? R.color.skyline_teal : R.color.skyline_blue));
        }

        int totalKg = baggage10s.get(0) * 10 + baggage23s.get(0) * 23;
        tvBag.setText(totalKg > 0 ? "+" + totalKg + "KG" : "Chọn");

        findViewById(R.id.itemSeatSingle).setOnClickListener(v -> {
            currentTargetPassengerIndex = 0;
            Intent intent = new Intent(this, SeatSelectionActivity.class);
            intent.putExtra("flight_json", gson.toJson(flight));
            intent.putExtra("initialSeat", selectedSeats.get(0));
            intent.putExtra("fareType", fareType);
            intent.putExtra("isRoundTrip", false);
            startActivityForResult(intent, REQUEST_CODE_SEAT);
        });

        findViewById(R.id.itemBaggageSingle).setOnClickListener(v -> {
            currentTargetPassengerIndex = 0;
            Intent intent = new Intent(this, BaggageSelectionActivity.class);
            intent.putExtra("flight_json", gson.toJson(flight));
            intent.putExtra("initialB10", baggage10s.get(0));
            intent.putExtra("initialB23", baggage23s.get(0));
            intent.putExtra("fareType", fareType);
            intent.putExtra("isRoundTrip", false);
            startActivityForResult(intent, REQUEST_CODE_BAGGAGE);
        });
    }

    private View createPassengerCard(int index, boolean isReturn, ViewGroup parent) {
        View card = LayoutInflater.from(this).inflate(R.layout.item_passenger_addon, parent, false);
        TextView tvName = card.findViewById(R.id.tvPassengerName);
        TextView tvSeat = card.findViewById(R.id.tvSelectedSeat);
        TextView tvBaggage = card.findViewById(R.id.tvSelectedBaggage);
        
        // Logic hiển thị số thứ tự
        int totalPax = adults + children;
        if (totalPax > 1) {
            tvName.setText("Hành khách " + (index + 1));
        } else {
            tvName.setText("Hành khách");
        }

        // Cập nhật dữ liệu hiện tại
        String seatVal = isReturn ? returnSelectedSeats.get(index) : selectedSeats.get(index);
        tvSeat.setText(seatVal.isEmpty() ? "Chọn" : seatVal);
        if (!seatVal.isEmpty()) {
            int color = (isReturn ? "Business".equalsIgnoreCase(returnFareType) : "Business".equalsIgnoreCase(fareType))
                    ? ContextCompat.getColor(this, R.color.skyline_teal) 
                    : ContextCompat.getColor(this, R.color.skyline_blue);
            tvSeat.setTextColor(color);
        }

        int b10 = isReturn ? returnB10s.get(index) : baggage10s.get(index);
        int b23 = isReturn ? returnB23s.get(index) : baggage23s.get(index);
        int totalKg = b10 * 10 + b23 * 23;
        tvBaggage.setText(totalKg > 0 ? "+" + totalKg + "KG" : "Chọn");

        // Event click
        card.findViewById(R.id.btnSelectSeat).setOnClickListener(v -> {
            currentTargetPassengerIndex = index;
            Intent intent = new Intent(this, SeatSelectionActivity.class);
            intent.putExtra("flight_json", gson.toJson(isReturn ? returnFlight : flight));
            intent.putExtra("initialSeat", isReturn ? returnSelectedSeats.get(index) : selectedSeats.get(index));
            intent.putExtra("fareType", isReturn ? returnFareType : fareType);
            intent.putExtra("isRoundTrip", isRoundTrip);
            startActivityForResult(intent, isReturn ? REQUEST_CODE_SEAT_RETURN : REQUEST_CODE_SEAT);
        });

        card.findViewById(R.id.btnSelectBaggage).setOnClickListener(v -> {
            currentTargetPassengerIndex = index;
            Intent intent = new Intent(this, BaggageSelectionActivity.class);
            intent.putExtra("flight_json", gson.toJson(isReturn ? returnFlight : flight));
            intent.putExtra("initialB10", isReturn ? returnB10s.get(index) : baggage10s.get(index));
            intent.putExtra("initialB23", isReturn ? returnB23s.get(index) : baggage23s.get(index));
            intent.putExtra("fareType", isReturn ? returnFareType : fareType);
            intent.putExtra("isRoundTrip", isRoundTrip);
            startActivityForResult(intent, isReturn ? REQUEST_CODE_BAGGAGE_RETURN : REQUEST_CODE_BAGGAGE);
        });

        return card;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null && currentTargetPassengerIndex != -1) {
            int idx = currentTargetPassengerIndex;
            if (requestCode == REQUEST_CODE_SEAT) {
                selectedSeats.set(idx, data.getStringExtra("selectedSeat"));
            } else if (requestCode == REQUEST_CODE_BAGGAGE) {
                baggage10s.set(idx, data.getIntExtra("baggage10", 0));
                baggage23s.set(idx, data.getIntExtra("baggage23", 0));
            } else if (requestCode == REQUEST_CODE_SEAT_RETURN) {
                returnSelectedSeats.set(idx, data.getStringExtra("selectedSeat"));
            } else if (requestCode == REQUEST_CODE_BAGGAGE_RETURN) {
                returnB10s.set(idx, data.getIntExtra("baggage10", 0));
                returnB23s.set(idx, data.getIntExtra("baggage23", 0));
            }
            setupPassengerCards(); // Refresh UI
            updateTotalPrice();
        }
    }

    private void updateUI() {
        updateFlightInfo();
        startPlaneAnimation();
        updateTotalPrice();
        
        TextView tvLabelOut = findViewById(R.id.tvLabelOutbound);
        if (tvLabelOut != null) {
            if (isRoundTrip) {
                tvLabelOut.setText("LƯỢT ĐI");
                tvLabelOut.setVisibility(View.VISIBLE);
            } else {
                tvLabelOut.setVisibility(View.GONE);
            }
        }
        
        TextView tvLabelRet = findViewById(R.id.tvLabelReturn);
        if (tvLabelRet != null) {
            tvLabelRet.setText("LƯỢT VỀ");
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

        ImageView imgPlaneRet = findViewById(R.id.imgPlaneReturn);
        if (imgPlaneRet != null && isRoundTrip) {
            ObjectAnimator animatorRet = ObjectAnimator.ofFloat(imgPlaneRet, "translationY", -15f, 15f);
            animatorRet.setDuration(2000);
            animatorRet.setRepeatMode(ValueAnimator.REVERSE);
            animatorRet.setRepeatCount(ValueAnimator.INFINITE);
            animatorRet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorRet.start();
        }
    }

    private void updateFlightInfo() {
        setTextSafe(R.id.tvFlightNumberHeader, isRoundTrip ? "CHUYẾN BAY KHỨ HỒI" : "CHUYẾN BAY MỘT CHIỀU");
        setTextSafe(R.id.tvDepCode, flight.getDepartureAirport().getCode());
        setTextSafe(R.id.tvArrCode, flight.getArrivalAirport().getCode());
        setTextSafe(R.id.tvDepAirport, cleanAirportName(flight.getDepartureAirport().getName()));
        setTextSafe(R.id.tvArrAirport, cleanAirportName(flight.getArrivalAirport().getName()));
        setTextSafe(R.id.tvFlightNumberOut, flight.getFlightNumber());

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.US);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN"));
        timeFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
        dateFormat.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));

        Date dDate = parseIsoDate(flight.getDepartureAt());
        Date aDate = parseIsoDate(flight.getArrivalAt());
        if (dDate != null) { setTextSafe(R.id.tvDepTime, timeFormat.format(dDate)); setTextSafe(R.id.tvDepDate, dateFormat.format(dDate)); }
        if (aDate != null) { setTextSafe(R.id.tvArrTime, timeFormat.format(aDate)); setTextSafe(R.id.tvArrDate, dateFormat.format(aDate)); }
        
        int durationMinutes = flight.getDuration();
        if (durationMinutes > 0) setTextSafe(R.id.tvDuration, (durationMinutes/60) + "g " + (durationMinutes%60) + "p");

        if (isRoundTrip && returnFlight != null) {
            findViewById(R.id.cardFlightReturn).setVisibility(View.VISIBLE);
            setTextSafe(R.id.tvDepCodeReturn, returnFlight.getDepartureAirport().getCode());
            setTextSafe(R.id.tvArrCodeReturn, returnFlight.getArrivalAirport().getCode());
            setTextSafe(R.id.tvDepAirportReturn, cleanAirportName(returnFlight.getDepartureAirport().getName()));
            setTextSafe(R.id.tvArrAirportReturn, cleanAirportName(returnFlight.getArrivalAirport().getName()));
            setTextSafe(R.id.tvFlightNumberRet, returnFlight.getFlightNumber());

            Date dDateR = parseIsoDate(returnFlight.getDepartureAt());
            Date aDateR = parseIsoDate(returnFlight.getArrivalAt());
            if (dDateR != null) { setTextSafe(R.id.tvDepTimeReturn, timeFormat.format(dDateR)); setTextSafe(R.id.tvDepDateReturn, dateFormat.format(dDateR)); }
            if (aDateR != null) { setTextSafe(R.id.tvArrTimeReturn, timeFormat.format(aDateR)); setTextSafe(R.id.tvArrDateReturn, dateFormat.format(aDateR)); }
            if (returnFlight.getDuration() > 0) setTextSafe(R.id.tvDurationReturn, (returnFlight.getDuration()/60) + "g " + (returnFlight.getDuration()%60) + "p");
        }
    }

    private void setTextSafe(int viewId, String text) {
        TextView textView = findViewById(viewId);
        if (textView != null && text != null) textView.setText(text);
    }

    private Date parseIsoDate(String iso) {
        if (iso == null) return null;
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
        for (String p : patterns) { try { SimpleDateFormat f = new SimpleDateFormat(p, Locale.US); f.setTimeZone(java.util.TimeZone.getTimeZone("UTC")); return f.parse(iso); } catch (Exception ignored) {} }
        return null;
    }

    private String cleanAirportName(String name) {
        if (name == null) return "";
        return name.replaceAll("(?i)(SÂN BAY QUỐC TẾ |SÂN BAY |AIRPORT )", "").trim().toUpperCase();
    }

    private void updateTotalPrice() {
        int totalPax = adults + children;
        double totalBaggage = 0;
        for (int i = 0; i < baggage10s.size(); i++) {
            totalBaggage += baggage10s.get(i) * 200000 + baggage23s.get(i) * 450000;
            if (isRoundTrip) totalBaggage += returnB10s.get(i) * 200000 + returnB23s.get(i) * 450000;
        }

        double vatOut = (baseFarePrice * totalPax) * 0.10;
        double totalOut = (baseFarePrice * totalPax) + vatOut + (450000 * totalPax);
        
        double totalIn = 0;
        double roundTripDiscount = 0;
        if (isRoundTrip) {
            double vatIn = (returnBasePrice * totalPax) * 0.10;
            totalIn = (returnBasePrice * totalPax) + vatIn + (450000 * totalPax);
            roundTripDiscount = ((baseFarePrice + returnBasePrice) * totalPax) * 0.05;
        }
        
        txtTotalPrice.setText(new DecimalFormat("#,###").format(totalOut + totalIn + totalBaggage - roundTripDiscount) + " VND");
    }

    private void goNext() {
        Intent intent = new Intent(this, BookingConfirmationActivity.class);
        intent.putExtra("isRoundTrip", isRoundTrip);
        intent.putExtra("adults", adults);
        intent.putExtra("children", children);
        intent.putExtra("flight_json", gson.toJson(flight));
        intent.putExtra("totalPrice", baseFarePrice);
        intent.putExtra("fareType", fareType);
        
        // Pass arrays/lists
        intent.putStringArrayListExtra("selectedSeats", new ArrayList<>(selectedSeats));
        intent.putIntegerArrayListExtra("baggage10s", new ArrayList<>(baggage10s));
        intent.putIntegerArrayListExtra("baggage23s", new ArrayList<>(baggage23s));

        if (isRoundTrip) {
            intent.putExtra("return_flight_json", gson.toJson(returnFlight));
            intent.putExtra("returnTotalPrice", returnBasePrice);
            intent.putExtra("returnFareType", returnFareType);
            intent.putStringArrayListExtra("returnSelectedSeats", new ArrayList<>(returnSelectedSeats));
            intent.putIntegerArrayListExtra("returnB10s", new ArrayList<>(returnB10s));
            intent.putIntegerArrayListExtra("returnB23s", new ArrayList<>(returnB23s));
        }

        startActivity(intent);
    }
}
