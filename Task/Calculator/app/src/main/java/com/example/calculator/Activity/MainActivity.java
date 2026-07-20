package com.example.calculator.Activity;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calculator.Database.HistoryItem;
import com.example.calculator.R;
import com.example.calculator.Database.HistoryViewModel;
import com.example.calculator.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.shape.ShapeAppearanceModel;

import java.text.DecimalFormat;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private HistoryViewModel historyViewModel;
    private boolean scientificMode = false;
    private boolean isDegreeMode = true;
    private boolean isInverseMode = false;
    private String expression = "";
    private boolean resultShown = false;
    private String lastOperator = "";
    private String lastOperand = "";

    private MaterialButton[] numberButtons;
    private MaterialButton[] iconsButtons;
    private MaterialButton[] scientificButtons;
    private MaterialButton btnAc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Apply saved theme before super.onCreate
        SharedPreferences preferences = getSharedPreferences("theme_prefs", MODE_PRIVATE);
        int savedTheme = preferences.getInt("selected_theme", 2);
        if (savedTheme == 0) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        else if (savedTheme == 1) AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        else AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);

        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SharedPreferences sciPrefs = getSharedPreferences("scientific_prefs", MODE_PRIVATE);
        scientificMode = sciPrefs.getBoolean("scientific_enabled", false);

        initializeStandardButtons();

        View scientificView = binding.layoutScientific.getRoot();

        if (scientificMode){
            scientificView.setVisibility(View.VISIBLE);
            binding.imageSwitch.setImageResource(R.drawable.ic_standard);

            // for the buttons to change their sizes
            applyLerpedLayoutChanges(1.0f);
        }
        else {
            scientificView.setVisibility(View.GONE);
            binding.imageSwitch.setImageResource(R.drawable.ic_scientific);
            applyLerpedLayoutChanges(0.0f);
        }
        updateResultFieldForMode();


        // for Calculate Functionality
        setUpCalculatorButtons();

        historyViewModel = new androidx.lifecycle.ViewModelProvider(this).get(HistoryViewModel.class);

        historyViewModel.getAllHistory().observe(this, items -> {
            if (items != null && !items.isEmpty()) {
                binding.imageHistory.setVisibility(View.VISIBLE);
            } else {
                binding.imageHistory.setVisibility(View.GONE);
            }
        });

        binding.textResult.setShowSoftInputOnFocus(false);
        binding.textResult.requestFocus();

        binding.imageSwitch.setOnClickListener(v -> toggleScientificMode());

        binding.imageLayout.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, OtherCalculatorActivity.class);
            startActivity(intent);
        });
        binding.imageHistory.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });
        binding.imageSettings.setOnClickListener(v -> {
        Intent intent = new Intent(this, SettingsScreenActivity.class);
        startActivity(intent);
        });

        expression = "0";
        refreshDisplay();
        handleIntent(getIntent());
    }

    private void handleIntent(Intent intent) {
        if (intent != null && intent.hasExtra("expression")) {
            expression = intent.getStringExtra("expression");
            refreshDisplay();
            CalculateLive();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        if (intent != null && intent.hasExtra("expression")) {
            expression = intent.getStringExtra("expression");
            refreshDisplay();
            CalculateLive();
        }
    }

    private void triggerVibration(){

        // Check if the user turned vibration on/off in the settings screen
        SharedPreferences sharedPrefs = getSharedPreferences("vibration_prefs", MODE_PRIVATE);
        boolean isVibrationEnabled = sharedPrefs.getBoolean("vibration_enabled", true);

        if (!isVibrationEnabled){
            return;
        }
        Vibrator vibrator;


        // android 12 or above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            VibratorManager vibratorManager = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = vibratorManager.getDefaultVibrator();
        }
        else {
            vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        }
        // android oreo or above version
        if (vibrator != null && vibrator.hasVibrator()){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                vibrator.vibrate(VibrationEffect.createOneShot(60, VibrationEffect.DEFAULT_AMPLITUDE));

            }
            else {
                vibrator.vibrate(60);
            }
        }
    }

    private void toggleScientificMode() {
        scientificMode = !scientificMode;

        float start = scientificMode ? 0.0f : 1.0f;
        float end = scientificMode ? 1.0f : 0.0f;

        ValueAnimator animator = ValueAnimator.ofFloat(start, end);
        animator.setDuration(350);
        animator.setInterpolator(new AccelerateDecelerateInterpolator());

        View scientificView = binding.layoutScientific.getRoot();

        animator.addUpdateListener(animation -> {
            float fraction = (float) animation.getAnimatedValue();

            ConstraintLayout.LayoutParams lpSci = (ConstraintLayout.LayoutParams) scientificView.getLayoutParams();
            lpSci.verticalWeight = Math.max(0.001f, fraction * 3.0f);
            scientificView.setLayoutParams(lpSci);

            applyLerpedLayoutChanges(fraction);
        });

        animator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationStart(Animator animation) {
                if (scientificMode) {
                    scientificView.setVisibility(View.VISIBLE);
                    binding.imageSwitch.setImageResource(R.drawable.ic_standard);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!scientificMode) {
                    scientificView.setVisibility(View.GONE);
                    binding.imageSwitch.setImageResource(R.drawable.ic_scientific);
                }
            }
        });

        animator.start();
        updateResultFieldForMode();
    }

    private void applyLerpedLayoutChanges(float fraction) {
        float numSize = 32f + (22f - 32f) * fraction;
        float acSize = 26f + (24f - 26f) * fraction;
        int iconSizePx = dpToPx((int) (34f + (26f - 34f) * fraction));

        // Calculate heights and shapes for standard buttons
        int currentHeightPx = dpToPx((int) (78f + (46f - 78f) * fraction));
        ShapeAppearanceModel currentShape = ShapeAppearanceModel.builder(this, fraction > 0.5f ? R.style.ScientificButtonShape : R.style.CircleButtonShape, 0).build();

        if (btnAc != null) {
            btnAc.setTextSize(TypedValue.COMPLEX_UNIT_SP, acSize);
            btnAc.setGravity(Gravity.CENTER);
            btnAc.setPadding(0, 0, 0, 0);
            btnAc.getLayoutParams().width = dpToPx(78);
            btnAc.getLayoutParams().height = currentHeightPx;
            btnAc.setShapeAppearanceModel(currentShape);
        }

        if (numberButtons != null) {
            for (MaterialButton btn : numberButtons) {
                if (btn != null) {
                    btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, numSize);
                    btn.setGravity(Gravity.CENTER);
                    btn.setPadding(0, 0, 0, 0);
                    btn.getLayoutParams().width = dpToPx(78);
                    btn.getLayoutParams().height = currentHeightPx;
                    btn.setShapeAppearanceModel(currentShape);
                }
            }
        }

        if (iconsButtons != null) {
            for (MaterialButton btn : iconsButtons) {
                if (btn != null) {
                    btn.setIconSize(iconSizePx);
                    btn.setGravity(Gravity.CENTER);
                    btn.setIconPadding(0);
                    btn.setPadding(0, 0, 0, 0);
                    btn.getLayoutParams().width = dpToPx(78);
                    btn.getLayoutParams().height = currentHeightPx;
                    btn.setShapeAppearanceModel(currentShape);
                }
            }
        }

        // Animate scientific buttons text size and icon size
        float sciSize = 22f * fraction;
        int sciIconSizePi = dpToPx((int) (26f * fraction));
        int sciIconSizeSwap = dpToPx((int) (30f * fraction));

        if (scientificButtons != null) {
            for (MaterialButton btn : scientificButtons) {
                if (btn != null) {
                    if (btn.getText() != null && !btn.getText().toString().isEmpty()) {
                        btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, sciSize);
                    }
                    if (btn.getId() == R.id.btn_pi) {
                        btn.setIconSize(sciIconSizePi);
                    } else if (btn.getId() == R.id.btn_swap) {
                        btn.setIconSize(sciIconSizeSwap);
                    }
                    btn.setGravity(Gravity.CENTER);
                    btn.setPadding(0, 0, 0, 0);
                    btn.getLayoutParams().width = dpToPx(78);
                    btn.getLayoutParams().height = currentHeightPx;
                    btn.setShapeAppearanceModel(currentShape);
                }
            }
        }
    }

    private void updateResultFieldForMode() {
        binding.textResult.requestFocus();
        binding.textResult.setSelection(binding.textResult.getText().length());
    }

    private void initializeStandardButtons() {
        btnAc = binding.layoutStandard.btnAc;

        numberButtons = new MaterialButton[]{
                binding.layoutStandard.btn7, binding.layoutStandard.btn8, binding.layoutStandard.btn9,
                binding.layoutStandard.btn4, binding.layoutStandard.btn5, binding.layoutStandard.btn6,
                binding.layoutStandard.btn1, binding.layoutStandard.btn2, binding.layoutStandard.btn3,
                binding.layoutStandard.btn00, binding.layoutStandard.btn0, binding.layoutStandard.btnDot
        };

        iconsButtons = new MaterialButton[]{
                binding.layoutStandard.btnPlusMinus, binding.layoutStandard.btnPercent,
                binding.layoutStandard.btnDivide, binding.layoutStandard.btnMultiply,
                binding.layoutStandard.btnMinus, binding.layoutStandard.btnPlus,
                binding.layoutStandard.idEquals
        };

        scientificButtons = new MaterialButton[]{
                binding.layoutScientific.btnFactorial, binding.layoutScientific.btnE,
                binding.layoutScientific.btnPower, binding.layoutScientific.btnDeg,
                binding.layoutScientific.btnLog, binding.layoutScientific.btnPi,
                binding.layoutScientific.btnBrackets, binding.layoutScientific.btnSwap,
                binding.layoutScientific.btnSin, binding.layoutScientific.btnCos,
                binding.layoutScientific.btnTan, binding.layoutScientific.btnCot
        };
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }
    private void setUpCalculatorButtons() {

        // for numbers
        binding.layoutStandard.btn00.setOnClickListener(v-> appendNumber("00"));
        binding.layoutStandard.btn0.setOnClickListener(v-> appendNumber("0"));
        binding.layoutStandard.btn1.setOnClickListener(v-> appendNumber("1"));
        binding.layoutStandard.btn2.setOnClickListener(v-> appendNumber("2"));
        binding.layoutStandard.btn3.setOnClickListener(v-> appendNumber("3"));
        binding.layoutStandard.btn4.setOnClickListener(v-> appendNumber("4"));
        binding.layoutStandard.btn5.setOnClickListener(v-> appendNumber("5"));
        binding.layoutStandard.btn6.setOnClickListener(v-> appendNumber("6"));
        binding.layoutStandard.btn7.setOnClickListener(v-> appendNumber("7"));
        binding.layoutStandard.btn8.setOnClickListener(v-> appendNumber("8"));
        binding.layoutStandard.btn9.setOnClickListener(v-> appendNumber("9"));
        binding.layoutStandard.btnDot.setOnClickListener(v-> appendNumber("."));


        // operation  button operators
        binding.layoutStandard.btnPlus.setOnClickListener(v -> appendOperator("+"));
        binding.layoutStandard.btnMinus.setOnClickListener(v -> appendOperator("-"));
        binding.layoutStandard.btnMultiply.setOnClickListener(v -> appendOperator("×"));
        binding.layoutStandard.btnDivide.setOnClickListener(v -> appendOperator("÷"));

        // AC
        binding.layoutStandard.btnAc.setOnClickListener(v->{
            triggerVibration();
            expression = "";
            lastOperator = "";
            lastOperand = "";
            binding.textResult.setText("");
            binding.textFinalResult.setText("");
            binding.textResult.requestFocus();
        });
        // backspace
        binding.imageBackspace.setOnClickListener(v->{
            triggerVibration();
            if (!expression.isEmpty() && !expression.equals("0")){
                // Remove grouping separators if any before backspacing
                expression = expression.replace(",", "");
                expression = expression.substring(0, expression.length()-1);

                if (expression.isEmpty()){
                    expression = "0";
                    binding.textResult.setText("0");
                    binding.textFinalResult.setText("");
                }else {
                    refreshDisplay();
                    CalculateLive();
                }
                binding.textResult.setSelection(binding.textResult.getText().length());
            }
        });
        // percentage
        binding.layoutStandard.btnPercent.setOnClickListener(v->{
            triggerVibration();
            if (!expression.isEmpty()){
                try {
                    // Clean expression for processing
                    String cleanExp = expression.replace(",", "");
                    int i = cleanExp.length() - 1;
                    while (i >= 0 && (Character.isDigit(cleanExp.charAt(i)) || cleanExp.charAt(i) == '.')) {
                        i--;
                    }

                    String lastNumber = cleanExp.substring(i + 1);
                    if (!lastNumber.isEmpty()) {
                        double value = Double.parseDouble(lastNumber);
                        value = value / 100.0;
                        expression = cleanExp.substring(0, i + 1) + format(value);
                        refreshDisplay();
                        CalculateLive();
                    }
                } catch (Exception ignored) {}
            }
        });
        // plus minus
        binding.layoutStandard.btnPlusMinus.setOnClickListener(v -> {
            triggerVibration();
            expression = expression.replace(",", "");
            if (expression.endsWith("(-")) {
                expression = expression.substring(0, expression.length() - 2);
            } else {
                if (expression.equals("0")) expression = "";
                expression += "(-";
            }
            refreshDisplay();
            CalculateLive();
        });
        // Equal button
        binding.layoutStandard.idEquals.setOnClickListener(v -> {
            triggerVibration();

            if (resultShown && !lastOperator.isEmpty() && !lastOperand.isEmpty()) {
                expression = expression + lastOperator + lastOperand;
            } else {
                extractLastOperation();
            }

            String currentExpression = expression;
            String answer = CalculateExpression();

            if (answer.equals(getString(R.string.infinity_text))) {
                Toast.makeText(this, R.string.err_value_too_large, Toast.LENGTH_SHORT).show();
                return;
            }

            if (answer.equals(getString(R.string.error_text))) {
                Toast.makeText(this, R.string.err_invalid_input, Toast.LENGTH_SHORT).show();
                return;
            }

            if (!answer.isEmpty()){
                // Save to History only if it's an actual calculation (expression differs from answer)
                String formattedExp = getFormattedExpression(currentExpression);
                if (!formattedExp.equals(answer) && !currentExpression.isEmpty()) {
                    SharedPreferences historyPrefs = getSharedPreferences("history_prefs", MODE_PRIVATE);
                    int allowedCapacity = historyPrefs.getInt("history_capacity", -1);

                    if (allowedCapacity != 0) {
                        historyViewModel.insert(new HistoryItem(formattedExp, answer, System.currentTimeMillis()));
                        if (allowedCapacity > 0) {
                            historyViewModel.prune(allowedCapacity);
                        }
                    }
                }

                expression = answer;
                refreshDisplay();
                binding.textFinalResult.setText("");
                resultShown = true;
            }
        });

        for (MaterialButton btn : scientificButtons) {
            btn.setOnClickListener(v -> {
                triggerVibration();
                if (btn.getId() == R.id.btn_factorial) {
                    appendOperator("!");
                } else if (btn.getId() == R.id.btn_e) {
                    appendOperator(isInverseMode ? "exp(" : "e");
                } else if (btn.getId() == R.id.btn_pi) {
                    appendOperator("π");
                } else if (btn.getId() == R.id.btn_power) {
                    appendOperator("^");
                } else if (btn.getId() == R.id.btn_deg) {
                    isDegreeMode = !isDegreeMode;
                    btn.setText(isDegreeMode ? R.string.btn_deg_text : R.string.btn_rad_text);
                    CalculateLive();
                } else if (btn.getId() == R.id.btn_sin) {
                    appendOperator(isInverseMode ? "asin(" : "sin(");
                } else if (btn.getId() == R.id.btn_cos) {
                    appendOperator(isInverseMode ? "acos(" : "cos(");
                } else if (btn.getId() == R.id.btn_tan) {
                    appendOperator(isInverseMode ? "atan(" : "tan(");
                } else if (btn.getId() == R.id.btn_cot) {
                    appendOperator(isInverseMode ? "acot(" : "cot(");
                } else if (btn.getId() == R.id.btn_log) {
                    appendOperator(isInverseMode ? "ln(" : "log(");
                } else if (btn.getId() == R.id.btn_swap) {
                    toggleInverseMode();
                } else if (btn.getId() == R.id.btn_brackets) {
                    handleBrackets();
                }
            });
        }
    }
    private void extractLastOperation() {
        if (expression.isEmpty()) return;

        String cleanExp = expression.replace(",", "");
        int i = cleanExp.length() - 1;

        // Find the last number
        while (i >= 0 && (Character.isDigit(cleanExp.charAt(i)) || cleanExp.charAt(i) == '.')) {
            i--;
        }

        if (i >= 0) {
            lastOperand = cleanExp.substring(i + 1);
            lastOperator = String.valueOf(cleanExp.charAt(i));
        } else {
            lastOperator = "";
            lastOperand = "";
        }
    }

    private void toggleInverseMode() {
        isInverseMode = !isInverseMode;
        binding.layoutScientific.btnE.setText(isInverseMode ? R.string.btn_exp : R.string.btn_e_text);
        binding.layoutScientific.btnLog.setText(isInverseMode ? R.string.btn_ln : R.string.btn_log_text);
        binding.layoutScientific.btnSin.setText(isInverseMode ? R.string.btn_sin_inv : R.string.btn_sin_text);
        binding.layoutScientific.btnCos.setText(isInverseMode ? R.string.btn_cos_inv : R.string.btn_cos_text);
        binding.layoutScientific.btnTan.setText(isInverseMode ? R.string.btn_tan_inv : R.string.btn_tan_text);
        binding.layoutScientific.btnCot.setText(isInverseMode ? R.string.btn_cot_inv : R.string.btn_cot_text);
        CalculateLive();
    }

    private void handleBrackets() {
        int openCount = 0;
        for (char c : expression.toCharArray()) {
            if (c == '(') openCount++;
            else if (c == ')') openCount--;
        }

        if (openCount > 0) {
            char last = expression.isEmpty() ? ' ' : expression.charAt(expression.length() - 1);
            if (Character.isDigit(last) || last == ')' || last == 'e' || last == 'π' || last == '!') {
                appendOperator(")");
            } else {
                appendOperator("(");
            }
        } else {
            appendOperator("(");
        }
    }

    private String getLastNumber(String exp) {
        if (exp.isEmpty()) return "";
        int i = exp.length() - 1;
        while (i >= 0 && (Character.isDigit(exp.charAt(i)) || exp.charAt(i) == '.')) {
            i--;
        }
        return exp.substring(i + 1);
    }
    private void appendNumber(String value){
        triggerVibration();
        if (resultShown){
            expression = "";
            resultShown = false;
        }

        String cleanExp = expression.replace(",", "");
        String lastNum = getLastNumber(cleanExp);

        int currentDigits = 0;
        for (char c : lastNum.toCharArray()) {
            if (Character.isDigit(c)) currentDigits++;
        }

        int digitsToAdd = 0;
        for (char c : value.toCharArray()) {
            if (Character.isDigit(c)) digitsToAdd++;
        }

        if (currentDigits + digitsToAdd > 20) {
            Toast.makeText(this, R.string.err_max_20_digits, Toast.LENGTH_SHORT).show();
            return;
        }

        expression = cleanExp;

        if (expression.equals("0") && value.equals("00")) return;

        if (expression.equals("0") && !value.equals(".")){
            expression = value;
        }
        else if (expression.isEmpty() && value.equals("00")) {
            expression = "0";
        }
        else {
            expression += value;
        }
        refreshDisplay();
        CalculateLive();
    }

    private void refreshDisplay() {
        binding.textResult.setText(getFormattedExpression(expression));
        binding.textResult.setSelection(binding.textResult.getText().length());
    }

    private String getFormattedExpression(String exp) {
        if (exp.isEmpty()) return "";
        if (exp.equals("-")) return "-";

        // Strip existing commas first to avoid splitting numbers incorrectly
        String cleanExp = exp.replace(",", "");

        // Handle inverse trig formatting for display
        cleanExp = cleanExp.replace("asin", "sin⁻¹")
                .replace("acos", "cos⁻¹")
                .replace("atan", "tan⁻¹")
                .replace("acot", "cot⁻¹");

        StringBuilder formatted = new StringBuilder();
        StringBuilder currentNumber = new StringBuilder();

        for (int i = 0; i < cleanExp.length(); i++) {
            char c = cleanExp.charAt(i);
            if (Character.isDigit(c) || c == '.') {
                currentNumber.append(c);
            } else {
                if (currentNumber.length() > 0) {
                    formatted.append(formatNumberString(currentNumber.toString()));
                    currentNumber.setLength(0);
                }
                formatted.append(c);
            }
        }

        if (currentNumber.length() > 0) {
            formatted.append(formatNumberString(currentNumber.toString()));
        }

        return formatted.toString();
    }

    private String formatNumberString(String numStr) {
        if (numStr.isEmpty()) return "";
        try {
            if (numStr.equals(".")) return ".";
            boolean hasDot = numStr.contains(".");
            String beforeDot = hasDot ? numStr.substring(0, numStr.indexOf(".")) : numStr;
            String afterDot = hasDot ? numStr.substring(numStr.indexOf(".")) : "";

            if (beforeDot.isEmpty() && hasDot) beforeDot = "0";

            if (!beforeDot.isEmpty() && !beforeDot.equals("-")) {
                double val = Double.parseDouble(beforeDot);
                DecimalFormat df = new DecimalFormat("#,###");
                beforeDot = df.format(val);
            }

            return beforeDot + afterDot;
        } catch (Exception e) {
            return numStr;
        }
    }

    private void appendOperator(String op){
        triggerVibration();
        expression = expression.replace(",", "");

        if (expression.isEmpty() || expression.equals("0")){
            // Allow starting expression with constants, brackets, or functions
            if (op.equals("e") || op.equals("π") || op.equals("(") ||
                    op.contains("sin") || op.contains("cos") || op.contains("tan") ||
                    op.contains("cot") || op.contains("log") || op.contains("ln") ||
                    op.contains("exp")) {
                expression = op;
                refreshDisplay();
                CalculateLive();
                return;
            }
            if (expression.equals("0") && op.equals("-")) {
                expression = "-";
                refreshDisplay();
                return;
            }
        }

        char last = expression.charAt(expression.length()-1);

        if (last == '+' || last == '-' || last == '×' || last == '÷'){
            // Replace operator only if both are basic arithmetic operators
            if (op.equals("+") || op.equals("-") || op.equals("×") || op.equals("÷")) {
                expression = expression.substring(0, expression.length()-1) + op;
            } else {
                expression += op;
            }
        }
        else {
            expression += op;
        }
        refreshDisplay();
        CalculateLive();
    }
    // calculate live result
    private void CalculateLive(){
        if (expression.isEmpty()) {
            binding.textFinalResult.setText("");
            return;
        }

        // Only show live result if there's an operator/bracket and it doesn't end with a starting one
        boolean hasOperator = expression.contains("+") || expression.contains("-") ||
                expression.contains("×") || expression.contains("÷") ||
                expression.contains("^") || expression.contains("sin") ||
                expression.contains("cos") || expression.contains("tan") ||
                expression.contains("cot") || expression.contains("log") ||
                expression.contains("ln") || expression.contains("!") ||
                expression.contains("e") || expression.contains("π") ||
                expression.contains("exp") || expression.contains("(") ||
                expression.contains(")");

        char last = expression.charAt(expression.length() - 1);
        boolean endsWithStartOperator = last == '+' || last == '-' || last == '×' || last == '÷' || last == '(';

        if (!hasOperator || endsWithStartOperator) {
            binding.textFinalResult.setText("");
            return;
        }

        String result = CalculateExpression();

        // Remove commas for comparison to avoid showing result for a single large number
        String cleanResult = result.replace(",", "");
        String cleanExpression = expression.replace(",", "").replace("e", String.valueOf(Math.E)).replace("π", String.valueOf(Math.PI));

        if (!result.isEmpty() && !cleanResult.equals(cleanExpression) && !cleanResult.equals("0") && !cleanResult.equals("0.0")){
            binding.textFinalResult.setText(result);
        }
        else {
            binding.textFinalResult.setText("");
        }
    }
    // calculate the expression
    private String CalculateExpression(){
        try {
            String exp = expression.replace("×","*").replace("÷","/")
                    .replace("π", "pi")
                    .replace(",", ""); // Remove grouping separators

            double answer = new Object(){
                int pos = -1,ch;

                void nextChar(){
                    ch = (++pos < exp.length()) ? exp.charAt(pos) : -1;
                }
                boolean eat(int charToEat){
                    while (ch == ' ')
                        nextChar();

                    if (ch == charToEat){
                        nextChar();
                        return true;
                    }
                    return false;


                }
                double parse(){
                    nextChar();

                    return parseExpression();
                }
                double parseExpression(){
                    double x = parseTerm();

                    while (true){
                        if (eat('+'))
                            x += parseTerm();
                        else if (eat('-'))
                            x -= parseTerm();
                        else
                            return x;
                    }

                }
                double parseTerm(){
                    double x = parsePower();
                    while (true){
                        if (eat('*'))
                            x *= parsePower();
                        else if (eat('/'))
                            x /= parsePower();
                        else if (ch == '(' || ch == 'e' || ch == 'p' ||
                                (ch >= 'a' && ch <= 'z'))
                            x *= parsePower();
                        else
                            return x;
                    }

                }

                double parsePower() {
                    double x = parseFactor();
                    if (eat('^')) x = Math.pow(x, parsePower());
                    return x;
                }

                double parseFactor(){
                    if (eat('+'))
                        return parseFactor();
                    if (eat('-'))
                        return -parseFactor();

                    double x;

                    int start = pos;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else if ((ch >= '0' && ch <= '9') || ch == '.') {
                        while ((ch >= '0' && ch <= '9') || ch == '.' || ch == 'E' || ch == 'e') {
                            nextChar();
                            // Handle negative exponent e.g. 1.2E-5
                            if (ch == '-' && (exp.charAt(pos - 1) == 'E' || exp.charAt(pos - 1) == 'e')) {
                                nextChar();
                            }
                        }
                        x = Double.parseDouble(exp.substring(start, pos));
                    } else if (ch >= 'a' && ch <= 'z') {
                        x = parseFunctionsAndConstants();
                    } else {
                        return 0;
                    }

                    if (eat('!')) x = factorial(x);

                    return  x;
                }

                double parseFunctionsAndConstants() {
                    int start = pos;
                    while (ch >= 'a' && ch <= 'z') nextChar();
                    String func = exp.substring(start, pos);

                    if (func.equals("pi")) return Math.PI;
                    if (func.equals("e")) return Math.E;

                    double x;
                    if (eat('(')) {
                        x = parseExpression();
                        eat(')');
                    } else {
                        x = parseFactor();
                    }

                    switch (func) {
                        case "sin": return Math.sin(isDegreeMode ? Math.toRadians(x) : x);
                        case "cos": return Math.cos(isDegreeMode ? Math.toRadians(x) : x);
                        case "tan": return Math.tan(isDegreeMode ? Math.toRadians(x) : x);
                        case "cot": return 1.0 / Math.tan(isDegreeMode ? Math.toRadians(x) : x);
                        case "asin":
                            double asinVal = Math.asin(x);
                            return isDegreeMode ? Math.toDegrees(asinVal) : asinVal;
                        case "acos":
                            double acosVal = Math.acos(x);
                            return isDegreeMode ? Math.toDegrees(acosVal) : acosVal;
                        case "atan":
                            double atanVal = Math.atan(x);
                            return isDegreeMode ? Math.toDegrees(atanVal) : atanVal;
                        case "acot":
                            double acotVal = Math.atan(1.0 / x);
                            return isDegreeMode ? Math.toDegrees(acotVal) : acotVal;
                        case "log": return Math.log10(x);
                        case "ln": return Math.log(x);
                        case "exp": return Math.exp(x);
                        default: return 0;
                    }
                }
            }.parse();

            return format(answer);
        }catch (Exception e){
            return "";
        }
    }

    private double factorial(double n) {
        if (n < 0) return 0;
        if (n == 0 || n == 1) return 1;
        double result = 1;
        for (int i = 2; i <= (int)n; i++) {
            result *= i;
        }
        return result;
    }
    private String format(double value){
        if (Double.isInfinite(value)) return getString(R.string.infinity_text);
        if (Double.isNaN(value)) return getString(R.string.error_text);

        // Show E notation only for numbers with 15 or more digits
        if (Math.abs(value) >= 1e14) {
            return new DecimalFormat("0.########E0").format(value);
        }

        // For smaller numbers, show the long format with commas
        DecimalFormat df = new DecimalFormat("#,###.###############");
        return df.format(value);
    }
    @Override
    protected void onResume() {
        super.onResume();

        // Check if the user changed the scientific preference while in the settings screen
        SharedPreferences sciPrefs = getSharedPreferences("scientific_prefs", MODE_PRIVATE);
        boolean savedSciMode = sciPrefs.getBoolean("scientific_enabled", false);

        // If the state is different than what's currently showing, trigger the switch transition animation
        if (savedSciMode != scientificMode) {
            toggleScientificMode();
        }
    }
}