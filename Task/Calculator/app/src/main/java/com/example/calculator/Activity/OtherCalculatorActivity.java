package com.example.calculator.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.calculator.Adapter.OtherCalculatorAdapter;
import com.example.calculator.Model.CalculatorItem;
import com.example.calculator.R;
import com.example.calculator.databinding.ActivityOtherCalculatorBinding;

import java.util.ArrayList;
import java.util.List;

public class OtherCalculatorActivity extends AppCompatActivity {

    private ActivityOtherCalculatorBinding binding;
    private OtherCalculatorAdapter adapter;
    private List<CalculatorItem> calculatorList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityOtherCalculatorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initData();
        setupRecyclerView();

        binding.btnBack.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
        });
    }

    private void initData() {
        calculatorList = new ArrayList<>();
        calculatorList.add(new CalculatorItem("SIP Calculator", R.drawable.sip_calculator, 87));
        calculatorList.add(new CalculatorItem("Loan Calculator", R.drawable.loan_calculator, 96));
        calculatorList.add(new CalculatorItem("GST Calculator", R.drawable.gst_calculator, 93));
        calculatorList.add(new CalculatorItem("Investment Calculator", R.drawable.investment_calculator, 134));
        calculatorList.add(new CalculatorItem("Currency Converter", R.drawable.hugeicons_coins_swap, 121));
        calculatorList.add(new CalculatorItem("Saving Calculator", R.drawable.saving_calculator, 107));
        calculatorList.add(new CalculatorItem("Age Calculator", R.drawable.age_calculator, 90));
        calculatorList.add(new CalculatorItem("BMI Calculator", R.drawable.bmi_calculator, 90));
        calculatorList.add(new CalculatorItem("Unit Calculator", R.drawable.unit_calculator, 92));
        calculatorList.add(new CalculatorItem("Date Calculator", R.drawable.date_calculator, 95));
        calculatorList.add(new CalculatorItem("Discount Calculator", R.drawable.discount_calculator, 121));
    }

    private void setupRecyclerView() {
        adapter = new OtherCalculatorAdapter(calculatorList, item -> {
            // Handle clicks if needed
        });
        binding.rvOtherCalculators.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvOtherCalculators.setAdapter(adapter);
    }
}
