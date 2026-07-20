package com.example.calculator.Activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import android.widget.Toast;

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

        binding.toolbar.setNavigationOnClickListener(v -> {
            finish();
        });
    }

    private void initData() {
        calculatorList = new ArrayList<>();
        calculatorList.add(new CalculatorItem(R.string.sip_calculator, R.drawable.sip_calculator, 87));
        calculatorList.add(new CalculatorItem(R.string.loan_calculator, R.drawable.loan_calculator, 96));
        calculatorList.add(new CalculatorItem(R.string.gst_calculator, R.drawable.gst_calculator, 93));
        calculatorList.add(new CalculatorItem(R.string.investment_calculator, R.drawable.investment_calculator, 134));
        calculatorList.add(new CalculatorItem(R.string.currency_converter, R.drawable.hugeicons_coins_swap, 121));
        calculatorList.add(new CalculatorItem(R.string.saving_calculator, R.drawable.saving_calculator, 107));
        calculatorList.add(new CalculatorItem(R.string.age_calculator, R.drawable.age_calculator, 90));
        calculatorList.add(new CalculatorItem(R.string.bmi_calculator, R.drawable.bmi_calculator, 90));
        calculatorList.add(new CalculatorItem(R.string.unit_converter, R.drawable.unit_calculator, 92));
        calculatorList.add(new CalculatorItem(R.string.date_calculator, R.drawable.date_calculator, 95));
        calculatorList.add(new CalculatorItem(R.string.discount_calculator, R.drawable.discount_calculator, 121));
    }

    private void setupRecyclerView() {
        adapter = new OtherCalculatorAdapter(calculatorList, item -> {
            int id = item.getNameResId();
            if (id == R.string.sip_calculator) {
                startActivity(new Intent(this, SIPCalculatorActivity.class));
            } else if (id == R.string.loan_calculator) {
                startActivity(new Intent(this, LoanCalculatorActivity.class));
            } else if (id == R.string.gst_calculator) {
                startActivity(new Intent(this, GSTCalculatorActivity.class));
            } else if (id == R.string.investment_calculator) {
                startActivity(new Intent(this, InvestmentCalculatorActivity.class));
            } else if (id == R.string.currency_converter) {
                startActivity(new Intent(this, CurrencyConvertorCalculator.class));
            } else if (id == R.string.saving_calculator) {
                startActivity(new Intent(this, SavingCalculatorActivity.class));
            } else if (id == R.string.age_calculator) {
                startActivity(new Intent(this, AgeCalculatorActivity.class));
            } else if (id == R.string.bmi_calculator) {
                startActivity(new Intent(this, BmiCalculatorActivity.class));
            } else if (id == R.string.date_calculator) {
                startActivity(new Intent(this, DateCalculatorActivity.class));
            } else if (id == R.string.discount_calculator) {
                startActivity(new Intent(this, DiscountCalculatorActivity.class));
            } else {
                startActivity(new Intent(this, UnitConverterCalculatorActivity.class));
            }
        });
        binding.rvOtherCalculators.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvOtherCalculators.setAdapter(adapter);
    }
}
