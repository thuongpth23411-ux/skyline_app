package com.skyline.app;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.LayoutInflater;
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
    private Flight flight;
    private final Gson gson = new Gson();
    private double baseFarePrice, addonPrice, seatPrice, taxes;
    private String selectedSeat, fareType;
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
        ObjectAnimator animator = ObjectAnimator.ofFloat(binding.imgPlane, "translationY", -12f, 12f);
        animator.setDuration(2000);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());
        animator.start();
    }

    private void initData() {
        Intent intent = getIntent();
        String json = intent.getStringExtra("flight_json");
        if (json != null) {
            flight = gson.fromJson(json, Flight.class);
        }
        baseFarePrice = intent.getDoubleExtra("totalPrice", 0);
        fareType = intent.getStringExtra("fareType");
        selectedSeat = intent.getStringExtra("selectedSeat");
        addonPrice = intent.getDoubleExtra("addonPrice", 0);
        seatPrice = intent.getDoubleExtra("seatPrice", 0);
        taxes = intent.getDoubleExtra("taxes", 0);
    }

    private void assignRandomSeat() {
        if (selectedSeat != null && !selectedSeat.equalsIgnoreCase("Chưa chọn") && !selectedSeat.isEmpty()) {
            return;
        }

        RetrofitClient.getInstance().getFlightSeats(flight.getId()).enqueue(new Callback<List<FlightSeat>>() {
            @Override
            public void onResponse(Call<List<FlightSeat>> call, Response<List<FlightSeat>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<FlightSeat> availableSeats = new ArrayList<>();
                    String cabinFilter = fareType != null && fareType.equalsIgnoreCase("Business") ? "BUSINESS" : "ECONOMY";
                    
                    for (FlightSeat seat : response.body()) {
                        if ("AVAILABLE".equalsIgnoreCase(seat.getSeatStatus()) && cabinFilter.equalsIgnoreCase(seat.getCabinClass())) {
                            availableSeats.add(seat);
                        }
                    }
                    
                    if (!availableSeats.isEmpty()) {
                        int randomIndex = new Random().nextInt(availableSeats.size());
                        selectedSeat = availableSeats.get(randomIndex).getSeatNumber();
                        binding.tvSelectedSeat.setText(selectedSeat + " (Ngẫu nhiên)");
                        binding.txtSeatPrice.setText("Miễn phí");
                        binding.txtSeatPrice.setTextColor(ContextCompat.getColor(BookingConfirmationActivity.this, R.color.skyline_teal));
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
                    
                    // Điền thông tin vào form
                    String fullName = user.getName();
                    if (fullName != null) {
                        String[] parts = fullName.trim().split(" ");
                        if (parts.length > 1) {
                            binding.edtLastName.setText(parts[0]);
                            StringBuilder firstName = new StringBuilder();
                            for (int i = 1; i < parts.length; i++) {
                                firstName.append(parts[i]).append(i == parts.length - 1 ? "" : " ");
                            }
                            binding.edtFirstName.setText(firstName.toString());
                        } else {
                            binding.edtFirstName.setText(fullName);
                        }
                    }
                    
                    binding.edtPhone.setText(user.getPhone());
                    binding.edtEmail.setText(user.getEmail());
                    binding.edtBirthDate.setText(user.getDob());
                    binding.edtDocumentNo.setText(user.getCccd() != null ? user.getCccd() : user.getPassport());
                    
                    if ("Bà".equalsIgnoreCase(user.getTitle())) {
                        binding.rbMs.setChecked(true);
                    } else {
                        binding.rbMr.setChecked(true);
                    }
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
            if (validateFields()) {
                processBookingAndAccount();
            }
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
        String email = binding.edtEmail.getText().toString().trim();
        String firstName = binding.edtFirstName.getText().toString().trim();
        String lastName = binding.edtLastName.getText().toString().trim();
        String phone = binding.edtPhone.getText().toString().trim();
        String docNo = binding.edtDocumentNo.getText().toString().trim();

        // CHUYỂN DỮ LIỆU SANG THANH TOÁN
        // Lưu ý: Chúng ta truyền đi để trang Thanh toán của bên kia sử dụng, không can thiệp vào code của họ
        Intent intent = new Intent(this, ConfirmPaymentActivity.class);
        intent.putExtra("totalAmount", baseFarePrice + addonPrice + seatPrice + taxes);
        intent.putExtra("passenger_email", email);
        intent.putExtra("passenger_name", lastName + " " + firstName);
        intent.putExtra("passenger_phone", phone);
        intent.putExtra("passenger_doc", docNo);
        intent.putExtra("selected_seat", selectedSeat);
        intent.putExtra("fare_type", fareType);
        intent.putExtra("flight_json", gson.toJson(flight));
        
        startActivity(intent);
    }

    private void showDatePicker() {
        MaterialDatePicker<Long> picker = MaterialDatePicker.Builder.datePicker()
                .setTitleText("CHỌN NGÀY SINH")
                .setTheme(R.style.CustomDatePickerTheme)
                .setSelection(MaterialDatePicker.todayInUtcMilliseconds())
                .setInputMode(MaterialDatePicker.INPUT_MODE_CALENDAR)
                .build();

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
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    String fullText = getItem(position);
                    if (fullText != null && fullText.contains("(")) {
                        tv.setText(fullText.substring(fullText.indexOf("("), fullText.indexOf(")") + 1));
                    }
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
                    tv.setTextSize(14);
                    tv.setPadding(0, 0, 0, 0);
                    tv.setGravity(android.view.Gravity.CENTER);
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    TextView tv = (TextView) v;
                    tv.setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
                    tv.setPadding(48, 48, 48, 48);
                    tv.setTextSize(15);
                }
                v.setBackgroundColor(Color.WHITE);
                return v;
            }
        };
        binding.spPhoneCountry.setAdapter(phoneAdapter);
        binding.spPhoneCountry.setDropDownWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        binding.spPhoneCountry.setDropDownVerticalOffset(10);
    }

    private void setupSpinner(Spinner spinner, String[] data) {
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, data) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
                    ((TextView) v).setTextSize(14);
                    ((TextView) v).setPadding(0, 0, 0, 0);
                }
                return v;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                View v = super.getDropDownView(position, convertView, parent);
                if (v instanceof TextView) {
                    ((TextView) v).setTextColor(ContextCompat.getColor(getContext(), R.color.skyline_blue_dark));
                    ((TextView) v).setPadding(48, 48, 48, 48);
                    ((TextView) v).setTextSize(15);
                }
                v.setBackgroundColor(Color.WHITE);
                return v;
            }
        };
        spinner.setAdapter(adapter);
        spinner.setDropDownWidth(ViewGroup.LayoutParams.WRAP_CONTENT);
        spinner.setDropDownVerticalOffset(10);
    }

    private void updateUI() {
        if (flight == null) return;

        binding.tvFlightNumberHeader.setText("CHUYẾN BAY " + flight.getFlightNumber());
        binding.tvDepCode.setText(flight.getDepartureAirport().getCode());
        binding.tvArrCode.setText(flight.getArrivalAirport().getCode());

        Date dDate = parseIsoDate(flight.getDepartureAt());
        Date aDate = parseIsoDate(flight.getArrivalAt());
        SimpleDateFormat timeF = new SimpleDateFormat("HH:mm", Locale.US);
        SimpleDateFormat dateF = new SimpleDateFormat("dd 'Th'MM", new Locale("vi", "VN"));
        timeF.setTimeZone(TimeZone.getTimeZone("UTC"));
        dateF.setTimeZone(TimeZone.getTimeZone("UTC"));

        if (dDate != null) {
            binding.tvDepTime.setText(timeF.format(dDate));
            binding.tvDepDate.setText(dateF.format(dDate));
            binding.tvDepAirport.setText(cleanAirportName(flight.getDepartureAirport().getName()));
        }
        if (aDate != null) {
            binding.tvArrTime.setText(timeF.format(aDate));
            binding.tvArrDate.setText(dateF.format(aDate));
            binding.tvArrAirport.setText(cleanAirportName(flight.getArrivalAirport().getName()));
        }
        binding.tvDuration.setText((flight.getDuration()/60) + "g " + (flight.getDuration()%60) + "p");

        String vnFare = fareType != null && fareType.equalsIgnoreCase("Business") ? "Thương gia" : "Phổ thông";
        binding.tvSelectedFare.setText(vnFare);
        
        binding.tvSelectedSeat.setText(selectedSeat != null && !selectedSeat.isEmpty() ? selectedSeat : "Chưa chọn");
        
        if (addonPrice > 0) {
            String weight = addonPrice >= 450000 ? "+23kg" : "+10kg";
            binding.tvSelectedBaggage.setText("Mua thêm " + weight);
        } else {
            binding.tvSelectedBaggage.setText("Không mua thêm");
        }

        DecimalFormat df = new DecimalFormat("#,###");
        binding.txtBasePrice.setText(df.format(baseFarePrice) + " VND");
        binding.txtTaxes.setText(df.format(taxes) + " VND");
        
        if (seatPrice > 0) {
            binding.txtSeatPrice.setText(df.format(seatPrice) + " VND");
            binding.txtSeatPrice.setTextColor(ContextCompat.getColor(this, R.color.skyline_blue_dark));
        } else {
            binding.txtSeatPrice.setText("Miễn phí");
            binding.txtSeatPrice.setTextColor(ContextCompat.getColor(this, R.color.skyline_teal));
        }
        
        binding.txtBaggagePrice.setText(df.format(addonPrice) + " VND");
        binding.txtGrandTotal.setText(df.format(baseFarePrice + addonPrice + seatPrice + taxes) + " VND");
    }

    private String cleanAirportName(String name) {
        if (name == null) return "";
        return name.toUpperCase()
                .replace("SÂN BAY QUỐC TẾ ", "")
                .replace("SÂN BAY ", "")
                .replace("AIRPORT", "")
                .trim();
    }

    private void setupTermsText() {
        String fullText = "Tôi đã đọc và đồng ý với Điều khoản & Điều kiện đặt vé và Chính sách bảo mật của Skyline.";
        SpannableString ss = new SpannableString(fullText);

        ClickableSpan termsSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showContentBottomSheet("ĐIỀU KHOẢN & ĐIỀU KIỆN", 
                    "1. Quy định về đặt vé: Vé máy bay được đặt qua Skyline có giá trị theo đúng quy định của hãng hàng không...\n\n" +
                    "2. Quy định thanh toán: Khách hàng cần hoàn tất thanh toán trong vòng 24h kể từ khi đặt chỗ thành công.\n\n" +
                    "3. Thay đổi & Hủy vé: Tùy thuộc vào hạng vé, khách hàng có thể thay đổi ngày giờ bay hoặc hoàn vé theo quy định.");
            }
        };

        ClickableSpan privacySpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                showContentBottomSheet("CHÍNH SÁCH BẢO MẬT", 
                    "Skyline cam kết bảo vệ tuyệt đối thông tin cá nhân của khách hàng. Thông tin của quý khách chỉ được sử dụng cho mục đích:\n\n" +
                    "- Xác nhận đặt vé máy bay.\n" +
                    "- Gửi thông báo về chuyến bay.\n" +
                    "- Nâng cao trải nghiệm dịch vụ người dùng.");
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
        // Tận dụng layout có sẵn nhưng dọn dẹp sạch sẽ để làm nội dung văn bản
        View view = getLayoutInflater().inflate(R.layout.layout_selector_country, null);
        
        TextView tvTitle = view.findViewById(R.id.tv_title);
        tvTitle.setText(title);
        tvTitle.setTextColor(ContextCompat.getColor(this, R.color.skyline_blue_dark));
        
        // Gỡ bỏ các thành phần của màn hình chọn quốc gia
        view.findViewById(R.id.search_card).setVisibility(View.GONE);
        view.findViewById(R.id.list_items).setVisibility(View.GONE);
        
        ViewGroup root = (ViewGroup) view;
        
        // Tạo ScrollView chứa nội dung văn bản để không bị đè lên tiêu đề
        androidx.core.widget.NestedScrollView scrollView = new androidx.core.widget.NestedScrollView(this);
        androidx.constraintlayout.widget.ConstraintLayout.LayoutParams params = 
            new androidx.constraintlayout.widget.ConstraintLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            );
        // Thiết lập vị trí: Nằm dưới tiêu đề và cách một khoảng 20dp
        params.topToBottom = R.id.tv_title;
        params.startToStart = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.endToEnd = androidx.constraintlayout.widget.ConstraintLayout.LayoutParams.PARENT_ID;
        params.topMargin = (int) (20 * getResources().getDisplayMetrics().density);
        // Giới hạn chiều cao tối đa để không chiếm hết màn hình
        params.matchConstraintMaxHeight = (int) (400 * getResources().getDisplayMetrics().density);
        
        scrollView.setLayoutParams(params);

        TextView tvContent = new TextView(this);
        tvContent.setText(content);
        tvContent.setTextColor(ContextCompat.getColor(this, R.color.skyline_text_secondary));
        tvContent.setTextSize(15);
        tvContent.setLineSpacing(0, 1.5f);
        tvContent.setPadding(0, 0, 0, 60); // Padding đáy để không sát mép
        
        scrollView.addView(tvContent);
        root.addView(scrollView);

        dialog.setContentView(view);
        dialog.show();
    }

    private Date parseIsoDate(String iso) {
        if (iso == null) return null;
        String[] patterns = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
        for (String p : patterns) {
            try {
                SimpleDateFormat f = new SimpleDateFormat(p, Locale.US);
                f.setTimeZone(TimeZone.getTimeZone("UTC"));
                return f.parse(iso);
            } catch (Exception ignored) {}
        }
        return null;
    }
}