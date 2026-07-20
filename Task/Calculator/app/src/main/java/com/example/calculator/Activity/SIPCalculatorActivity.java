package com.example.calculator.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
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
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calculator.R;
import com.example.calculator.databinding.ActivitySipcalculatorBinding;
import com.shawnlin.numberpicker.NumberPicker;

import java.text.DecimalFormat;
import java.util.Locale;

public class SIPCalculatorActivity extends AppCompatActivity {

    private ActivitySipcalculatorBinding binding;
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


        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUI();
        setupKeypad();

        binding.resultContainer.setVisibility(View.GONE);

        handleIntent(getIntent());
    }

    @Override
    protected void onNewIntent(@NonNull Intent intent) {
        super.onNewIntent(intent);
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("expression")) {
            String exp = intent.getStringExtra("expression");
            parseExpression(exp);
        }
    }

    private void parseExpression(String exp) {
        try {
            if (exp != null && exp.startsWith("SIP: ")) {
                String data = exp.substring(5);
                String[] parts = data.split(" @ ");
                sipAmount = parts[0].replace(",", "");
                
                String[] rateParts = parts[1].split("% for ");
                interestRate = rateParts[0];
                
                String periodPart = rateParts[1];
                String[] yParts = periodPart.split("y ");
                selectedYears = Integer.parseInt(yParts[0]);
                selectedMonths = Integer.parseInt(yParts[1].replace("m", ""));
                
                binding.textSipAmount.setText(formatCurrencyNoSymbol(sipAmount));
                binding.textRate.setText(interestRate);
                updatePeriodText();
                calculateSIP();
                updateSelectionUI();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initUI() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        View.OnClickListener fieldClickListener = v -> {
            if (v.getId() == R.id.layout_sip_amount || v.getId() == R.id.text_sip_amount) selectedField = 0;
            else if (v.getId() == R.id.layout_rate || v.getId() == R.id.text_rate) selectedField = 1;
            else if (v.getId() == R.id.layout_period) {
                selectedField = 2;
                showPeriodDialog();
            }
            
            binding.resultContainer.setVisibility(View.GONE);
            binding.keypad.setVisibility(View.VISIBLE);
            updateSelectionUI();
        };

        binding.layoutSipAmount.setOnClickListener(fieldClickListener);
        binding.textSipAmount.setOnClickListener(fieldClickListener);
        binding.layoutRate.setOnClickListener(fieldClickListener);
        binding.textRate.setOnClickListener(fieldClickListener);
        binding.layoutPeriod.setOnClickListener(fieldClickListener);

        binding.textSipAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 0;
                updateSelectionUI();
            }
        });

        binding.textRate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 1;
                updateSelectionUI();
            }
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
            binding.textSipAmount.setCursorVisible(true);
            binding.textRate.setCursorVisible(false);
        } else if (selectedField == 1) {
            binding.textRate.requestFocus();
            binding.textRate.setSelection(binding.textRate.getText().length());
            binding.textRate.setCursorVisible(true);
            binding.textSipAmount.setCursorVisible(false);
        } else {
            binding.textSipAmount.clearFocus();
            binding.textRate.clearFocus();
            binding.textSipAmount.setCursorVisible(false);
            binding.textRate.setCursorVisible(false);
        }

        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView == null) focusedView = binding.getRoot();
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
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
            selectedYears = 1;
            selectedMonths = 0;
            binding.textSipAmount.setText("");
            binding.textRate.setText("");
            updatePeriodText();
            binding.resultContainer.setVisibility(View.GONE);
            binding.keypad.setVisibility(View.VISIBLE);
            selectedField = 0;
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
            if (sipAmount.length() + val.length() > 6) {
                Toast.makeText(this, R.string.err_max_6_digits, Toast.LENGTH_SHORT).show();
                return;
            }
            if (val.equals(".") && sipAmount.contains(".")) return;
            sipAmount += val;
            binding.textSipAmount.setText(formatCurrencyNoSymbol(sipAmount));
        } else if (selectedField == 1) {
            String tempRate = interestRate + val;
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
            int widthPx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 336, getResources().getDisplayMetrics());
            int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(Math.min(widthPx, maxWidth), WindowManager.LayoutParams.WRAP_CONTENT);
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
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            String cleanAmount = sipAmount.replace(",", "");
            double monthlySip = Double.parseDouble(cleanAmount);
            double annualRate = Double.parseDouble(interestRate);

            if (monthlySip < 100) {
                Toast.makeText(this, R.string.err_max_amount_100, Toast.LENGTH_SHORT).show();
                return;
            }

            int months = (selectedYears * 12) + selectedMonths;
            double totalInvestment = monthlySip * months;
            double futureValue;

            if (annualRate > 0) {
                double monthlyRate = Math.pow(1 + (annualRate / 100.0), 1.0 / 12.0) - 1;
                futureValue = monthlySip * ((Math.pow(1 + monthlyRate, months) - 1) / monthlyRate) * (1 + monthlyRate);
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

            binding.resultContainer.setVisibility(View.VISIBLE);
            binding.keypad.setVisibility(View.GONE);
            binding.layoutSipAmount.setSelected(false);
            binding.layoutRate.setSelected(false);
            binding.layoutPeriod.setSelected(false);

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.err_invalid_input, Toast.LENGTH_SHORT).show();
        }
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("₹#,##,##,###");
        return df.format(Math.round(amount));
    }

    private String formatCurrencyNoSymbol(String val) {
        if (val.isEmpty()) return "";
        try {
            double amount = Double.parseDouble(val.replace(",", ""));
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