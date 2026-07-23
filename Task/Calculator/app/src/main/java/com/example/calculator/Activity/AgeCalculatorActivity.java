package com.example.calculator.Activity;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.calculator.R;
import com.example.calculator.databinding.ActivityAgeCalculatorBinding;
import com.shawnlin.numberpicker.NumberPicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class AgeCalculatorActivity extends AppCompatActivity {

    private ActivityAgeCalculatorBinding binding;
    private final Calendar todayCalendar = Calendar.getInstance();
    private final Calendar birthCalendar = Calendar.getInstance();
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.US);
    private int selectedField = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAgeCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // Hide soft keyboard
        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        setupToolbar();
        initAgeCalculatorUI();
    }

    private void setupToolbar() {
        binding.ageToolbar.setNavigationOnClickListener(v -> finish());
    }

    private void initAgeCalculatorUI() {
        binding.textToday.setText(dateFormat.format(todayCalendar.getTime()));

        View.OnClickListener clickListener = v -> {
            binding.resultLabelContainer.setVisibility(View.GONE);
            binding.resultContainerAge.setVisibility(View.GONE);
            updateLabelColors(false);
            if (v.getId() == R.id.layout_dob || v.getId() == R.id.text_dob || v.getId() == R.id.label_dob) {
                selectedField = 0;
                updateSelectionUI();
                showDatePickerDialog(birthCalendar);
            } else if (v.getId() == R.id.layout_today || v.getId() == R.id.text_today || v.getId() == R.id.label_today) {
                selectedField = 1;
                updateSelectionUI();
                showDatePickerDialog(todayCalendar);
            }
        };

        binding.layoutDob.setOnClickListener(clickListener);
        binding.textDob.setOnClickListener(clickListener);
        binding.labelDob.setOnClickListener(clickListener);

        binding.layoutToday.setOnClickListener(clickListener);
        binding.textToday.setOnClickListener(clickListener);
        binding.labelToday.setOnClickListener(clickListener);
    }

    private void updateSelectionUI() {
        binding.layoutDob.setSelected(selectedField == 0);
        binding.layoutToday.setSelected(selectedField == 1);

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        View focusView = getCurrentFocus();
        if (focusView == null) focusView = binding.getRoot();
        if (imm != null) {
            imm.hideSoftInputFromWindow(focusView.getWindowToken(), 0);
        }
    }

    private void showDatePickerDialog(Calendar targetCalendar) {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.age_dialogpicker);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 336, getResources().getDisplayMetrics());
            int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(Math.min(widthPx, maxWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        NumberPicker pickerMonth = dialog.findViewById(R.id.picker_month);
        NumberPicker pickerDay = dialog.findViewById(R.id.picker_day);
        NumberPicker pickerYear = dialog.findViewById(R.id.picker_year);

        String[] months = {getString(R.string.jan), getString(R.string.feb), getString(R.string.mar), getString(R.string.apr), getString(R.string.may), getString(R.string.jun), getString(R.string.jul), getString(R.string.aug), getString(R.string.sep), getString(R.string.oct), getString(R.string.nov), getString(R.string.dec)};
        pickerMonth.setMinValue(0);
        pickerMonth.setMaxValue(months.length - 1);
        pickerMonth.setDisplayedValues(months);

        pickerDay.setMinValue(1);
        pickerDay.setMaxValue(31);
        pickerDay.setFormatter(value -> String.format(Locale.US, "%d", value));

        pickerYear.setMinValue(1900);
        pickerYear.setMaxValue(2100);
        pickerYear.setFormatter(value -> String.format(Locale.US, "%d", value));

        pickerMonth.setValue(targetCalendar.get(Calendar.MONTH));
        pickerDay.setValue(targetCalendar.get(Calendar.DAY_OF_MONTH));
        pickerYear.setValue(targetCalendar.get(Calendar.YEAR));

        dialog.findViewById(R.id.btn_cancel).setOnClickListener(v -> {
            dialog.dismiss();
            clearSelections();
        });

        dialog.findViewById(R.id.btn_ok).setOnClickListener(v -> {
            targetCalendar.set(Calendar.MONTH, pickerMonth.getValue());
            targetCalendar.set(Calendar.DAY_OF_MONTH, pickerDay.getValue());
            targetCalendar.set(Calendar.YEAR, pickerYear.getValue());

            String formattedDate = dateFormat.format(targetCalendar.getTime());

            if (selectedField == 0) {
                binding.textDob.setText(formattedDate);
                binding.textDob.setTextColor(ContextCompat.getColor(this, R.color.input_text_color));
            } else {
                binding.textToday.setText(formattedDate);
            }
            dialog.dismiss();
            clearSelections();
            performAgeCalculation();
        });

        dialog.show();
    }

    private void updateLabelColors(boolean isResultMode) {
        int color = isResultMode ? getColor(R.color.text_secondary) : getColor(R.color.text_label_gray);
        binding.labelDob.setTextColor(color);
        binding.labelToday.setTextColor(color);
    }

    private void clearSelections() {
        selectedField = -1;
        updateSelectionUI();
    }

    private void performAgeCalculation() {
        String dobStr = binding.textDob.getText().toString();
        String todayStr = binding.textToday.getText().toString();

        if (dobStr.equalsIgnoreCase("DD-MM-YYYY") || dobStr.equalsIgnoreCase(getString(R.string.placeholder_dob)) || dobStr.isEmpty()) {
            return;
        }

        try {
            Date dobDate = dateFormat.parse(dobStr);
            Date todayDate = dateFormat.parse(todayStr);

            if (dobDate != null && todayDate != null) {
                if (dobDate.after(todayDate)) {
                    Toast.makeText(this, R.string.err_birthday_future, Toast.LENGTH_SHORT).show();
                    binding.resultLabelContainer.setVisibility(View.GONE);
                    binding.resultContainerAge.setVisibility(View.GONE);
                    return;
                }

                Calendar dob = Calendar.getInstance();
                dob.setTime(dobDate);

                Calendar today = Calendar.getInstance();
                today.setTime(todayDate);

                // --- Calculate Current Age ---
                int years = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR);
                int months = today.get(Calendar.MONTH) - dob.get(Calendar.MONTH);
                int days = today.get(Calendar.DAY_OF_MONTH) - dob.get(Calendar.DAY_OF_MONTH);

                if (days < 0) {
                    months--;
                    Calendar temp = (Calendar) today.clone();
                    temp.add(Calendar.MONTH, -1);
                    days += temp.getActualMaximum(Calendar.DAY_OF_MONTH);
                }

                if (months < 0) {
                    years--;
                    months += 12;
                }

                // --- Calculate Next Birthday ---
                Calendar nextBday = Calendar.getInstance();
                nextBday.setTime(dobDate);
                nextBday.set(Calendar.YEAR, today.get(Calendar.YEAR));

                if (nextBday.before(today) || nextBday.equals(today)) {
                    nextBday.add(Calendar.YEAR, 1);
                }

                int nextMonths = nextBday.get(Calendar.MONTH) - today.get(Calendar.MONTH);
                int nextDays = nextBday.get(Calendar.DAY_OF_MONTH) - today.get(Calendar.DAY_OF_MONTH);

                if (nextDays < 0) {
                    nextMonths--;
                    Calendar temp = (Calendar) today.clone();
                    temp.add(Calendar.MONTH, -1);
                    nextDays += temp.getActualMaximum(Calendar.DAY_OF_MONTH);
                }
                if (nextMonths < 0 || (nextMonths == 0 && nextDays == 0)) {
                    nextMonths += 12;
                }

                String dayOfWeekStr = new SimpleDateFormat("EEEE", Locale.US).format(nextBday.getTime());

                // Update UI
                String yearText = years == 1 ? getString(R.string.year_label) : getString(R.string.years_label);
                binding.textCurrentAgeYears.setText(String.format(Locale.US, "%d %s", years, yearText));
                binding.textCurrentAgeDetail.setText(getString(R.string.age_result_format, months, days));

                binding.textNextBirthdayDay.setText(dayOfWeekStr);
                binding.textNextBirthdayDetail.setText(getString(R.string.age_result_format, nextMonths, nextDays));

                binding.resultLabelContainer.setVisibility(View.VISIBLE);
                binding.resultContainerAge.setVisibility(View.VISIBLE);
                updateLabelColors(true);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
    }
}
