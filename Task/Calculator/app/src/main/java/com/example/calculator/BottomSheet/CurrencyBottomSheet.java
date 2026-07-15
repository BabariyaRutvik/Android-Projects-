package com.example.calculator.BottomSheet;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.Adapter.CurrencyAdapter;
import com.example.calculator.Currency.CurrencyModel;
import com.example.calculator.R;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CurrencyBottomSheet extends BottomSheetDialogFragment {

    // interface for the currency screen
    public interface OnCurrencySelectedListener {
        void onCurrencySelected(CurrencyModel currency);
    }

    private OnCurrencySelectedListener listener;
    private List<CurrencyModel> allCurrency = new ArrayList<>();
    private List<CurrencyModel> filteredList = new ArrayList<>();
    private CurrencyAdapter adapter;

    // constructor to pass the lister from dialog to currency activity
    public CurrencyBottomSheet(OnCurrencySelectedListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NORMAL, R.style.CustomBottomSheetDialogTheme);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_currency_picker, container, false);
    }

    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog instanceof BottomSheetDialog) {
            BottomSheetDialog bottomSheetDialog = (BottomSheetDialog) dialog;
            FrameLayout bottomSheet = bottomSheetDialog.findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                BottomSheetBehavior<FrameLayout> behavior = BottomSheetBehavior.from(bottomSheet);
                
                // Set peek height to half of screen height
                int screenHeight = getResources().getDisplayMetrics().heightPixels;
                behavior.setPeekHeight(screenHeight / 2);

                // Allow the bottom sheet to take full height when swiped up
                ViewGroup.LayoutParams layoutParams = bottomSheet.getLayoutParams();
                if (layoutParams != null) {
                    layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
                }
                bottomSheet.setLayoutParams(layoutParams);

                // Start in collapsed state (half screen)
                behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
                behavior.setSkipCollapsed(false);
            }
        }
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edtSearch = view.findViewById(R.id.edit_search_currency);
        ImageView imgClear = view.findViewById(R.id.img_clear_search);
        RecyclerView recyclerViewCurrency = view.findViewById(R.id.recycler_currencies);

        // loading the all country from the json
        loadMetaData();
        filteredList.addAll(allCurrency);

        recyclerViewCurrency.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new CurrencyAdapter(getContext(), filteredList);
        recyclerViewCurrency.setAdapter(adapter);

        // now implementing the click event on recyclerview
        recyclerViewCurrency.addOnItemTouchListener(new RecyclerItemClickListener(getContext(), recyclerViewCurrency, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                if (listener != null) {
                    listener.onCurrencySelected(filteredList.get(position));
                    dismiss(); // Close the bottom sheet
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {
            }
        }));

        imgClear.setOnClickListener(v -> edtSearch.setText(""));

        // search functionality to filter the country by name
        edtSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString();
                imgClear.setVisibility(query.isEmpty() ? View.GONE : View.VISIBLE);
                filter(query);
            }
        });
    }

    private void loadMetaData() {
        try {
            InputStream inputStream = requireContext().getAssets().open("currencies_metadata.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            allCurrency = new Gson().fromJson(json, new TypeToken<List<CurrencyModel>>() {
            }.getType());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // filtering recyclerview items
    private void filter(String text) {
        filteredList.clear();

        if (text.isEmpty()) {
            filteredList.addAll(allCurrency);
        } else {
            String query = text.toLowerCase().trim();

            for (CurrencyModel item : allCurrency) {
                if (item.getCountryCode().toLowerCase().contains(query) ||
                        item.getCountryName().toLowerCase().contains(query) ||
                        item.getCurrencyName().toLowerCase().contains(query) ||
                        item.getCurrencyCode().toLowerCase().contains(query)) {

                    filteredList.add(item);
                }
            }
        }
        adapter.FilterList(filteredList);
    }
}
