package com.skyline.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;
import com.bumptech.glide.Glide;
import com.skyline.app.databinding.FragmentTicketDetailBinding;
import com.skyline.app.network.ApiService;
import com.skyline.app.network.BaseResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.app.utils.SessionManager;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TicketDetailFragment extends Fragment {

    private FragmentTicketDetailBinding binding;
    private boolean isUnlocked = false;
    private String bookingCode = "";
    private SessionManager sessionManager;

    public static TicketDetailFragment newInstance(String flightNo, String originCode, String originCity, 
                                                   String destCode, String destCity, String date, String time, String seat, String passengerName, double totalAmount) {
        TicketDetailFragment fragment = new TicketDetailFragment();
        Bundle args = new Bundle();
        args.putString("flightNo", flightNo);
        args.putString("originCode", originCode);
        args.putString("originCity", originCity);
        args.putString("destCode", destCode);
        args.putString("destCity", destCity);
        args.putString("date", date);
        args.putString("time", time);
        args.putString("seat", seat);
        args.putString("passengerName", passengerName);
        args.putDouble("totalAmount", totalAmount);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTicketDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        sessionManager = new SessionManager(requireContext());

        if (getArguments() != null) {
            bookingCode = getArguments().getString("flightNo");
            if (bookingCode == null || bookingCode.isEmpty()) {
                bookingCode = "SKYLINE_" + System.currentTimeMillis() % 10000;
            }
            binding.tvTicketId.setText(bookingCode);
            binding.tvOriginCode.setText(getArguments().getString("originCode"));
            binding.tvOriginCity.setText(getArguments().getString("originCity"));
            binding.tvDestCode.setText(getArguments().getString("destCode"));
            binding.tvDestCity.setText(getArguments().getString("destCity"));
            binding.tvDate.setText(getArguments().getString("date"));
            binding.tvPassenger.setText(getArguments().getString("passengerName"));
            binding.tvTimeRange.setText(getArguments().getString("time") + " - 10:40");
            binding.tvSeatClass.setText(getArguments().getString("seat") + " / Phổ thông");
            
            loadRandomQR(bookingCode);
        }

        setupClickListeners();
        
        binding.ivQR.setAlpha(0.6f);
        binding.btnUnlock.setVisibility(View.VISIBLE);
    }

    private void loadRandomQR(String code) {
        if (code == null || code.isEmpty()) code = "SKYLINE_TICKET";
        // Mã hóa URL để tránh lỗi với các ký tự đặc biệt
        String encodedCode = android.net.Uri.encode(code);
        String qrUrl = "https://api.qrserver.com/v1/create-qr-code/?size=250x250&data=" + encodedCode;
        
        Glide.with(this)
            .load(qrUrl)
            .placeholder(R.drawable.bg_square_placeholder)
            .error(R.drawable.bg_square_placeholder) // Thêm ảnh lỗi
            .into(binding.ivQR);
    }

    private void showEmailConfirmationDialog() {
        if (!isUnlocked) {
            Toast.makeText(requireContext(), "Vui lòng mở khóa QR trước khi nhận vé", Toast.LENGTH_LONG).show();
            return;
        }

        String userEmail = sessionManager.getUserEmail();
        if (userEmail == null || userEmail.isEmpty()) {
            Toast.makeText(requireContext(), "Không tìm thấy email đăng ký của bạn", Toast.LENGTH_LONG).show();
            return;
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("Nhận vé qua Email")
                .setMessage("Bạn có muốn nhận thông tin vé chi tiết qua email " + userEmail + " không?")
                .setPositiveButton("Gửi ngay", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Đang chuẩn bị gửi vé đến email của bạn...", Toast.LENGTH_SHORT).show();
                    sendTicketEmailAutomatically(userEmail);
                })
                .setNegativeButton("Để sau", (dialog, which) -> {
                    Toast.makeText(requireContext(), "Bạn đã chọn để sau. Đừng quên nhận vé trước khi bay nhé!", Toast.LENGTH_SHORT).show();
                    sessionManager.addLocalNotification("Nhận vé điện tử", "Bạn đã từ chối nhận vé qua email cho mã đặt chỗ " + bookingCode + ". Bạn có thể nhận lại bất cứ lúc nào.");
                })
                .show();
    }

    private void sendTicketEmailAutomatically(String email) {
        if (getArguments() == null) return;

        Map<String, Object> ticketData = new HashMap<>();
        ticketData.put("bookingCode", bookingCode);
        ticketData.put("passengerName", getArguments().getString("passengerName"));
        ticketData.put("origin", getArguments().getString("originCity"));
        ticketData.put("originCode", getArguments().getString("originCode"));
        ticketData.put("destination", getArguments().getString("destCity"));
        ticketData.put("destCode", getArguments().getString("destCode"));
        ticketData.put("date", getArguments().getString("date"));
        ticketData.put("time", getArguments().getString("time"));
        ticketData.put("seat", getArguments().getString("seat"));

        Map<String, Object> body = new HashMap<>();
        body.put("ticketData", ticketData);

        String token = "Bearer " + sessionManager.fetchAuthToken();
        RetrofitClient.getInstance().sendTicketEmail(token, body).enqueue(new Callback<BaseResponse>() {
            @Override
            public void onResponse(Call<BaseResponse> call, Response<BaseResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    Toast.makeText(requireContext(), "Vé đã được gửi thành công đến " + email, Toast.LENGTH_LONG).show();
                    sessionManager.addLocalNotification("Gửi vé thành công", "Vé điện tử cho mã đặt chỗ " + bookingCode + " đã được gửi đến " + email);
                } else {
                    Toast.makeText(requireContext(), "Gửi mail thất bại. Vui lòng thử lại sau.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<BaseResponse> call, Throwable t) {
                Toast.makeText(requireContext(), "Lỗi kết nối: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupClickListeners() {
        binding.btnBack.setOnClickListener(v -> getParentFragmentManager().popBackStack());
        binding.btnClose.setOnClickListener(v -> getParentFragmentManager().popBackStack());

        binding.btnUnlock.setOnClickListener(v -> {
            if (!isUnlocked) {
                isUnlocked = true;
                binding.ivQR.animate().alpha(1.0f).setDuration(400).start();
                binding.btnUnlock.animate().alpha(0f).scaleX(0.5f).scaleY(0.5f).setDuration(300)
                    .withEndAction(() -> binding.btnUnlock.setVisibility(View.GONE)).start();
                Toast.makeText(requireContext(), "Đã mở khóa vé", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSave.setOnClickListener(v -> saveTicketAsImage());
        binding.btnShare.setOnClickListener(v -> showEmailConfirmationDialog());

        binding.btnCancel.setOnClickListener(v -> {
            if (getArguments() != null) {
                CancelTicketFragment fragment = CancelTicketFragment.newInstance(
                    getArguments().getString("flightNo"),
                    getArguments().getString("originCode"),
                    getArguments().getString("originCity"), 
                    getArguments().getString("destCode"),
                    getArguments().getString("destCity"),
                    getArguments().getString("date"),
                    getArguments().getString("time"),
                    0 // old price logic could be added here
                );
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
            }
        });

        binding.btnEdit.setOnClickListener(v -> {
            if (getArguments() != null) {
                ChangeTicketFragment fragment = ChangeTicketFragment.newInstance(
                    getArguments().getString("flightNo"),
                    getArguments().getString("originCode"),
                    getArguments().getString("destCode"),
                    getArguments().getDouble("totalAmount", 0)
                );
                getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, fragment)
                    .addToBackStack(null)
                    .commit();
            }
        });
    }

    private void saveTicketAsImage() {
        if (!isUnlocked) {
            Toast.makeText(requireContext(), "Vui lòng mở khóa QR trước khi lưu", Toast.LENGTH_LONG).show();
            return;
        }

        binding.btnSave.setVisibility(View.INVISIBLE);
        binding.btnShare.setVisibility(View.INVISIBLE);
        binding.layoutHeader.setVisibility(View.INVISIBLE);

        View view = binding.getRoot();
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.drawColor(Color.WHITE);
        view.draw(canvas);

        binding.btnSave.setVisibility(View.VISIBLE);
        binding.btnShare.setVisibility(View.VISIBLE);
        binding.layoutHeader.setVisibility(View.VISIBLE);

        try {
            String fileName = "Skyline_Ticket_" + bookingCode + ".png";
            ContentOutputStream(bitmap, fileName);
            Toast.makeText(requireContext(), "Đã lưu vé vào bộ sưu tập ảnh!", Toast.LENGTH_LONG).show();
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi khi lưu ảnh: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void shareTicketAsPdf() {
        if (!isUnlocked) {
            Toast.makeText(requireContext(), "Vui lòng mở khóa QR trước khi chia sẻ", Toast.LENGTH_LONG).show();
            return;
        }

        PdfDocument document = new PdfDocument();
        binding.btnSave.setVisibility(View.INVISIBLE);
        binding.btnShare.setVisibility(View.INVISIBLE);
        binding.layoutHeader.setVisibility(View.INVISIBLE);

        View view = binding.getRoot();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getWidth(), view.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawColor(Color.WHITE);
        view.draw(canvas);
        document.finishPage(page);

        binding.btnSave.setVisibility(View.VISIBLE);
        binding.btnShare.setVisibility(View.VISIBLE);
        binding.layoutHeader.setVisibility(View.VISIBLE);

        File cachePath = new File(requireContext().getCacheDir(), "tickets");
        if (!cachePath.exists()) cachePath.mkdirs();
        File file = new File(cachePath, "Skyline_Ticket_" + bookingCode + ".pdf");

        try (FileOutputStream out = new FileOutputStream(file)) {
            document.writeTo(out);
            document.close();

            Uri contentUri = FileProvider.getUriForFile(requireContext(), 
                    requireContext().getPackageName() + ".fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                
                String userEmail = sessionManager.getUserEmail();
                if (userEmail != null && !userEmail.isEmpty()) {
                    shareIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{userEmail});
                }
                
                String passengerName = binding.tvPassenger.getText().toString();
                String origin = binding.tvOriginCity.getText().toString();
                String dest = binding.tvDestCity.getText().toString();
                String time = binding.tvTimeRange.getText().toString();

                String emailSubject = "[Skyline Airways] Thông tin vé điện tử - Mã đặt chỗ: " + bookingCode;
                String emailBody = "Chào " + passengerName + ",\n\n" +
                        "Cảm ơn bạn đã lựa chọn Skyline Airways cho hành trình của mình.\n" +
                        "Chúng tôi xin gửi kèm theo đây vé điện tử (PDF) cho chuyến bay của bạn.\n\n" +
                        "Thông tin tóm tắt:\n" +
                        "- Mã đặt chỗ: " + bookingCode + "\n" +
                        "- Hành trình: " + origin + " - " + dest + "\n" +
                        "- Giờ khởi hành: " + time + "\n\n" +
                        "Vui lòng mang theo vé này (bản in hoặc điện tử) cùng giấy tờ tùy thân để làm thủ tục.\n\n" +
                        "Chúc bạn có một chuyến bay tốt đẹp!\n" +
                        "Đội ngũ Skyline Airways.";

                shareIntent.putExtra(Intent.EXTRA_SUBJECT, emailSubject);
                shareIntent.putExtra(Intent.EXTRA_TEXT, emailBody);

                startActivity(Intent.createChooser(shareIntent, "Chia sẻ vé qua PDF/Email"));
            }
        } catch (Exception e) {
            Toast.makeText(requireContext(), "Lỗi khi tạo PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void ContentOutputStream(Bitmap bitmap, String fileName) throws Exception {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {
            android.content.ContentValues values = new android.content.ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/png");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/Skyline");

            android.net.Uri uri = requireContext().getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
            if (uri != null) {
                try (OutputStream out = requireContext().getContentResolver().openOutputStream(uri)) {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
                }
            }
        } else {
            File path = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "Skyline");
            if (!path.exists()) path.mkdirs();
            File file = new File(path, fileName);
            try (FileOutputStream out = new FileOutputStream(file)) {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            }
            MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), file.getAbsolutePath(), fileName, null);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
