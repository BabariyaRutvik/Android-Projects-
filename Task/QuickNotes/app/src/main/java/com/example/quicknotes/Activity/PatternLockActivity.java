package com.example.quicknotes.Activity;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.quicknotes.R;
import com.example.quicknotes.Utils.PatternView;
import com.example.quicknotes.databinding.ActivityPatternLockBinding;

import java.util.List;

public class PatternLockActivity extends AppCompatActivity {

    public static final String MODE_SET = "mode_set";
    public static final String MODE_CHANGE = "mode_change";
    public static final String MODE_REMOVE = "mode_remove";
    public static final String MODE_VERIFY = "mode_verify";
    public static final String EXTRA_MODE = "extra_mode";

    private ActivityPatternLockBinding binding;
    private SharedPreferences prefs;
    private String mode;
    private String originalMode;
    private List<Integer> firstPattern = null;
    private boolean isConfirmStep = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPatternLockBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE);
        mode = getIntent().getStringExtra(EXTRA_MODE);
        if (mode == null) mode = MODE_VERIFY;
        originalMode = mode;

        setupUI();
        setupPatternView();

        binding.toolbar.setNavigationOnClickListener(v -> {
            setResult(RESULT_CANCELED);
            finish();
        });
    }

    private void setupUI() {
        switch (mode) {
            case MODE_SET:
                binding.txtTitle.setText(R.string.set_password_title);
                binding.txtMessage.setText(R.string.draw_pattern_msg);
                break;
            case MODE_CHANGE:
                binding.txtTitle.setText(R.string.change_password_title);
                binding.txtMessage.setText(R.string.draw_pattern_msg); // Draw current
                break;
            case MODE_REMOVE:
                binding.txtTitle.setText(R.string.remove_password_title);
                binding.txtMessage.setText(R.string.draw_pattern_msg); // Draw current
                break;
            case MODE_VERIFY:
                binding.txtTitle.setText(R.string.app_name);
                binding.txtMessage.setText(R.string.draw_pattern_msg);
                binding.txtForgot.setVisibility(View.VISIBLE);
                break;
        }
    }

    private void setupPatternView() {
        binding.patternView.setOnPatternListener(pattern -> {
            if (pattern.size() < 4) {
                binding.txtMessage.setText(R.string.connect_4_dots);
                binding.txtMessage.setTextColor(getResources().getColor(R.color.badge_untitled_red_text, null));
                binding.patternView.setError();
                new Handler().postDelayed(() -> {
                    binding.patternView.clearPattern();
                    binding.txtMessage.setTextColor(getResources().getColor(R.color.gray_text, null));
                }, 1000);
                return;
            }

            handlePatternComplete(pattern);
        });
    }

    private void handlePatternComplete(List<Integer> pattern) {
        if (mode.equals(MODE_SET)) {
            if (!isConfirmStep) {
                firstPattern = pattern;
                isConfirmStep = true;
                binding.txtMessage.setText(R.string.draw_pattern_confirm_msg);
                binding.patternView.clearPattern();
            } else {
                if (pattern.equals(firstPattern)) {
                    savePattern(pattern);
                    if (originalMode.equals(MODE_CHANGE)) {
                        Toast.makeText(this, R.string.password_changed_success, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(this, R.string.password_set_success, Toast.LENGTH_SHORT).show();
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    binding.txtMessage.setText(R.string.pattern_not_match);
                    binding.txtMessage.setTextColor(getResources().getColor(R.color.badge_untitled_red_text, null));
                    binding.patternView.setError();
                    new Handler().postDelayed(() -> {
                        binding.patternView.clearPattern();
                        binding.txtMessage.setTextColor(getResources().getColor(R.color.gray_text, null));
                    }, 1000);
                }
            }
        } else if (mode.equals(MODE_VERIFY) || mode.equals(MODE_REMOVE) || mode.equals(MODE_CHANGE)) {
            if (isPatternCorrect(pattern)) {
                if (mode.equals(MODE_REMOVE)) {
                    removePattern();
                    Toast.makeText(this, R.string.password_removed_success, Toast.LENGTH_SHORT).show();
                    setResult(RESULT_OK);
                    finish();
                } else if (mode.equals(MODE_CHANGE)) {
                    // Current pattern verified, now start SET mode logic within same activity
                    mode = MODE_SET;
                    isConfirmStep = false;
                    binding.txtTitle.setText(R.string.set_password_title);
                    binding.txtMessage.setText(R.string.draw_pattern_msg);
                    binding.patternView.clearPattern();
                } else {
                    setResult(RESULT_OK);
                    finish();
                }
            } else {
                binding.txtMessage.setText(R.string.pattern_wrong);
                binding.txtMessage.setTextColor(getResources().getColor(R.color.badge_untitled_red_text, null));
                binding.patternView.setError();
                new Handler().postDelayed(() -> {
                    binding.patternView.clearPattern();
                    binding.txtMessage.setTextColor(getResources().getColor(R.color.gray_text, null));
                }, 1000);
            }
        }
    }

    private void savePattern(List<Integer> pattern) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : pattern) sb.append(i).append(",");
        prefs.edit().putString("pattern_code", sb.toString()).putBoolean("is_enabled", true).apply();
    }

    private boolean isPatternCorrect(List<Integer> pattern) {
        String saved = prefs.getString("pattern_code", "");
        StringBuilder sb = new StringBuilder();
        for (Integer i : pattern) sb.append(i).append(",");
        return saved.equals(sb.toString());
    }

    private void removePattern() {
        prefs.edit().remove("pattern_code").putBoolean("is_enabled", false).apply();
    }
}
