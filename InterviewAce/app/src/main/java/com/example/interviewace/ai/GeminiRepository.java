package com.example.interviewace.ai;

import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;

import com.example.interviewace.BuildConfig;

import java.util.Collections;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GeminiRepository {

    private final GeminiApiService apiService;
    private static final String API_KEY = BuildConfig.GEMINI_API_KEY;
    
    private boolean isRequestInProgress = false;
    private long lastRequestTime = 0;
    private static final long MIN_DELAY_MS = 10000; // 10 seconds delay

    // constructor
    public GeminiRepository() {
        apiService = GeminiApiClient.getClient().create(GeminiApiService.class);
    }

    public interface GeminiCallback {
        void onSuccess(String resultText);
        void onError(String error);
    }

    public void GenerateFeedback(String question, String answer, GeminiCallback callback) {
        if (isRequestInProgress) {
            callback.onError("Please wait, analysis is already in progress.");
            return;
        }

        long currentTime = System.currentTimeMillis();
        long timeSinceLastRequest = currentTime - lastRequestTime;

        if (timeSinceLastRequest < MIN_DELAY_MS) {
            long waitTimeSeconds = (MIN_DELAY_MS - timeSinceLastRequest) / 1000;
            callback.onError("Please wait " + waitTimeSeconds + " seconds before the next request.");
            return;
        }

        isRequestInProgress = true;
        lastRequestTime = currentTime;

        String prompt = "You are an interview expert. Analyze the following interview answer.\n" +
                "Question: " + question + "\n" +
                "User's Answer: " + answer + "\n\n" +
                "Provide feedback in strict JSON format with the following fields:\n" +
                "overallScore (integer 0-10),\n" +
                "technicalAccuracy (integer 0-10),\n" +
                "communication (integer 0-10),\n" +
                "confidence (integer 0-10),\n" +
                "completeness (integer 0-10),\n" +
                "examples (integer 0-10),\n" +
                "resultMessage (short string like 'Great Answer!'),\n" +
                "correctPoints (list of strings),\n" +
                "missingPoints (list of strings),\n" +
                "suggestedAnswer (string).";


        GeminiRequest.Part part = new GeminiRequest.Part(prompt);
        GeminiRequest.Content content = new GeminiRequest.Content(Collections.singletonList(part));
        GeminiRequest request = new GeminiRequest(Collections.singletonList(content));

        apiService.generateContent(API_KEY, request).enqueue(new Callback<GeminiResponse>() {
            @Override
            public void onResponse(Call<GeminiResponse> call, Response<GeminiResponse> response) {
                isRequestInProgress = false;
                if (response.isSuccessful() && response.body() != null && response.body().candidates != null && !response.body().candidates.isEmpty()) {
                    String text = response.body().candidates.get(0).content.parts.get(0).text;
                    callback.onSuccess(text);
                } else {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "Unknown error";
                        if (response.code() == 429) {
                            callback.onError("Daily limit reached or too many requests. Please try again later.");
                        } else {
                            callback.onError("API Error " + response.code() + ": " + errorBody);
                        }
                    } catch (Exception e) {
                        callback.onError("Failed to generate feedback: " + response.code());
                    }
                }
            }

            @Override
            public void onFailure(Call<GeminiResponse> call, Throwable t) {
                isRequestInProgress = false;
                callback.onError(t.getMessage());
            }
        });
    }
}
