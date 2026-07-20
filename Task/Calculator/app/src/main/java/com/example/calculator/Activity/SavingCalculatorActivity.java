package com.example.calculator.Activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calculator.R;
import com.example.calculator.databinding.ActivitySavingCalculatorBinding;
import com.shawnlin.numberpicker.NumberPicker;

import java.text.NumberFormat;
import java.util.Locale;

public class SavingCalculatorActivity extends AppCompatActivity {

    private ActivitySavingCalculatorBinding binding;
    private int selectedField = 0; // 0 for Goal, 1 for Rate, 2 for Period
    private String savingGoalStr = "";
    private String interestRateStr = "";
    private int selectedYears = 1;
    private int selectedMonths = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySavingCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // hiding system keyboard
        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        initSavingUI();
        setupSavingKeyboard();
    }

    private void initSavingUI() {
        binding.savingToolbar.setNavigationOnClickListener(v -> finish());

        View.OnClickListener fieldClickListener = v -> {
            if (v.getId() == R.id.layout_saving_goal || v.getId() == R.id.text_saving_goal) {
                selectedField = 0;
            } else if (v.getId() == R.id.layout_saving_rate || v.getId() == R.id.text_saving_rate) {
                selectedField = 1;
            } else if (v.getId() == R.id.layout_saving_period) {
                selectedField = 2;
                showSavingPeriodDialog();
            }
            binding.resultContainerSaving.setVisibility(View.GONE);
            binding.keypadSaving.setVisibility(View.VISIBLE);
            updateSavingSelectionUI();
        };

        binding.layoutSavingGoal.setOnClickListener(fieldClickListener);
        binding.textSavingGoal.setOnClickListener(fieldClickListener);
        binding.layoutSavingRate.setOnClickListener(fieldClickListener);
        binding.textSavingRate.setOnClickListener(fieldClickListener);
        binding.layoutSavingPeriod.setOnClickListener(fieldClickListener);

        binding.textSavingGoal.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 0;
                updateSavingSelectionUI();
            }
        });
        binding.textSavingRate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 1;
                updateSavingSelectionUI();
            }
        });
        binding.textSavingGoal.setShowSoftInputOnFocus(false);
        binding.textSavingRate.setShowSoftInputOnFocus(false);

        updateSavingPeriodText();
        updateSavingSelectionUI();
    }

    private void updateSavingSelectionUI() {
        binding.layoutSavingGoal.setSelected(selectedField == 0);
        binding.layoutSavingRate.setSelected(selectedField == 1);
        binding.layoutSavingPeriod.setSelected(selectedField == 2);

        if (selectedField == 0) {
            binding.textSavingGoal.requestFocus();
            binding.textSavingGoal.setSelection(binding.textSavingGoal.getText().length());
            binding.textSavingGoal.setCursorVisible(true);
            binding.textSavingRate.setCursorVisible(false);
        } else if (selectedField == 1) {
            binding.textSavingRate.requestFocus();
            binding.textSavingRate.setSelection(binding.textSavingRate.getText().length());
            binding.textSavingRate.setCursorVisible(true);
            binding.textSavingGoal.setCursorVisible(false);
        } else {
            binding.textSavingGoal.clearFocus();
            binding.textSavingRate.clearFocus();
            binding.textSavingGoal.setCursorVisible(false);
            binding.textSavingRate.setCursorVisible(false);
        }

        // force stop the system keyboard
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = getCurrentFocus();
        if (focusView == null) focusView = binding.getRoot();
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    private void setupSavingKeyboard() {
        View.OnClickListener listener = v -> {
            triggerVibration();
            String val = ((TextView) v).getText().toString();
            handleSavingInput(val);
        };

        binding.btnSaving0.setOnClickListener(listener);
        binding.btnSaving1.setOnClickListener(listener);
        binding.btnSaving2.setOnClickListener(listener);
        binding.btnSaving3.setOnClickListener(listener);
        binding.btnSaving4.setOnClickListener(listener);
        binding.btnSaving5.setOnClickListener(listener);
        binding.btnSaving6.setOnClickListener(listener);
        binding.btnSaving7.setOnClickListener(listener);
        binding.btnSaving8.setOnClickListener(listener);
        binding.btnSaving9.setOnClickListener(listener);
        binding.btnSaving00.setOnClickListener(listener);
        binding.btnSavingDot.setOnClickListener(listener);

        binding.btnSavingAc.setOnClickListener(v -> {
            triggerVibration();
            savingGoalStr = "";
            interestRateStr = "";
            selectedYears = 1;
            selectedMonths = 0;
            binding.textSavingGoal.setText("");
            binding.textSavingRate.setText("");
            updateSavingPeriodText();
            binding.resultContainerSaving.setVisibility(View.GONE);
            binding.keypadSaving.setVisibility(View.VISIBLE);
            selectedField = 0;
            updateSavingSelectionUI();
        });

        binding.btnSavingBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (selectedField == 0) {
                if (!savingGoalStr.isEmpty()) {
                    savingGoalStr = savingGoalStr.substring(0, savingGoalStr.length() - 1);
                    binding.textSavingGoal.setText(formatCurrencyNoSymbol(savingGoalStr));
                    binding.textSavingGoal.setSelection(binding.textSavingGoal.getText().length());
                }
            } else if (selectedField == 1) {
                if (!interestRateStr.isEmpty()) {
                    interestRateStr = interestRateStr.substring(0, interestRateStr.length() - 1);
                    binding.textSavingRate.setText(interestRateStr);
                    binding.textSavingRate.setSelection(binding.textSavingRate.getText().length());
                }
            }
            updateSavingSelectionUI();
        });

        binding.btnSavingEquals.setOnClickListener(v -> {
            triggerVibration();
            calculateSaving();
        });
    }

    private void handleSavingInput(String val) {
        if (selectedField == 0) {
            if (savingGoalStr.replace(",", "").length() + val.length() > 9) {
                Toast.makeText(this, R.string.err_max_input_limit, Toast.LENGTH_SHORT).show();
                return;
            }
            if (val.equals(".") && savingGoalStr.contains(".")) return;
            savingGoalStr += val;
            binding.textSavingGoal.setText(formatCurrencyNoSymbol(savingGoalStr));
            binding.textSavingGoal.setSelection(binding.textSavingGoal.getText().length());
        } else if (selectedField == 1) {
            String tempRate = interestRateStr + val;
            try {
                double rate = Double.parseDouble(tempRate);
                if (rate > 99.99) {
                    Toast.makeText(this, R.string.err_max_rate_99, Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tempRate.contains(".") && tempRate.substring(tempRate.indexOf(".") + 1).length() > 2) {
                    return;
                }
            } catch (Exception e) {
                if (!tempRate.equals(".")) return;
            }
            interestRateStr = tempRate;
            binding.textSavingRate.setText(interestRateStr);
            binding.textSavingRate.setSelection(binding.textSavingRate.getText().length());
        }
        updateSavingSelectionUI();
    }

    private void showSavingPeriodDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_investment_period);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int widthPx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 336, getResources().getDisplayMetrics());
            int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(Math.min(widthPx, maxWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        TextView title = dialog.findViewById(R.id.text_dialog_title);
        title.setText(R.string.saving_period);

        NumberPicker yearPicker = dialog.findViewById(R.id.picker_years);
        NumberPicker monthPicker = dialog.findViewById(R.id.picker_months);

        yearPicker.setMinValue(0);
        yearPicker.setMaxValue(50);
        yearPicker.setValue(selectedYears);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setValue(selectedMonths);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            updateSavingSelectionUI();
        });

        dialog.findViewById(R.id.btn_done).setOnClickListener(v -> {
            selectedYears = yearPicker.getValue();
            selectedMonths = monthPicker.getValue();
            if (selectedYears == 0 && selectedMonths == 0) {
                selectedMonths = 1;
            }
            updateSavingPeriodText();
            dialog.dismiss();
            updateSavingSelectionUI();
        });

        dialog.show();
    }

    private void updateSavingPeriodText() {
        String fullText = String.format(Locale.getDefault(), "%02dyear & %02dmonths", selectedYears, selectedMonths);
        binding.textSavingPeriod.setText(fullText);
    }

    private void calculateSaving() {
        if (savingGoalStr.isEmpty() || interestRateStr.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double targetAmount = Double.parseDouble(savingGoalStr.replace(",", ""));
            double annualRate = Double.parseDouble(interestRateStr);
            int totalMonths = (selectedYears * 12) + selectedMonths;

            if (targetAmount < 100) {
                Toast.makeText(this, R.string.err_min_goal_100, Toast.LENGTH_SHORT).show();
                return;
            }

            if (totalMonths <= 0) {
                Toast.makeText(this, R.string.err_invalid_period, Toast.LENGTH_SHORT).show();
                return;
            }

            double monthlyRate = annualRate / (12 * 100);
            double monthlySaving;

            if (annualRate > 0) {
                // Formula: PMT = FV * (i / ((1 + i)^n - 1))
                monthlySaving = targetAmount * (monthlyRate / (Math.pow(1 + monthlyRate, totalMonths) - 1));
            } else {
                monthlySaving = targetAmount / totalMonths;
            }

            double totalInvested = monthlySaving * totalMonths;
            double interestEarned = targetAmount - totalInvested;
            double dailySaving = monthlySaving / 30.0;

            binding.textMonthlySavingAmount.setText(formatCurrency(monthlySaving));
            binding.textTotalAmountSaving.setText(formatCurrency(targetAmount));
            binding.textTotalInvestedSaving.setText(formatCurrency(totalInvested));
            binding.textInterestEarnedSaving.setText(formatCurrency(interestEarned));
            binding.textDailySaving.setText(formatCurrency(dailySaving));

            binding.resultContainerSaving.setVisibility(View.VISIBLE);
            binding.keypadSaving.setVisibility(View.GONE);

            binding.layoutSavingGoal.setSelected(false);
            binding.layoutSavingRate.setSelected(false);
            binding.layoutSavingPeriod.setSelected(false);
        } catch (Exception e) {
            Toast.makeText(this, R.string.err_calculation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private String formatCurrency(double amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        nf.setMaximumFractionDigits(0);
        return nf.format(Math.round(amount));
    }

    private String formatCurrencyNoSymbol(String val) {
        if (val == null || val.isEmpty()) return "";
        try {
            double amount = Double.parseDouble(val.replace(",", ""));
            NumberFormat nf = NumberFormat.getInstance(new Locale("en", "IN"));
            nf.setMaximumFractionDigits(0);
            return nf.format(Math.round(amount));
        } catch (Exception e) {
            return val;
        }
    }

    private void triggerVibration() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = (vibratorManager != null) ? vibratorManager.getDefaultVibrator() : null;
        } else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (vibrator != null && vibrator.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(60);
            }
        }
    }
}
