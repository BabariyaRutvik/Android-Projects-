package com.example.calculator.Activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.text.InputType;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.calculator.R;
import com.example.calculator.databinding.ActivityGstCalculatorBinding;

import java.text.DecimalFormat;

public class GSTCalculatorActivity extends AppCompatActivity {

    private ActivityGstCalculatorBinding binding;
    private int selectedField = 0; // 0: Amount, 1: Rate
    private String rawAmountStr = "";
    private String rawRateStr = "";
    private boolean isAddGST = true;
    private boolean isIntraState = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGstCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUI();
        setupKeypad();
        
        binding.resultContainerGst.setVisibility(View.GONE);
    }

    private void initUI() {
        binding.gstToolbar.setNavigationOnClickListener(v -> finish());

        disableSystemKeyboard(binding.textGstAmount);
        disableSystemKeyboard(binding.textGstRate);

        View.OnClickListener fieldClickListener = v -> {
            if (v.getId() == R.id.layout_original_price || v.getId() == R.id.text_gst_amount) selectedField = 0;
            else if (v.getId() == R.id.layout_gst_rate || v.getId() == R.id.text_gst_rate) selectedField = 1;
            
            binding.resultContainerGst.setVisibility(View.GONE);
            binding.keypadGst.setVisibility(View.VISIBLE);
            updateSelectionUI();
        };

        binding.layoutOriginalPrice.setOnClickListener(fieldClickListener);
        binding.textGstAmount.setOnClickListener(fieldClickListener);
        binding.layoutGstRate.setOnClickListener(fieldClickListener);
        binding.textGstRate.setOnClickListener(fieldClickListener);

        binding.textGstAmount.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 0;
                updateSelectionUI();
            }
        });

        binding.textGstRate.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 1;
                updateSelectionUI();
            }
        });

        // Toggles
        binding.btnGstAdd.setOnClickListener(v -> {
            if (isAddGST) return;
            isAddGST = true;
            binding.btnGstAdd.setChecked(true);
            binding.btnGstRemove.setChecked(false);
            updateSelectionUI();
            if (binding.resultContainerGst.getVisibility() == View.VISIBLE) updateCalculations();
        });

        binding.btnGstRemove.setOnClickListener(v -> {
            if (!isAddGST) return;
            isAddGST = false;
            binding.btnGstAdd.setChecked(false);
            binding.btnGstRemove.setChecked(true);
            updateSelectionUI();
            if (binding.resultContainerGst.getVisibility() == View.VISIBLE) updateCalculations();
        });

        binding.btnIntraState.setOnClickListener(v -> {
            if (isIntraState) return;
            isIntraState = true;
            binding.btnIntraState.setChecked(true);
            binding.btnInterState.setChecked(false);
            updateSelectionUI();
            if (binding.resultContainerGst.getVisibility() == View.VISIBLE) updateCalculations();
        });

        binding.btnInterState.setOnClickListener(v -> {
            if (!isIntraState) return;
            isIntraState = false;
            binding.btnIntraState.setChecked(false);
            binding.btnInterState.setChecked(true);
            updateSelectionUI();
            if (binding.resultContainerGst.getVisibility() == View.VISIBLE) updateCalculations();
        });

        updateSelectionUI();
    }

    private void disableSystemKeyboard(EditText editText) {
        editText.setShowSoftInputOnFocus(false);
        editText.setRawInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        editText.setTextIsSelectable(true);
        editText.setShowSoftInputOnFocus(false);
    }

    private void updateSelectionUI() {
        boolean resultsVisible = binding.resultContainerGst.getVisibility() == View.VISIBLE;

        binding.layoutOriginalPrice.setSelected(!resultsVisible && selectedField == 0);
        binding.layoutGstRate.setSelected(!resultsVisible && selectedField == 1);

        if (resultsVisible) {
            binding.keypadGst.setVisibility(View.GONE);
            binding.textGstAmount.setCursorVisible(false);
            binding.textGstRate.setCursorVisible(false);
            binding.textGstAmount.clearFocus();
            binding.textGstRate.clearFocus();
            return;
        }

        binding.keypadGst.setVisibility(View.VISIBLE);

        if (selectedField == 0) {
            binding.textGstAmount.requestFocus();
            if (binding.textGstAmount.getText() != null) {
                binding.textGstAmount.setSelection(binding.textGstAmount.getText().length());
            }
            binding.textGstAmount.setCursorVisible(true);
            binding.textGstRate.setCursorVisible(false);
        } else if (selectedField == 1) {
            binding.textGstRate.requestFocus();
            if (binding.textGstRate.getText() != null) {
                binding.textGstRate.setSelection(binding.textGstRate.getText().length());
            }
            binding.textGstRate.setCursorVisible(true);
            binding.textGstAmount.setCursorVisible(false);
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
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

        binding.btnGst7.setOnClickListener(listener);
        binding.btnGst8.setOnClickListener(listener);
        binding.btnGst9.setOnClickListener(listener);
        binding.btnGst4.setOnClickListener(listener);
        binding.btnGst5.setOnClickListener(listener);
        binding.btnGst6.setOnClickListener(listener);
        binding.btnGst1.setOnClickListener(listener);
        binding.btnGst2.setOnClickListener(listener);
        binding.btnGst3.setOnClickListener(listener);
        binding.btnGst00.setOnClickListener(listener);
        binding.btnGst0.setOnClickListener(listener);
        binding.btnGstDot.setOnClickListener(listener);

        binding.btnGstAc.setOnClickListener(v -> {
            triggerVibration();
            rawAmountStr = ""; rawRateStr = "";
            binding.textGstAmount.setText("");
            binding.textGstRate.setText("");
            
            isAddGST = true;
            binding.btnGstAdd.setChecked(true);
            binding.btnGstRemove.setChecked(false);
            
            isIntraState = true;
            binding.btnIntraState.setChecked(true);
            binding.btnInterState.setChecked(false);
            
            binding.resultContainerGst.setVisibility(View.GONE);
            binding.keypadGst.setVisibility(View.VISIBLE);
            selectedField = 0;
            updateSelectionUI();
        });

        binding.btnGstBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (selectedField == 0) {
                if (!rawAmountStr.isEmpty()) {
                    rawAmountStr = rawAmountStr.substring(0, rawAmountStr.length() - 1);
                    binding.textGstAmount.setText(formatCurrencyNoSymbol(rawAmountStr));
                }
            } else {
                if (!rawRateStr.isEmpty()) {
                    rawRateStr = rawRateStr.substring(0, rawRateStr.length() - 1);
                    binding.textGstRate.setText(rawRateStr);
                }
            }
            if (binding.resultContainerGst.getVisibility() == View.VISIBLE) updateCalculations();
            updateSelectionUI();
        });

        binding.btnGstEquals.setOnClickListener(v -> {
            triggerVibration();
            calculateGST();
        });
    }

    private void handleInput(String val) {
        if (selectedField == 0) {
            String digitsOnly = rawAmountStr.replace(".", "");
            int increment = val.equals("00") ? 2 : (val.equals(".") ? 0 : 1);
            
            if (digitsOnly.length() + increment > 9) {
                Toast.makeText(this, R.string.err_max_9_digits, Toast.LENGTH_SHORT).show();
                return;
            }
            
            if (val.equals(".") && rawAmountStr.contains(".")) return;
            
            rawAmountStr += val;
            binding.textGstAmount.setText(formatCurrencyNoSymbol(rawAmountStr));
        } else {
            String tempRate = rawRateStr + val;
            try {
                if (!tempRate.equals(".")) {
                    double rate = Double.parseDouble(tempRate);
                    if (rate >= 40.0) {
                        Toast.makeText(this, R.string.err_rate_limit_40, Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
                if (tempRate.contains(".") && tempRate.substring(tempRate.indexOf(".") + 1).length() > 2) return;
            } catch (Exception e) { if (!tempRate.equals(".")) return; }
            rawRateStr = tempRate;
            binding.textGstRate.setText(rawRateStr);
        }
        if (binding.resultContainerGst.getVisibility() == View.VISIBLE) updateCalculations();
        updateSelectionUI();
    }

    private void calculateGST() {
        if (rawAmountStr.isEmpty() || rawRateStr.isEmpty() || rawAmountStr.equals(".") || rawRateStr.equals(".")) {
            Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            double amount = Double.parseDouble(rawAmountStr.replace(",", ""));
            if (amount < 100) {
                Toast.makeText(this, R.string.err_min_amount_100, Toast.LENGTH_SHORT).show();
                return;
            }
        } catch (Exception ignored) {}

        updateCalculations();
        binding.resultContainerGst.setVisibility(View.VISIBLE);
        binding.main.requestFocus();
        updateSelectionUI();
    }

    private void updateCalculations() {
        try {
            double amount = Double.parseDouble(rawAmountStr.replace(",", ""));
            double rate = Double.parseDouble(rawRateStr);
            if (amount < 100) return;

            boolean isAdd = isAddGST;
            boolean isIntra = isIntraState;

            double base, tax, total;
            if (isAdd) { base = amount; tax = base * (rate / 100.0); total = base + tax; }
            else { total = amount; base = total / (1.0 + (rate / 100.0)); tax = total - base; }

            binding.textTotalAmount.setText(formatCurrency(isAdd ? total : base));
            binding.textResGstAmount.setText(formatCurrency(tax));

            if (isIntra) {
                binding.layoutCgstSgst.setVisibility(View.VISIBLE);
                binding.layoutIgst.setVisibility(View.GONE);
                double halfTax = tax / 2.0; double halfRate = rate / 2.0;
                binding.labelCgst.setText(String.format("CGST (%.1f%%)", halfRate));
                binding.labelSgst.setText(String.format("SGST (%.1f%%)", halfRate));
                binding.textCgst.setText(formatCurrency(halfTax));
                binding.textSgst.setText(formatCurrency(halfTax));
            } else {
                binding.layoutCgstSgst.setVisibility(View.GONE);
                binding.layoutIgst.setVisibility(View.VISIBLE);
                binding.labelIgst.setText(String.format("IGST (%.0f%%)", rate));
                binding.textIgst.setText(formatCurrency(tax));
            }
        } catch (Exception ignored) {}
    }

    private String formatCurrency(double val) {
        return new DecimalFormat("₹#,##,##,###").format(Math.round(val));
    }

    private String formatCurrencyNoSymbol(String val) {
        if (val.isEmpty() || val.equals(".")) return val;
        try {
            DecimalFormat df = val.contains(".") ? new DecimalFormat("#,##,##,###.##") : new DecimalFormat("#,##,##,###");
            return df.format(Double.parseDouble(val.replace(",", "")));
        } catch (Exception e) { return val; }
    }

    private void triggerVibration() {
        Vibrator v;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) v = ((VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE)).getDefaultVibrator();
        else v = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (v != null && v.hasVibrator()) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) v.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
            else v.vibrate(30);
        }
    }
}
