package com.skyline.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.gson.Gson;
import com.skyline.app.network.Flight;
import com.skyline.app.network.FlightSeat;
import com.skyline.app.network.RetrofitClient;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SeatSelectionActivity extends AppCompatActivity {

    private Flight flight;
    private final Gson gson = new Gson();
    private LinearLayout seatMap;
    private TextView txtSelectedSeat;
    private String selectedSeat = "";
    private TextView currentSelectedView = null;
    private int maxRowNumber = 0;
    private String fareType = "";
    private int themeColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seat_selection);

        initData();
        initViews();
        updateFlightInfo();
        loadSeatsFromApi();
        startPlaneAnimation();
    }

    private void initData() {
        Intent intent = getIntent();
        String json = intent.getStringExtra("flight_json");
        if (json != null) flight = gson.fromJson(json, Flight.class);
        
        String initialSeat = intent.getStringExtra("initialSeat");
        selectedSeat = (initialSeat != null) ? initialSeat : "";
        fareType = intent.getStringExtra("fareType");

        if ("Business".equalsIgnoreCase(fareType)) {
            themeColor = ContextCompat.getColor(this, R.color.skyline_teal);
        } else {
            themeColor = ContextCompat.getColor(this, R.color.skyline_blue);
        }
    }

    private void initViews() {
        seatMap = findViewById(R.id.seatMap);
        txtSelectedSeat = findViewById(R.id.txtSelectedSeat);
        if (!selectedSeat.isEmpty()) {
            txtSelectedSeat.setText(selectedSeat);
        }

        // Thống nhất hình thức trình bày cho Legend (Pill shape)
        setupLegendItem(R.id.legendEmpty, Color.parseColor("#26FFFFFF"));
        setupLegendItem(R.id.legendOccupied, Color.parseColor("#26FFFFFF"));
        setupLegendItem(R.id.legendSelected, themeColor);

        findViewById(R.id.btnClose).setOnClickListener(v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra("selectedSeat", ""); 
            setResult(RESULT_OK, resultIntent);
            finish();
        });

        findViewById(R.id.btnContinue).setOnClickListener(v -> {
            if (selectedSeat.isEmpty()) {
                Toast.makeText(this, "Vui lòng chọn ghế", Toast.LENGTH_SHORT).show();
            } else {
                Intent resultIntent = new Intent();
                resultIntent.putExtra("selectedSeat", selectedSeat);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void setupLegendItem(int viewId, int color) {
        View view = findViewById(viewId);
        if (view != null) {
            GradientDrawable gd = new GradientDrawable();
            gd.setColor(color);
            gd.setCornerRadius(dp(25)); // Đảm bảo bo tròn hoàn toàn kiểu Pill
            view.setBackground(gd);
        }
    }

    private void loadSeatsFromApi() {
        if (flight == null) return;
        RetrofitClient.getInstance().getFlightSeats(flight.getId()).enqueue(new Callback<List<FlightSeat>>() {
            @Override
            public void onResponse(Call<List<FlightSeat>> call, Response<List<FlightSeat>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    List<FlightSeat> filteredSeats = new ArrayList<>();
                    for (FlightSeat s : response.body()) {
                        if (fareType != null && fareType.equalsIgnoreCase(s.getCabinClass())) {
                            filteredSeats.add(s);
                        }
                    }
                    if (filteredSeats.isEmpty()) {
                        Toast.makeText(SeatSelectionActivity.this, "Không có ghế phù hợp", Toast.LENGTH_LONG).show();
                    } else {
                        renderSeatMap(filteredSeats);
                    }
                } else {
                    Toast.makeText(SeatSelectionActivity.this, "Không thể tải sơ đồ ghế", Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onFailure(Call<List<FlightSeat>> call, Throwable t) {
                Toast.makeText(SeatSelectionActivity.this, "Lỗi kết nối máy chủ", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void renderSeatMap(List<FlightSeat> seats) {
        seatMap.removeAllViews();
        int minRow = 100;
        maxRowNumber = 0;
        for(FlightSeat s : seats) {
            if(s.getRowNumber() < minRow) minRow = s.getRowNumber();
            if(s.getRowNumber() > maxRowNumber) maxRowNumber = s.getRowNumber();
        }
        for (int row = minRow; row <= maxRowNumber; row++) {
            addRowToLayout(row, seats);
        }
    }

    private void addRowToLayout(int row, List<FlightSeat> apiSeats) {
        LinearLayout rowLayout = new LinearLayout(this);
        rowLayout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
        rowLayout.setOrientation(LinearLayout.HORIZONTAL);
        rowLayout.setGravity(Gravity.CENTER);
        rowLayout.setPadding(0, dp(6), 0, dp(6));

        String[] leftCols, rightCols;
        if ("Business".equalsIgnoreCase(fareType)) {
            leftCols = new String[]{"A", "C"};
            rightCols = new String[]{"D", "F"};
        } else {
            leftCols = new String[]{"A", "B", "C"};
            rightCols = new String[]{"D", "E", "F"};
        }

        for (String col : leftCols) rowLayout.addView(createSeatView(row, col, apiSeats));

        TextView rowNumber = new TextView(this);
        rowNumber.setLayoutParams(new LinearLayout.LayoutParams(dp(34), dp(44)));
        rowNumber.setGravity(Gravity.CENTER);
        rowNumber.setText(String.valueOf(row));
        rowNumber.setTextColor(Color.parseColor("#73777F"));
        rowNumber.setTextSize(12);
        rowNumber.setTypeface(null, Typeface.BOLD);
        rowLayout.addView(rowNumber);

        for (String col : rightCols) rowLayout.addView(createSeatView(row, col, apiSeats));
        seatMap.addView(rowLayout);
    }

    private View createSeatView(int row, String col, List<FlightSeat> apiSeats) {
        TextView seat = new TextView(this);
        seat.setLayoutParams(new LinearLayout.LayoutParams(0, dp(44), 1f));
        ((LinearLayout.LayoutParams)seat.getLayoutParams()).setMargins(dp(4), 0, dp(4), 0);
        seat.setGravity(Gravity.CENTER);
        seat.setTextSize(11);
        seat.setTypeface(null, Typeface.BOLD);

        FlightSeat found = null;
        for (FlightSeat s : apiSeats) {
            if (s.getRowNumber() == row && s.getSeatColumn().equalsIgnoreCase(col)) {
                found = s; break;
            }
        }

        if (found == null) {
            seat.setVisibility(View.INVISIBLE);
            return seat;
        }

        final String seatCode = found.getSeatNumber();
        final String cabinClass = found.getCabinClass();
        seat.setText(seatCode);

        if ("OCCUPIED".equalsIgnoreCase(found.getSeatStatus()) || "BOOKED".equalsIgnoreCase(found.getSeatStatus())) {
            seat.setBackground(createSeatDrawable(Color.parseColor("#E0E3E5"), Color.parseColor("#E0E3E5")));
            seat.setTextColor(Color.parseColor("#9CA3AF"));
            seat.setEnabled(false);
        } else {
            seat.setBackground(createSeatDrawable(Color.WHITE, ContextCompat.getColor(this, R.color.skyline_blue_dark)));
            seat.setTextColor(ContextCompat.getColor(this, R.color.skyline_blue_dark));
            
            seat.setOnClickListener(v -> {
                showSeatDescription(row, col, cabinClass);
                selectSeat(seat, seatCode);
            });
            
            if (seatCode.equals(selectedSeat)) selectSeat(seat, seatCode);
        }
        return seat;
    }

    private void showSeatDescription(int row, String letter, String cabinClass) {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_seat_info, null);
        TextView tvCode = dialogView.findViewById(R.id.tvDialogSeatCode);
        TextView tvDesc = dialogView.findViewById(R.id.tvDialogDesc);
        
        tvCode.setText("Ghế " + row + letter);
        StringBuilder desc = new StringBuilder();
        
        if ("BUSINESS".equalsIgnoreCase(cabinClass)) {
            desc.append("• Hạng Thương gia cao cấp\n• Không gian riêng tư tuyệt đối\n");
        } else {
            if (row == 11) desc.append("• Hàng ghế lối thoát hiểm\n• Chỗ để chân siêu rộng rãi\n");
            else desc.append("• Hạng Phổ thông tiêu chuẩn\n");
        }
        
        if (letter.equalsIgnoreCase("A") || letter.equalsIgnoreCase("F")) desc.append("• Ghế cạnh cửa sổ ngắm cảnh đẹp");
        else if (letter.equalsIgnoreCase("C") || letter.equalsIgnoreCase("D")) desc.append("• Ghế cạnh lối đi, dễ di chuyển");
        else desc.append("• Ghế ở giữa, chỗ ngồi êm ái");

        if (maxRowNumber > 0 && row >= maxRowNumber - 2) desc.append("\n• Gần khu vực nhà vệ sinh & Galley");

        tvDesc.setText(desc.toString());

        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialogTheme).setView(dialogView).setCancelable(true).create();
        dialog.show();

        Window window = dialog.getWindow();
        if (window != null) {
            WindowManager.LayoutParams lp = new WindowManager.LayoutParams();
            lp.copyFrom(window.getAttributes());
            lp.width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
            window.setAttributes(lp);
        }
    }

    private void selectSeat(TextView seatView, String seatCode) {
        if (currentSelectedView != null) {
            currentSelectedView.setBackground(createSeatDrawable(Color.WHITE, ContextCompat.getColor(this, R.color.skyline_blue_dark)));
            currentSelectedView.setTextColor(ContextCompat.getColor(this, R.color.skyline_blue_dark));
        }
        currentSelectedView = seatView;
        selectedSeat = seatCode;
        seatView.setBackground(createSeatDrawable(themeColor, Color.parseColor("#AAC9F3")));
        seatView.setTextColor(Color.WHITE);
        txtSelectedSeat.setText(seatCode);
    }

    private GradientDrawable createSeatDrawable(int solidColor, int strokeColor) {
        GradientDrawable gd = new GradientDrawable();
        gd.setColor(solidColor);
        gd.setCornerRadius(dp(8));
        gd.setStroke(dp(2), strokeColor);
        return gd;
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
        if (flight.getDuration() > 0) setTextSafe(R.id.tvDuration, (flight.getDuration()/60) + "g " + (flight.getDuration()%60) + "p");
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

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density);
    }
}