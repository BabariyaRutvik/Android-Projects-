package com.example.calculator.Activity;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;

import com.example.calculator.R;
import com.example.calculator.databinding.ActivityDateCalculatorBinding;
import com.shawnlin.numberpicker.NumberPicker;

import java.util.Calendar;
import java.util.Locale;

public class DateCalculatorActivity extends AppCompatActivity {

    private ActivityDateCalculatorBinding binding;
    private final Calendar fromDate = Calendar.getInstance();
    private final Calendar toDate = Calendar.getInstance();

    private boolean isFromSet = false;
    private boolean isToSet = true; // default today
    private int selectedField = -1; // 0 for from, 1 for to

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDateCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // hiding input
        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        initDateUI();
    }

    private void initDateUI() {
        binding.dateToolbar.setNavigationOnClickListener(v -> finish());

        // default today
        binding.textTo.setText(formatDate(toDate));
        binding.textTo.setTextColor(ContextCompat.getColor(this, R.color.input_text_color));

        View.OnClickListener clickListener = v -> {
            if (v.getId() == R.id.layout_date_from || v.getId() == R.id.text_from || v.getId() == R.id.label_date_from) {
                selectedField = 0;
                updateSelectionUI();
                showDatePicker(true);
            } else if (v.getId() == R.id.layout_to_date || v.getId() == R.id.text_to || v.getId() == R.id.label_to_date) {
                selectedField = 1;
                updateSelectionUI();
                showDatePicker(false);
            }
        };

        binding.layoutDateFrom.setOnClickListener(clickListener);
        binding.textFrom.setOnClickListener(clickListener);
        binding.labelDateFrom.setOnClickListener(clickListener);

        binding.layoutToDate.setOnClickListener(clickListener);
        binding.textTo.setOnClickListener(clickListener);
        binding.labelToDate.setOnClickListener(clickListener);

        binding.resultContainerDate.setVisibility(View.GONE);
        binding.resultLabelContainerDate.setVisibility(View.GONE);
    }

    private void updateSelectionUI() {
        binding.layoutDateFrom.setSelected(selectedField == 0);
        binding.layoutToDate.setSelected(selectedField == 1);
    }

    private void clearSelections() {
        selectedField = -1;
        updateSelectionUI();
    }

    private void showDatePicker(boolean isFromField) {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.age_dialogpicker);

        Window window = dialog.getWindow();
        if (window != null) {
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int widthPx = (int) TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP, 336, getResources().getDisplayMetrics());
            int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            window.setLayout(Math.min(widthPx, maxWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        NumberPicker pickerMonth = dialog.findViewById(R.id.picker_month);
        NumberPicker pickerDay = dialog.findViewById(R.id.picker_day);
        NumberPicker pickerYear = dialog.findViewById(R.id.picker_year);
        TextView btnCancel = dialog.findViewById(R.id.btn_cancel);
        TextView btnOk = dialog.findViewById(R.id.btn_ok);

        String[] monthNames = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        pickerMonth.setMinValue(0);
        pickerMonth.setMaxValue(monthNames.length - 1);
        pickerMonth.setDisplayedValues(monthNames);

        Calendar existing = isFromField ? fromDate : toDate;
        boolean alreadySet = isFromField ? isFromSet : isToSet;
        Calendar initial = alreadySet ? existing : Calendar.getInstance();

        pickerYear.setMinValue(1900);
        pickerYear.setMaxValue(2100);
        pickerYear.setValue(initial.get(Calendar.YEAR));
        pickerYear.setFormatter(value -> String.valueOf(value));

        pickerMonth.setValue(initial.get(Calendar.MONTH));

        pickerDay.setMinValue(1);
        pickerDay.setMaxValue(getDaysInMonth(initial.get(Calendar.YEAR), initial.get(Calendar.MONTH) + 1));
        pickerDay.setValue(Math.min(initial.get(Calendar.DAY_OF_MONTH), pickerDay.getMaxValue()));

        // keeping the date range valid month e.g no fab 30
        NumberPicker.OnValueChangeListener updateDayRange = (picker, oldVal, newVal) -> {
            int year = pickerYear.getValue();
            int month = pickerMonth.getValue() + 1;
            int maxDay = getDaysInMonth(year, month);
            int currentDay = pickerDay.getValue();
            pickerDay.setMaxValue(maxDay);

            if (currentDay > maxDay) {
                pickerDay.setValue(maxDay);
            }
        };

        pickerMonth.setOnValueChangedListener(updateDayRange);
        pickerYear.setOnValueChangedListener(updateDayRange);

        btnCancel.setOnClickListener(v -> {
            dialog.dismiss();
            clearSelections();
        });

        btnOk.setOnClickListener(v -> {
            Calendar selected = Calendar.getInstance();
            selected.clear();
            selected.set(pickerYear.getValue(), pickerMonth.getValue(), pickerDay.getValue());

            if (isFromField) {
                fromDate.setTimeInMillis(selected.getTimeInMillis());
                isFromSet = true;
                binding.textFrom.setText(formatDate(fromDate));
                binding.textFrom.setTextColor(ContextCompat.getColor(this, R.color.input_text_color));
            } else {
                toDate.setTimeInMillis(selected.getTimeInMillis());
                isToSet = true;
                binding.textTo.setText(formatDate(toDate));
                binding.textTo.setTextColor(ContextCompat.getColor(this, R.color.input_text_color));
            }

            dialog.dismiss();
            clearSelections();

            if (isFromSet && isToSet) {
                calculateDateDifference();
            }
        });

        dialog.show();
    }

    private int getDaysInMonth(int year, int month) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.YEAR, year);
        cal.set(Calendar.MONTH, month - 1);
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH);
    }

    private void calculateDateDifference() {
        Calendar start = (Calendar) fromDate.clone();
        Calendar end = (Calendar) toDate.clone();

        // If user picks them in reverse order, swap so the math always works
        if (start.after(end)) {
            Calendar temp = start;
            start = end;
            end = temp;
        }

        int years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR);
        int months = end.get(Calendar.MONTH) - start.get(Calendar.MONTH);
        int days = end.get(Calendar.DAY_OF_MONTH) - start.get(Calendar.DAY_OF_MONTH);

        if (days < 0) {
            months--;
            Calendar tempCal = (Calendar) end.clone();
            tempCal.add(Calendar.MONTH, -1);
            days += tempCal.getActualMaximum(Calendar.DAY_OF_MONTH);
        }

        if (months < 0) {
            years--;
            months += 12;
        }

        binding.textResultYears.setText(String.valueOf(years));
        binding.textResultMonths.setText(String.valueOf(months));
        binding.textResultDays.setText(String.valueOf(days));

        binding.textResultFrom.setText(formatDate(fromDate));
        binding.textResultTo.setText(formatDate(toDate));

        binding.resultLabelContainerDate.setVisibility(View.VISIBLE);
        binding.resultContainerDate.setVisibility(View.VISIBLE);
    }

    private String formatDate(Calendar calendar) {
        return String.format(Locale.US, "%02d-%02d-%04d",
                calendar.get(Calendar.DAY_OF_MONTH),
                calendar.get(Calendar.MONTH) + 1,
                calendar.get(Calendar.YEAR));
    }
}
