package com.example.calculator.Activity;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import com.shawnlin.numberpicker.NumberPicker;

import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.calculator.Database.HistoryItem;
import com.example.calculator.Database.HistoryViewModel;
import com.example.calculator.R;
import com.example.calculator.databinding.ActivitySipcalculatorBinding;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class SIPCalculatorActivity extends AppCompatActivity {

    private ActivitySipcalculatorBinding binding;
    private HistoryViewModel historyViewModel;
    private int selectedField = 0; // 0: Amount, 1: Rate, 2: Period
    private String sipAmount = "";
    private String interestRate = "";
    private int selectedYears = 1;
    private int selectedMonths = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySipcalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUI();
        setupKeypad();

        historyViewModel = new ViewModelProvider(this).get(HistoryViewModel.class);
    }

    private void initUI() {
        binding.btnBack.setOnClickListener(v -> finish());

        binding.layoutSipAmount.setOnClickListener(v -> {
            selectedField = 0;
            binding.resultContainer.setVisibility(View.GONE);
            binding.keypad.setVisibility(View.VISIBLE);
            updateSelectionUI();
        });

        binding.layoutRate.setOnClickListener(v -> {
            selectedField = 1;
            binding.resultContainer.setVisibility(View.GONE);
            binding.keypad.setVisibility(View.VISIBLE);
            updateSelectionUI();
        });

        binding.layoutPeriod.setOnClickListener(v -> {
            selectedField = 2;
            binding.resultContainer.setVisibility(View.GONE);
            binding.keypad.setVisibility(View.VISIBLE);
            updateSelectionUI();
            showPeriodDialog();
        });

        binding.textSipAmount.setShowSoftInputOnFocus(false);
        binding.textRate.setShowSoftInputOnFocus(false);

        updatePeriodText();
        updateSelectionUI();
    }

    private void updateSelectionUI() {
        binding.layoutSipAmount.setSelected(selectedField == 0);
        binding.layoutRate.setSelected(selectedField == 1);
        binding.layoutPeriod.setSelected(selectedField == 2);

        if (selectedField == 0) {
            binding.textSipAmount.requestFocus();
            binding.textSipAmount.setSelection(binding.textSipAmount.getText().length());
        } else if (selectedField == 1) {
            binding.textRate.requestFocus();
            binding.textRate.setSelection(binding.textRate.getText().length());
        }
    }

    private void setupKeypad() {
        View.OnClickListener listener = v -> {
            triggerVibration();
            String val = ((TextView) v).getText().toString();
            handleInput(val);
        };

        binding.btn0.setOnClickListener(listener);
        binding.btn1.setOnClickListener(listener);
        binding.btn2.setOnClickListener(listener);
        binding.btn3.setOnClickListener(listener);
        binding.btn4.setOnClickListener(listener);
        binding.btn5.setOnClickListener(listener);
        binding.btn6.setOnClickListener(listener);
        binding.btn7.setOnClickListener(listener);
        binding.btn8.setOnClickListener(listener);
        binding.btn9.setOnClickListener(listener);
        binding.btn00.setOnClickListener(listener);
        binding.btnDot.setOnClickListener(listener);

        binding.btnAc.setOnClickListener(v -> {
            triggerVibration();
            sipAmount = "";
            interestRate = "";
            binding.textSipAmount.setText("");
            binding.textRate.setText("");
            binding.resultContainer.setVisibility(View.GONE);
            binding.keypad.setVisibility(View.VISIBLE);
            updateSelectionUI();
        });

        binding.btnBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (selectedField == 0) {
                if (!sipAmount.isEmpty()) {
                    sipAmount = sipAmount.substring(0, sipAmount.length() - 1);
                    binding.textSipAmount.setText(formatCurrencyNoSymbol(sipAmount));
                }
            } else if (selectedField == 1) {
                if (!interestRate.isEmpty()) {
                    interestRate = interestRate.substring(0, interestRate.length() - 1);
                    binding.textRate.setText(interestRate);
                }
            }
            updateSelectionUI();
        });

        binding.btnEquals.setOnClickListener(v -> {
            triggerVibration();
            calculateSIP();
        });
    }

    private void handleInput(String val) {
        if (selectedField == 0) {
            if (sipAmount.length() >= 12) return;
            if (val.equals(".") && sipAmount.contains(".")) return;
            sipAmount += val;
            binding.textSipAmount.setText(formatCurrencyNoSymbol(sipAmount));
        } else if (selectedField == 1) {
            String tempRate = interestRate + val;
            try {
                double rate = Double.parseDouble(tempRate);
                if (rate > 99.99) {
                    Toast.makeText(this, "Rate cannot exceed 99.99%", Toast.LENGTH_SHORT).show();
                    return; // Prevent exceeding 99.99%
                }
                if (tempRate.contains(".") && tempRate.substring(tempRate.indexOf(".") + 1).length() > 2) {
                    return; // Prevent more than 2 decimal places
                }
            } catch (Exception e) {
                if (!tempRate.equals(".")) return;
            }
            interestRate = tempRate;
            binding.textRate.setText(interestRate);
        }
        updateSelectionUI();
    }

    private void showPeriodDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_investment_period);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            window.setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.90),
                    WindowManager.LayoutParams.WRAP_CONTENT
            );
        }

        NumberPicker yearPicker = dialog.findViewById(R.id.picker_years);
        NumberPicker monthPicker = dialog.findViewById(R.id.picker_months);

        yearPicker.setMinValue(1);
        yearPicker.setMaxValue(40);
        yearPicker.setValue(selectedYears);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setValue(selectedMonths);

        yearPicker.setOnValueChangedListener((picker, oldVal, newVal) -> {
            if (newVal == 40) {
                monthPicker.setValue(0);
                monthPicker.setEnabled(false);
            } else {
                monthPicker.setEnabled(true);
            }
        });

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            updateSelectionUI();
        });
        dialog.findViewById(R.id.btn_done).setOnClickListener(v -> {
            selectedYears = yearPicker.getValue();
            selectedMonths = selectedYears == 40 ? 0 : monthPicker.getValue();

            updatePeriodText();
            dialog.dismiss();
            updateSelectionUI();
        });

        dialog.show();
    }

    private void updatePeriodText() {
        String fullText = selectedYears + "years & " + selectedMonths + "months";
        binding.textPeriod.setText(fullText);
    }

    private void calculateSIP() {
        if (sipAmount.isEmpty() || interestRate.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            // Remove commas if they were accidentally included in the raw string
            String cleanAmount = sipAmount.replace(",", "");
            double monthlySip = Double.parseDouble(cleanAmount);
            double annualRate = Double.parseDouble(interestRate);

            if (monthlySip <= 0) {
                Toast.makeText(this, "Invalid Amount", Toast.LENGTH_SHORT).show();
                return;
            }

            // Total months
            int months = (selectedYears * 12) + selectedMonths;
            double totalInvestment = monthlySip * months;
            double futureValue;

            if (annualRate > 0) {
                // Effective monthly interest rate
                double monthlyRate = Math.pow(1 + (annualRate / 100.0), 1.0 / 12.0) - 1;

                // SIP Future Value Formula (Annuity Due)
                // FV = P × ((1 + i)^n - 1) / i × (1 + i)
                futureValue = monthlySip *
                        ((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate)
                        * (1 + monthlyRate);
            } else {
                futureValue = totalInvestment;
            }

            double wealthGain = futureValue - totalInvestment;
            double growth = totalInvestment > 0 ? futureValue / totalInvestment : 1.0;

            binding.textExpectedAmount.setText(formatCurrency(futureValue));
            binding.textAmountInvested.setText(formatCurrency(totalInvestment));
            binding.textWealthGain.setText(formatCurrency(wealthGain));
            binding.textInvestmentGrowth.setText(String.format(Locale.getDefault(), "%.2f Times", growth));
            binding.textInvestmentGrowth.setTextColor(getColor(R.color.orange));

            // Save to History
            String expression = "SIP: " + formatCurrencyNoSymbol(sipAmount) + " @ " + interestRate + "% for " + selectedYears + "y " + selectedMonths + "m";
            String result = formatCurrency(futureValue);
            historyViewModel.insert(new HistoryItem(expression, result, System.currentTimeMillis()));

            binding.resultContainer.setVisibility(View.VISIBLE);
            binding.keypad.setVisibility(View.GONE);
            binding.layoutSipAmount.setSelected(false);
            binding.layoutRate.setSelected(false);
            binding.layoutPeriod.setSelected(false);

        } catch (NumberFormatException e) {
            Toast.makeText(this, "Invalid input", Toast.LENGTH_SHORT).show();
        }
    }


    private String formatCurrency(double amount) {
        // Indian numbering system format: ₹ 20,89,621.33
        DecimalFormat df = new DecimalFormat("₹ #,##,##,##0.00");
        return df.format(amount);
    }

    private String formatCurrencyNoSymbol(String val) {
        if (val.isEmpty()) return "";
        try {
            double amount = Double.parseDouble(val);
            DecimalFormat df = new DecimalFormat("#,##,##,###");
            return df.format(Math.round(amount));
        } catch (Exception e) {
            return val;
        }
    }

    private void triggerVibration() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
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
