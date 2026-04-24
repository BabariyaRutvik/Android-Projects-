package com.example.interviewace.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.interviewace.R;
import com.example.interviewace.ViewModel.InterviewViewModel;
import com.example.interviewace.ai.GeminiRepository;
import com.example.interviewace.databinding.ActivityFeedbackBinding;
import com.example.interviewace.model.AnswerModel;
import com.example.interviewace.model.FeedbackResponse;
import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.Gson;


public class FeedbackActivity extends AppCompatActivity {

    private ActivityFeedbackBinding binding;
    private GeminiRepository repository;
    private String questionText;
    private String userAnswer;
    private int currentQuestionIndex;
    private int totalQuestions;
    private String roleName;
    private String difficulty;
    private InterviewViewModel viewModel;
    private FeedbackResponse currentFeedback;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFeedbackBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(InterviewViewModel.class);
        repository = new GeminiRepository();

        getIntentDataFrom();
        setupClicks();

        if (userAnswer == null || userAnswer.isEmpty() || userAnswer.equalsIgnoreCase("skipped")){
            SkipFeedback();
        }
        else {
            AnalyseAnswer();
        }
    }

    private void getIntentDataFrom(){
        questionText = getIntent().getStringExtra("questionText");
        userAnswer = getIntent().getStringExtra("userAnswer");
        currentQuestionIndex = getIntent().getIntExtra("currentQuestionIndex",0);
        totalQuestions = getIntent().getIntExtra("totalQuestions",0);
        roleName = getIntent().getStringExtra("roleName");
        difficulty = getIntent().getStringExtra("difficulty");

        binding.tvQuestionNumber.setText("Q" + (currentQuestionIndex + 1) + " of " + totalQuestions);
    }

    private void setupClicks(){
        binding.ivBack.setOnClickListener(view -> {
            finish();
        });
        binding.btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveFeedbackToFirestore();
                Intent resultIntent = new Intent();
                resultIntent.putExtra("shouldGoNext", true);
                resultIntent.putExtra("score", currentFeedback != null ? currentFeedback.getOverallScore() : 0);
                setResult(RESULT_OK, resultIntent);
                finish();
            }
        });
    }

    private void saveFeedbackToFirestore() {
        if (currentFeedback == null) return;
        
        String userId = FirebaseAuth.getInstance().getUid();
        AnswerModel answerModel = new AnswerModel(
                questionText,
                userAnswer,
                roleName,
                difficulty,
                System.currentTimeMillis(),
                userId
        );
        answerModel.setFeedback(currentFeedback);
        viewModel.submitAnswer(answerModel);
    }

    private void AnalyseAnswer(){
        binding.loadingLayout.setVisibility(View.VISIBLE);
        binding.feedbackScrollview.setVisibility(View.GONE);

        repository.GenerateFeedback(questionText, userAnswer, new GeminiRepository.GeminiCallback() {
            @Override
            public void onSuccess(String resultText) {
                String jsonResult = resultText;
                if (jsonResult.contains("```json")) {
                    jsonResult = jsonResult.substring(jsonResult.indexOf("```json") + 7, jsonResult.lastIndexOf("```"));
                } else if (jsonResult.contains("```")) {
                    jsonResult = jsonResult.substring(jsonResult.indexOf("```") + 3, jsonResult.lastIndexOf("```"));
                }
                String finaljson = jsonResult;

                new Handler(Looper.getMainLooper()).post(()->{
                   try {
                       currentFeedback = new Gson().fromJson(finaljson, FeedbackResponse.class);
                       DisplayFeedback(currentFeedback);
                   } catch (Exception e){
                       Toast.makeText(FeedbackActivity.this, "Error parsing AI response", Toast.LENGTH_SHORT).show();
                       binding.loadingLayout.setVisibility(View.GONE);
                       binding.feedbackScrollview.setVisibility(View.VISIBLE);
                   }
                });
            }

            @Override
            public void onError(String error) {
                   new Handler(Looper.getMainLooper()).post(()->{
                       Toast.makeText(FeedbackActivity.this, "AI Analysis failed: " + error, Toast.LENGTH_SHORT).show();
                       binding.loadingLayout.setVisibility(View.GONE);
                       binding.feedbackScrollview.setVisibility(View.VISIBLE);
                   });
            }
        });
    }

    private void DisplayFeedback(FeedbackResponse feedbackResponse){
        binding.loadingLayout.setVisibility(View.GONE);
        binding.feedbackScrollview.setVisibility(View.VISIBLE);

        binding.tvScore.setText(feedbackResponse.getOverallScore() + "/10");
        binding.progressScore.setProgress(feedbackResponse.getOverallScore());
        binding.tvResult.setText(feedbackResponse.getResultMessage());

        binding.progressTechnicalAccuracy.setProgress(feedbackResponse.getTechnicalAccuracy());
        binding.textTechnicalAccuracyScore.setText(feedbackResponse.getTechnicalAccuracy() + "/10");

        binding.progressCommunication.setProgress(feedbackResponse.getCommunication());
        binding.textCommunicationScore.setText(feedbackResponse.getCommunication() + "/10");

        binding.progressConfidence.setProgress(feedbackResponse.getConfidence());
        binding.textConfidenceScore.setText(feedbackResponse.getConfidence() + "/10");

        binding.progressCompleteness.setProgress(feedbackResponse.getCompleteness());
        binding.textCompletenessScore.setText(feedbackResponse.getCompleteness() + "/10");

        binding.progressExamples.setProgress(feedbackResponse.getExamples());
        binding.textExamplesScore.setText(feedbackResponse.getExamples() + "/10");

        binding.llCorrectPointsContainer.removeAllViews();
        if (feedbackResponse.getCorrectPoints() != null) {
            for (String point : feedbackResponse.getCorrectPoints()) {
                addPointView(binding.llCorrectPointsContainer, point, R.drawable.bg_point_correct, ContextCompat.getColor(this, R.color.success_green));
            }
        }

        binding.llMissingPointsContainer.removeAllViews();
        if (feedbackResponse.getMissingPoints() != null) {
            for (String point : feedbackResponse.getMissingPoints()) {
                addPointView(binding.llMissingPointsContainer, point, R.drawable.bg_point_missing, ContextCompat.getColor(this, R.color.warning_orange));
            }
        }
        binding.tvSuggestedAnswer.setText(feedbackResponse.getSuggestedAnswer());
    }

    private void addPointView(LinearLayout container, String text, int backgroundResId, int textColor) {
        TextView textView = new TextView(this);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 8);
        textView.setLayoutParams(params);
        textView.setBackgroundResource(backgroundResId);
        textView.setPadding(32, 32, 32, 32);
        textView.setText("• " + text);
        textView.setTextColor(textColor);
        container.addView(textView);
    }

    private void SkipFeedback() {
        currentFeedback = new FeedbackResponse();
        currentFeedback.setOverallScore(0);
        currentFeedback.setResultMessage("Question Skipped");
        currentFeedback.setSuggestedAnswer("You skipped this question. Try to answer next time to get detailed feedback!");
        DisplayFeedback(currentFeedback);
    }
}
