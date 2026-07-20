package com.example.calculator.Activity;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.view.WindowInsetsControllerCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.calculator.Adapter.UnitCategoryAdapter;
import com.example.calculator.BottomSheet.UnitConverterBottomSheet;
import com.example.calculator.Model.UnitItem;
import com.example.calculator.R;
import com.example.calculator.databinding.ActivityUnitConverterCalculatorBinding;

import java.lang.reflect.Array;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UnitConverterCalculatorActivity extends AppCompatActivity {

    private ActivityUnitConverterCalculatorBinding binding;
    private UnitCategoryAdapter adapter;
    private String currentCategory = "Weight";
    private String enteredAmount = "";
    private UnitItem selectedUnitFrom;
    private UnitItem selectedUnitTo;

    private Map<String, List<UnitItem>> unitDataMap = new HashMap<>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityUnitConverterCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // hiding system keyboard
        if (getWindow() != null) {
            getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        }

        binding.textUnitValueFrom.setShowSoftInputOnFocus(false);

        initializeCategoryData();


        // for different categories
        List<String> categoryList = Arrays.asList(
                "Weight","Length","Area","Time","Volume",
                "Temperature", "Speed","Data","Power", "Current", "Voltage"
        );

        binding.categoryRecyerview.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        adapter = new UnitCategoryAdapter(categoryList, this::onCategoryChanged);
        binding.categoryRecyerview.setAdapter(adapter);

        // core UI
        initUnitConverter();
        setupUnitKeypad();

        // setup the category Change on chip
        onCategoryChanged("Weight");
        updateUnitConverterUI(true);

    }
    private void initializeCategoryData() {
        // 1. Weight data (Base: Gram)
        List<UnitItem> weightList = new ArrayList<>();
        weightList.add(new UnitItem("Kilogram", "kg", 1000.0));
        weightList.add(new UnitItem("Gram", "g", 1.0));
        weightList.add(new UnitItem("Milligram", "mg", 0.001));
        weightList.add(new UnitItem("Microgram", "µg", 0.000001));
        weightList.add(new UnitItem("Quintal", "q", 100000.0));
        weightList.add(new UnitItem("Pound", "lb", 453.592));
        weightList.add(new UnitItem("Ounce", "oz", 28.3495));
        weightList.add(new UnitItem("Carat", "ct", 0.2));
        weightList.add(new UnitItem("Tonne", "t", 1000000.0));
        weightList.add(new UnitItem("Grain", "gr", 0.06479891));
        weightList.add(new UnitItem("Long Tonne (UK)", "t", 1016046.91));
        weightList.add(new UnitItem("Short Tonne (US)", "t", 907184.74));
        weightList.add(new UnitItem("Stone", "st", 6350.29));
        unitDataMap.put("Weight", weightList);

        // 2. Length data (Base: Metre)
        List<UnitItem> lengthList = new ArrayList<>();
        lengthList.add(new UnitItem("Kilometer", "km", 1000.0));
        lengthList.add(new UnitItem("Metre", "m", 1.0));
        lengthList.add(new UnitItem("Decimeter", "dm", 0.1));
        lengthList.add(new UnitItem("Centimeter", "cm", 0.01));
        lengthList.add(new UnitItem("Millimeter", "mm", 0.001));
        lengthList.add(new UnitItem("Micrometer", "µm", 0.000001));
        lengthList.add(new UnitItem("Nanometer", "nm", 1e-9));
        lengthList.add(new UnitItem("Picometer", "pm", 1e-12));
        lengthList.add(new UnitItem("Nautical Mile", "nmi", 1852.0));
        lengthList.add(new UnitItem("Mile", "mi", 1609.34));
        lengthList.add(new UnitItem("Fathom", "fum", 1.8288));
        lengthList.add(new UnitItem("Yard", "yd", 0.9144));
        lengthList.add(new UnitItem("Foot", "ft", 0.3048));
        lengthList.add(new UnitItem("Inch", "in", 0.0254));
        unitDataMap.put("Length", lengthList);

        // 3. Area data (Base: Square metre)
        List<UnitItem> areaList = new ArrayList<>();
        areaList.add(new UnitItem("Square Kilometre", "km²", 1000000.0));
        areaList.add(new UnitItem("Hectare", "ha", 10000.0));
        areaList.add(new UnitItem("Square metre", "m²", 1.0));
        areaList.add(new UnitItem("Square decimetre", "dm²", 0.01));
        areaList.add(new UnitItem("Square centimetre", "cm²", 0.0001));
        areaList.add(new UnitItem("Square millimetre", "mm²", 0.000001));
        areaList.add(new UnitItem("Square micron", "µm²", 1e-12));
        areaList.add(new UnitItem("Acre", "ac", 4046.86));
        areaList.add(new UnitItem("Are", "a", 100.0));
        areaList.add(new UnitItem("Square mile", "mi²", 2589988.11));
        areaList.add(new UnitItem("Square yard", "yd²", 0.836127));
        areaList.add(new UnitItem("Square foot", "ft²", 0.092903));
        areaList.add(new UnitItem("Square inch", "in²", 0.00064516));
        areaList.add(new UnitItem("Square rod", "rd²", 25.2929));
        unitDataMap.put("Area", areaList);

        // 4. Time data (Base: Second)
        List<UnitItem> timeList = new ArrayList<>();
        timeList.add(new UnitItem("Second", "s", 1.0));
        timeList.add(new UnitItem("Year", "y", 31536000.0));
        timeList.add(new UnitItem("Week", "wk", 604800.0));
        timeList.add(new UnitItem("Days", "d", 86400.0));
        timeList.add(new UnitItem("Hour", "h", 3600.0));
        timeList.add(new UnitItem("Minute", "min", 60.0));
        timeList.add(new UnitItem("Millisecond", "ms", 0.001));
        timeList.add(new UnitItem("Microsecond", "µs", 0.000001));
        timeList.add(new UnitItem("Nanosecond", "ns", 1e-9));
        unitDataMap.put("Time", timeList);

        // 5. Volume data (Base: Litre)
        List<UnitItem> volumeList = new ArrayList<>();
        volumeList.add(new UnitItem("Litre", "ℓ", 1.0));
        volumeList.add(new UnitItem("US liquid gallon", "gal", 3.78541));
        volumeList.add(new UnitItem("US liquid quart", "qt", 0.946353));
        volumeList.add(new UnitItem("US liquid pint", "pt", 0.473176));
        volumeList.add(new UnitItem("US legal cup", "c", 0.24));
        volumeList.add(new UnitItem("US fluid ounce", "fl oz", 0.0295735));
        volumeList.add(new UnitItem("US tablespoon", "tbsp", 0.0147868));
        volumeList.add(new UnitItem("US teaspoon", "tsp", 0.00492892));
        volumeList.add(new UnitItem("Cubic metre", "m³", 1000.0));
        volumeList.add(new UnitItem("Cubic decimetre", "dm³", 1.0));
        volumeList.add(new UnitItem("Cubic centimetre", "cm³", 0.001));
        volumeList.add(new UnitItem("Cubic millimetre", "mm³", 0.000001));
        volumeList.add(new UnitItem("Cubic foot", "ft³", 28.3168));
        volumeList.add(new UnitItem("Cubic inch", "in³", 0.0163871));
        volumeList.add(new UnitItem("Millilitre", "mℓ", 0.001));
        volumeList.add(new UnitItem("Centilitre", "cℓ", 0.01));
        volumeList.add(new UnitItem("Decilitre", "dℓ", 0.1));
        volumeList.add(new UnitItem("Hectolitre", "hℓ", 100.0));
        volumeList.add(new UnitItem("Imperial gallon", "gal (imp)", 4.54609));
        volumeList.add(new UnitItem("Imperial quart", "qt (imp)", 1.13652));
        volumeList.add(new UnitItem("Imperial pint", "pt (imp)", 0.568261));
        volumeList.add(new UnitItem("Imperial cup", "c (imp)", 0.284131));
        volumeList.add(new UnitItem("Imperial fluid ounce", "fl oz (imp)", 0.0284131));
        volumeList.add(new UnitItem("Imperial tablespoon", "tbsp (imp)", 0.0177582));
        volumeList.add(new UnitItem("Imperial teaspoon", "tsp (imp)", 0.00591939));
        unitDataMap.put("Volume", volumeList);

        // 6. Temperature data (Formula based, factor used as placeholder)
        List<UnitItem> temperatureList = new ArrayList<>();
        temperatureList.add(new UnitItem("Celsius", "°C", 1.0));
        temperatureList.add(new UnitItem("Fahrenheit", "°F", 1.0));
        temperatureList.add(new UnitItem("Kelvin", "k", 1.0));
        unitDataMap.put("Temperature", temperatureList);

        // 7. Speed data (Base: Metre per second)
        List<UnitItem> speedList = new ArrayList<>();
        speedList.add(new UnitItem("Miles Per Hour", "mph", 0.44704));
        speedList.add(new UnitItem("Kilometre per hour", "km/h", 0.277778));
        speedList.add(new UnitItem("Kilometre per second", "km/s", 1000.0));
        speedList.add(new UnitItem("Kilometre per minutes", "km/min", 16.6667));
        speedList.add(new UnitItem("Kilometre per day", "km/d", 0.0115741));
        speedList.add(new UnitItem("Lightspeed", "c", 299792458.0));
        speedList.add(new UnitItem("Speed of sound", "sps", 343.0));
        speedList.add(new UnitItem("Metre per second", "m/s", 1.0));
        speedList.add(new UnitItem("Metre per minutes", "m/min", 0.0166667));
        speedList.add(new UnitItem("Metre per hour", "m/h", 0.000277778));
        speedList.add(new UnitItem("Metre per day", "m/d", 0.0000115741));
        speedList.add(new UnitItem("Knot", "kn", 0.514444));
        speedList.add(new UnitItem("Inch per second", "in/s", 0.0254));
        speedList.add(new UnitItem("Inch per minutes", "in/min", 0.000423333));
        speedList.add(new UnitItem("Inch per hour", "in/h", 0.00000705556));
        speedList.add(new UnitItem("Inch per millisecond", "in/ms", 25.4));
        speedList.add(new UnitItem("Inch per day", "in/d", 0.00000029398));
        speedList.add(new UnitItem("Foot per second", "ft/s", 0.3048));
        speedList.add(new UnitItem("Foot per minutes", "ft/min", 0.00508));
        speedList.add(new UnitItem("Foot per hour", "ft/h", 0.0000846667));
        unitDataMap.put("Speed", speedList);

        // 8. Data units (Base: Byte)
        List<UnitItem> dataList = new ArrayList<>();
        dataList.add(new UnitItem("Bit", "bit", 0.125));
        dataList.add(new UnitItem("Byte", "B", 1.0));
        dataList.add(new UnitItem("Kilobyte", "KB", 1024.0));
        dataList.add(new UnitItem("Megabyte", "MB", 1048576.0));
        dataList.add(new UnitItem("Gigabyte", "GB", 1073741824.0));
        dataList.add(new UnitItem("Terabyte", "TB", 1099511627776.0));
        dataList.add(new UnitItem("Petabyte", "PB", 1125899906842624.0));
        unitDataMap.put("Data", dataList);

        // 9. Power units (Base: Watt)
        List<UnitItem> powerList = new ArrayList<>();
        powerList.add(new UnitItem("Watt", "W", 1.0));
        powerList.add(new UnitItem("Kilowatt", "kW", 1000.0));
        powerList.add(new UnitItem("Megawatt", "MW", 1000000.0));
        powerList.add(new UnitItem("Gigawatt", "GW", 1000000000.0));
        powerList.add(new UnitItem("Electrical Horsepower", "hp", 746.0));
        powerList.add(new UnitItem("Kilowatt Hour", "kWh", 3600000.0));
        powerList.add(new UnitItem("Decibel-MilliWatt", "dBm", 1.0));
        powerList.add(new UnitItem("British thermal Unit", "Btu", 0.293071));
        unitDataMap.put("Power", powerList);

        // 10. Current units (Base: Ampere)
        List<UnitItem> currentList = new ArrayList<>();
        currentList.add(new UnitItem("Ampere", "A", 1.0));
        currentList.add(new UnitItem("KiloAmpere", "kA", 1000.0));
        currentList.add(new UnitItem("MilliAmpere", "mA", 0.001));
        currentList.add(new UnitItem("abAmpere (Biot)", "abA", 10.0));
        currentList.add(new UnitItem("EMU of current", "emuC", 10.0));
        currentList.add(new UnitItem("StatAmpere", "statA", 3.33564e-10));
        currentList.add(new UnitItem("ESU of current", "esuC", 3.33564e-10));
        currentList.add(new UnitItem("CGS e.m. unit", "cgsem", 10.0));
        currentList.add(new UnitItem("CGS e.s. unit", "cgses", 3.33564e-10));
        unitDataMap.put("Current", currentList);

        // 11. Voltage units (Base: Volt)
        List<UnitItem> voltageList = new ArrayList<>();
        voltageList.add(new UnitItem("Volt", "V", 1.0));
        voltageList.add(new UnitItem("Millivolt", "mV", 0.001));
        voltageList.add(new UnitItem("Microvolt", "µV", 0.000001));
        voltageList.add(new UnitItem("Nanovolt", "nV", 1e-9));
        voltageList.add(new UnitItem("Picovolt", "pV", 1e-12));
        voltageList.add(new UnitItem("Kilovolt", "kV", 1000.0));
        voltageList.add(new UnitItem("Megavolt", "mgV", 1000000.0));
        unitDataMap.put("Voltage", voltageList);
    }

    private void onCategoryChanged(String targetCategory) {
        this.currentCategory = targetCategory;
        List<UnitItem> items = unitDataMap.get(targetCategory);

        if (items != null && items.size() >= 2) {
            selectedUnitFrom = items.get(0);
            selectedUnitTo = items.get(1);

            binding.textUnitLabelFrom.setText(selectedUnitFrom.getName());
            binding.textUnitCodeFrom.setText(selectedUnitFrom.getCode());

            binding.textUnitLabelTo.setText(selectedUnitTo.getName());
            binding.textUnitCodeTo.setText(selectedUnitTo.getCode());

            // Set default value to 1 and calculate
            enteredAmount = "1";
            binding.textUnitValueFrom.setText("1");
            binding.textUnitValueFrom.post(() -> {
                binding.textUnitValueFrom.setSelection(binding.textUnitValueFrom.getText().length());
            });
            performConversionCalculation();
        } else {
            enteredAmount = "";
            binding.textUnitValueFrom.setText("");
            binding.textUnitValueTo.setText("0");
        }
    }

    private void initUnitConverter() {
        binding.toolbar.setNavigationOnClickListener(v -> finish());

        // picker interaction from to bottom shhet
        binding.layoutUnitPickerFrom.setOnClickListener(v -> showUnitPicker(true));
        binding.layoutUnitPickerTo.setOnClickListener(v -> showUnitPicker(false));

        binding.layoutUnitFrom.setOnClickListener(v -> updateUnitConverterUI(true));
        binding.textUnitValueFrom.setOnClickListener(v -> updateUnitConverterUI(true));
        binding.layoutUnitTo.setOnClickListener(v -> updateUnitConverterUI(false));

        binding.textUnitValueFrom.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                updateUnitConverterUI(true);
            }
        });

    }

    private void setupUnitKeypad() {
        View.OnClickListener numberClick = v -> {
            triggerVibration();
            String enteredVal = ((TextView) v).getText().toString();
            handleKeypadInputs(enteredVal);
        };

        binding.btnUnit7.setOnClickListener(numberClick);
        binding.btnUnit8.setOnClickListener(numberClick);
        binding.btnUnit9.setOnClickListener(numberClick);
        binding.btnUnit4.setOnClickListener(numberClick);
        binding.btnUnit5.setOnClickListener(numberClick);
        binding.btnUnit6.setOnClickListener(numberClick);
        binding.btnUnit1.setOnClickListener(numberClick);
        binding.btnUnit2.setOnClickListener(numberClick);
        binding.btnUnit3.setOnClickListener(numberClick);
        binding.btnUnit0.setOnClickListener(numberClick);
        binding.btnUnit00.setOnClickListener(numberClick);
        binding.btnUnitDot.setOnClickListener(numberClick);

        binding.btnUnitAc.setOnClickListener(v -> {
            triggerVibration();
            enteredAmount = "";
            binding.textUnitValueFrom.setText("");
            binding.textUnitValueTo.setText("0");
            updateUnitConverterUI(true);
        });

        binding.btnUnitBackspace.setOnClickListener(v -> {
            triggerVibration();
            if (!enteredAmount.isEmpty()) {
                enteredAmount = enteredAmount.substring(0, enteredAmount.length() - 1);
                binding.textUnitValueFrom.setText(enteredAmount);
                binding.textUnitValueFrom.setSelection(enteredAmount.length());
                performConversionCalculation();
            }
        });

        binding.btnUnitSwap.setOnClickListener(v -> {
            triggerVibration();
            
            // Swap unit objects
            UnitItem tempUnit = selectedUnitFrom;
            selectedUnitFrom = selectedUnitTo;
            selectedUnitTo = tempUnit;

            // Swap values
            String currentResult = binding.textUnitValueTo.getText().toString();
            // If it's temperature, remove the unit code suffix before using as new enteredAmount
            if (currentCategory.equals("Temperature")) {
                currentResult = currentResult.replace(" " + selectedUnitFrom.getCode(), "");
            }
            
            enteredAmount = currentResult.equals("0") ? "" : currentResult;
            
            // Update UI Labels and Codes
            binding.textUnitLabelFrom.setText(selectedUnitFrom.getName());
            binding.textUnitCodeFrom.setText(selectedUnitFrom.getCode());
            binding.textUnitLabelTo.setText(selectedUnitTo.getName());
            binding.textUnitCodeTo.setText(selectedUnitTo.getCode());
            
            binding.textUnitValueFrom.setText(enteredAmount);
            binding.textUnitValueFrom.setSelection(binding.textUnitValueFrom.getText().length());

            performConversionCalculation();
        });


    }
    private void handleKeypadInputs(String character) {
        if (enteredAmount.length() + character.length() > 9) {
            Toast.makeText(this, R.string.err_max_9_digits, Toast.LENGTH_SHORT).show();
            return;
        }
        if (character.equals(".") && enteredAmount.contains(".")) {
            return;
        }
        enteredAmount += character;
        binding.textUnitValueFrom.setText(enteredAmount);
        binding.textUnitValueFrom.setSelection(binding.textUnitValueFrom.getText().length());
        performConversionCalculation();
    }
    private void performConversionCalculation() {
        if (enteredAmount.isEmpty() || selectedUnitFrom == null || selectedUnitTo == null) {
            binding.textUnitValueTo.setText("0");
            return;
        }

        try {
            double inputVal = Double.parseDouble(enteredAmount);
            String resultStr;

            if (currentCategory.equals("Temperature")) {
                double finalResult = calculateTemperature(inputVal, selectedUnitFrom.getCode(), selectedUnitTo.getCode());
                if (finalResult == (long) finalResult) {
                    resultStr = String.format(java.util.Locale.getDefault(), "%d", (long) finalResult);
                } else {
                    resultStr = String.format(java.util.Locale.getDefault(), "%.4f", finalResult)
                            .replaceAll("0+$", "")
                            .replaceAll("\\.$", "");
                }
                // Only Temperature shows the unit code in the result field
                binding.textUnitValueTo.setText(resultStr + " " + selectedUnitTo.getCode());
            } else {
                // Use BigDecimal for high precision and to avoid scientific notation (E-8) for Time, etc.
                BigDecimal input = new BigDecimal(enteredAmount);
                BigDecimal fromFactor = BigDecimal.valueOf(selectedUnitFrom.getFactor());
                BigDecimal toFactor = BigDecimal.valueOf(selectedUnitTo.getFactor());

                // Calculate: (input * fromFactor) / toFactor
                BigDecimal result = input.multiply(fromFactor).divide(toFactor, 12, RoundingMode.HALF_UP);
                
                resultStr = result.stripTrailingZeros().toPlainString();
                binding.textUnitValueTo.setText(resultStr);
            }

        } catch (Exception e) {
            binding.textUnitValueTo.setText("0");
        }
    }

    // Dedicated isolated helper to process unique temperature scaling
    private double calculateTemperature(double value, String fromCode, String toCode) {
        if (fromCode.equals(toCode)) return value;

        // 1. Convert everything to Celsius first
        double celsius;
        if (fromCode.equals("°C")) {
            celsius = value;
        } else if (fromCode.equals("°F")) {
            celsius = (value - 32) * 5 / 9;
        } else { // Kelvin
            celsius = value - 273.15;
        }

        // 2. Convert Celsius to the target unit
        if (toCode.equals("°C")) {
            return celsius;
        } else if (toCode.equals("°F")) {
            return (celsius * 9 / 5) + 32;
        } else { // Kelvin
            return celsius + 273.15;
        }
    }



    private void updateUnitConverterUI(boolean isFromSelected) {
        // Toggle selected triggers for background targets defined via custom drawables
        binding.layoutUnitFrom.setSelected(isFromSelected);
        binding.layoutUnitTo.setSelected(!isFromSelected);

        if (isFromSelected) {
            binding.textUnitValueFrom.requestFocus();
            binding.textUnitValueFrom.setSelection(binding.textUnitValueFrom.getText().length());
            binding.textUnitValueFrom.setCursorVisible(true);

            WindowInsetsControllerCompat controller = WindowCompat.getInsetsController(getWindow(), getWindow().getDecorView());
            if (controller != null) {
                controller.hide(WindowInsetsCompat.Type.ime());
            }
        } else {
            binding.textUnitValueFrom.clearFocus();
            binding.textUnitValueFrom.setCursorVisible(false);
        }
    }
    private void showUnitPicker(boolean isSourcePicker) {
        List<UnitItem> dynamicList = unitDataMap.get(currentCategory);
        if (dynamicList == null) return;

        UnitConverterBottomSheet bottomSheet = new UnitConverterBottomSheet(currentCategory, dynamicList, selectedUnit -> {
            if (isSourcePicker) {
                selectedUnitFrom = selectedUnit;
                binding.textUnitLabelFrom.setText(selectedUnit.getName());
                binding.textUnitCodeFrom.setText(selectedUnit.getCode());
            } else {
                selectedUnitTo = selectedUnit;
                binding.textUnitLabelTo.setText(selectedUnit.getName());
                binding.textUnitCodeTo.setText(selectedUnit.getCode());
            }
            performConversionCalculation();
        });
        bottomSheet.show(getSupportFragmentManager(), "BottomSheet");
    }

    private void triggerVibration() {
        Vibrator vibrator;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            VibratorManager vm = (VibratorManager) getSystemService(Context.VIBRATOR_MANAGER_SERVICE);
            vibrator = (vm != null) ? vm.getDefaultVibrator() : null;
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
}