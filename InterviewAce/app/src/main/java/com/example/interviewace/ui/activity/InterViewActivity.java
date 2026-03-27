package com.example.interviewace.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.speech.RecognizerIntent;
import android.speech.tts.TextToSpeech;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.example.interviewace.R;
import com.example.interviewace.ViewModel.InterviewViewModel;
import com.example.interviewace.databinding.ActivityInterViewBinding;
import com.example.interviewace.model.AnswerModel;
import com.example.interviewace.model.QuestionItem;
import com.example.interviewace.model.SessionItem;
import com.google.firebase.auth.FirebaseAuth;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class InterViewActivity extends AppCompatActivity {

    private ActivityInterViewBinding binding;
    private InterviewViewModel viewModel;
    private String roleName, difficulty;
    private List<QuestionItem> questionItems;
    private int currentQuestionIndex = 0;

    private TextToSpeech textToSpeech;
    private boolean isTtsInitialized = false;
    private String pendingQuestion = null;
    
    private CountDownTimer timer;
    private String selectedLanguage = "en";
    private boolean isMuted = false;
    private int totalScore = 0;

    private SharedPreferences sharedPreferences;
    private static final String PREF_NAME = "InterviewPrefs";
    private static final String KEY_INDEX = "last_question_index";
    private static final String KEY_ROLE = "last_role";
    private static final String KEY_DIFFICULTY = "last_difficulty";

    // Handling activity result for feedback
    private final ActivityResultLauncher<Intent> feedbackLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    boolean shouldGoNext = result.getData().getBooleanExtra("shouldGoNext", false);
                    int score = result.getData().getIntExtra("score", 0);
                    totalScore += score;
                    
                    if (shouldGoNext) {
                        goNext();
                    }
                }
            });

    // Handling activity result for speech recognition
    private final ActivityResultLauncher<Intent> speechLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    ArrayList<String> matches = result.getData().getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
                    if (matches != null && !matches.isEmpty()) {
                        String answer = matches.get(0);
                        navigateToFeedback(answer);
                    }
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInterViewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        viewModel = new ViewModelProvider(this).get(InterviewViewModel.class);
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

        getIntentData();
        initTTS();
        observeViewModel();
        SetupClicks();
        setupTextAnswer();

        if (viewModel.getQuestions().getValue() == null || viewModel.getQuestions().getValue().isEmpty()) {
            viewModel.LoadQuestions(roleName, difficulty);
        }
    }

    private void setupTextAnswer() {
        binding.etAnswer.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 0) {
                    binding.btnSubmitText.setVisibility(View.VISIBLE);
                    binding.btnMic.setVisibility(View.GONE);
                    binding.tvMicHint.setVisibility(View.GONE);
                } else {
                    binding.btnSubmitText.setVisibility(View.GONE);
                    binding.btnMic.setVisibility(View.VISIBLE);
                    binding.tvMicHint.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.btnSubmitText.setOnClickListener(v -> {
            String answer = binding.etAnswer.getText().toString().trim();
            if (!answer.isEmpty()) {
                hideKeyboard();
                navigateToFeedback(answer);
            }
        });
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void getIntentData() {
        if (getIntent() != null) {
            roleName = getIntent().getStringExtra("roleName");
            difficulty = getIntent().getStringExtra("difficulty");
            String language = getIntent().getStringExtra("language");
            if (language != null) selectedLanguage = language;
        }

        // Check if there is saved progress for the same role and difficulty
        String savedRole = sharedPreferences.getString(KEY_ROLE, "");
        String savedDifficulty = sharedPreferences.getString(KEY_DIFFICULTY, "");
        
        if (roleName != null && roleName.equals(savedRole) && difficulty != null && difficulty.equals(savedDifficulty)) {
            currentQuestionIndex = sharedPreferences.getInt(KEY_INDEX, 0);
            viewModel.setCustomQuestionIndex(currentQuestionIndex);
        }
    }

    private void observeViewModel() {
        viewModel.getQuestions().observe(this, questionItems1 -> {
            if (questionItems1 != null && !questionItems1.isEmpty()) {
                questionItems = questionItems1;
                ShowQuestion();
            } else if (questionItems1 != null) {
                Toast.makeText(this, "No Questions Found", Toast.LENGTH_SHORT).show();
                finish();
            }
        });

        viewModel.getCustomQuestionIndex().observe(this, index -> {
            currentQuestionIndex = index;
            saveProgress(); // Save progress whenever index changes
            ShowQuestion();
        });

        viewModel.getErrorMessage().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }

    private void saveProgress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(KEY_INDEX, currentQuestionIndex);
        editor.putString(KEY_ROLE, roleName);
        editor.putString(KEY_DIFFICULTY, difficulty);
        editor.apply();
    }

    private void clearProgress() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(KEY_INDEX);
        editor.remove(KEY_ROLE);
        editor.remove(KEY_DIFFICULTY);
        editor.apply();
    }

    private Locale getLocaleForLanguage() {
        switch (selectedLanguage) {
            case "hi":
                return new Locale("hi", "IN");
            case "gu":
                return new Locale("gu", "IN");
            default:
                return Locale.ENGLISH;
        }
    }

    private void initTTS() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                textToSpeech.setLanguage(getLocaleForLanguage());
                isTtsInitialized = true;
                // If a question was waiting for initialization, speak it now
                if (pendingQuestion != null) {
                    SpeakQuestion(pendingQuestion);
                    pendingQuestion = null;
                }
            } else {
                Toast.makeText(this, "TTS Initialization failed!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void SpeakQuestion(String text) {
        if (isMuted) return;

        if (textToSpeech != null && isTtsInitialized) {
            textToSpeech.setLanguage(getLocaleForLanguage());
            textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null, "InterviewTTS");
        } else {
            // Store it to speak once initialization is done
            pendingQuestion = text;
        }
    }

    private void ShowQuestion() {
        if (questionItems == null || questionItems.isEmpty()) return;
        
        // Safety check to prevent IndexOutOfBounds
        if (currentQuestionIndex >= questionItems.size()) {
            currentQuestionIndex = 0;
            viewModel.setCustomQuestionIndex(0);
        }

        binding.etAnswer.setText(""); // Clear previous answer

        QuestionItem question = questionItems.get(currentQuestionIndex);
        binding.tvQuestion.setText(question.getQuestionText());

        String countText = "Question " + (currentQuestionIndex + 1) + " of " + questionItems.size();
        binding.textQuestionCount.setText(countText);

        int progress = (int) (((float)(currentQuestionIndex + 1) / questionItems.size()) * 100);
        binding.progressInterview.setProgress(progress);

        startTimer();
        SpeakQuestion(question.getQuestionText());
    }

    private void startTimer() {
        if (timer != null) timer.cancel();
        // 3 minutes = 180,000 milliseconds
        timer = new CountDownTimer(180000, 1000) {
            @Override
            public void onTick(long l) {
                long minutes = (l / 1000) / 60;
                long seconds = (l / 1000) % 60;
                binding.tvTimer.setText(String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds));
            }

            @Override
            public void onFinish() {
                String typedAnswer = binding.etAnswer.getText().toString().trim();
                if (!typedAnswer.isEmpty()) {
                    navigateToFeedback(typedAnswer);
                } else {
                    navigateToFeedback("Skipped");
                }
            }
        }.start();
    }

    private void StartSpeech() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, getLocaleForLanguage().toString());
        intent.putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your answer");

        try {
            speechLauncher.launch(intent);
        } catch (Exception e) {
            Toast.makeText(this, "Speech Recognition not supported", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToFeedback(String answer) {
        if (timer != null) timer.cancel();
        if (textToSpeech != null) textToSpeech.stop();
        pendingQuestion = null;

        QuestionItem question = questionItems.get(currentQuestionIndex);
        Intent intent = new Intent(this, FeedbackActivity.class);
        intent.putExtra("questionText", question.getQuestionText());
        intent.putExtra("userAnswer", answer);
        intent.putExtra("currentQuestionIndex", currentQuestionIndex);
        intent.putExtra("totalQuestions", questionItems.size());
        intent.putExtra("roleName", roleName);
        intent.putExtra("difficulty", difficulty);
        feedbackLauncher.launch(intent);
    }

    private void goNext() {
        if (timer != null) timer.cancel();
        if (textToSpeech != null) textToSpeech.stop();
        pendingQuestion = null;

        if (questionItems != null && currentQuestionIndex < questionItems.size() - 1) {
            viewModel.NextQuestion();
        } else {
            saveSession();
            clearProgress(); // Clear progress once interview is successfully completed
            Toast.makeText(this, "Interview Successfully Completed!", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void saveSession() {
        if (questionItems == null || questionItems.isEmpty()) return;
        
        int averageScore = (totalScore * 10) / questionItems.size(); // Scale to 100%
        String userId = FirebaseAuth.getInstance().getUid();
        String currentDate = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault()).format(new Date());
        
        List<String> skills = new ArrayList<>();
        for (QuestionItem item : questionItems) {
            if (item.getTags() != null) {
                for (String tag : item.getTags()) {
                    if (!skills.contains(tag)) {
                        skills.add(tag);
                    }
                }
            }
        }
        
        SessionItem sessionItem = new SessionItem(roleName, currentDate, averageScore, userId, skills);
        viewModel.saveSession(sessionItem);
    }

    private void SetupClicks() {
        binding.ivClose.setOnClickListener(v -> finish());
        binding.btnMic.setOnClickListener(v -> StartSpeech());
        binding.tvSkip.setOnClickListener(v -> navigateToFeedback("Skipped"));

        binding.ivSpeakQuestion.setOnClickListener(v -> {
            if (isMuted) {
                isMuted = false;
                binding.ivSpeakQuestion.setImageResource(R.drawable.ic_volume_up);
                if (questionItems != null && currentQuestionIndex < questionItems.size()) {
                    SpeakQuestion(questionItems.get(currentQuestionIndex).getQuestionText());
                }
            } else {
                isMuted = true;
                binding.ivSpeakQuestion.setImageResource(R.drawable.ic_volume_off);
                if (textToSpeech != null) textToSpeech.stop();
                pendingQuestion = null;
            }
        });
    }

    @Override
    protected void onDestroy() {
        if (timer != null) timer.cancel();
        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        super.onDestroy();
    }
}
