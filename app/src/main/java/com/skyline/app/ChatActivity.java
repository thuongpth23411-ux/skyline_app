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
    
    // Groq API KEY
    private static final String GROQ_API_KEY = "Bearer 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";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        edtMessage = findViewById(R.id.edtMessage);
        rvChat = findViewById(R.id.rvChat);
        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

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
        // Sử dụng mô hình Llama 3.3 70B mạnh mẽ và ổn định của Groq
        GroqRequest request = new GroqRequest("llama-3.3-70b-versatile", userText);
        
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
