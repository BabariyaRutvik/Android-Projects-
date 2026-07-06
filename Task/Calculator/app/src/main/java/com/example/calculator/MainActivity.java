package com.example.calculator;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.calculator.databinding.ActivityMainBinding;
import com.google.android.material.button.MaterialButton;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private boolean scientificMode = false;
    private String expression = "";
    private boolean resultShown = false;

    private MaterialButton[] numberButtons;
    private MaterialButton[] iconsButtons;
    private MaterialButton btnAc;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeStandardButtons();
        // for Calculate Functionality
        setUpCalculatorButtons();


        binding.textResult.setShowSoftInputOnFocus(false);
        binding.textResult.requestFocus();
        binding.textResult.setSelection(binding.textResult.getText().length());

        binding.imageSwitch.setOnClickListener(v -> toggleScientificMode());
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
                    binding.imageHistory.setVisibility(View.INVISIBLE);
                    binding.imageSwitch.setImageResource(R.drawable.ic_standard);
                }
            }

            @Override
            public void onAnimationEnd(Animator animation) {
                if (!scientificMode) {
                    scientificView.setVisibility(View.GONE);
                    binding.imageHistory.setVisibility(View.VISIBLE);
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

        if (btnAc != null) {
            btnAc.setTextSize(TypedValue.COMPLEX_UNIT_SP, acSize);
            btnAc.setGravity(Gravity.CENTER);
            btnAc.setPadding(0, 0, 0, 0);
        }

        for (MaterialButton btn : numberButtons) {
            if (btn != null) {
                btn.setTextSize(TypedValue.COMPLEX_UNIT_SP, numSize);
                btn.setGravity(Gravity.CENTER);
                btn.setPadding(0, 0, 0, 0);
            }
        }

        for (MaterialButton btn : iconsButtons) {
            if (btn != null) {
                btn.setIconSize(iconSizePx);
                btn.setGravity(Gravity.CENTER);
                btn.setIconPadding(0);
                btn.setPadding(0, 0, 0, 0);
            }
        }
    }

    private void updateResultFieldForMode() {
        binding.textResult.requestFocus();
        if (!scientificMode) {
            if (binding.textResult.getText().toString().isEmpty()) {
                binding.textResult.setText("0");
            }
        }
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
            expression = "";
            binding.textResult.setText("0");
            binding.textFinalResult.setText("");
            binding.textResult.setSelection(binding.textResult.getText().length());
        });
        // backspace
        binding.imageBackspace.setOnClickListener(v->{

            if (!expression.isEmpty()){
                expression = expression.substring(0, expression.length()-1);

                if (expression.isEmpty()){
                    binding.textResult.setText("0");
                    binding.textFinalResult.setText("");
                }else {
                    binding.textResult.setText(expression);
                    CalculateLive();
                }
                binding.textResult.setSelection(binding.textResult.getText().length());
            }
        });
        // percentage
        binding.layoutStandard.btnPercent.setOnClickListener(v->{
            if (! expression.isEmpty()){
                try {
                    double value = Double.parseDouble(expression);

                    value = value / 100;

                    expression = format(value);
                    binding.textResult.setText(expression);
                    binding.textResult.setSelection(binding.textResult.getText().length());

                }catch (Exception ignored){

                }
            }
        });
        // plus minus
        binding.layoutStandard.btnPlusMinus.setOnClickListener(v -> {
            if (!expression.isEmpty()){
                if (expression.startsWith("-")){
                    expression = expression.substring(1);

                }
                else {
                    expression = "-" + expression;
                }
                binding.textResult.setText(expression);
                binding.textResult.setSelection(binding.textResult.getText().length());
            }
        });
        // Equal button
        binding.layoutStandard.idEquals.setOnClickListener(v -> {
            String answer = CalculateExpression();

            if (!answer.isEmpty()){
                binding.textResult.setText(answer);
                binding.textFinalResult.setText("");

                expression = answer;
                resultShown = true;
                binding.textResult.setSelection(binding.textResult.getText().length());
            }
        });
    }
    private void appendNumber(String value){
        if (resultShown){
            expression = "";
            resultShown = false;

        }
        if (expression.equals("0")){
            expression = value;

        }
        else {
            expression += value;
        }
        binding.textResult.setText(expression);
        binding.textResult.setSelection(binding.textResult.getText().length());
        CalculateLive();

    }
    // operator
    private void appendOperator(String op){

        if (expression.isEmpty()){
            return;
        }

        char last = expression.charAt(expression.length()-1);

        if (last == '+' || last == '-' || last == '×' || last == '÷'){
            expression = expression.substring(0, expression.length()-1)+op;
        }
        else {
            expression += op;
        }
        binding.textResult.setText(expression);
        binding.textResult.setSelection(binding.textResult.getText().length());
    }
    // calculate live result
    private void CalculateLive(){
        String result = CalculateExpression();

        if (!result .isEmpty() && !result.equals(expression)){
            binding.textFinalResult.setText(result);
        }
        else {
            binding.textFinalResult.setText("");

        }
    }
    // calculate the expression
    private String CalculateExpression(){
        try {
            String exp = expression.replace("×","*").replace("÷","/");

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
                    double x = parseFactor();
                    while (true){
                        if (eat('*'))
                            x *= parseFactor();
                        else if (eat('/'))
                            x /= parseFactor();
                        else
                            return x;
                    }

                }

                double parseFactor(){
                    if (eat('+'))
                        return parseFactor();
                    if (eat('-'))
                        return -parseFactor();

                    double x;

                    int start = pos;

                    while((ch>='0' && ch<='9') || ch=='.')
                        nextChar();

                    x = Double.parseDouble(exp.substring(start,pos));

                    return  x;
                }
            }.parse();

            return format(answer);
        }catch (Exception e){
            return "";
        }
    }
    private String format(double value){

        if(value == (long)value)

            return String.valueOf((long)value);


        else

            return String.valueOf(value);

    }
}
