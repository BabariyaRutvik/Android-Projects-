package com.example.calculator.BottomSheet;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.Adapter.CurrencyAdapter;
import com.example.calculator.Currency.CurrencyModel;
import com.example.calculator.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.gson.Gson;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class CurrencyBottomSheet extends BottomSheetDialogFragment
{

    // interface for the currency screen
    public interface OnCurrencySelectedListener{
        void onCurrencySelected(String currency);
    }
    private OnCurrencySelectedListener listener;
    private List<CurrencyModel> allCurrency = new ArrayList<>();
    private List<CurrencyModel> filteredList = new ArrayList<>();
    private CurrencyAdapter adapter;

    // constructor to pass the lister from dialog to currency activity
    public CurrencyBottomSheet(OnCurrencySelectedListener listener){
        this.listener = listener;

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_currency_picker,container,false);

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        EditText edtSearch = view.findViewById(R.id.edit_search_currency);
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
                    listener.onCurrencySelected(filteredList.get(position).getCurrencyCode());
                    dismiss(); // Close the bottom sheet
                }
            }

            @Override
            public void onLongItemClick(View view, int position) {}
        }));



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
                  filter(s.toString());
            }
        });
    }
    private void loadMetaData(){
        try {
            InputStream inputStream = requireContext().getAssets().open("currencies_metadata.json");
            int size = inputStream.available();
            byte[] buffer = new byte[size];
            inputStream.read(buffer);
            inputStream.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            allCurrency = new Gson().fromJson(json, new TypeToken<List<CurrencyModel>>(){}.getType());
        }catch (Exception e){
            e.printStackTrace();

        }

    }
    // filtering recyclerview iotems
    private void filter(String text){
        filteredList.clear();

        if (text.isEmpty()){
            String query = text.toLowerCase().trim();

            for (CurrencyModel item : allCurrency){
                if (item.getCountryCode().toLowerCase().contains(query)||
                item.getCountryName().toLowerCase().contains(query)||
                item.getCurrencyName().contains(query)){

                    filteredList.add(item);
                }
            }
        }
        adapter.FilterList(filteredList);
    }
}
