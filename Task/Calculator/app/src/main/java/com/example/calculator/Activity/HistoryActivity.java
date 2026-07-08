package com.example.calculator.Activity;

import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
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


        binding.btnBack.setOnClickListener(v->{
            if (adapter.isSelectionMode()){
                exitSelectionMode();
            }
            else {
                finish();
            }
        });
        binding.btnDeleteHistory.setOnClickListener(v -> {
            if (historyItems.isEmpty()){
                Toast.makeText(this, "History is empty", Toast.LENGTH_SHORT).show();
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

    private void initRecycerview() {
        adapter = new HistoryAdapter(historyItems, this);
        binding.rvHistory.setLayoutManager(new LinearLayoutManager(this));
        binding.rvHistory.setAdapter(adapter);
    }

    private void observeHistory() {
        viewModel.getAllHistory().observe(this, items -> {
            historyItems = items;
            adapter.updateList(items);

            if (items.isEmpty()) {
                binding.emptyStateLayout.setVisibility(View.VISIBLE);
                binding.rvHistory.setVisibility(View.GONE);
                binding.selectAllLayout.setVisibility(View.GONE);
            } else {
                binding.emptyStateLayout.setVisibility(View.GONE);
                binding.rvHistory.setVisibility(View.VISIBLE);
            }
        });
    }

    private void showDeleteDialog() {
        View view = getLayoutInflater().inflate(R.layout.dialog_delete_history, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(view)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        TextView textMessage = view.findViewById(R.id.text_dialog_message);
        if (adapter.isSelectionMode()) {
            textMessage.setText("Do you want to delete selected calculation?");
        } else {
            textMessage.setText("Do you want to delete calculation?");
        }

        view.findViewById(R.id.btn_no).setOnClickListener(v -> dialog.dismiss());

        view.findViewById(R.id.btn_yes).setOnClickListener(v -> {
            if (adapter.isSelectionMode()) {
                viewModel.deleteSelected(adapter.getSelectedIds());
                exitSelectionMode();
            } else {
                viewModel.deleteAll();
            }
            Toast.makeText(this, "History deleted", Toast.LENGTH_SHORT).show();
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
