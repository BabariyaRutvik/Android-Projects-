package com.example.quickbazaar.QuickActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quickbazaar.BazaarAdapter.ChatAdapter;
import com.example.quickbazaar.BazaarModel.ChatMessage;
import com.example.quickbazaar.BuildConfig;
import com.example.quickbazaar.Network.GeminiApiClient;
import com.example.quickbazaar.Network.GeminiRequest;
import com.example.quickbazaar.Network.GeminiResponse;
import com.example.quickbazaar.databinding.ActivityAichatBinding;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AIChatActivity extends AppCompatActivity {

    private ActivityAichatBinding binding;
    private final String API_KEY = BuildConfig.GEMINI_API_KEY;


    private ChatAdapter chatAdapter;
    private List<ChatMessage> chatMessages;
    private FirebaseFirestore db;
    private String currentUserId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAichatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        db = FirebaseFirestore.getInstance();
        if (FirebaseAuth.getInstance().getCurrentUser() != null) {
            currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        }

        setupRecyclerView();

        binding.btnBackAi.setOnClickListener(v -> finish());

        binding.btnSendAi.setOnClickListener(v -> {
            String query = binding.etAiQuery.getText().toString().trim();
            if (!query.isEmpty()) {
                addUserMessage(query);
                askGemini(query);
            } else {
                Toast.makeText(this, "Please enter a question", Toast.LENGTH_SHORT).show();
            }
        });

        // Initial Greeting
        addAiMessage("Hello! 👋 I am your personal QuickBazaar assistant. Ask me anything about cooking, health, or our products!");
    }

    private void setupRecyclerView() {
        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(chatMessages);
        binding.recyclerViewChat.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerViewChat.setAdapter(chatAdapter);
    }

    private void addUserMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, ChatMessage.TYPE_USER);
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        binding.recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
        binding.etAiQuery.setText("");
        saveMessageToFirestore(chatMessage);
    }

    private void addAiMessage(String message) {
        ChatMessage chatMessage = new ChatMessage(message, ChatMessage.TYPE_AI);
        chatMessages.add(chatMessage);
        chatAdapter.notifyItemInserted(chatMessages.size() - 1);
        binding.recyclerViewChat.scrollToPosition(chatMessages.size() - 1);
        saveMessageToFirestore(chatMessage);
    }

    private void saveMessageToFirestore(ChatMessage chatMessage) {
        if (currentUserId != null) {
            db.collection("users").document(currentUserId)
                    .collection("ai_chats").add(chatMessage)
                    .addOnFailureListener(e -> Log.e("AIChatActivity", "Error saving chat", e));
        }
    }

    private void askGemini(String query) {
        if (binding == null) return;
        binding.progressBarAi.setVisibility(View.VISIBLE);

        GeminiRequest request = new GeminiRequest(query);

        GeminiApiClient.getApiService().generateContent(API_KEY, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(@NonNull Call<GeminiResponse> call, @NonNull Response<GeminiResponse> response) {
                if (isFinishing() || isDestroyed() || binding == null) return;
                binding.progressBarAi.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    GeminiResponse geminiResponse = response.body();
                    if (geminiResponse.getCandidates() != null && !geminiResponse.getCandidates().isEmpty() 
                            && geminiResponse.getCandidates().get(0).getContent() != null) {
                        String text = geminiResponse.getCandidates().get(0).getContent().getParts().get(0).getText();
                        addAiMessage(text);
                    } else {
                        // Suppress error codes/messages for empty responses
                        Log.w("AIChatActivity", "Gemini returned no candidates");
                    }
                } else {
                    handleError(response);
                }
            }

            @Override
            public void onFailure(@NonNull Call<GeminiResponse> call, @NonNull Throwable t) {
                if (isFinishing() || isDestroyed() || binding == null) return;
                binding.progressBarAi.setVisibility(View.GONE);
                Log.e("AIChatActivity", "Failure: " + t.getMessage());
                addAiMessage("Connection issue. Please check your internet and try again.");
            }
        });
    }

    private void handleError(Response<GeminiResponse> response) {
        if (isFinishing() || isDestroyed() || binding == null) return;
        Log.e("AIChatActivity", "Error Code: " + response.code());
        addAiMessage("I'm sorry, I couldn't process your request. Please try again later.");
    }
}
