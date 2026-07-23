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
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.calculator.R;
import com.example.calculator.databinding.ActivityInvestmentCalculatorBinding;
import com.shawnlin.numberpicker.NumberPicker;

import java.text.NumberFormat;
import java.util.Locale;

public class InvestmentCalculatorActivity extends AppCompatActivity {

    private ActivityInvestmentCalculatorBinding binding;
    private int selectedField = 0; // 0: Amount, 1: Rate, 2: Period
    private String investAmountStr = "";
    private String interestRateStr = "";
    private int selectedYears = 1;
    private int selectedMonths = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityInvestmentCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Hiding system keyboard
        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        initInvestmentUI();
        setupInvestmentKeypad();

        binding.investResultContainer.setVisibility(View.GONE);
    }

    private void initInvestmentUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        View.OnClickListener fieldClickListener = v -> {
            if (v.getId() == R.id.layout_total_invest_amount || v.getId() == R.id.text_invest_amount) {
                selectedField = 0;
            } else if (v.getId() == R.id.layout_invest_rate || v.getId() == R.id.text_invest_rate) {
                selectedField = 1;
            } else if (v.getId() == R.id.layout_invest_saving_period) {
                selectedField = 2;
                showPeriodDialog();
            }
            binding.investResultContainer.setVisibility(View.GONE);
            binding.keypadInvestment.setVisibility(View.VISIBLE);
            updateInvestmentSelectionUI();
            updateLabelColors(false);
        };

        binding.layoutTotalInvestAmount.setOnClickListener(fieldClickListener);
        binding.textInvestAmount.setOnClickListener(fieldClickListener);
        binding.layoutInvestRate.setOnClickListener(fieldClickListener);
        binding.textInvestRate.setOnClickListener(fieldClickListener);
        binding.layoutInvestSavingPeriod.setOnClickListener(fieldClickListener);

        binding.textInvestAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 0;
                updateInvestmentSelectionUI();
            }
        });

        binding.textInvestRate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 1;
                updateInvestmentSelectionUI();
            }
        });

        binding.textInvestAmount.setShowSoftInputOnFocus(false);
        binding.textInvestRate.setShowSoftInputOnFocus(false);

        updatePeriodText();
        updateInvestmentSelectionUI();
    }

    private void updateInvestmentSelectionUI() {
        binding.layoutTotalInvestAmount.setSelected(selectedField == 0);
        binding.layoutInvestRate.setSelected(selectedField == 1);
        binding.layoutInvestSavingPeriod.setSelected(selectedField == 2);

        if (selectedField == 0) {
            binding.textInvestAmount.requestFocus();
            binding.textInvestAmount.setSelection(binding.textInvestAmount.getText().length());
            binding.textInvestAmount.setCursorVisible(true);
            binding.textInvestRate.setCursorVisible(false);
        } else if (selectedField == 1) {
            binding.textInvestRate.requestFocus();
            binding.textInvestRate.setSelection(binding.textInvestRate.getText().length());
            binding.textInvestRate.setCursorVisible(true);
            binding.textInvestAmount.setCursorVisible(false);
        } else {
            binding.textInvestAmount.clearFocus();
            binding.textInvestRate.clearFocus();
            binding.textInvestAmount.setCursorVisible(false);
            binding.textInvestRate.setCursorVisible(false);
        }

        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView == null) focusedView = binding.getRoot();
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void setupInvestmentKeypad() {
        View.OnClickListener listener = v -> {
            triggerVibration();
            String val = ((TextView) v).getText().toString();
            handleInvestmentInput(val);
        };

        binding.btnInvest0.setOnClickListener(listener);
        binding.btnInvest1.setOnClickListener(listener);
        binding.btnInvest2.setOnClickListener(listener);
        binding.btnInvest3.setOnClickListener(listener);
        binding.btnInvest4.setOnClickListener(listener);
        binding.btnInvest5.setOnClickListener(listener);
        binding.btnInvest6.setOnClickListener(listener);
        binding.btnInvest7.setOnClickListener(listener);
        binding.btnInvest8.setOnClickListener(listener);
        binding.btnInvest9.setOnClickListener(listener);
        binding.btnInvest00.setOnClickListener(listener);
        binding.btnInvestDot.setOnClickListener(listener);

        binding.btnInvestAc.setOnClickListener(v -> {
            triggerVibration();
            investAmountStr = "";
            interestRateStr = "";
            selectedYears = 1;
            selectedMonths = 0;
            binding.textInvestAmount.setText("");
            binding.textInvestRate.setText("");
            updatePeriodText();
            binding.investResultContainer.setVisibility(View.GONE);
            binding.keypadInvestment.setVisibility(View.VISIBLE);
            selectedField = 0;
            updateInvestmentSelectionUI();
            updateLabelColors(false);
        });

        binding.btnInvestBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (selectedField == 0) {
                if (!investAmountStr.isEmpty()) {
                    investAmountStr = investAmountStr.substring(0, investAmountStr.length() - 1);
                    binding.textInvestAmount.setText(formatCurrencyNoSymbol(investAmountStr));
                }
            } else if (selectedField == 1) {
                if (!interestRateStr.isEmpty()) {
                    interestRateStr = interestRateStr.substring(0, interestRateStr.length() - 1);
                    binding.textInvestRate.setText(interestRateStr);
                }
            }
            updateInvestmentSelectionUI();
        });

        binding.btnInvestEquals.setOnClickListener(v -> {
            triggerVibration();
            calculateInvestment();
        });
    }

    private void handleInvestmentInput(String val) {
        if (selectedField == 0) {
            if (investAmountStr.replace(".", "").length() + val.length() > 9) {
                Toast.makeText(this, R.string.err_max_9_digits, Toast.LENGTH_SHORT).show();
                return;
            }
            if (val.equals(".") && investAmountStr.contains(".")) return;
            investAmountStr += val;
            binding.textInvestAmount.setText(formatCurrencyNoSymbol(investAmountStr));
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
            binding.textInvestRate.setText(interestRateStr);
        }
        updateInvestmentSelectionUI();
    }

    private void showPeriodDialog() {
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

        yearPicker.setMinValue(1);
        yearPicker.setMaxValue(40);
        yearPicker.setValue(selectedYears);
        yearPicker.setFormatter(value -> String.format(Locale.US, "%d", value));

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setValue(selectedMonths);
        monthPicker.setFormatter(value -> String.format(Locale.US, "%d", value));

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            updateInvestmentSelectionUI();
        });

        dialog.findViewById(R.id.btn_done).setOnClickListener(v -> {
            selectedYears = yearPicker.getValue();
            selectedMonths = monthPicker.getValue();
            updatePeriodText();
            dialog.dismiss();
            updateInvestmentSelectionUI();
        });

        dialog.show();
    }

    private void updatePeriodText() {
        String fullText = String.format(Locale.US, "%02dyears & %02dmonths", selectedYears, selectedMonths);
        binding.textInvestPeriod.setText(fullText);
    }

    private void calculateInvestment() {
        if (investAmountStr.isEmpty() || interestRateStr.isEmpty()) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double principal = Double.parseDouble(investAmountStr.replace(",", ""));
            double annualRate = Double.parseDouble(interestRateStr);
            double totalYears = selectedYears + (selectedMonths / 12.0);

            if (principal < 100) {
                Toast.makeText(this, R.string.err_min_investment_100, Toast.LENGTH_SHORT).show();
                return;
            }

            // A = P * (1 + r/100)^t
            double futureValue = principal * Math.pow(1 + (annualRate / 100.0), totalYears);
            double totalInterest = futureValue - principal;

            binding.textInvestTotalValue.setText(formatCurrency(futureValue));
            binding.textInvestTotalAmount.setText(formatCurrency(principal));
            binding.textInvestTotalInterest.setText(formatCurrency(totalInterest));

            binding.investResultContainer.setVisibility(View.VISIBLE);
            binding.keypadInvestment.setVisibility(View.GONE);

            binding.layoutTotalInvestAmount.setSelected(false);
            binding.layoutInvestRate.setSelected(false);
            binding.layoutInvestSavingPeriod.setSelected(false);
            updateLabelColors(true);
        } catch (Exception e) {
            Toast.makeText(this, R.string.err_calculation_error, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateLabelColors(boolean isResultMode) {
        int color = isResultMode ? getColor(R.color.text_secondary) : getColor(R.color.text_label_gray);
        binding.labelInvestAmount.setTextColor(color);
        binding.labelInvestRate.setTextColor(color);
        binding.labelInvestPeriod.setTextColor(color);
    }

    private String formatCurrency(double amount) {
        NumberFormat nf = NumberFormat.getCurrencyInstance(new Locale("en", "IN"));
        nf.setMinimumFractionDigits(0);
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
                vibrator.vibrate(VibrationEffect.createOneShot(50, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(50);
            }
        }
    }
}