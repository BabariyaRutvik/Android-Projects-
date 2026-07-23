package com.example.calculator.Activity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.calculator.R;
import com.example.calculator.databinding.ActivityBmiCalculatorBinding;
import com.google.android.material.button.MaterialButton;

import java.util.Locale;

public class BmiCalculatorActivity extends AppCompatActivity {

    private ActivityBmiCalculatorBinding binding;
    private int selectedField = 0; // 0 for weight, 1 for height
    private String weightInput = "";
    private String heightInput = "";
    private boolean isKg = true;
    private boolean isCm = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityBmiCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // hiding Soft Keyboard
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        initBMIUI();
        setupBMIKeypad();
    }

    private void initBMIUI() {
        binding.bmiToolbar.setNavigationOnClickListener(v -> finish());

        View.OnClickListener fieldClickListener = v -> {
            if (v.getId() == R.id.layout_bmi_weight || v.getId() == R.id.text_bmi_weight) {
                selectedField = 0;
            } else if (v.getId() == R.id.layout_bmi_height || v.getId() == R.id.text_bmi_height) {
                selectedField = 1;
            }
            updateBMIUI();
            updateLabelColors(false);
        };

        binding.layoutBmiWeight.setOnClickListener(fieldClickListener);
        binding.textBmiWeight.setOnClickListener(fieldClickListener);
        binding.layoutBmiHeight.setOnClickListener(fieldClickListener);
        binding.textBmiHeight.setOnClickListener(fieldClickListener);

        binding.textBmiWeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 0;
                updateBMIUI();
            }
        });

        binding.textBmiHeight.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                selectedField = 1;
                updateBMIUI();
            }
        });

        // Disable system keyboard
        binding.textBmiWeight.setShowSoftInputOnFocus(false);
        binding.textBmiHeight.setShowSoftInputOnFocus(false);

        // Toggles
        binding.btnBmiKg.setOnClickListener(v -> {
            if (isKg) return;
            isKg = true;
            binding.btnBmiKg.setChecked(true);
            binding.btnBmiLbs.setChecked(false);
            binding.resultContainer.setVisibility(View.GONE);
            updateBMIUI();
        });

        binding.btnBmiLbs.setOnClickListener(v -> {
            if (!isKg) return;
            isKg = false;
            binding.btnBmiKg.setChecked(false);
            binding.btnBmiLbs.setChecked(true);
            binding.resultContainer.setVisibility(View.GONE);
            updateBMIUI();
        });

        binding.btnBmiCm.setOnClickListener(v -> {
            if (isCm) return;
            isCm = true;
            binding.btnBmiCm.setChecked(true);
            binding.btnBmiIn.setChecked(false);
            binding.resultContainer.setVisibility(View.GONE);
            updateBMIUI();
        });

        binding.btnBmiIn.setOnClickListener(v -> {
            if (!isCm) return;
            isCm = false;
            binding.btnBmiCm.setChecked(false);
            binding.btnBmiIn.setChecked(true);
            binding.resultContainer.setVisibility(View.GONE);
            updateBMIUI();
        });

        updateBMIUI();
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

    private void updateLabelColors(boolean isResultMode) {
        int color = isResultMode ? getColor(R.color.text_secondary) : getColor(R.color.text_label_gray);
        binding.labelBmiWeight.setTextColor(color);
        binding.labelBmiHeight.setTextColor(color);
    }

    private void updateBMIUI() {
        binding.layoutBmiWeight.setSelected(selectedField == 0);
        binding.layoutBmiHeight.setSelected(selectedField == 1);

        // Show keypad when focus is back to inputs
        binding.keypadContainer.setVisibility(View.VISIBLE);

        if (selectedField == 0) {
            binding.textBmiWeight.requestFocus();
            binding.textBmiWeight.setSelection(binding.textBmiWeight.getText().length());
            binding.textBmiWeight.setCursorVisible(true);
            binding.textBmiHeight.setCursorVisible(false);
            setSelectionToEnd(binding.textBmiWeight);
        } else {
            binding.textBmiHeight.requestFocus();
            binding.textBmiHeight.setSelection(binding.textBmiHeight.getText().length());
            binding.textBmiHeight.setCursorVisible(true);
            binding.textBmiWeight.setCursorVisible(false);
            setSelectionToEnd(binding.textBmiHeight);
        }
    }

    private void setSelectionToEnd(TextView textView) {
        if (!(textView instanceof EditText)) return;
        EditText editText = (EditText) textView;
        editText.post(() -> {
            if (editText.getText() != null) {
                editText.setSelection(editText.getText().length());
            }
        });
    }

    private void setupBMIKeypad() {
        View.OnClickListener numberListener = v -> {
            triggerVibration();
            String val = ((MaterialButton) v).getText().toString();
            handleInput(val);
        };

        binding.btnKey0.setOnClickListener(numberListener);
        binding.btnKey1.setOnClickListener(numberListener);
        binding.btnKey2.setOnClickListener(numberListener);
        binding.btnKey3.setOnClickListener(numberListener);
        binding.btnKey4.setOnClickListener(numberListener);
        binding.btnKey5.setOnClickListener(numberListener);
        binding.btnKey6.setOnClickListener(numberListener);
        binding.btnKey7.setOnClickListener(numberListener);
        binding.btnKey8.setOnClickListener(numberListener);
        binding.btnKey9.setOnClickListener(numberListener);
        binding.btnKey00.setOnClickListener(numberListener);
        binding.btnKeyDot.setOnClickListener(numberListener);

        binding.btnKeyAc.setOnClickListener(v -> {
            triggerVibration();
            weightInput = "";
            heightInput = "";
            binding.textBmiWeight.setText("");
            binding.textBmiHeight.setText("");
            binding.resultContainer.setVisibility(View.GONE);
            selectedField = 0;
            updateBMIUI();
            updateLabelColors(false);
        });

        binding.btnKeyBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (selectedField == 0) {
                if (!weightInput.isEmpty()) {
                    weightInput = weightInput.substring(0, weightInput.length() - 1);
                    binding.textBmiWeight.setText(weightInput);
                    setSelectionToEnd(binding.textBmiWeight);
                }
            } else {
                if (!heightInput.isEmpty()) {
                    heightInput = heightInput.substring(0, heightInput.length() - 1);
                    binding.textBmiHeight.setText(heightInput);
                    setSelectionToEnd(binding.textBmiHeight);
                }
            }
            if (binding.resultContainer.getVisibility() == View.VISIBLE) calculateBMI(false);
        });

        binding.btnKeyEquals.setOnClickListener(v -> {
            triggerVibration();
            calculateBMI(true);
        });
    }

    private void handleInput(String val) {
        String currentInput = (selectedField == 0) ? weightInput : heightInput;
        if (val.equals(".") && currentInput.contains(".")) return;

        String digitsOnly = currentInput.replace(".", "");
        int increment = val.equals("00") ? 2 : (val.equals(".") ? 0 : 1);

        if (digitsOnly.length() + increment > 3) {
            Toast.makeText(this, R.string.err_max_3_digits, Toast.LENGTH_SHORT).show();
            return;
        }

        if (selectedField == 0) {
            weightInput += val;
            binding.textBmiWeight.setText(weightInput);
            setSelectionToEnd(binding.textBmiWeight);
        } else {
            heightInput += val;
            binding.textBmiHeight.setText(heightInput);
            setSelectionToEnd(binding.textBmiHeight);
        }
        if (binding.resultContainer.getVisibility() == View.VISIBLE) calculateBMI(false);
    }

    private void showLimitsDialog() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.dialog_bmi_invalid_input);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 336, getResources().getDisplayMetrics());
            int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(Math.min(widthPx, maxWidth), WindowManager.LayoutParams.WRAP_CONTENT);

            TextView btnOk = dialog.findViewById(R.id.btn_ok);
            btnOk.setOnClickListener(v -> {
                dialog.dismiss();
            });

            dialog.show();
        }
    }

    private void calculateBMI(boolean hideKeypad) {
        if (weightInput.isEmpty() || heightInput.isEmpty()) {
            if (hideKeypad) {
                Toast.makeText(this, R.string.error_fill_all_fields, Toast.LENGTH_SHORT).show();
            }
            return;
        }

        try {
            double weight = Double.parseDouble(weightInput);
            double height = Double.parseDouble(heightInput);

            // Validation based on selected unit
            boolean isInvalid;
            if (isKg) {
                isInvalid = (weight < 1.0 || weight > 175.0);
            } else {
                isInvalid = (weight < 2.0 || weight > 390.0);
            }

            if (!isInvalid) {
                if (isCm) {
                    isInvalid = (height < 50.0 || height > 250.0);
                } else {
                    isInvalid = (height < 20.0 || height > 98.0);
                }
            }

            if (isInvalid) {
                showLimitsDialog();
                return;
            }

            // Convert to metric for calculation
            double weightInKg = isKg ? weight : weight * 0.45359237;
            double heightInMeters = isCm ? (height / 100.0) : (height * 0.0254);

            if (heightInMeters == 0) return;

            double bmi = weightInKg / (heightInMeters * heightInMeters);

            binding.textBmiValue.setText(String.format(Locale.US, "%.2f", bmi));

            String status;
            int color;
            if (bmi < 18.5) {
                status = getString(R.string.underweight);
                color = ContextCompat.getColor(this, R.color.bmi_blue);
            } else if (bmi < 25.0) {
                status = getString(R.string.normal);
                color = ContextCompat.getColor(this, R.color.bmi_green);
            } else if (bmi < 30.0) {
                status = getString(R.string.overweight);
                color = ContextCompat.getColor(this, R.color.bmi_orange);
            } else {
                status = getString(R.string.obese);
                color = ContextCompat.getColor(this, R.color.bmi_red);
            }

            binding.textBmiValue.setTextColor(color);
            binding.textBmiStatus.setText(getString(R.string.bmi_result_prefix, status));
            binding.resultContainer.setVisibility(View.VISIBLE);

            if (hideKeypad) {
                binding.keypadContainer.setVisibility(View.GONE);
                // Clear focus and visual selection on result
                binding.layoutBmiWeight.setSelected(false);
                binding.layoutBmiHeight.setSelected(false);
                binding.textBmiWeight.clearFocus();
                binding.textBmiHeight.clearFocus();
                updateLabelColors(true);
            }

        } catch (NumberFormatException e) {
            Toast.makeText(this, R.string.err_invalid_calculation, Toast.LENGTH_SHORT).show();
        }
    }
}