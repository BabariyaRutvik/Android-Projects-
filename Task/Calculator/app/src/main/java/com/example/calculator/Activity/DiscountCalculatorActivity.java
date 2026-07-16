package com.example.calculator.Activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.calculator.R;
import com.example.calculator.databinding.ActivityDiscountCalculatorBinding;

import java.text.DecimalFormat;

public class DiscountCalculatorActivity extends AppCompatActivity {

    private ActivityDiscountCalculatorBinding binding;
    private int selectedField = 0; // 0: Original Price, 1: Discount %
    private String originalPriceStr = "";
    private String discountPercentStr = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDiscountCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Status bar setup
        WindowInsetsControllerCompat windowInsetsControllerCompat = ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsControllerCompat != null) {
            windowInsetsControllerCompat.setAppearanceLightStatusBars(true);
        }

        // hiding Soft Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initDiscountUI();
        setupDiscountKeypad();

        binding.resultContainerDiscount.setVisibility(View.GONE);
    }

    private void initDiscountUI() {
        binding.discountToolbar.setNavigationOnClickListener(v -> finish());

        View.OnClickListener fieldClickListener = v -> {
            if (v.getId() == R.id.layout_original_price || v.getId() == R.id.text_original_price) {
                selectedField = 0;
            } else if (v.getId() == R.id.layout_discount_percent || v.getId() == R.id.text_discount_percent) {
                selectedField = 1;
            }
            binding.resultContainerDiscount.setVisibility(View.GONE);
            binding.keypadDiscount.setVisibility(View.VISIBLE);
            updateDiscountSelectionUI();
        };

        binding.layoutOriginalPrice.setOnClickListener(fieldClickListener);
        binding.textOriginalPrice.setOnClickListener(fieldClickListener);
        binding.layoutDiscountPercent.setOnClickListener(fieldClickListener);
        binding.textDiscountPercent.setOnClickListener(fieldClickListener);

        binding.textOriginalPrice.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 0;
                updateDiscountSelectionUI();
            }
        });

        binding.textDiscountPercent.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 1;
                updateDiscountSelectionUI();
            }
        });

        binding.textOriginalPrice.setShowSoftInputOnFocus(false);
        binding.textDiscountPercent.setShowSoftInputOnFocus(false);

        updateDiscountSelectionUI();
    }

    private void updateDiscountSelectionUI() {
        binding.layoutOriginalPrice.setSelected(selectedField == 0);
        binding.layoutDiscountPercent.setSelected(selectedField == 1);

        if (selectedField == 0) {
            binding.textOriginalPrice.requestFocus();
            binding.textOriginalPrice.setSelection(binding.textOriginalPrice.getText().length());
            binding.textOriginalPrice.setCursorVisible(true);
            binding.textDiscountPercent.setCursorVisible(false);
        } else if (selectedField == 1) {
            binding.textDiscountPercent.requestFocus();
            binding.textDiscountPercent.setSelection(binding.textDiscountPercent.getText().length());
            binding.textDiscountPercent.setCursorVisible(true);
            binding.textOriginalPrice.setCursorVisible(false);
        }

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = getCurrentFocus();
        if (focusView == null) focusView = binding.getRoot();
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    private void setupDiscountKeypad() {
        View.OnClickListener listener = v -> {
            triggerVibration();
            String val = ((TextView) v).getText().toString();
            handleDiscountInput(val);
        };

        binding.btnDiscount7.setOnClickListener(listener);
        binding.btnDiscount8.setOnClickListener(listener);
        binding.btnDiscount9.setOnClickListener(listener);
        binding.btnDiscount4.setOnClickListener(listener);
        binding.btnDiscount5.setOnClickListener(listener);
        binding.btnDiscount6.setOnClickListener(listener);
        binding.btnDiscount1.setOnClickListener(listener);
        binding.btnDiscount2.setOnClickListener(listener);
        binding.btnDiscount3.setOnClickListener(listener);
        binding.btnDiscount00.setOnClickListener(listener);
        binding.btnDiscount0.setOnClickListener(listener);
        binding.btnDiscountDot.setOnClickListener(listener);

        binding.btnDiscountAc.setOnClickListener(v -> {
            triggerVibration();
            originalPriceStr = "";
            discountPercentStr = "";
            binding.textOriginalPrice.setText("");
            binding.textDiscountPercent.setText("");
            binding.resultContainerDiscount.setVisibility(View.GONE);
            binding.keypadDiscount.setVisibility(View.VISIBLE);
            selectedField = 0;
            updateDiscountSelectionUI();
        });

        binding.btnDiscountBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (selectedField == 0) {
                if (!originalPriceStr.isEmpty()) {
                    originalPriceStr = originalPriceStr.substring(0, originalPriceStr.length() - 1);
                    binding.textOriginalPrice.setText(formatCurrencyNoSymbol(originalPriceStr));
                    binding.textOriginalPrice.setSelection(binding.textOriginalPrice.getText().length());
                }
            } else if (selectedField == 1) {
                if (!discountPercentStr.isEmpty()) {
                    discountPercentStr = discountPercentStr.substring(0, discountPercentStr.length() - 1);
                    binding.textDiscountPercent.setText(discountPercentStr);
                    binding.textDiscountPercent.setSelection(binding.textDiscountPercent.getText().length());
                }
            }
            updateDiscountSelectionUI();
        });

        binding.btnDiscountEquals.setOnClickListener(v -> {
            triggerVibration();
            calculateDiscount();
        });
    }

    private void handleDiscountInput(String val) {
        if (selectedField == 0) {
            if (originalPriceStr.replace(",", "").length() + val.length() > 9) {
                Toast.makeText(this, getString(R.string.error_max_amount), Toast.LENGTH_SHORT).show();
                return;
            }
            if (val.equals(".") && originalPriceStr.contains(".")) return;
            originalPriceStr += val;
            binding.textOriginalPrice.setText(formatCurrencyNoSymbol(originalPriceStr));
            binding.textOriginalPrice.setSelection(binding.textOriginalPrice.getText().length());
        } else if (selectedField == 1) {
            String tempRate = discountPercentStr + val;
            try {
                double rate = Double.parseDouble(tempRate);
                if (rate > 99.99) {
                    Toast.makeText(this, getString(R.string.error_max_discount), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (tempRate.contains(".") && tempRate.substring(tempRate.indexOf(".") + 1).length() > 2) {
                    return;
                }
            } catch (Exception e) {
                if (!tempRate.equals(".")) return;
            }
            discountPercentStr = tempRate;
            binding.textDiscountPercent.setText(discountPercentStr);
            binding.textDiscountPercent.setSelection(binding.textDiscountPercent.getText().length());
        }
        updateDiscountSelectionUI();
    }

    private void calculateDiscount() {
        if (originalPriceStr.isEmpty() || discountPercentStr.isEmpty()) {
            Toast.makeText(this, getString(R.string.error_fill_all_fields), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            double originalPrice = Double.parseDouble(originalPriceStr.replace(",", ""));
            double discountRate = Double.parseDouble(discountPercentStr);

            if (originalPrice < 1) {
                Toast.makeText(this, getString(R.string.error_min_amount), Toast.LENGTH_SHORT).show();
                return;
            }

            double discountAmount = originalPrice * (discountRate / 100.0);
            double finalPrice = originalPrice - discountAmount;

            binding.textFinalAmount.setText(formatCurrency(finalPrice));
            binding.textOriginalAmountRes.setText(formatCurrency(originalPrice));
            binding.textDiscountAmountRes.setText(formatCurrency(discountAmount));

            binding.resultContainerDiscount.setVisibility(View.VISIBLE);
            binding.keypadDiscount.setVisibility(View.GONE);

            binding.layoutOriginalPrice.setSelected(false);
            binding.layoutDiscountPercent.setSelected(false);

        } catch (Exception e) {
            Toast.makeText(this, "Calculation error", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatCurrency(double amount) {
        DecimalFormat df = new DecimalFormat("₹#,##,##,###");
        return df.format(amount);
    }

    private String formatCurrencyNoSymbol(String val) {
        if (val == null || val.isEmpty()) return "";
        try {
            double amount = Double.parseDouble(val.replace(",", ""));
            DecimalFormat df = new DecimalFormat("#,##,##,###");
            return df.format(amount);
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
