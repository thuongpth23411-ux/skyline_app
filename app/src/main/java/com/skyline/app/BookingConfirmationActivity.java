package com.skyline.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.gson.Gson;
import com.skyline.app.databinding.ActivityBookingConfirmationBinding;
import com.skyline.app.network.Flight;
import com.skyline.app.network.FlightSeat;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.network.User;
import com.skyline.app.utils.SessionManager;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.TimeZone;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class BookingConfirmationActivity extends AppCompatActivity {

    private ActivityBookingConfirmationBinding binding;
    private Flight flight, returnFlight;
    private final Gson gson = new Gson();
    private double baseFarePrice, addonPrice, seatPrice, taxes;
    private double returnBasePrice, returnAddonPrice, returnSeatPrice, returnTaxes;
    private String selectedSeat, fareType, returnSelectedSeat, returnFareType;
    private boolean isRoundTrip = false;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityBookingConfirmationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        sessionManager = new SessionManager(this);

        initData();
        initViews();
        updateUI();
        loadUserData();
        assignRandomSeat();
        startPlaneAnimation();
    }

    private void startPlaneAnimation() {
        // Hiệu ứng máy bay lượt đi
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.imgPlane, "translationY", -12f, 12f);
        animator.setDuration(2000);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();

        // Hiệu ứng máy bay lượt về nếu là khứ hồi
        if (isRoundTrip && binding.imgPlaneReturn != null) {
            ObjectAnimator animatorRet = ObjectAnimator.ofFloat(binding.imgPlaneReturn, "translationY", -12f, 12f);
            animatorRet.setDuration(2000);
            animatorRet.setRepeatMode(ValueAnimator.REVERSE);
            animatorRet.setRepeatCount(ValueAnimator.INFINITE);
            animatorRet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorRet.start();
        }
    }

    private void initData() {
        Intent intent = getIntent();
        isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);

        // Lượt đi
        String json = intent.getStringExtra("flight_json");
        if (json != null) flight = gson.fromJson(json, Flight.class);
        baseFarePrice = intent.getDoubleExtra("totalPrice", 0);
        fareType = intent.getStringExtra("fareType");
        selectedSeat = intent.getStringExtra("selectedSeat");
        addonPrice = intent.getDoubleExtra("addonPrice", 0);
        seatPrice = intent.getDoubleExtra("seatPrice", 0);
        taxes = intent.getDoubleExtra("taxes", 450000);

        // Lượt về
        if (isRoundTrip) {
            String retJson = intent.getStringExtra("return_flight_json");
            if (retJson != null) returnFlight = gson.fromJson(retJson, Flight.class);
            returnBasePrice = intent.getDoubleExtra("returnTotalPrice", 0);
            returnFareType = intent.getStringExtra("returnFareType");
            returnSelectedSeat = intent.getStringExtra("returnSelectedSeat");
            returnAddonPrice = intent.getDoubleExtra("returnAddonPrice", 0);
            returnSeatPrice = intent.getDoubleExtra("returnSeatPrice", 0);
            returnTaxes = intent.getDoubleExtra("returnTaxes", 450000);
        }
    }

    private void assignRandomSeat() {
        if (selectedSeat == null || selectedSeat.equalsIgnoreCase("Chưa chọn") || selectedSeat.isEmpty() || selectedSeat.equalsIgnoreCase("Chọn")) {
            fetchRandomSeat(flight, fareType, seat -> {
                selectedSeat = seat;
                binding.tvSelectedSeat.setText(selectedSeat + " (Ngẫu nhiên)");
            });
        }
        if (isRoundTrip && (returnSelectedSeat == null || returnSelectedSeat.equalsIgnoreCase("Chưa chọn") || returnSelectedSeat.isEmpty() || returnSelectedSeat.equalsIgnoreCase("Chọn"))) {
            fetchRandomSeat(returnFlight, returnFareType, seat -> {
                returnSelectedSeat = seat;
                if (binding.tvSelectedSeatReturn != null) {
                    binding.tvSelectedSeatReturn.setText(returnSelectedSeat + " (Ngẫu nhiên)");
                }
            });
        }
    }

    private void fetchRandomSeat(Flight targetFlight, String targetFare, java.util.function.Consumer<String> callback) {
        if (targetFlight == null) return;
        RetrofitClient.getInstance().getFlightSeats(targetFlight.getId()).enqueue(new Callback<List<FlightSeat>>() {
            @Override
            public void onResponse(Call<List<FlightSeat>> call, Response<List<FlightSeat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FlightSeat> availableSeats = new ArrayList<>();
                    String cabinFilter = targetFare != null && targetFare.equalsIgnoreCase("Business") ? "BUSINESS" : "ECONOMY";
                    for (FlightSeat seat : response.body()) {
                        if ("AVAILABLE".equalsIgnoreCase(seat.getSeatStatus()) && cabinFilter.equalsIgnoreCase(seat.getCabinClass())) {
                            availableSeats.add(seat);
                        }
                    }
                    if (!availableSeats.isEmpty()) {
                        String seatNum = availableSeats.get(new Random().nextInt(availableSeats.size())).getSeatNumber();
                        callback.accept(seatNum);
                    }
                }
            }
            @Override public void onFailure(Call<List<FlightSeat>> call, Throwable t) {}
        });
    }

    private void loadUserData() {
        if (!sessionManager.isLoggedIn()) return;
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    String fullName = user.getName();
                    if (fullName != null) {
                        String[] parts = fullName.trim().split(" ");
                        if (parts.length > 1) {
                            binding.edtLastName.setText(parts[0]);
                            StringBuilder firstName = new StringBuilder();
                            for (int i = 1; i < parts.length; i++) firstName.append(parts[i]).append(i == parts.length - 1 ? "" : " ");
                            binding.edtFirstName.setText(firstName.toString());
                        } else binding.edtFirstName.setText(fullName);
                    }
                    binding.edtPhone.setText(user.getPhone());
                    binding.edtEmail.setText(user.getEmail());
                    binding.edtBirthDate.setText(user.getDob());
                    binding.edtDocumentNo.setText(user.getCccd() != null ? user.getCccd() : user.getPassport());
                    if ("Bà".equalsIgnoreCase(user.getTitle())) binding.rbMs.setChecked(true);
                    else binding.rbMr.setChecked(true);
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        binding.edtBirthDate.setOnClickListener(v -> showDatePicker());
        setupSpinners();
        setupTermsText();
        binding.btnConfirmBooking.setEnabled(false);
        binding.btnConfirmBooking.setAlpha(0.5f);
        binding.cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.btnConfirmBooking.setEnabled(isChecked);
            binding.btnConfirmBooking.setAlpha(isChecked ? 1.0f : 0.5f);
        });
        binding.btnConfirmBooking.setOnClickListener(v -> {
            if (validateFields()) processBookingAndAccount();
        });
    }

    private boolean validateFields() {
        if (binding.edtFirstName.getText().toString().trim().isEmpty() ||
            binding.edtLastName.getText().toString().trim().isEmpty() ||
            binding.edtPhone.getText().toString().trim().isEmpty() ||
            binding.edtEmail.getText().toString().trim().isEmpty() ||
            binding.edtDocumentNo.getText().toString().trim().isEmpty()) {
            Toast.makeText(this, "Vui lòng điền đầy đủ thông tin hành khách", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void processBookingAndAccount() {
        double vatOut = baseFarePrice * 0.10;
        double finalAmount = baseFarePrice + addonPrice + seatPrice + vatOut + 450000;
        if (isRoundTrip) {
            double vatIn = returnBasePrice * 0.10;
            double roundTripDiscount = (baseFarePrice + returnBasePrice) * 0.05;
            finalAmount += returnBasePrice + returnAddonPrice + returnSeatPrice + vatIn + 450000 - roundTripDiscount;
        }
        Intent intent = new Intent(this, ConfirmPaymentActivity.class);
        intent.putExtra("totalAmount", finalAmount);
        intent.putExtra("passenger_email", binding.edtEmail.getText().toString().trim());
        intent.putExtra("passenger_name", binding.edtLastName.getText().toString().trim() + " " + binding.edtFirstName.getText().toString().trim());
        intent.putExtra("passenger_phone", binding.edtPhone.getText().toString().trim());
        intent.putExtra("passenger_doc", binding.edtDocumentNo.getText().toString().trim());
        intent.putExtra("selected_seat", selectedSeat);
        intent.putExtra("fare_type", fareType);
        intent.putExtra("flight_json", gson.toJson(flight));
        startActivity(intent);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker().setTitleText("CHỌN NGÀY SINH").setTheme(R.style.CustomDatePickerTheme).setSelection(MaterialDatePicker.todayInUtcMilliseconds()).build();
        picker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            binding.edtBirthDate.setText(sdf.format(new Date(selection)));
        });
        picker.show(getSupportFragmentManager(), "BirthDatePicker");
    }

    private void setupSpinners() {
        setupSpinner(binding.spNationality, new String[]{"Việt Nam", "Thái Lan", "Singapore", "Nhật Bản", "Hàn Quốc"});
        setupSpinner(binding.spDocumentType, new String[]{"CCCD", "Hộ chiếu"});
        String[] phoneData = new String[]{"Việt Nam (+84)", "Singapore (+65)", "Thái Lan (+66)"};
        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phoneData) {
            @NonNull @Override public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    String fullText = getItem(position);
                    if (fullText != null && fullText.contains("(")) ((TextView) v).setText(fullText.substring(fullText.indexOf("("), fullText.indexOf(")") + 1));
                    ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
                    ((TextView) v).setPadding(0, 0, 0, 0);
                }
                return v;
            }
            @Override public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
                    ((TextView) v).setPadding(48, 48, 48, 48);
                }
                v.setBackgroundColor(Color.WHITE);
                return v;
            }
        };
        binding.spPhoneCountry.setAdapter(phoneAdapter);
    }

    private void setupSpinner(Spinner spinner, String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data) {
            @NonNull @Override public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
                    ((TextView) v).setPadding(0, 0, 0, 0);
                }
                return v;
            }
            @Override public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
                    ((TextView) v).setPadding(48, 48, 48, 48);
                }
                v.setBackgroundColor(Color.WHITE);
                return v;
            }
        };
        spinner.setAdapter(adapter);
    }

    private void updateUI() {
        if (flight == null) return;
        DecimalFormat df = new DecimalFormat("#,###");
        binding.tvFlightNumberHeader.setText(isRoundTrip ? "CHUYẾN BAY KHỨ HỒI" : "CHUYẾN BAY MỘT CHIỀU");
        updateFlightCardUI(flight, binding.tvDepCode, binding.tvDepTime, binding.tvDepDate, binding.tvDepAirport, binding.tvArrCode, binding.tvArrTime, binding.tvArrDate, binding.tvArrAirport, binding.tvDuration);
        if (binding.tvFlightNumberOut != null) binding.tvFlightNumberOut.setText(flight.getFlightNumber());

        String vnFare = fareType != null && fareType.equalsIgnoreCase("Business") ? "Thương gia" : "Phổ thông";
        binding.tvSelectedFare.setText(vnFare);
        binding.tvSelectedSeat.setText(selectedSeat != null && !selectedSeat.isEmpty() && !selectedSeat.equalsIgnoreCase("Chọn") ? selectedSeat : "Chưa chọn");
        binding.tvSelectedBaggage.setText(addonPrice > 0 ? "Mua thêm " + (addonPrice >= 450000 ? "+23kg" : "+10kg") : "Không mua thêm");

        // Tự động ẩn tiêu đề "LƯỢT ĐI" nếu không phải khứ hồi
        int labelVisibility = isRoundTrip ? View.VISIBLE : View.GONE;
        if (binding.tvLabelOutbound != null) binding.tvLabelOutbound.setVisibility(labelVisibility);
        if (binding.tvLabelPaymentOutbound != null) binding.tvLabelPaymentOutbound.setVisibility(labelVisibility);

        double vatOut = baseFarePrice * 0.10;
        binding.txtBasePriceOut.setText(df.format(baseFarePrice) + " VND");
        binding.txtVATOut.setText(df.format(vatOut) + " VND");

        if (isRoundTrip && returnFlight != null) {
            binding.cardFlightReturn.setVisibility(View.VISIBLE);
            binding.layoutPaymentReturn.setVisibility(View.VISIBLE);
            binding.returnServiceSection.setVisibility(View.VISIBLE);
            updateFlightCardUI(returnFlight, binding.tvDepCodeReturn, binding.tvDepTimeReturn, binding.tvDepDateReturn, binding.tvDepAirportReturn, binding.tvArrCodeReturn, binding.tvArrTimeReturn, binding.tvArrDateReturn, binding.tvArrAirportReturn, binding.tvDurationReturn);
            if (binding.tvFlightNumberRet != null) binding.tvFlightNumberRet.setText(returnFlight.getFlightNumber());
            if (binding.tvSelectedFareReturn != null) binding.tvSelectedFareReturn.setText(returnFareType != null && returnFareType.equalsIgnoreCase("Business") ? "Thương gia" : "Phổ thông");
            if (binding.tvSelectedSeatReturn != null) binding.tvSelectedSeatReturn.setText(returnSelectedSeat != null && !returnSelectedSeat.isEmpty() && !returnSelectedSeat.equalsIgnoreCase("Chọn") ? returnSelectedSeat : "Chưa chọn");
            if (binding.tvSelectedBaggageReturn != null) binding.tvSelectedBaggageReturn.setText(returnAddonPrice > 0 ? "Mua thêm " + (returnAddonPrice >= 450000 ? "+23kg" : "+10kg") : "Không mua thêm");
            
            double vatIn = returnBasePrice * 0.10;
            binding.txtBasePriceIn.setText(df.format(returnBasePrice) + " VND");
            binding.txtVATIn.setText(df.format(vatIn) + " VND");
        }

        double seatTotal = seatPrice + (isRoundTrip ? returnSeatPrice : 0);
        binding.txtSeatPrice.setText(seatTotal > 0 ? df.format(seatTotal) + " VND" : "Miễn phí");
        binding.txtSeatPrice.setTextColor(ContextCompat.getColor(this, seatTotal > 0 ? R.color.skyline_blue_dark : R.color.skyline_teal));
        binding.txtBaggagePrice.setText(df.format(addonPrice + (isRoundTrip ? returnAddonPrice : 0)) + " VND");

        // Tính toán Ưu đãi Khứ hồi (5% Giá vé cơ bản)
        double vatIn = isRoundTrip ? (returnBasePrice * 0.10) : 0;
        double roundTripDiscount = isRoundTrip ? (baseFarePrice + returnBasePrice) * 0.05 : 0;

        if (isRoundTrip && binding.layoutRoundTripDiscount != null) {
            binding.layoutRoundTripDiscount.setVisibility(View.VISIBLE);
            binding.txtRoundTripDiscount.setText("- " + df.format(roundTripDiscount) + " VND");
        } else if (binding.layoutRoundTripDiscount != null) {
            binding.layoutRoundTripDiscount.setVisibility(View.GONE);
        }

        double grandTotal = baseFarePrice + vatOut + 450000 + addonPrice + seatPrice;
        if (isRoundTrip) {
            grandTotal += returnBasePrice + vatIn + 450000 + returnAddonPrice + returnSeatPrice - roundTripDiscount;
        }
        binding.txtGrandTotal.setText(df.format(grandTotal) + " VND");
    }

    private void updateFlightCardUI(Flight f, TextView tvDepC, TextView tvDepT, TextView tvDepD, TextView tvDepA, TextView tvArrC, TextView tvArrT, TextView tvArrD, TextView tvArrA, TextView tvDur) {
        tvDepC.setText(f.getDepartureAirport().getCode());
        tvArrC.setText(f.getArrivalAirport().getCode());
        SimpleDateFormat timeF = new SimpleDateFormat("HH:mm", Locale.US);
        SimpleDateFormat dateF = new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN"));
        timeF.setTimeZone(TimeZone.getTimeZone("UTC")); dateF.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date dDate = parseIsoDate(f.getDepartureAt()); Date aDate = parseIsoDate(f.getArrivalAt());
        if (dDate != null) { tvDepT.setText(timeF.format(dDate)); tvDepD.setText(dateF.format(dDate)); tvDepA.setText(cleanAirportName(f.getDepartureAirport().getName())); }
        if (aDate != null) { tvArrT.setText(timeF.format(aDate)); tvArrD.setText(dateF.format(aDate)); tvArrA.setText(cleanAirportName(f.getArrivalAirport().getName())); }
        tvDur.setText((f.getDuration()/60) + "g " + (f.getDuration()%60) + "p");
    }

    private String cleanAirportName(String name) { return name == null ? "" : name.toUpperCase().replace("SÂN BAY QUỐC TẾ ", "").replace("SÂN BAY ", "").replace("AIRPORT", "").trim(); }

    private void setupTermsText() {
        String fullText = "Tôi đã đọc và đồng ý với Điều khoản & Điều kiện đặt vé và Chính sách bảo mật của Skyline.";
        SpannableString ss = new SpannableString(fullText);
        ClickableSpan termsSpan = new ClickableSpan() { @Override public void onClick(@NonNull View widget) { showContentBottomSheet("ĐIỀU KHOẢN & ĐIỀU KIỆN", "1. Quy định về đặt vé: Vé máy bay được đặt qua Skyline có giá trị theo đúng quy định của hãng hàng không...\n\n2. Quy định thanh toán: Khách hàng cần hoàn tất thanh toán trong vòng 24h kể từ khi đặt chỗ thành công.\n\n3. Thay đổi & Hủy vé: Tùy thuộc vào hạng vé, khách hàng có thể thay đổi ngày giờ bay hoặc hoàn vé theo quy định."); } };
        ClickableSpan privacySpan = new ClickableSpan() { @Override public void onClick(@NonNull View widget) { showContentBottomSheet("CHÍNH SÁCH BẢO MẬT", "Skyline cam kết bảo vệ tuyệt đối thông tin cá nhân của khách hàng. Thông tin của quý khách chỉ được sử dụng cho mục đích:\n\n- Xác nhận đặt vé máy bay.\n- Gửi thông báo về chuyến bay.\n- Nâng cao trải nghiệm dịch vụ người dùng."); } };
        int sT = fullText.indexOf("Điều khoản & Điều kiện đặt vé"), eT = sT + "Điều khoản & Điều kiện đặt vé".length(), sP = fullText.indexOf("Chính sách bảo mật"), eP = sP + "Chính sách bảo mật".length();
        ss.setSpan(termsSpan, sT, eT, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); ss.setSpan(privacySpan, sP, eP, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        binding.tvTerms.setText(ss); binding.tvTerms.setMovementMethod(LinkMovementMethod.getInstance()); binding.tvTerms.setHighlightColor(Color.TRANSPARENT);
    }

    private void showContentBottomSheet(String title, String content) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_selector_country, null);
        ((TextView)view.findViewById(R.id.tv_title)).setText(title);
        view.findViewById(R.id.search_card).setVisibility(View.GONE); view.findViewById(R.id.list_items).setVisibility(View.GONE);
        androidx.core.widget.NestedScrollView sv = new androidx.core.widget.NestedScrollView(this);
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams p = new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(-1, -2);
        p.topToBottom = R.id.tv_title; p.topMargin = (int)(20 * getResources().getDisplayMetrics().density); p.matchConstraintMaxHeight = (int)(400 * getResources().getDisplayMetrics().density);
        sv.setLayoutParams(p);
        TextView tv = new TextView(this); tv.setText(content); tv.setTextColor(ContextCompat.getColor(this, R.color.skyline_text_secondary)); tv.setTextSize(15); tv.setLineSpacing(0, 1.5f); tv.setPadding(0, 0, 0, 60);
        sv.addView(tv); ((ViewGroup)view).addView(sv);
        dialog.setContentView(view); dialog.show();
    }

    private Date parseIsoDate(String iso) {
        if (iso == null) return null;
        String[] pts = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
        for (String p : pts) { try { SimpleDateFormat f = new SimpleDateFormat(p, Locale.US); f.setTimeZone(TimeZone.getTimeZone("UTC")); return f.parse(iso); } catch (Exception e) {} }
        return null;
    }
}
