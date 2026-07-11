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

    private Flight flight, returnFlight;
    private final Gson gson = new Gson();
    private double baseFarePrice, returnBasePrice;
    private double addonPrice = 0, returnAddonPrice = 0;
    private double seatPrice = 0, returnSeatPrice = 0;
    private double taxesAndFees = 450000, returnTaxesAndFees = 450000;
    private String fareType, returnFareType;
    private TextView txtTotalPrice;

    private boolean isRoundTrip = false;

    private static final int REQUEST_CODE_SEAT = 100;
    private static final int REQUEST_CODE_BAGGAGE = 101;
    private static final int REQUEST_CODE_SEAT_RETURN = 200;
    private static final int REQUEST_CODE_BAGGAGE_RETURN = 201;

    private String selectedSeat = "", returnSelectedSeat = "";
    private int baggage10 = 0, baggage23 = 0, returnB10 = 0, returnB23 = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addon_service);

        Intent intent = getIntent();
        isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);

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

        findViewById(R.id.btnContinue).setOnClickListener(v -> goNext());
        
        // --- LƯỢT ĐI ---
        findViewById(R.id.itemSeat).setOnClickListener(v -> {
            Intent intent = new Intent(this, SeatSelectionActivity.class);
            intent.putExtra("flight_json", gson.toJson(flight));
            intent.putExtra("initialSeat", selectedSeat);
            intent.putExtra("fareType", fareType);
            intent.putExtra("isRoundTrip", isRoundTrip);
            startActivityForResult(intent, REQUEST_CODE_SEAT);
        });

        findViewById(R.id.itemBaggage).setOnClickListener(v -> {
            Intent intent = new Intent(this, BaggageSelectionActivity.class);
            intent.putExtra("flight_json", gson.toJson(flight));
            intent.putExtra("initialB10", baggage10);
            intent.putExtra("initialB23", baggage23);
            intent.putExtra("fareType", fareType);
            intent.putExtra("isRoundTrip", isRoundTrip);
            startActivityForResult(intent, REQUEST_CODE_BAGGAGE);
        });

        // --- LƯỢT VỀ ---
        View returnSection = findViewById(R.id.returnAddonSection);
        if (isRoundTrip && returnSection != null) {
            returnSection.setVisibility(View.VISIBLE);
            findViewById(R.id.itemSeatReturn).setOnClickListener(v -> {
                Intent intent = new Intent(this, SeatSelectionActivity.class);
                intent.putExtra("flight_json", gson.toJson(returnFlight));
                intent.putExtra("initialSeat", returnSelectedSeat);
                intent.putExtra("fareType", returnFareType);
                intent.putExtra("isRoundTrip", isRoundTrip);
                startActivityForResult(intent, REQUEST_CODE_SEAT_RETURN);
            });

            findViewById(R.id.itemBaggageReturn).setOnClickListener(v -> {
                Intent intent = new Intent(this, BaggageSelectionActivity.class);
                intent.putExtra("flight_json", gson.toJson(returnFlight));
                intent.putExtra("initialB10", returnB10);
                intent.putExtra("initialB23", returnB23);
                intent.putExtra("fareType", returnFareType);
                intent.putExtra("isRoundTrip", isRoundTrip);
                startActivityForResult(intent, REQUEST_CODE_BAGGAGE_RETURN);
            });
        } else if (returnSection != null) {
            returnSection.setVisibility(View.GONE);
        }

        findViewById(R.id.btnContinue).setOnClickListener(v -> goNext());
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK && data != null) {
            if (requestCode == REQUEST_CODE_SEAT) {
                selectedSeat = data.getStringExtra("selectedSeat");
                TextView tv = findViewById(R.id.tvSelectSeat);
                if (tv != null) {
                    boolean isSelected = selectedSeat != null && !selectedSeat.isEmpty();
                    tv.setText(isSelected ? selectedSeat : "Chọn");
                    
                    // Cập nhật màu sắc theo hạng vé
                    if (isSelected) {
                        int color = "Business".equalsIgnoreCase(fareType) 
                                ? ContextCompat.getColor(this, R.color.skyline_teal) 
                                : ContextCompat.getColor(this, R.color.skyline_blue);
                        tv.setTextColor(color);
                    } else {
                        tv.setTextColor(ContextCompat.getColor(this, R.color.skyline_blue));
                    }
                }
                seatPrice = 0; 
            } else if (requestCode == REQUEST_CODE_BAGGAGE) {
                baggage10 = data.getIntExtra("baggage10", 0);
                baggage23 = data.getIntExtra("baggage23", 0);
                addonPrice = data.getDoubleExtra("baggagePrice", 0);
                TextView tv = findViewById(R.id.tvSelectBaggage);
                if (tv != null) tv.setText((baggage10 > 0 || baggage23 > 0) ? "+" + (baggage10 * 10 + baggage23 * 23) + "KG" : "Chọn");
            } else if (requestCode == REQUEST_CODE_SEAT_RETURN) {
                returnSelectedSeat = data.getStringExtra("selectedSeat");
                TextView tv = findViewById(R.id.tvSelectSeatReturn);
                if (tv != null) {
                    boolean isSelected = returnSelectedSeat != null && !returnSelectedSeat.isEmpty();
                    tv.setText(isSelected ? returnSelectedSeat : "Chọn");

                    // Cập nhật màu sắc theo hạng vé cho lượt về
                    if (isSelected) {
                        int color = "Business".equalsIgnoreCase(returnFareType) 
                                ? ContextCompat.getColor(this, R.color.skyline_teal) 
                                : ContextCompat.getColor(this, R.color.skyline_blue);
                        tv.setTextColor(color);
                    } else {
                        tv.setTextColor(ContextCompat.getColor(this, R.color.skyline_blue));
                    }
                }
                returnSeatPrice = 0;
            } else if (requestCode == REQUEST_CODE_BAGGAGE_RETURN) {
                returnB10 = data.getIntExtra("baggage10", 0);
                returnB23 = data.getIntExtra("baggage23", 0);
                returnAddonPrice = data.getDoubleExtra("baggagePrice", 0);
                TextView tv = findViewById(R.id.tvSelectBaggageReturn);
                if (tv != null) tv.setText((returnB10 > 0 || returnB23 > 0) ? "+" + (returnB10 * 10 + returnB23 * 23) + "KG" : "Chọn");
            }
            updateTotalPrice();
        }
    }

    private void updateUI() {
        updateFlightInfo();
        startPlaneAnimation();
        updateTotalPrice();
        
        TextView tvLabelOut = findViewById(R.id.tvLabelOutbound);
        if (tvLabelOut != null) {
            tvLabelOut.setVisibility(isRoundTrip ? View.VISIBLE : View.GONE);
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
        
        // Lượt đi
        setTextSafe(R.id.tvDepCode, flight.getDepartureAirport().getCode());
        setTextSafe(R.id.tvArrCode, flight.getArrivalAirport().getCode());
        setTextSafe(R.id.tvDepAirport, cleanAirportName(flight.getDepartureAirport().getName()));
        setTextSafe(R.id.tvArrAirport, cleanAirportName(flight.getArrivalAirport().getName()));
        setTextSafe(R.id.tvFlightNumberOut, flight.getFlightNumber());

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
            setTextSafe(R.id.tvDuration, h + "g " + m + "p");
        }

        // Lượt về
        if (isRoundTrip && returnFlight != null) {
            View cardRet = findViewById(R.id.cardFlightReturn);
            if (cardRet != null) cardRet.setVisibility(View.VISIBLE);

            setTextSafe(R.id.tvDepCodeReturn, returnFlight.getDepartureAirport().getCode());
            setTextSafe(R.id.tvArrCodeReturn, returnFlight.getArrivalAirport().getCode());
            setTextSafe(R.id.tvDepAirportReturn, cleanAirportName(returnFlight.getDepartureAirport().getName()));
            setTextSafe(R.id.tvArrAirportReturn, cleanAirportName(returnFlight.getArrivalAirport().getName()));
            setTextSafe(R.id.tvFlightNumberRet, returnFlight.getFlightNumber());

            Date dDateR = parseIsoDate(returnFlight.getDepartureAt());
            Date aDateR = parseIsoDate(returnFlight.getArrivalAt());

            if (dDateR != null) {
                setTextSafe(R.id.tvDepTimeReturn, timeFormat.format(dDateR));
                setTextSafe(R.id.tvDepDateReturn, dateFormat.format(dDateR));
            }
            if (aDateR != null) {
                setTextSafe(R.id.tvArrTimeReturn, timeFormat.format(aDateR));
                setTextSafe(R.id.tvArrDateReturn, dateFormat.format(aDateR));
            }
            
            int durR = returnFlight.getDuration();
            if (durR > 0) {
                setTextSafe(R.id.tvDurationReturn, (durR/60) + "g " + (durR%60) + "p");
            }
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
            // Lượt đi
            double vatOut = baseFarePrice * 0.10;
            double totalOut = baseFarePrice + vatOut + taxesAndFees + addonPrice + seatPrice;
            
            // Lượt về (nếu có)
            double totalIn = 0;
            double roundTripDiscount = 0;
            if (isRoundTrip) {
                double vatIn = returnBasePrice * 0.10;
                totalIn = returnBasePrice + vatIn + returnTaxesAndFees + returnAddonPrice + returnSeatPrice;
                // Áp dụng ưu đãi khứ hồi 5% trên tổng giá vé cơ bản
                roundTripDiscount = (baseFarePrice + returnBasePrice) * 0.05;
            }
            
            txtTotalPrice.setText(df.format(totalOut + totalIn - roundTripDiscount) + " VND");
        }
    }

    private void goNext() {
        Intent intent = new Intent(this, BookingConfirmationActivity.class);
        intent.putExtra("isRoundTrip", isRoundTrip);
        
        // Lượt đi
        intent.putExtra("flight_json", gson.toJson(flight));
        intent.putExtra("totalPrice", baseFarePrice);
        intent.putExtra("fareType", fareType);
        intent.putExtra("selectedSeat", selectedSeat);
        intent.putExtra("addonPrice", addonPrice);
        intent.putExtra("seatPrice", seatPrice);
        intent.putExtra("taxes", taxesAndFees);
        
        // Lượt về
        if (isRoundTrip) {
            intent.putExtra("return_flight_json", gson.toJson(returnFlight));
            intent.putExtra("returnTotalPrice", returnBasePrice);
            intent.putExtra("returnFareType", returnFareType);
            intent.putExtra("returnSelectedSeat", returnSelectedSeat);
            intent.putExtra("returnAddonPrice", returnAddonPrice);
            intent.putExtra("returnSeatPrice", returnSeatPrice);
            intent.putExtra("returnTaxes", returnTaxesAndFees);
        }

        startActivity(intent);
    }
}