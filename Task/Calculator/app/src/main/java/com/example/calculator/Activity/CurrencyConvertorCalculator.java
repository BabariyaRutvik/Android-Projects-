package com.example.calculator.Activity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.provider.Settings;
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
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.calculator.BottomSheet.CurrencyBottomSheet;
import com.example.calculator.Currency.Networking.ApiClient;
import com.example.calculator.Currency.Networking.ApiService;
import com.example.calculator.Currency.Networking.ExchangeResponse;
import com.example.calculator.R;
import com.example.calculator.databinding.ActivityCurrencyConvertorCalculatorBinding;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CurrencyConvertorCalculator extends AppCompatActivity {

    private ActivityCurrencyConvertorCalculatorBinding binding;
    // state flags for input
    private String sourceCurrency = "INR";
    private String targetCurrency = "USD";
    private String sourceCurrencyName = "";
    private String targetCurrencyName = "";
    private String enteredAmount = "";
    private Map<String, Double> exchangeRates = new HashMap<>();
    private ApiService apiService;

    private Dialog progressDialog;
    private Dialog connectionErrorDialog;
    private boolean isReturningFromSettings = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sourceCurrencyName = getString(R.string.default_currency_from_name);
        targetCurrencyName = getString(R.string.default_currency_to_name);
        EdgeToEdge.enable(this);
        binding = ActivityCurrencyConvertorCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        apiService = ApiClient.getClient().create(ApiService.class);

        // for status bar and hiding keyboard
        WindowInsetsControllerCompat windowInsetsController =
                WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.hide(WindowInsetsCompat.Type.ime());
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initUI();
        setUpCurrencykeypad();

        binding.textCurrencyCodeFrom.setText(sourceCurrency);
        binding.textCurrencyNameFrom.setText(sourceCurrencyName);
        binding.textCurrencyCodeTo.setText(targetCurrency);
        binding.textCurrencyNameTo.setText(targetCurrencyName);

        binding.textCurrencyValueFrom.setShowSoftInputOnFocus(false);
        updateCurrencySelectionUI(true);

        // Hide info text until data is loaded
        binding.textExchangeInfo.setVisibility(View.GONE);

        fetchExchangeRates();
    }

    private void initUI() {
        binding.currencyToolbar.setNavigationOnClickListener(v -> finish());

        // show the bottom sheet dialog
        binding.layoutPickerFrom.setOnClickListener(v -> showCurrencyPicker(true));
        binding.layoutPickerTo.setOnClickListener(v -> showCurrencyPicker(false));

        // highlight border
        binding.layoutCurrencyFrom.setOnClickListener(v -> updateCurrencySelectionUI(true));
        binding.textCurrencyValueFrom.setOnClickListener(v -> updateCurrencySelectionUI(true));
        binding.layoutCurrencyTo.setOnClickListener(v -> updateCurrencySelectionUI(false));
        binding.textCurrencyValueTo.setOnClickListener(v -> updateCurrencySelectionUI(false));

        binding.textCurrencyValueFrom.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                updateCurrencySelectionUI(true);
            }
        });
    }

    // update selection ui
    private void updateCurrencySelectionUI(boolean isFromSelected) {
        // toggle the Selected state
        binding.layoutCurrencyFrom.setSelected(isFromSelected);
        binding.layoutCurrencyTo.setSelected(!isFromSelected);

        if (isFromSelected) {
            binding.textCurrencyValueFrom.requestFocus();
            binding.textCurrencyValueFrom.setSelection(binding.textCurrencyValueFrom.getText().length());
            binding.textCurrencyValueFrom.setCursorVisible(true);
            
            // Force hide keyboard
            WindowInsetsControllerCompat windowInsetsController =
                    WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (windowInsetsController != null) {
                windowInsetsController.hide(WindowInsetsCompat.Type.ime());
            }
        } else {
            binding.textCurrencyValueFrom.clearFocus();
            binding.textCurrencyValueFrom.setCursorVisible(false);
        }
    }

    private void showCurrencyPicker(boolean isSourcePicker) {
        CurrencyBottomSheet bottomSheet = new CurrencyBottomSheet(selectedCurrency -> {
            if (isSourcePicker) {
                sourceCurrency = selectedCurrency.getCurrencyCode();
                sourceCurrencyName = selectedCurrency.getCurrencyName();
                binding.textCurrencyCodeFrom.setText(sourceCurrency);
                binding.textCurrencyNameFrom.setText(sourceCurrencyName);
            } else {
                targetCurrency = selectedCurrency.getCurrencyCode();
                targetCurrencyName = selectedCurrency.getCurrencyName();
                binding.textCurrencyCodeTo.setText(targetCurrency);
                binding.textCurrencyNameTo.setText(targetCurrencyName);
            }
            fetchExchangeRates();
        });
        bottomSheet.show(getSupportFragmentManager(), "CurrencyBottomSheet");
    }

    private void setUpCurrencykeypad() {
        View.OnClickListener numberListener = v -> {
            triggerVibration();
            String digit = ((TextView) v).getText().toString();
            handleKeypadInput(digit);
        };

        binding.btnCurr7.setOnClickListener(numberListener);
        binding.btnCurr8.setOnClickListener(numberListener);
        binding.btnCurr9.setOnClickListener(numberListener);
        binding.btnCurr4.setOnClickListener(numberListener);
        binding.btnCurr5.setOnClickListener(numberListener);
        binding.btnCurr6.setOnClickListener(numberListener);
        binding.btnCurr1.setOnClickListener(numberListener);
        binding.btnCurr2.setOnClickListener(numberListener);
        binding.btnCurr3.setOnClickListener(numberListener);
        binding.btnCurr0.setOnClickListener(numberListener);
        binding.btnCurr00.setOnClickListener(numberListener);
        binding.btnCurrDot.setOnClickListener(numberListener);

        // ac button
        binding.btnCurrAc.setOnClickListener(v -> {
            triggerVibration();
            enteredAmount = "";
            binding.textCurrencyValueFrom.setText("");
            binding.textCurrencyValueTo.setText("0");
            updateCurrencySelectionUI(true);
        });

        // backspace button
        binding.btnCurrBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (!enteredAmount.isEmpty()) {
                enteredAmount = enteredAmount.substring(0, enteredAmount.length() - 1);
                binding.textCurrencyValueFrom.setText(enteredAmount);
                binding.textCurrencyValueFrom.setSelection(enteredAmount.length());
                calculateConversion();
            }
        });

        // swap button
        binding.btnCurrSwap.setOnClickListener(v -> {
            triggerVibration();
            String tempCode = sourceCurrency;
            sourceCurrency = targetCurrency;
            targetCurrency = tempCode;

            String tempName = sourceCurrencyName;
            sourceCurrencyName = targetCurrencyName;
            targetCurrencyName = tempName;

            binding.textCurrencyCodeFrom.setText(sourceCurrency);
            binding.textCurrencyNameFrom.setText(sourceCurrencyName);
            binding.textCurrencyCodeTo.setText(targetCurrency);
            binding.textCurrencyNameTo.setText(targetCurrencyName);

            fetchExchangeRates();
        });
    }

    private void handleKeypadInput(String value) {
        if (enteredAmount.length() + value.length() > 9) {
            Toast.makeText(this, R.string.err_max_9_digits, Toast.LENGTH_SHORT).show();
            return;
        }
        if (value.equals(".") && enteredAmount.contains(".")) {
            return;
        }
        enteredAmount += value;
        binding.textCurrencyValueFrom.setText(enteredAmount);
        binding.textCurrencyValueFrom.setSelection(enteredAmount.length());
        calculateConversion();
    }

    private void fetchExchangeRates() {
        if (!isNetworkAvailable()) {
            showConnectionErrorDialog(getString(R.string.offline_title), getString(R.string.offline_message));
            return;
        }

        showLoadingProgress();

        apiService.getLatestExchangeRates(sourceCurrency).enqueue(new Callback<>() {
            @Override
            public void onResponse(@NonNull Call<ExchangeResponse> call, @NonNull Response<ExchangeResponse> response) {
                hideLoadingProgress();
                if (response.isSuccessful() && response.body() != null) {
                    exchangeRates = response.body().getRates();
                    String date = response.body().getDate();
                    if (date != null) {
                        binding.textExchangeInfo.setVisibility(View.VISIBLE);
                        binding.textExchangeInfo.setText(getString(R.string.exchange_rate_info, date));
                    }
                    calculateConversion();
                } else {
                    showConnectionErrorDialog(getString(R.string.no_data_title), getString(R.string.no_data_message));
                }
            }

            @Override
            public void onFailure(@NonNull Call<ExchangeResponse> call, @NonNull Throwable t) {
                hideLoadingProgress();
                showConnectionErrorDialog(getString(R.string.no_data_title), getString(R.string.no_data_message));
            }
        });
    }

    private void calculateConversion() {
        if (enteredAmount.isEmpty()) {
            binding.textCurrencyValueTo.setText("0");
            return;
        }

        try {
            double enteredVal = Double.parseDouble(enteredAmount);
            Double multiplier = exchangeRates.get(targetCurrency);
            if (multiplier != null) {
                double conversionOutput = enteredVal * multiplier;
                binding.textCurrencyValueTo.setText(String.format(Locale.getDefault(), "%.2f", conversionOutput));
            } else {
                binding.textCurrencyValueTo.setText("---");
            }
        } catch (NumberFormatException e) {
            binding.textCurrencyValueTo.setText("0");
        }
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm != null) {
            NetworkCapabilities capabilities = cm.getNetworkCapabilities(cm.getActiveNetwork());
            return capabilities != null && (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) || capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR));
        }
        return false;
    }

    private void showLoadingProgress() {
        if (progressDialog == null) {
            progressDialog = new Dialog(this);
            progressDialog.setContentView(R.layout.dialog_progress);
            Window window = progressDialog.getWindow();
            if (window != null) {
                window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                // Do not clear the dim background
                window.addFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
            }
            progressDialog.setCancelable(false);
        }
        if (!progressDialog.isShowing()) {
            progressDialog.show();
        }
    }

    private void hideLoadingProgress() {
        if (progressDialog != null && progressDialog.isShowing()) {
            progressDialog.dismiss();
        }
    }

    private void showConnectionErrorDialog(String title, String message) {
        if (connectionErrorDialog != null && connectionErrorDialog.isShowing()) {
            connectionErrorDialog.dismiss();
        }

        connectionErrorDialog = new Dialog(this);
        connectionErrorDialog.setContentView(R.layout.dialog_currency_info);

        Window window = connectionErrorDialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int widthPx = (int) android.util.TypedValue.applyDimension(android.util.TypedValue.COMPLEX_UNIT_DIP, 336, getResources().getDisplayMetrics());
            int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(Math.min(widthPx, maxWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        TextView txtTitle = connectionErrorDialog.findViewById(R.id.text_dialog_title);
        TextView txtMessage = connectionErrorDialog.findViewById(R.id.text_dialog_message);
        TextView btnCancel = connectionErrorDialog.findViewById(R.id.btn_cancel);
        TextView btnAction = connectionErrorDialog.findViewById(R.id.btn_action);

        txtTitle.setText(title);
        txtMessage.setText(message);

        if (title.equals(getString(R.string.offline_title))) {
            btnAction.setText(R.string.connect_wifi);
            btnAction.setOnClickListener(v -> {
                isReturningFromSettings = true;
                connectionErrorDialog.dismiss();
                startActivity(new Intent(Settings.ACTION_WIFI_SETTINGS));
            });
        } else {
            btnAction.setText(R.string.refresh);
            btnAction.setOnClickListener(v -> {
                connectionErrorDialog.dismiss();
                fetchExchangeRates();
            });
        }

        btnCancel.setOnClickListener(v -> connectionErrorDialog.dismiss());
        connectionErrorDialog.show();
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
                vibrator.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE));
            } else {
                vibrator.vibrate(30);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (exchangeRates.isEmpty()) {
            if (isReturningFromSettings) {
                isReturningFromSettings = false;
                if (isNetworkAvailable()) {
                    showConnectionErrorDialog(getString(R.string.no_data_title), getString(R.string.no_data_message));
                } else {
                    showConnectionErrorDialog(getString(R.string.offline_title), getString(R.string.offline_message));
                }
            } else {
                fetchExchangeRates();
            }
        }
    }
}
