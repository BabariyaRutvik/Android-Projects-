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
import com.example.calculator.databinding.ActivityLoanCalculatorBinding;
import com.shawnlin.numberpicker.NumberPicker;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;

public class LoanCalculatorActivity extends AppCompatActivity {

    private ActivityLoanCalculatorBinding binding;
    private int selectedField = 0;
    private String loanAmountStr = "";
    private String interestRateStr = "";
    private int selectedYears = 1;
    private int selectedMonths = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityLoanCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Handle Status Bar appearance (dark icons for light background)
        androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
        }

        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initLoanUI();
        setupLoanKeypad();

        binding.resultContainerLoan.setVisibility(View.GONE);

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
            if (exp != null && exp.startsWith("Loan: ")) {
                String data = exp.substring(6);
                String[] parts = data.split(" @ ");
                loanAmountStr = parts[0].replace(",", "");

                String[] rateParts = parts[1].split("% for ");
                interestRateStr = rateParts[0];

                String periodPart = rateParts[1];
                String[] yParts = periodPart.split("y ");
                selectedYears = Integer.parseInt(yParts[0]);
                selectedMonths = Integer.parseInt(yParts[1].replace("m", ""));

                binding.textLoanAmount.setText(formatCurrencyNoSymbol(loanAmountStr));
                binding.textRateLoan.setText(interestRateStr);
                updatePeriodText();
                calculateLoan();
                updateLoanSelectionUI();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void initLoanUI() {
        binding.loanToolbar.setNavigationOnClickListener(v -> finish());

        View.OnClickListener fieldClickListener = v -> {
            if (v.getId() == R.id.layout_loan_amount || v.getId() == R.id.text_loan_amount) selectedField = 0;
            else if (v.getId() == R.id.layout_rate_loan || v.getId() == R.id.text_rate_loan) selectedField = 1;
            else if (v.getId() == R.id.layout_period_loan) {
                selectedField = 2;
                showPeriodDialog();
            }
            
            binding.resultContainerLoan.setVisibility(View.GONE);
            binding.keypadLoan.setVisibility(View.VISIBLE);
            updateLoanSelectionUI();
        };

        binding.layoutLoanAmount.setOnClickListener(fieldClickListener);
        binding.textLoanAmount.setOnClickListener(fieldClickListener);
        binding.layoutRateLoan.setOnClickListener(fieldClickListener);
        binding.textRateLoan.setOnClickListener(fieldClickListener);
        binding.layoutPeriodLoan.setOnClickListener(fieldClickListener);

        binding.textLoanAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 0;
                updateLoanSelectionUI();
            }
        });

        binding.textRateLoan.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 1;
                updateLoanSelectionUI();
            }
        });

        binding.textLoanAmount.setShowSoftInputOnFocus(false);
        binding.textRateLoan.setShowSoftInputOnFocus(false);

        updatePeriodText();
        updateLoanSelectionUI();
    }

    private void updateLoanSelectionUI() {
        binding.layoutLoanAmount.setSelected(selectedField == 0);
        binding.layoutRateLoan.setSelected(selectedField == 1);
        binding.layoutPeriodLoan.setSelected(selectedField == 2);

        if (selectedField == 0) {
            binding.textLoanAmount.requestFocus();
            binding.textLoanAmount.setSelection(binding.textLoanAmount.getText().length());
            binding.textLoanAmount.setCursorVisible(true);
            binding.textRateLoan.setCursorVisible(false);
        } else if (selectedField == 1) {
            binding.textRateLoan.requestFocus();
            binding.textRateLoan.setSelection(binding.textRateLoan.getText().length());
            binding.textRateLoan.setCursorVisible(true);
            binding.textLoanAmount.setCursorVisible(false);
        } else {
            binding.textLoanAmount.clearFocus();
            binding.textRateLoan.clearFocus();
            binding.textLoanAmount.setCursorVisible(false);
            binding.textRateLoan.setCursorVisible(false);
        }

        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusedView = getCurrentFocus();
        if (focusedView == null) focusedView = binding.getRoot();
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusedView.getWindowToken(), 0);
        }
    }

    private void setupLoanKeypad() {
        View.OnClickListener listener = v -> {
            triggerVibration();
            String val = ((TextView) v).getText().toString();
            handleLoanInput(val);
        };

        binding.btnLoan0.setOnClickListener(listener);
        binding.btnLoan1.setOnClickListener(listener);
        binding.btnLoan2.setOnClickListener(listener);
        binding.btnLoan3.setOnClickListener(listener);
        binding.btnLoan4.setOnClickListener(listener);
        binding.btnLoan5.setOnClickListener(listener);
        binding.btnLoan6.setOnClickListener(listener);
        binding.btnLoan7.setOnClickListener(listener);
        binding.btnLoan8.setOnClickListener(listener);
        binding.btnLoan9.setOnClickListener(listener);
        binding.btnLoan00.setOnClickListener(listener);
        binding.btnLoanDot.setOnClickListener(listener);

        binding.btnLoanAc.setOnClickListener(v -> {
            triggerVibration();
            loanAmountStr = "";
            interestRateStr = "";
            binding.textLoanAmount.setText("");
            binding.textRateLoan.setText("");
            binding.resultContainerLoan.setVisibility(View.GONE);
            binding.keypadLoan.setVisibility(View.VISIBLE);
            updateLoanSelectionUI();
        });

        binding.btnLoanBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (selectedField == 0) {
                if (!loanAmountStr.isEmpty()) {
                    loanAmountStr = loanAmountStr.substring(0, loanAmountStr.length() - 1);
                    binding.textLoanAmount.setText(formatCurrencyNoSymbol(loanAmountStr));
                }
            } else if (selectedField == 1) {
                if (!interestRateStr.isEmpty()) {
                    interestRateStr = interestRateStr.substring(0, interestRateStr.length() - 1);
                    binding.textRateLoan.setText(interestRateStr);
                }
            }
            updateLoanSelectionUI();
        });

        binding.btnLoanEquals.setOnClickListener(v -> {
            triggerVibration();
            calculateLoan();
        });
    }

    private void handleLoanInput(String val) {
        if (selectedField == 0) {
            if (loanAmountStr.replace(".", "").length() + val.length() > 9) {
                Toast.makeText(this, "Maximum loan amount is 99,99,99,999", Toast.LENGTH_SHORT).show();
                return;
            }
            if (val.equals(".") && loanAmountStr.contains(".")) return;
            loanAmountStr += val;
            binding.textLoanAmount.setText(formatCurrencyNoSymbol(loanAmountStr));
        } else if (selectedField == 1) {
            String tempRate = interestRateStr + val;
            try {
                double rate = Double.parseDouble(tempRate);
                if (rate > 99.99) {
                    Toast.makeText(this, "Rate cannot exceed 99.99%", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tempRate.contains(".") && tempRate.substring(tempRate.indexOf(".") + 1).length() > 2) {
                    return;
                }
            } catch (Exception e) {
                if (!tempRate.equals(".")) return;
            }
            interestRateStr = tempRate;
            binding.textRateLoan.setText(interestRateStr);
        }
        updateLoanSelectionUI();
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
        title.setText("Loan Tenure");

        NumberPicker yearPicker = dialog.findViewById(R.id.picker_years);
        NumberPicker monthPicker = dialog.findViewById(R.id.picker_months);

        yearPicker.setMinValue(1);
        yearPicker.setMaxValue(30);
        yearPicker.setValue(selectedYears);

        monthPicker.setMinValue(0);
        monthPicker.setMaxValue(11);
        monthPicker.setValue(selectedMonths);

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            updateLoanSelectionUI();
        });

        dialog.findViewById(R.id.btn_done).setOnClickListener(v -> {
            selectedYears = yearPicker.getValue();
            selectedMonths = monthPicker.getValue();
            updatePeriodText();
            dialog.dismiss();
            updateLoanSelectionUI();
        });

        dialog.show();
    }

    private void updatePeriodText() {
        String fullText = String.format(Locale.getDefault(), "%02dyear & %02dmonths", selectedYears, selectedMonths);
        binding.textPeriodLoan.setText(fullText);
    }

    private void calculateLoan() {
        if (loanAmountStr.isEmpty() || interestRateStr.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double principal = Double.parseDouble(loanAmountStr.replace(",", ""));
            double annualRate = Double.parseDouble(interestRateStr);
            int totalMonths = (selectedYears * 12) + selectedMonths;

            if (principal < 100) {
                Toast.makeText(this, "Minimum loan amount is ₹100", Toast.LENGTH_SHORT).show();
                return;
            }

            if (totalMonths <= 0) {
                Toast.makeText(this, "Invalid tenure", Toast.LENGTH_SHORT).show();
                return;
            }

            double monthlyRate = annualRate / (12 * 100);
            double emi;

            if (annualRate > 0) {
                emi = (principal * monthlyRate * Math.pow(1 + monthlyRate, totalMonths)) / (Math.pow(1 + monthlyRate, totalMonths) - 1);
            } else {
                emi = principal / totalMonths;
            }

            double totalPayable = emi * totalMonths;
            double totalInterest = totalPayable - principal;

            binding.textTotalPayable.setText(formatCurrency(emi));
            binding.textMonthlyPayment.setText(formatCurrency(principal));
            binding.textResLoanAmount.setText(formatCurrency(totalInterest));
            binding.textInterestAmount.setText(formatCurrency(totalPayable));

            binding.resultContainerLoan.setVisibility(View.VISIBLE);
            binding.keypadLoan.setVisibility(View.GONE);

            binding.layoutLoanAmount.setSelected(false);
            binding.layoutRateLoan.setSelected(false);
            binding.layoutPeriodLoan.setSelected(false);
        } catch (Exception e) {
            Toast.makeText(this, "Calculation error", Toast.LENGTH_SHORT).show();
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