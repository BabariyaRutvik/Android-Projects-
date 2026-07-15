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

        // Ensure Status Bar icons are dark
        androidx.core.view.WindowInsetsControllerCompat windowInsetsController =
                androidx.core.view.ViewCompat.getWindowInsetsController(getWindow().getDecorView());
        if (windowInsetsController != null) {
            windowInsetsController.setAppearanceLightStatusBars(true);
            windowInsetsController.setAppearanceLightNavigationBars(true);
        }

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
            if (item.getName().equalsIgnoreCase("SIP Calculator")) {
                startActivity(new Intent(this, SIPCalculatorActivity.class));
            } else if (item.getName().equalsIgnoreCase("Loan Calculator")) {
                startActivity(new Intent(this, LoanCalculatorActivity.class));
            } else if (item.getName().equalsIgnoreCase("GST Calculator")) {
                startActivity(new Intent(this, GSTCalculatorActivity.class));
            } else if (item.getName().equalsIgnoreCase("Investment Calculator")) {
                startActivity(new Intent(this, InvestmentCalculatorActivity.class));
            }
            else if (item.getName().equalsIgnoreCase("Currency Converter")) {
                startActivity(new Intent(this, CurrencyConvertorCalculator.class));

            }
            else if (item.getName().equalsIgnoreCase("Saving Calculator")){
                startActivity(new Intent(this, SavingCalculatorActivity.class));
            }
            else if (item.getName().equalsIgnoreCase("Age Calculator")){
                startActivity(new Intent(this, AgeCalculatorActivity.class));
            }
            else {
                Toast.makeText(this, item.getName()+ "Coming Soon", Toast.LENGTH_SHORT).show();
            }
        });
        binding.rvOtherCalculators.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvOtherCalculators.setAdapter(adapter);
    }
}
