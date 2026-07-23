package com.example.calculator.Activity;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.calculator.Adapter.HistoryAdapter;
import com.example.calculator.Database.HistoryItem;
import com.example.calculator.R;
import com.example.calculator.Database.HistoryViewModel;
import com.example.calculator.databinding.ActivityHistoryBinding;

import java.util.ArrayList;
import java.util.List;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnSelectionChangeListener {

    ActivityHistoryBinding binding;
    HistoryAdapter adapter;
    HistoryViewModel viewModel;
    List<HistoryItem> historyItems = new ArrayList<>();
    private int allowedCapacity = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityHistoryBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        viewModel = new ViewModelProvider(this).get(HistoryViewModel.class);

        initRecycerview();
        observeHistory();

        binding.toolbar.setNavigationOnClickListener(v -> {
            if (adapter.isSelectionMode()) {
                exitSelectionMode();
            } else {
                finish();
            }
        });

        binding.btnDeleteHistory.setOnClickListener(v -> {
            if (historyItems.isEmpty()) {
                Toast.makeText(this, R.string.err_history_empty, Toast.LENGTH_SHORT).show();
                return;
            }

            if (adapter.getSelectedIds().isEmpty()) {
                Toast.makeText(this, R.string.err_select_items_delete, Toast.LENGTH_SHORT).show();
                if (!adapter.isSelectionMode()) {
                    adapter.setSelectionMode(true);
                    onSelectionChanged(0);
                }
                return;
            }
            showDeleteDialog();
        });

        binding.cbSelectAll.setOnClickListener(v -> adapter.selectAll(binding.cbSelectAll.isChecked()));

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (adapter.isSelectionMode()) {
                    exitSelectionMode();
                } else {
                    finish();
                }
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshHistoryLimit();
    }

    private void initRecycerview() {
        adapter = new HistoryAdapter(historyItems, this, item -> {
            Intent intent;
            String expression = item.getExpression();

            if (expression.startsWith("SIP: ")) {
                intent = new Intent(this, SIPCalculatorActivity.class);
            } else if (expression.startsWith("Loan: ")) {
                intent = new Intent(this, LoanCalculatorActivity.class);
            } else {
                intent = new Intent(this, MainActivity.class);
            }

            intent.putExtra("expression", expression);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    private void observeHistory() {
        SharedPreferences sharedPrefs = getSharedPreferences("history_prefs", MODE_PRIVATE);
        allowedCapacity = sharedPrefs.getInt("history_capacity", -1);

        viewModel.getAllHistory().observe(this, items -> {
            processAndDisplayHistory(items);
        });
    }

    // Changed to public so the BottomSheet layout can access it safely
    public void refreshHistoryLimit() {
        SharedPreferences sharedPrefs = getSharedPreferences("history_prefs", MODE_PRIVATE);
        allowedCapacity = sharedPrefs.getInt("history_capacity", -1);

        if (allowedCapacity >= 0) {
            viewModel.prune(allowedCapacity);
        }

        if (viewModel.getAllHistory().getValue() != null) {
            processAndDisplayHistory(viewModel.getAllHistory().getValue());
        }
    }

    private void processAndDisplayHistory(List<HistoryItem> items) {
        List<HistoryItem> displayList = items;

        if (allowedCapacity >= 0 && items.size() > allowedCapacity) {
            displayList = items.subList(0, allowedCapacity);
        }

        historyItems = displayList;
        adapter.updateList(displayList);

        if (displayList.isEmpty()) {
            binding.emptyStateLayout.setVisibility(View.VISIBLE);
            binding.rvHistory.setVisibility(View.GONE);
            binding.selectAllLayout.setVisibility(View.GONE);

            // hiding the delete icon when state is empty
            binding.btnDeleteHistory.setVisibility(View.GONE);
        } else {
            binding.emptyStateLayout.setVisibility(View.GONE);
            binding.rvHistory.setVisibility(View.VISIBLE);

            // showing the delete icon when state is not empty
            binding.btnDeleteHistory.setVisibility(View.VISIBLE);
        }
    }

    private void showDeleteDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_history, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            int widthPx = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 336, getResources().getDisplayMetrics());
            int maxWidth = (int) (getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(Math.min(widthPx, maxWidth), WindowManager.LayoutParams.WRAP_CONTENT);
        }

        TextView textTitle = view.findViewById(R.id.text_dialog_title);
        TextView textMessage = view.findViewById(R.id.text_dialog_message);

        textTitle.setText(R.string.delete_history);
        textMessage.setText(R.string.delete_history_message);

        view.findViewById(R.id.btn_no).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btn_yes).setOnClickListener(v -> {
            if (adapter.isSelectionMode()) {
                viewModel.deleteSelected(adapter.getSelectedIds());
                exitSelectionMode();
            } else {
                viewModel.deleteAll();
            }
            Toast.makeText(this, R.string.msg_history_deleted, Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void exitSelectionMode() {
        adapter.setSelectionMode(false);
        binding.selectAllLayout.setVisibility(View.GONE);
        binding.cbSelectAll.setChecked(false);
    }

    @Override
    public void onSelectionChanged(int selectedCount) {
        if (adapter.isSelectionMode()) {
            binding.selectAllLayout.setVisibility(View.VISIBLE);
        } else {
            binding.selectAllLayout.setVisibility(View.GONE);
        }
        binding.cbSelectAll.setChecked(selectedCount == historyItems.size() && !historyItems.isEmpty());
    }
}