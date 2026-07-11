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
import android.widget.ImageView;
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
    private User currentUser;

    private List<String> selectedSeats, returnSelectedSeats;
    private List<Integer> b10s, b23s, rb10s, rb23s;

    private List<com.skyline.app.network.Promotion> allPromotions;
    private com.skyline.app.network.Promotion appliedPromotion;
    private double voucherDiscountAmount = 0;

    private final List<PassengerForm> passengerForms = new ArrayList<>();

    private static class PassengerForm {
        View view;
        String type;
        EditText edtLastName, edtFirstName, edtBirthDate, edtDocumentNo;
        RadioGroup rgGender;
        Spinner spNationality, spDocumentType;
        TextView tvErrorLastName, tvErrorFirstName, tvErrorBirthDate, tvErrorDocumentNo;
    }

    // Static storage to persist data across navigation (Draft)
    private static class PassengerData {
        String lastName, firstName, dob, docNo, gender, nationality, docType;
    }
    private static final List<PassengerData> passengerDrafts = new ArrayList<>();
    private static String draftEmail = "", draftPhone = "";

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
        fetchPromotions();
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

        // Set click listener on both card and inner layout for reliability
        View.OnClickListener voucherClick = v -> showVoucherBottomSheet();
        binding.cardVoucherRow.setOnClickListener(voucherClick);
        if (binding.layoutVoucherClick != null) {
            binding.layoutVoucherClick.setOnClickListener(voucherClick);
        }

        // Restore contact draft
        if (!draftEmail.isEmpty()) binding.edtEmail.setText(draftEmail);
        if (!draftPhone.isEmpty()) binding.edtPhone.setText(draftPhone);
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveDraftData();
    }

    private void saveDraftData() {
        passengerDrafts.clear();
        for (PassengerForm f : passengerForms) {
            PassengerData data = new PassengerData();
            data.lastName = f.edtLastName.getText().toString().trim();
            data.firstName = f.edtFirstName.getText().toString().trim();
            data.dob = f.edtBirthDate.getText().toString().trim();
            data.docNo = f.edtDocumentNo.getText().toString().trim();
            data.gender = ((RadioButton)f.rgGender.findViewById(f.rgGender.getCheckedRadioButtonId())).getText().toString();
            passengerDrafts.add(data);
        }
        draftEmail = binding.edtEmail.getText().toString().trim();
        draftPhone = binding.edtPhone.getText().toString().trim();
    }

    private void showVoucherBottomSheet() {
        BottomSheetDialog dialog = new BottomSheetDialog(this, R.style.BottomSheetDialogTheme);
        View view = getLayoutInflater().inflate(R.layout.layout_voucher_bottom_sheet, null);
        
        EditText edtCode = view.findViewById(R.id.edtPromoCode);
        TextView tvStatus = view.findViewById(R.id.tvPromoStatus);
        com.google.android.material.button.MaterialButton btnApplySmall = view.findViewById(R.id.btnApplyPromo);
        com.google.android.material.button.MaterialButton btnApplyBig = view.findViewById(R.id.btnDone);
        androidx.recyclerview.widget.RecyclerView rvVouchers = view.findViewById(R.id.rvVouchers);

        // State for selection within bottom sheet
        final com.skyline.app.network.Promotion[] tempSelected = { appliedPromotion };
        final double[] tempDiscount = { voucherDiscountAmount };

        int totalPax = adults + children;
        final double subTotal = (baseFarePrice * 1.1 + 450000) * totalPax + (isRoundTrip ? (returnBasePrice * 1.1 + 450000) * totalPax : 0);

        if (allPromotions != null && !allPromotions.isEmpty()) {
            VoucherAdapter adapter = new VoucherAdapter(allPromotions, 
                tempSelected[0] != null ? tempSelected[0].getCode() : null, 
                subTotal,
                currentUser != null ? currentUser.getRank() : "NONE",
                new VoucherAdapter.OnVoucherSelectedListener() {
                    @Override
                    public void onSelected(com.skyline.app.network.Promotion promotion) {
                        tempSelected[0] = promotion;
                        tempDiscount[0] = calculatePotentialDiscount(promotion, subTotal);
                        edtCode.setText(promotion.getCode());
                        tvStatus.setText("Đã chọn: " + promotion.getTitle());
                        tvStatus.setTextColor(ContextCompat.getColor(BookingConfirmationActivity.this, R.color.skyline_teal));
                        tvStatus.setVisibility(View.VISIBLE);
                        btnApplySmall.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(BookingConfirmationActivity.this, R.color.skyline_teal)));
                    }

                    @Override
                    public void onConditionClick(com.skyline.app.network.Promotion promotion) {
                        showVoucherDetailPopup(promotion);
                    }
                });
            rvVouchers.setAdapter(adapter);
        }

        if (tempSelected[0] != null) {
            edtCode.setText(tempSelected[0].getCode());
            btnApplySmall.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.skyline_teal)));
            tvStatus.setText("Đã chọn mã: " + tempSelected[0].getTitle());
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.skyline_teal));
            tvStatus.setVisibility(View.VISIBLE);
        }

        edtCode.addTextChangedListener(new android.text.TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}
            @Override
            public void afterTextChanged(android.text.Editable s) {
                boolean hasText = s.length() > 0;
                btnApplySmall.setBackgroundTintList(android.content.res.ColorStateList.valueOf(
                    hasText ? ContextCompat.getColor(BookingConfirmationActivity.this, R.color.skyline_teal) : android.graphics.Color.parseColor("#A0AEC0")
                ));
            }
        });

        btnApplySmall.setOnClickListener(v -> {
            String code = edtCode.getText().toString().trim();
            if (code.isEmpty()) return;
            
            applyPromoLogic(code, tvStatus, btnApplySmall, subTotal, tempSelected, tempDiscount, (VoucherAdapter)rvVouchers.getAdapter());
        });

        btnApplyBig.setOnClickListener(v -> {
            appliedPromotion = tempSelected[0];
            voucherDiscountAmount = tempDiscount[0];
            
            if (appliedPromotion != null) {
                binding.tvSelectedVoucherTag.setText("-" + new DecimalFormat("#,###").format(voucherDiscountAmount) + " VND");
                binding.tvSelectedVoucherTag.setTextColor(ContextCompat.getColor(this, R.color.skyline_teal));
            } else {
                binding.tvSelectedVoucherTag.setText("Chọn voucher");
                binding.tvSelectedVoucherTag.setTextColor(ContextCompat.getColor(this, R.color.skyline_text_secondary));
            }
            
            updateUI();
            dialog.dismiss();
        });

        dialog.setContentView(view);
        dialog.setOnShowListener(dialogInterface -> {
            View bottomSheet = ((BottomSheetDialog) dialogInterface).findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                com.google.android.material.bottomsheet.BottomSheetBehavior.from(bottomSheet)
                    .setState(com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED);
            }
        });
        dialog.show();
    }

    private void applyPromoLogic(String code, TextView tvStatus, View btnApply, double subTotal, com.skyline.app.network.Promotion[] tempSelected, double[] tempDiscount, VoucherAdapter adapter) {
        if (allPromotions == null || allPromotions.isEmpty()) {
            tvStatus.setText("Không có mã khuyến mãi khả dụng");
            tvStatus.setTextColor(Color.RED);
            tvStatus.setVisibility(View.VISIBLE);
            return;
        }

        com.skyline.app.network.Promotion found = null;
        for (com.skyline.app.network.Promotion p : allPromotions) {
            if (p.getCode().equalsIgnoreCase(code)) {
                found = p;
                break;
            }
        }

        if (found == null) {
            tvStatus.setText("Mã khuyến mãi không hợp lệ");
            tvStatus.setTextColor(Color.RED);
            tvStatus.setVisibility(View.VISIBLE);
            return;
        }

        if (!"Active".equalsIgnoreCase(found.getStatus())) {
            tvStatus.setText("Voucher hiện không khả dụng");
            tvStatus.setTextColor(Color.RED);
            tvStatus.setVisibility(View.VISIBLE);
            return;
        }

        if (subTotal < found.getMinimumOrder()) {
            tvStatus.setText("Đơn hàng chưa đạt mức tối thiểu " + new DecimalFormat("#,###").format(found.getMinimumOrder()) + "đ");
            tvStatus.setTextColor(Color.RED);
            tvStatus.setVisibility(View.VISIBLE);
            return;
        }
        
        if ("MEMBER".equalsIgnoreCase(found.getCategory()) && (currentUser == null)) {
            tvStatus.setText("Voucher dành riêng cho hội viên");
            tvStatus.setTextColor(Color.RED);
            tvStatus.setVisibility(View.VISIBLE);
            return;
        }

        tempSelected[0] = found;
        tempDiscount[0] = calculatePotentialDiscount(found, subTotal);

        tvStatus.setText("Áp dụng thành công: " + found.getTitle());
        tvStatus.setTextColor(ContextCompat.getColor(this, R.color.skyline_teal));
        tvStatus.setVisibility(View.VISIBLE);
        
        btnApply.setBackgroundTintList(android.content.res.ColorStateList.valueOf(ContextCompat.getColor(this, R.color.skyline_teal)));
        
        if (adapter != null) {
            adapter.updateSelection(found.getCode());
        }
    }

    private double calculatePotentialDiscount(com.skyline.app.network.Promotion v, double currentSubTotal) {
        if (currentSubTotal < v.getMinimumOrder()) return 0;
        double discount;
        if ("FIXED".equalsIgnoreCase(v.getDiscountType())) {
            discount = v.getDiscountValue();
        } else {
            discount = currentSubTotal * (v.getDiscountValue() / 100.0);
            if (v.getMaxDiscount() > 0 && discount > v.getMaxDiscount()) {
                discount = v.getMaxDiscount();
            }
        }
        return discount;
    }

    private boolean isVoucherEligible(com.skyline.app.network.Promotion p, double subTotal) {
        if (!"Active".equalsIgnoreCase(p.getStatus())) return false;
        if (p.getQuantity() <= 0) return false;
        if (subTotal < p.getMinimumOrder()) return false;
        if ("MEMBER".equalsIgnoreCase(p.getCategory()) && (currentUser == null)) return false;
        return true;
    }

    private void fetchPromotions() {
        RetrofitClient.getInstance().getPromotions().enqueue(new Callback<List<com.skyline.app.network.Promotion>>() {
            @Override
            public void onResponse(Call<List<com.skyline.app.network.Promotion>> call, Response<List<com.skyline.app.network.Promotion>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    allPromotions = response.body();
                    autoApplyBestVoucher();
                }
            }
            @Override public void onFailure(Call<List<com.skyline.app.network.Promotion>> call, Throwable t) {}
        });
    }

    private void autoApplyBestVoucher() {
        if (allPromotions == null || allPromotions.isEmpty()) return;

        int totalPax = adults + children;
        double subTotal = (baseFarePrice * 1.1 + 450000) * totalPax;
        if (isRoundTrip) subTotal += (returnBasePrice * 1.1 + 450000) * totalPax;

        com.skyline.app.network.Promotion best = null;
        double maxDiscount = 0;

        for (com.skyline.app.network.Promotion p : allPromotions) {
            // Kiểm tra điều kiện cơ bản
            if (!"Active".equalsIgnoreCase(p.getStatus())) continue;
            if (p.getQuantity() <= 0) continue;
            if (subTotal < p.getMinimumOrder()) continue;
            if ("MEMBER".equalsIgnoreCase(p.getCategory()) && (currentUser == null)) continue;

            double discount = calculatePotentialDiscount(p, subTotal);
            if (discount > maxDiscount) {
                maxDiscount = discount;
                best = p;
            }
        }

        if (best != null) {
            appliedPromotion = best;
            voucherDiscountAmount = maxDiscount;
            
            // Cập nhật tag hiển thị
            binding.tvSelectedVoucherTag.setText("-" + new DecimalFormat("#,###").format(voucherDiscountAmount) + " VND");
            binding.tvSelectedVoucherTag.setTextColor(ContextCompat.getColor(this, R.color.skyline_teal));
            
            updateUI();
        }
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
        
        form.tvErrorLastName = view.findViewById(R.id.tvErrorLastName);
        form.tvErrorFirstName = view.findViewById(R.id.tvErrorFirstName);
        form.tvErrorBirthDate = view.findViewById(R.id.tvErrorBirthDate);
        form.tvErrorDocumentNo = view.findViewById(R.id.tvErrorDocumentNo);

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

        form.edtBirthDate.setFocusable(true);
        form.edtBirthDate.setClickable(true);
        form.edtBirthDate.setFocusableInTouchMode(true);
        view.findViewById(R.id.ivCalendar).setOnClickListener(v -> showBirthDatePicker(form));
        binding.containerForms.addView(view);
        passengerForms.add(form);

        // Restore draft data if index matches
        int formIdx = passengerForms.size() - 1;
        if (formIdx < passengerDrafts.size()) {
            PassengerData data = passengerDrafts.get(formIdx);
            form.edtLastName.setText(data.lastName);
            form.edtFirstName.setText(data.firstName);
            form.edtBirthDate.setText(data.dob);
            form.edtDocumentNo.setText(data.docNo);
            if ("Bà".equalsIgnoreCase(data.gender) || "Bé gái".equalsIgnoreCase(data.gender)) {
                ((RadioButton)form.rgGender.findViewById(R.id.rbMs)).setChecked(true);
            }
            // Spinner restoration would need position mapping, for now let's focus on text
        }
    }

    private boolean validateFields() {
        Calendar today = Calendar.getInstance();
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.US);
        boolean allValid = true;

        for (int i = 0; i < passengerForms.size(); i++) {
            PassengerForm f = passengerForms.get(i);
            String lastName = f.edtLastName.getText().toString().trim();
            String firstName = f.edtFirstName.getText().toString().trim();
            String dobStr = f.edtBirthDate.getText().toString().trim();
            String docNo = f.edtDocumentNo.getText().toString().trim();

            // Reset errors
            f.tvErrorLastName.setVisibility(View.GONE);
            f.tvErrorFirstName.setVisibility(View.GONE);
            f.tvErrorBirthDate.setVisibility(View.GONE);
            f.tvErrorDocumentNo.setVisibility(View.GONE);
            f.edtLastName.setBackgroundResource(R.drawable.bg_auth_input);
            f.edtFirstName.setBackgroundResource(R.drawable.bg_auth_input);
            f.edtBirthDate.setBackgroundResource(R.drawable.bg_auth_input);
            f.edtDocumentNo.setBackgroundResource(R.drawable.bg_auth_input);

            if (lastName.isEmpty()) {
                showInlineError(f.edtLastName, f.tvErrorLastName, "Họ không được để trống");
                allValid = false;
            }
            if (firstName.isEmpty()) {
                showInlineError(f.edtFirstName, f.tvErrorFirstName, "Tên không được để trống");
                allValid = false;
            }
            if (dobStr.isEmpty()) {
                showInlineError(f.edtBirthDate, f.tvErrorBirthDate, "Vui lòng chọn ngày sinh");
                allValid = false;
            }

            if (docNo.isEmpty()) {
                showInlineError(f.edtDocumentNo, f.tvErrorDocumentNo, "Số giấy tờ không được để trống");
                allValid = false;
            } else {
                String docType = f.spDocumentType.getSelectedItem().toString();
                if (docType.equals("CCCD") && docNo.length() != 12) {
                    showInlineError(f.edtDocumentNo, f.tvErrorDocumentNo, "CCCD phải đủ 12 chữ số");
                    allValid = false;
                }
            }

            if (!dobStr.isEmpty()) {
                try {
                    Date birthDate = sdf.parse(dobStr);
                    Calendar birthCal = Calendar.getInstance();
                    if (birthDate != null) {
                        birthCal.setTime(birthDate);
                        int age = today.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
                        if (today.get(Calendar.DAY_OF_YEAR) < birthCal.get(Calendar.DAY_OF_YEAR)) age--;

                        if (f.type.equals("ADULT") && age < 12) {
                            showInlineError(f.edtBirthDate, f.tvErrorBirthDate, "Người lớn phải từ 12 tuổi trở lên");
                            allValid = false;
                        }
                        if (f.type.equals("CHILD") && age >= 12) {
                            showInlineError(f.edtBirthDate, f.tvErrorBirthDate, "Trẻ em phải dưới 12 tuổi");
                            allValid = false;
                        }
                    }
                } catch (Exception ignored) {}
            }
        }

        // Contact Info Validation
        binding.tvErrorEmail.setVisibility(View.GONE);
        binding.tvErrorPhone.setVisibility(View.GONE);
        binding.edtEmail.setBackgroundResource(R.drawable.bg_auth_input);
        binding.edtPhone.setBackgroundResource(R.drawable.bg_auth_input);

        String email = binding.edtEmail.getText().toString().trim();
        if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            showInlineError(binding.edtEmail, binding.tvErrorEmail, "Email không hợp lệ (ví dụ: name@gmail.com)");
            allValid = false;
        }

        String phone = binding.edtPhone.getText().toString().trim();
        if (phone.isEmpty() || phone.length() < 10 || phone.length() > 11) {
            showInlineError(binding.edtPhone, binding.tvErrorPhone, "Số điện thoại phải từ 10 đến 11 chữ số");
            allValid = false;
        }

        if (!allValid) {
            Toast.makeText(this, "Vui lòng kiểm tra lại thông tin bị lỗi", Toast.LENGTH_SHORT).show();
        }
        return allValid;
    }

    private void showInlineError(View input, TextView errorTv, String msg) {
        errorTv.setText(msg);
        errorTv.setVisibility(View.VISIBLE);
        // Set red tint to the background border
        if (input.getBackground() != null) {
            input.getBackground().mutate().setColorFilter(Color.RED, android.graphics.PorterDuff.Mode.SRC_ATOP);
        }
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
        
        // Áp dụng giảm giá Voucher
        if (voucherDiscountAmount > 0) {
            binding.layoutVoucherDiscount.setVisibility(View.VISIBLE);
            binding.txtVoucherDiscount.setText("- " + df.format(voucherDiscountAmount) + " VND");
            grandTotal -= voucherDiscountAmount;
        } else {
            binding.layoutVoucherDiscount.setVisibility(View.GONE);
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
        tvSeat.setText(seat.isEmpty() ? "SGN_1A" : seat);
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
                    selectedSeats.set(idx, seat);
                    updateUI();
                });
            }
            if (isRoundTrip && returnSelectedSeats.get(i).isEmpty()) {
                final int idx = i;
                fetchRandomSeat(returnFlight, returnFareType, seat -> {
                    returnSelectedSeats.set(idx, seat);
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
                    currentUser = response.body();
                    User user = currentUser;
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
        
        // Trừ thêm tiền Voucher
        grandTotal -= voucherDiscountAmount;

        intent.putExtra("totalAmount", grandTotal);
        intent.putExtra("passenger_email", binding.edtEmail.getText().toString().trim());
        intent.putExtra("passenger_phone", binding.edtPhone.getText().toString().trim());
        intent.putExtra("passenger_names", names);
        intent.putExtra("adults", adults);
        intent.putExtra("children", children);
        intent.putExtra("fare_type", fareType);
        intent.putExtra("isRoundTrip", isRoundTrip);
        intent.putExtra("flight_json", gson.toJson(flight));
        
        // Chuỗi ghế cách nhau dấu phẩy cho ConfirmPaymentActivity xử lý
        StringBuilder sbSeats = new StringBuilder();
        for (int i = 0; i < selectedSeats.size(); i++) {
            sbSeats.append(selectedSeats.get(i)).append(i == selectedSeats.size() - 1 ? "" : ", ");
        }
        intent.putExtra("selected_seat", sbSeats.toString());

        // Pass detailed fees for Price Detail dialog
        intent.putExtra("baseFare", (baseFarePrice * totalPax) + (isRoundTrip ? returnBasePrice * totalPax : 0));
        intent.putExtra("taxes", (baseFarePrice * 0.1 * totalPax) + (isRoundTrip ? returnBasePrice * 0.1 * totalPax : 0));
        intent.putExtra("airportFees", (450000.0 * totalPax) + (isRoundTrip ? 450000.0 * totalPax : 0));
        intent.putExtra("addonPrice", totalAddons);
        intent.putExtra("voucher_discount", voucherDiscountAmount);
        intent.putExtra("roundTripDiscount", roundTripDiscount);

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

    private void showVoucherDetailPopup(com.skyline.app.network.Promotion item) {
        android.app.Dialog dialog = new android.app.Dialog(this);
        View view = getLayoutInflater().inflate(R.layout.layout_promotion_detail, null);
        dialog.setContentView(view);
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT));
            dialog.getWindow().setLayout(android.view.ViewGroup.LayoutParams.MATCH_PARENT, android.view.ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        TextView tvTitle = view.findViewById(R.id.tv_title);
        TextView tvDesc = view.findViewById(R.id.tv_desc);
        TextView tvCode = view.findViewById(R.id.tv_code);
        TextView tvExpiry = view.findViewById(R.id.tv_expiry);
        ImageView imgPromo = view.findViewById(R.id.img_promo);
        
        tvTitle.setText(item.getTitle());
        
        String rawDesc = item.getDescription();
        String formattedDesc = (rawDesc != null) ? rawDesc.replace("\\n", "\n").replace(". ", ".\n\n") : "";
        tvDesc.setText(formattedDesc);

        tvCode.setText(item.getCode());
        tvExpiry.setText("Hạn dùng: " + item.getExpiryDate());

        String imageUrl = item.getImageUrl();
        int placeholderRes = R.drawable.img_brand_banner;
        if (item.getCategory().contains("MEMBER")) placeholderRes = R.drawable.img_experience_first;

        if (imageUrl != null && !imageUrl.isEmpty()) {
            com.bumptech.glide.Glide.with(this)
                    .load(item.getFullImageUrl())
                    .placeholder(placeholderRes)
                    .into(imgPromo);
        } else {
            imgPromo.setImageResource(placeholderRes);
        }

        view.findViewById(R.id.btn_copy).setOnClickListener(v -> {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(android.content.Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText("Voucher Code", item.getCode());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(this, "Đã sao chép mã ưu đãi", Toast.LENGTH_SHORT).show();
        });

        view.findViewById(R.id.btn_close).setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    private Date parseIsoDate(String iso) {
        if (iso == null) return null;
        String[] pts = {"yyyy-MM-dd'T'HH:mm:ssXXX", "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ss"};
        for (String p : pts) { try { SimpleDateFormat f = new SimpleDateFormat(p, Locale.US); f.setTimeZone(TimeZone.getTimeZone("UTC")); return f.parse(iso); } catch (Exception e) {} }
        return null;
    }
}
