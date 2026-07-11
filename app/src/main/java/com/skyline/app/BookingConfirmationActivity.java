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
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
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
import java.util.Calendar;
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
    private double baseFarePrice, returnBasePrice;
    private int adults, children;
    private boolean isRoundTrip = false;
    private String fareType, returnFareType;
    private SessionManager sessionManager;

    private List<String> selectedSeats, returnSelectedSeats;
    private List<Integer> b10s, b23s, rb10s, rb23s;

    private final List<PassengerForm> passengerForms = new ArrayList<>();

    private static class PassengerForm {
        View view;
        String type;
        EditText edtLastName, edtFirstName, edtBirthDate, edtDocumentNo;
        RadioGroup rgGender;
        Spinner spNationality, spDocumentType;
    }

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
        assignRandomSeatsIfNeeded();
        startPlaneAnimation();
    }

    private void initData() {
        Intent intent = getIntent();
        isRoundTrip = intent.getBooleanExtra("isRoundTrip", false);
        adults = intent.getIntExtra("adults", 1);
        children = intent.getIntExtra("children", 0);

        String json = intent.getStringExtra("flight_json");
        if (json != null) flight = gson.fromJson(json, Flight.class);
        baseFarePrice = intent.getDoubleExtra("totalPrice", 0);
        fareType = intent.getStringExtra("fareType");

        selectedSeats = intent.getStringArrayListExtra("selectedSeats");
        b10s = intent.getIntegerArrayListExtra("baggage10s");
        b23s = intent.getIntegerArrayListExtra("baggage23s");

        if (isRoundTrip) {
            String retJson = intent.getStringExtra("return_flight_json");
            if (retJson != null) returnFlight = gson.fromJson(retJson, Flight.class);
            returnBasePrice = intent.getDoubleExtra("returnTotalPrice", 0);
            returnFareType = intent.getStringExtra("returnFareType");
            returnSelectedSeats = intent.getStringArrayListExtra("returnSelectedSeats");
            rb10s = intent.getIntegerArrayListExtra("returnB10s");
            rb23s = intent.getIntegerArrayListExtra("returnB23s");
        }
    }

    private void initViews() {
        binding.btnBack.setOnClickListener(v -> finish());
        setupPassengerForms();
        setupContactSpinners();
        setupTermsText();
        
        binding.btnConfirmBooking.setEnabled(false);
        binding.btnConfirmBooking.setAlpha(0.5f);
        binding.cbTerms.setOnCheckedChangeListener((buttonView, isChecked) -> {
            binding.btnConfirmBooking.setEnabled(isChecked);
            binding.btnConfirmBooking.setAlpha(isChecked ? 1.0f : 0.5f);
        });
        binding.btnConfirmBooking.setOnClickListener(v -> {
            if (validateFields()) processBooking();
        });
    }

    private void setupPassengerForms() {
        binding.containerForms.removeAllViews();
        passengerForms.clear();

        int totalPaxCount = adults + children;
        int count = 1;
        for (int i = 0; i < adults; i++) addForm("ADULT", count++, totalPaxCount);
        for (int i = 0; i < children; i++) addForm("CHILD", count++, totalPaxCount);
    }

    private void addForm(String type, int index, int totalPax) {
        View view = LayoutInflater.from(this).inflate(R.layout.item_passenger_form, binding.containerForms, false);
        PassengerForm form = new PassengerForm();
        form.view = view;
        form.type = type;
        form.edtLastName = view.findViewById(R.id.edtLastName);
        form.edtFirstName = view.findViewById(R.id.edtFirstName);
        form.edtBirthDate = view.findViewById(R.id.edtBirthDate);
        form.edtDocumentNo = view.findViewById(R.id.edtDocumentNo);
        form.rgGender = view.findViewById(R.id.rgGender);
        form.spNationality = view.findViewById(R.id.spNationality);
        form.spDocumentType = view.findViewById(R.id.spDocumentType);

        TextView tvTitle = view.findViewById(R.id.tvPassengerTitle);
        String typeName = type.equals("ADULT") ? "NGƯỜI LỚN" : "TRẺ EM";
        
        if (totalPax > 1) {
            tvTitle.setText("HÀNH KHÁCH " + index + " (" + typeName + ")");
        } else {
            tvTitle.setText("THÔNG TIN HÀNH KHÁCH");
        }

        // Đổi danh xưng dễ thương cho trẻ em
        RadioButton rbMr = view.findViewById(R.id.rbMr);
        RadioButton rbMs = view.findViewById(R.id.rbMs);
        if (type.equals("CHILD")) {
            rbMr.setText("Bé trai");
            rbMs.setText("Bé gái");
        }

        setupSpinner(form.spNationality, new String[]{"Việt Nam", "Thái Lan", "Singapore", "Nhật Bản", "Hàn Quốc"});
        setupSpinner(form.spDocumentType, new String[]{"CCCD", "Hộ chiếu"});

        form.edtBirthDate.setOnClickListener(v -> showBirthDatePicker(form));
        binding.containerForms.addView(view);
        passengerForms.add(form);
    }

    private boolean validateFields() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);

        for (int i = 0; i < passengerForms.size(); i++) {
            PassengerForm f = passengerForms.get(i);
            String lastName = f.edtLastName.getText().toString().trim();
            String firstName = f.edtFirstName.getText().toString().trim();
            String dobStr = f.edtBirthDate.getText().toString().trim();
            String docNo = f.edtDocumentNo.getText().toString().trim();

            if (lastName.isEmpty() || firstName.isEmpty() || dobStr.isEmpty()) {
                Toast.makeText(this, "Vui lòng điền đủ thông tin Hành khách " + (i+1), Toast.LENGTH_SHORT).show();
                return false;
            }

            if (docNo.isEmpty()) {
                Toast.makeText(this, "Vui lòng nhập số giấy tờ cho Hành khách " + (i+1), Toast.LENGTH_SHORT).show();
                return false;
            }

            String docType = f.spDocumentType.getSelectedItem().toString();
            if (docType.equals("CCCD") && docNo.length() != 12) {
                Toast.makeText(this, "Số CCCD của Hành khách " + (i+1) + " phải đủ 12 chữ số", Toast.LENGTH_SHORT).show();
                return false;
            }

            // Kiểm tra độ tuổi (Phòng hờ trường hợp người dùng nhập tay nếu không dùng picker)
            try {
                Date birthDate = sdf.parse(dobStr);
                Calendar birthCal = Calendar.getInstance();
                if (birthDate != null) {
                    birthCal.setTime(birthDate);
                    int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
                    if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) age--;

                    if (f.type.equals("ADULT") && age < 12) {
                        Toast.makeText(this, "Hành khách " + (i+1) + " phải từ 12 tuổi trở lên", Toast.LENGTH_LONG).show();
                        return false;
                    }
                    if (f.type.equals("CHILD") && age >= 12) {
                        Toast.makeText(this, "Hành khách " + (i+1) + " phải dưới 12 tuổi", Toast.LENGTH_LONG).show();
                        return false;
                    }
                }
            } catch (Exception e) {
                return false;
            }
        }

        String email = binding.edtEmail.getText().toString().trim();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Vui lòng nhập đúng định dạng email liên hệ (ví dụ: name@gmail.com)", Toast.LENGTH_SHORT).show();
            return false;
        }

        String phone = binding.edtPhone.getText().toString().trim();
        if (phone.isEmpty() || phone.length() < 10 || phone.length() > 11) {
            Toast.makeText(this, "Số điện thoại liên hệ phải từ 10 đến 11 chữ số", Toast.LENGTH_SHORT).show();
            return false;
        }

        return true;
    }

    private void updateUI() {
        if (flight == null) return;
        DecimalFormat df = new DecimalFormat("#,###");
        int totalSeatPax = adults + children;

        binding.tvFlightNumberHeader.setText(isRoundTrip ? "CHUYẾN BAY KHỨ HỒI" : "CHUYẾN BAY MỘT CHIỀU");
        updateFlightCardUI(flight, binding.tvDepCode, binding.tvDepTime, binding.tvDepDate, binding.tvDepAirport, 
                          binding.tvArrCode, binding.tvArrTime, binding.tvArrDate, binding.tvArrAirport, binding.tvDuration);
        if (binding.tvFlightNumberOut != null) binding.tvFlightNumberOut.setText(flight.getFlightNumber());

        // Tự động ẩn tiêu đề "LƯỢT ĐI" nếu không phải khứ hồi
        int labelVisibility = isRoundTrip ? View.VISIBLE : View.GONE;
        if (binding.tvLabelOutbound != null) binding.tvLabelOutbound.setVisibility(labelVisibility);

        // Dịch vụ đã chọn - Tách theo từng người
        LinearLayout contOut = findViewById(R.id.containerServicesOut);
        contOut.removeAllViews();
        for (int i = 0; i < totalSeatPax; i++) {
            contOut.addView(createServiceItem(i, false, contOut));
        }

        if (isRoundTrip && returnFlight != null) {
            binding.cardFlightReturn.setVisibility(View.VISIBLE);
            binding.returnServiceSection.setVisibility(View.VISIBLE);
            updateFlightCardUI(returnFlight, binding.tvDepCodeReturn, binding.tvDepTimeReturn, binding.tvDepDateReturn, binding.tvDepAirportReturn, 
                              binding.tvArrCodeReturn, binding.tvArrTimeReturn, binding.tvArrDateReturn, binding.tvArrAirportReturn, binding.tvDurationReturn);
            if (binding.tvFlightNumberRet != null) binding.tvFlightNumberRet.setText(returnFlight.getFlightNumber());
            
            LinearLayout contIn = findViewById(R.id.containerServicesIn);
            contIn.removeAllViews();
            for (int i = 0; i < totalSeatPax; i++) {
                contIn.addView(createServiceItem(i, true, contIn));
            }
        }

        // Chi tiết thanh toán - Tách theo từng người
        LinearLayout contPay = findViewById(R.id.containerPaymentDetails);
        contPay.removeAllViews();
        for (int i = 0; i < totalSeatPax; i++) {
            contPay.addView(createPaymentItem(i, totalSeatPax, contPay));
        }

        // Ẩn nhãn tổng bị lặp trong phần thanh toán
        if (binding.tvLabelPaymentOutbound != null) {
            binding.tvLabelPaymentOutbound.setVisibility(View.GONE);
        }

        // Tính tổng Addons
        double totalAddons = 0;
        for (int i = 0; i < b10s.size(); i++) {
            totalAddons += b10s.get(i) * 200000 + b23s.get(i) * 450000;
            if (isRoundTrip) totalAddons += rb10s.get(i) * 200000 + rb23s.get(i) * 450000;
        }

        double roundTripDiscount = isRoundTrip ? ((baseFarePrice + returnBasePrice) * totalSeatPax) * 0.05 : 0;
        if (isRoundTrip) {
            binding.layoutRoundTripDiscount.setVisibility(View.VISIBLE);
            binding.txtRoundTripDiscount.setText("- " + df.format(roundTripDiscount) + " VND");
        } else {
            binding.layoutRoundTripDiscount.setVisibility(View.GONE);
        }

        double grandTotal = (baseFarePrice * 1.1 + 450000) * totalSeatPax + totalAddons;
        if (isRoundTrip) {
            grandTotal += (returnBasePrice * 1.1 + 450000) * totalSeatPax - roundTripDiscount;
        }
        
        binding.txtGrandTotal.setText(df.format(grandTotal) + " VND");
    }

    private View createServiceItem(int index, boolean isReturn, ViewGroup parent) {
        View v = LayoutInflater.from(this).inflate(R.layout.item_service_confirmation, parent, false);
        TextView tvPax = v.findViewById(R.id.tvPaxName);
        TextView tvFare = v.findViewById(R.id.tvPaxFare);
        TextView tvSeat = v.findViewById(R.id.tvPaxSeat);
        TextView tvBag = v.findViewById(R.id.tvPaxBaggage);

        int totalSeatPax = adults + children;
        if (totalSeatPax > 1) {
            tvPax.setText("Hành khách " + (index + 1));
            tvPax.setVisibility(View.VISIBLE);
        } else {
            tvPax.setVisibility(View.GONE);
        }

        String fType = isReturn ? returnFareType : fareType;
        String vnFare = fType != null && fType.equalsIgnoreCase("Business") ? "Thương gia" : "Phổ thông";
        tvFare.setText(vnFare);
        tvFare.setTextColor(ContextCompat.getColor(this, fType != null && fType.equalsIgnoreCase("Business") ? R.color.skyline_teal : R.color.skyline_blue));

        String seat = isReturn ? returnSelectedSeats.get(index) : selectedSeats.get(index);
        tvSeat.setText(seat.isEmpty() ? "Ngẫu nhiên" : seat);
        tvSeat.setTextColor(ContextCompat.getColor(this, fType != null && fType.equalsIgnoreCase("Business") ? R.color.skyline_teal : R.color.skyline_blue));
        
        int b10 = isReturn ? rb10s.get(index) : b10s.get(index);
        int b23 = isReturn ? rb23s.get(index) : b23s.get(index);
        int totalKg = b10 * 10 + b23 * 23;
        tvBag.setText(totalKg > 0 ? "Mua thêm +" + totalKg + "kg" : "Không mua thêm");

        return v;
    }

    private View createPaymentItem(int index, int totalSeatPax, ViewGroup parent) {
        View v = LayoutInflater.from(this).inflate(R.layout.item_payment_detail, parent, false);
        TextView tvPax = v.findViewById(R.id.tvPaxTitle);
        TextView tvLabelOut = v.findViewById(R.id.tvLabelOutbound);
        View layoutReturn = v.findViewById(R.id.layoutReturn);

        // Lượt đi views
        TextView txtBase = v.findViewById(R.id.txtBasePrice);
        TextView txtTax = v.findViewById(R.id.txtTaxes);
        TextView txtAir = v.findViewById(R.id.txtAirportFees);
        TextView txtSeat = v.findViewById(R.id.txtSeatPrice);
        TextView txtBag = v.findViewById(R.id.txtBaggagePrice);

        // Lượt về views
        TextView txtBaseRet = v.findViewById(R.id.txtBasePriceReturn);
        TextView txtTaxRet = v.findViewById(R.id.txtTaxesReturn);
        TextView txtAirRet = v.findViewById(R.id.txtAirportFeesReturn);
        TextView txtSeatRet = v.findViewById(R.id.txtSeatPriceReturn);
        TextView txtBagRet = v.findViewById(R.id.txtBaggagePriceReturn);

        if (totalSeatPax > 1) {
            tvPax.setText("Hành khách " + (index + 1));
            tvPax.setVisibility(View.VISIBLE);
        } else {
            tvPax.setVisibility(View.GONE);
        }

        // Ẩn nhãn "LƯỢT ĐI" bên trong item nếu chỉ có 1 chặng
        tvLabelOut.setVisibility(isRoundTrip ? View.VISIBLE : View.GONE);

        DecimalFormat df = new DecimalFormat("#,###");

        // LƯỢT ĐI
        double vatOut = baseFarePrice * 0.1;
        txtBase.setText(df.format(baseFarePrice) + " VND");
        txtTax.setText(df.format(vatOut) + " VND");
        txtAir.setText("450,000 VND");
        txtSeat.setText("Miễn phí");
        double bagOut = b10s.get(index) * 200000 + b23s.get(index) * 450000;
        txtBag.setText(df.format(bagOut) + " VND");

        // LƯỢT VỀ
        if (isRoundTrip) {
            layoutReturn.setVisibility(View.VISIBLE);
            double vatRet = returnBasePrice * 0.1;
            txtBaseRet.setText(df.format(returnBasePrice) + " VND");
            txtTaxRet.setText(df.format(vatRet) + " VND");
            txtAirRet.setText("450,000 VND");
            txtSeatRet.setText("Miễn phí");
            double bagRet = rb10s.get(index) * 200000 + rb23s.get(index) * 450000;
            txtBagRet.setText(df.format(bagRet) + " VND");
        } else {
            layoutReturn.setVisibility(View.GONE);
        }

        if (index == totalSeatPax - 1) {
            v.findViewById(R.id.paxDivider).setVisibility(View.GONE);
        }

        return v;
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

    private void assignRandomSeatsIfNeeded() {
        for (int i = 0; i < selectedSeats.size(); i++) {
            if (selectedSeats.get(i).isEmpty()) {
                final int idx = i;
                fetchRandomSeat(flight, fareType, seat -> {
                    selectedSeats.set(idx, seat + " (Ngẫu nhiên)");
                    updateUI();
                });
            }
            if (isRoundTrip && returnSelectedSeats.get(i).isEmpty()) {
                final int idx = i;
                fetchRandomSeat(returnFlight, returnFareType, seat -> {
                    returnSelectedSeats.set(idx, seat + " (Ngẫu nhiên)");
                    updateUI();
                });
            }
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
                    if (!availableSeats.isEmpty()) callback.accept(availableSeats.get(new Random().nextInt(availableSeats.size())).getSeatNumber());
                }
            }
            @Override public void onFailure(Call<List<FlightSeat>> call, Throwable t) {}
        });
    }

    private void loadUserData() {
        if (!sessionManager.isLoggedIn() || passengerForms.isEmpty()) return;
        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().getProfile(token).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    User user = response.body();
                    PassengerForm main = passengerForms.get(0);
                    String fullName = user.getName();
                    if (fullName != null) {
                        String[] parts = fullName.trim().split(" ");
                        if (parts.length > 1) {
                            main.edtLastName.setText(parts[0]);
                            StringBuilder firstName = new StringBuilder();
                            for (int i = 1; i < parts.length; i++) firstName.append(parts[i]).append(i == parts.length - 1 ? "" : " ");
                            main.edtFirstName.setText(firstName.toString());
                        } else main.edtFirstName.setText(fullName);
                    }
                    binding.edtPhone.setText(user.getPhone());
                    binding.edtEmail.setText(user.getEmail());
                    main.edtBirthDate.setText(user.getDob());
                    main.edtDocumentNo.setText(user.getCccd() != null ? user.getCccd() : user.getPassport());
                    if ("Bà".equalsIgnoreCase(user.getTitle())) ((RadioButton)main.rgGender.findViewById(R.id.rbMs)).setChecked(true);
                }
            }
            @Override public void onFailure(Call<User> call, Throwable t) {}
        });
    }

    private void processBooking() {
        Intent intent = new Intent(this, ConfirmPaymentActivity.class);
        
        int totalPax = adults + children;
        ArrayList<String> names = new ArrayList<>();
        for (PassengerForm f : passengerForms) {
            names.add(f.edtLastName.getText().toString().trim() + " " + f.edtFirstName.getText().toString().trim());
        }
        
        // Tính tổng tiền bao gồm tất cả pax, addons và discount
        DecimalFormat df = new DecimalFormat("#,###");
        double totalAddons = 0;
        for (int i = 0; i < b10s.size(); i++) {
            totalAddons += b10s.get(i) * 200000 + b23s.get(i) * 450000;
            if (isRoundTrip) totalAddons += rb10s.get(i) * 200000 + rb23s.get(i) * 450000;
        }
        double roundTripDiscount = isRoundTrip ? ((baseFarePrice + returnBasePrice) * totalPax) * 0.05 : 0;
        double grandTotal = (baseFarePrice * 1.1 + 450000) * totalPax + totalAddons;
        if (isRoundTrip) grandTotal += (returnBasePrice * 1.1 + 450000) * totalPax - roundTripDiscount;

        intent.putExtra("totalAmount", grandTotal);
        intent.putExtra("passenger_email", binding.edtEmail.getText().toString().trim());
        intent.putExtra("passenger_phone", binding.edtPhone.getText().toString().trim());
        intent.putExtra("passenger_names", names);
        intent.putExtra("fare_type", fareType);
        intent.putExtra("isRoundTrip", isRoundTrip);
        intent.putExtra("flight_json", gson.toJson(flight));
        
        // Chuỗi ghế cách nhau dấu phẩy cho ConfirmPaymentActivity xử lý
        StringBuilder sbSeats = new StringBuilder();
        for (int i = 0; i < selectedSeats.size(); i++) {
            sbSeats.append(selectedSeats.get(i)).append(i == selectedSeats.size() - 1 ? "" : ", ");
        }
        intent.putExtra("selected_seat", sbSeats.toString());

        if (isRoundTrip) {
            intent.putExtra("return_flight_json", gson.toJson(returnFlight));
            StringBuilder sbRetSeats = new StringBuilder();
            for (int i = 0; i < returnSelectedSeats.size(); i++) {
                sbRetSeats.append(returnSelectedSeats.get(i)).append(i == returnSelectedSeats.size() - 1 ? "" : ", ");
            }
            intent.putExtra("returnSelectedSeat", sbRetSeats.toString());
        }

        startActivity(intent);
    }

    private void showBirthDatePicker(PassengerForm form) {
        Calendar today = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
        Calendar twelveYearsAgo = (Calendar) today.clone();
        twelveYearsAgo.add(Calendar.YEAR, -12);

        com.google.android.material.datepicker.CalendarConstraints.Builder constraintsBuilder = new com.google.android.material.datepicker.CalendarConstraints.Builder();
        
        if (form.type.equals("ADULT")) {
            // Người lớn: <= 12 năm trước
            constraintsBuilder.setValidator(com.google.android.material.datepicker.DateValidatorPointBackward.before(twelveYearsAgo.getTimeInMillis()));
        } else {
            // Trẻ em: > 12 năm trước và <= hôm nay
            constraintsBuilder.setValidator(com.google.android.material.datepicker.CompositeDateValidator.allOf(java.util.Arrays.asList(
                com.google.android.material.datepicker.DateValidatorPointForward.from(twelveYearsAgo.getTimeInMillis() + 86400000), // +1 day to be >
                com.google.android.material.datepicker.DateValidatorPointBackward.now()
            )));
        }

        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("CHỌN NGÀY SINH")
                .setCalendarConstraints(constraintsBuilder.build())
                .setTheme(R.style.CustomDatePickerTheme)
                .build();

        picker.addOnPositiveButtonClickListener(date -> {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            form.edtBirthDate.setText(sdf.format(new Date(date)));
        });
        picker.show(getSupportFragmentManager(), "BirthDatePicker");
    }

    private void setupContactSpinners() {
        String[] phoneData = new String[]{"Việt Nam (+84)", "Singapore (+65)", "Thái Lan (+66)"};
        ArrayAdapter<String> phoneAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, phoneData) {
            @NonNull @Override public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    String fullText = getItem(position);
                    if (fullText != null && fullText.contains("(")) ((TextView) v).setText(fullText.substring(fullText.indexOf("("), fullText.indexOf(")") + 1));
                    ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
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
                if (v instanceof TextView) ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
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

    private void startPlaneAnimation() {
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.imgPlane, "translationY", -12f, 12f);
        animator.setDuration(2000);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
        if (isRoundTrip && binding.imgPlaneReturn != null) {
            ObjectAnimator animatorRet = ObjectAnimator.ofFloat(binding.imgPlaneReturn, "translationY", -12f, 12f);
            animatorRet.setDuration(2000);
            animatorRet.setRepeatMode(ValueAnimator.REVERSE);
            animatorRet.setRepeatCount(ValueAnimator.INFINITE);
            animatorRet.setInterpolator(new AccelerateDecelerateInterpolator());
            animatorRet.start();
        }
    }

    private String cleanAirportName(String name) {
        if (name == null) return "";
        return name.replaceAll("(?i)(SÂN BAY QUỐC TẾ |SÂN BAY |AIRPORT )", "").trim().toUpperCase();
    }

    private void setupTermsText() {
        String fullText = "Tôi đã đọc và đồng ý với Điều khoản & Điều kiện đặt vé và Chính sách bảo mật của Skyline.";
        SpannableString ss = new SpannableString(fullText);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showContentBottomSheet("ĐIỀU KHOẢN & ĐIỀU KIỆN", 
                    "<b>1. Quy định về đặt vé:</b><br>" +
                    "Văn bản này quy định các điều khoản và điều kiện cho việc đặt vé trực tuyến thông qua ứng dụng Skyline. Bằng việc thực hiện giao dịch, quý khách xác nhận đã hiểu và chấp thuận các điều khoản này.<br><br>" +
                    "<b>2. Quy định thanh toán:</b><br>" +
                    "- Giá vé hiển thị là giá tổng cộng cuối cùng đã bao gồm các loại thuế và phí.<br>" +
                    "- Quý khách cần hoàn tất thanh toán trong vòng 24 giờ kể từ khi đặt giữ chỗ.<br><br>" +
                    "<b>3. Thay đổi và Hủy vé:</b><br>" +
                    "- Việc thay đổi ngày bay, giờ bay hoặc hành trình sẽ áp dụng mức phí theo quy định của từng hạng vé cộng với chênh lệch giá vé tại thời điểm đổi.<br>" +
                    "- Vé đã mua có thể không được hoàn lại tùy theo điều kiện của loại giá vé mà quý khách đã chọn.<br><br>" +
                    "<b>4. Trách nhiệm của hành khách:</b><br>" +
                    "- Hành khách có trách nhiệm cung cấp thông tin cá nhân chính xác theo giấy tờ tùy thân.<br>" +
                    "- Skyline không chịu trách nhiệm trong trường hợp hành khách bị từ chối vận chuyển do thông tin sai lệch hoặc thiếu giấy tờ hợp lệ.");
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showContentBottomSheet("CHÍNH SÁCH BẢO MẬT", 
                    "<b>1. Thu thập thông tin:</b><br>" +
                    "Skyline thu thập thông tin cần thiết để thực hiện dịch vụ đặt vé bao gồm: Họ tên, Ngày sinh, Số điện thoại, Email và thông tin giấy tờ tùy thân.<br><br>" +
                    "<b>2. Sử dụng thông tin:</b><br>" +
                    "Thông tin của quý khách được sử dụng để:<br>" +
                    "- Xử lý các yêu cầu đặt vé và thanh toán.<br>" +
                    "- Gửi thông báo xác nhận và thông tin cập nhật về chuyến bay.<br>" +
                    "- Nâng cao trải nghiệm dịch vụ người dùng.<br><br>" +
                    "<b>3. Bảo mật dữ liệu:</b><br>" +
                    "Chúng tôi áp dụng các biện pháp bảo mật kỹ thuật hiện đại để bảo vệ dữ liệu cá nhân của quý khách khỏi việc truy cập trái phép hoặc tiết lộ thông tin.<br><br>" +
                    "<b>4. Chia sẻ thông tin:</b><br>" +
                    "Chúng tôi chỉ chia sẻ thông tin cần thiết với các đối tác hàng không để hoàn tất dịch vụ đặt vé. Chúng tôi cam kết không bán thông tin cho bên thứ ba.");
            }
        };

        int startTerms = fullText.indexOf("Điều khoản & Điều kiện đặt vé");
        int endTerms = startTerms + "Điều khoản & Điều kiện đặt vé".length();
        int startPrivacy = fullText.indexOf("Chính sách bảo mật");
        int endPrivacy = startPrivacy + "Chính sách bảo mật".length();

        ss.setSpan(termsSpan, startTerms, endTerms, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(privacySpan, startPrivacy, endPrivacy, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        binding.tvTerms.setText(ss);
        binding.tvTerms.setMovementMethod(LinkMovementMethod.getInstance());
        binding.tvTerms.setHighlightColor(Color.TRANSPARENT);
    }

    private void showContentBottomSheet(String title, String content) {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_selector_country, null);
        
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(title);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.skyline_blue_dark));
        
        view.findViewById(R.id.search_card).setVisibility(View.GONE);
        view.findViewById(R.id.list_items).setVisibility(View.GONE);
        
        ViewGroup root = (ViewGroup) view;
        androidx.core.widget.NestedScrollView scrollView = new androidx.core.widget.NestedScrollView(this);
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
            new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(-1, -2);
        params.topToBottom = R.id.tv_title;
        params.topMargin = (int) (20 * getResources().getDisplayMetrics().density);
        scrollView.setLayoutParams(params);

        TextView tvContent = new TextView(this);
        tvContent.setText(android.text.Html.fromHtml(content, android.text.Html.FROM_HTML_MODE_COMPACT));
        tvContent.setTextColor(ContextCompat.getColor(this, R.color.skyline_text_secondary));
        tvContent.setTextSize(15);
        tvContent.setLineSpacing(0, 1.4f);
        tvContent.setPadding(0, 0, 0, 80);
        
        scrollView.addView(tvContent);
        root.addView(scrollView);

        dialog.setContentView(view);
        
        // Mở rộng tối đa BottomSheet ngay khi hiện
        dialog.setOnShowListener(dialogInterface -> {
            View bottomSheet = ((BottomSheetDialog) dialogInterface).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
                    .setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            }
        });

        dialog.show();
    }

    private Date parseIsoDate(String iso) {
        if (iso == null) return null;
        String[] pts = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
        for (String p : pts) { try { SimpleDateFormat f = new SimpleDateFormat(p, Locale.US); f.setTimeZone(TimeZone.getTimeZone("UTC")); return f.parse(iso); } catch (Exception e) {} }
        return null;
    }
}
