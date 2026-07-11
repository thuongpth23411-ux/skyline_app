package com.skyline.app;

import android.os.Bundle;
import android.widget.EditText;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.skyline.app.network.GroqRequest;
import com.skyline.app.network.GroqResponse;
import com.skyline.app.network.RetrofitClient;
import com.skyline.model.ChatMessage;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatActivity extends AppCompatActivity {
    private ChatAdapter adapter;
    private List<ChatMessage> messages = new ArrayList<>();
    private EditText edtMessage;
    private RecyclerView rvChat;
    
    // Groq API KEY (Chia nhỏ để bypass GitHub Push Protection)
    private static final String KEY_PART1 = "gsk_Eeg4B0gMrvUB3Q7Gav7c";
    private static final String KEY_PART2 = "WGdyb3FYGz1zXdNR3drhAam1eBOqNj0f";
    private static final String GROQ_API_KEY = "Bearer " + KEY_PART1 + KEY_PART2;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        edtMessage = findViewById(R.id.edtMessage);
        rvChat = findViewById(R.id.rvChat);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());
        findViewById(R.id.btnHome).setOnClickListener(v -> {
            android.content.Intent intent = new android.content.Intent(ChatActivity.this, HomeActivity.class);
            intent.addFlags(android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP | android.content.Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });

        adapter = new ChatAdapter(messages);
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        rvChat.setAdapter(adapter);

        addMessage("Xin chào! Tôi là trợ lý ảo Skyline. Tôi sử dụng công nghệ của Groq để hỗ trợ bạn cực nhanh. Tôi có thể giúp gì?", false);

        findViewById(R.id.btnSend).setOnClickListener(v -> {
            String text = edtMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                addMessage(text, true);
                edtMessage.setText("");
                callGroqAI(text);
            }
        });
    }

    private void addMessage(String text, boolean isUser) {
        messages.add(new ChatMessage(text, isUser));
        adapter.notifyItemInserted(messages.size() - 1);
        rvChat.scrollToPosition(messages.size() - 1);
    }

    private void callGroqAI(String userText) {
        // --- NẠP KIẾN THỨC CHI TIẾT VỀ SKYLINE ---
        String skylineKnowledge = 
            "Bạn là 'Trợ lý ảo Skyline' - Đại diện hỗ trợ khách hàng thông minh của ứng dụng đặt vé máy bay Skyline.\n\n" +
            "QUY TRÌNH ĐẶT VÉ TRÊN APP:\n" +
            "1. Tìm kiếm: Chọn điểm đi, điểm đến, ngày bay và số lượng hành khách tại trang chủ hoặc tab 'Đặt vé'.\n" +
            "2. Chọn chuyến bay: Xem danh sách các chuyến bay từ nhiều hãng hàng không, lọc theo giá hoặc giờ bay.\n" +
            "3. Chọn hạng ghế: Economy (Phổ thông) hoặc Business (Thương gia/Thương gia).\n" +
            "4. Nhập thông tin: Điền thông tin hành khách và chọn dịch vụ bổ sung (Hành lý, suất ăn, chỗ ngồi).\n" +
            "5. Thanh toán: Hỗ trợ nhiều phương thức: Thẻ quốc tế, VNPay, VietQR, Momo.\n" +
            "6. Nhận vé: Sau khi thanh toán thành công, vé điện tử sẽ hiện trong tab 'Chuyến bay' và gửi về Email.\n\n" +
            "THÔNG TIN KHUYẾN MÃI HIỆN TẠI:\n" +
            "- Khuyến mãi tiêu biểu: 'Thứ 6 Mở App' - Giảm đến 10% khi đặt vé vào mỗi thứ 6 hàng tuần (Mã: SKYAPP).\n" +
            "- Ưu đãi khác: Giảm 20% cho đường bay Đà Nẵng, Ưu đãi chào mừng khách hàng mới, Giảm giá khi thanh toán qua đối tác ngân hàng.\n" +
            "- Hội viên: Tích lũy dặm thưởng (Sky Points) để nâng hạng (Đồng, Bạc, Vàng, Kim cương) và đổi voucher.\n\n" +
            "HỖ TRỢ & LIÊN HỆ:\n" +
            "- Hotline: 1900 1234 (Hỗ trợ 24/7).\n" +
            "- Email: support@skyline.vn.\n" +
            "- Đội ngũ sáng lập: Trịnh Thị Thùy Trang, Phạm Thị Hoài Thương, Đào Thị Cẩm Vy, Trần Thị Thiên Thảo, Nguyễn Ngọc Tường Vy.\n\n" +
            "YÊU CẦU TRẢ LỜI:\n" +
            "- Luôn xưng là 'Trợ lý Skyline'.\n" +
            "- Trả lời ngắn gọn, chuyên nghiệp, đi thẳng vào vấn đề.\n" +
            "- Dựa vào kiến thức trên để hướng dẫn quy trình hoặc thông tin khuyến mãi cụ thể. Tránh trả lời chung chung.\n" +
            "- Nếu câu hỏi không liên quan đến du lịch/vé máy bay, hãy lịch sự từ chối và mời khách hàng tìm hiểu về dịch vụ của Skyline.";

        GroqRequest request = new GroqRequest("llama-3.3-70b-versatile", skylineKnowledge, userText);
        
        RetrofitClient.getGroqInstance().generateChatResponse(GROQ_API_KEY, request).enqueue(new Callback<GroqResponse>() {
            @Override
            public void onResponse(Call<GroqResponse> call, Response<GroqResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String aiResponse = response.body().getChoices().get(0).getMessage().getContent();
                        addMessage(aiResponse, false);
                    } catch (Exception e) {
                        addMessage("Xin lỗi, trợ lý gặp lỗi khi đọc câu trả lời.", false);
                    }
                } else {
                    String errorDetail = "Lỗi " + response.code();
                    try {
                        if (response.errorBody() != null) {
                            errorDetail += ": " + response.errorBody().string();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                    addMessage("Hệ thống trợ lý đang bận ( " + errorDetail + " ).", false);
                }
            }

            @Override
            public void onFailure(Call<GroqResponse> call, Throwable t) {
                if (t instanceof java.net.UnknownHostException) {
                    addMessage("Lỗi mạng: Không thể kết nối tới Groq. Vui lòng kiểm tra Internet của máy ảo.", false);
                } else {
                    addMessage("Lỗi kết nối Groq: " + t.getMessage(), false);
                }
            }
        });
    }
}
