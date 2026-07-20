package com.example.calculator.BottomSheet;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.example.calculator.Activity.HistoryActivity;
import com.example.calculator.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class HistoryCapacityBottomSheet extends BottomSheetDialogFragment {
    private SharedPreferences sharedPreferences;
    private int currentCapacity;

    private ConstraintLayout layout0, layout10, layout20, layout50, layout100, layout200, layout500, layout1000, layoutInf;
    private ImageView img0, img10, img20, img50, img100, img200, img500, img1000, imgInf;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_history_capacity_selection, container, false);

        sharedPreferences = requireActivity().getSharedPreferences("history_prefs", Context.MODE_PRIVATE);
        currentCapacity = sharedPreferences.getInt("history_capacity", -1);

        // Fixed: Passed 'view' variable safely here to prevent signature parameter errors
        initView(view);
        updateCheckdUI();
        setupClicks();

        return view;
    }

    private void initView(View view) {
        layout0 = view.findViewById(R.id.layout_capacity_0);
        layout10 = view.findViewById(R.id.layout_capacity_10);
        layout20 = view.findViewById(R.id.layout_capacity_20);
        layout50 = view.findViewById(R.id.layout_capacity_50);
        layout100 = view.findViewById(R.id.layout_capacity_100);
        layout200 = view.findViewById(R.id.layout_capacity_200);
        layout500 = view.findViewById(R.id.layout_capacity_500);
        layout1000 = view.findViewById(R.id.layout_capacity_1000);
        layoutInf = view.findViewById(R.id.layout_capacity_inf);

        img0 = view.findViewById(R.id.img_check_0);
        img10 = view.findViewById(R.id.img_check_10);
        img20 = view.findViewById(R.id.img_check_20);
        img50 = view.findViewById(R.id.img_check_50);
        img100 = view.findViewById(R.id.img_check_100);
        img200 = view.findViewById(R.id.img_check_200);
        img500 = view.findViewById(R.id.img_check_500);
        img1000 = view.findViewById(R.id.img_check_1000);
        imgInf = view.findViewById(R.id.img_check_inf);
    }

    public interface OnCapacityChangeListener {
        void onCapacityChanged(int capacity);
    }

    private OnCapacityChangeListener listener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnCapacityChangeListener) {
            listener = (OnCapacityChangeListener) context;
        }
    }

    private void updateCheckdUI() {
        img0.setImageResource(currentCapacity == 0 ? R.drawable.select_all_history : R.drawable.select_historry);
        img10.setImageResource(currentCapacity == 10 ? R.drawable.select_all_history : R.drawable.select_historry);
        img20.setImageResource(currentCapacity == 20 ? R.drawable.select_all_history : R.drawable.select_historry);
        img50.setImageResource(currentCapacity == 50 ? R.drawable.select_all_history : R.drawable.select_historry);
        img100.setImageResource(currentCapacity == 100 ? R.drawable.select_all_history : R.drawable.select_historry);
        img200.setImageResource(currentCapacity == 200 ? R.drawable.select_all_history : R.drawable.select_historry);
        img500.setImageResource(currentCapacity == 500 ? R.drawable.select_all_history : R.drawable.select_historry);
        img1000.setImageResource(currentCapacity == 1000 ? R.drawable.select_all_history : R.drawable.select_historry);
        imgInf.setImageResource(currentCapacity == -1 ? R.drawable.select_all_history : R.drawable.select_historry);

        // Ensure they are all visible since we are changing the image resource instead of visibility
        img0.setVisibility(View.VISIBLE);
        img10.setVisibility(View.VISIBLE);
        img20.setVisibility(View.VISIBLE);
        img50.setVisibility(View.VISIBLE);
        img100.setVisibility(View.VISIBLE);
        img200.setVisibility(View.VISIBLE);
        img500.setVisibility(View.VISIBLE);
        img1000.setVisibility(View.VISIBLE);
        imgInf.setVisibility(View.VISIBLE);
    }

    private void setupClicks() {
        layout0.setOnClickListener(v -> saveCapacity(0));
        layout10.setOnClickListener(v -> saveCapacity(10));
        layout20.setOnClickListener(v -> saveCapacity(20));
        layout50.setOnClickListener(v -> saveCapacity(50));
        layout100.setOnClickListener(v -> saveCapacity(100));
        layout200.setOnClickListener(v -> saveCapacity(200));
        layout500.setOnClickListener(v -> saveCapacity(500));
        layout1000.setOnClickListener(v -> saveCapacity(1000));
        layoutInf.setOnClickListener(v -> saveCapacity(-1));
    }

    private void saveCapacity(int capacity) {
        sharedPreferences.edit().putInt("history_capacity", capacity).apply();
        if (listener != null) {
            listener.onCapacityChanged(capacity);
        }
        dismiss();

        if (getActivity() instanceof HistoryActivity) {
            ((HistoryActivity) getActivity()).refreshHistoryLimit();
        }
    }
}