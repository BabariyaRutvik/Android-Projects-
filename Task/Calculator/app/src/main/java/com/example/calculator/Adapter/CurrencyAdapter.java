package com.example.calculator.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.calculator.Currency.CurrencyModel;
import com.example.calculator.R;
import com.google.android.material.imageview.ShapeableImageView;

import java.util.List;

public class CurrencyAdapter extends RecyclerView.Adapter<CurrencyAdapter.MyCurrencyView>  {
    private Context context;
    private List<CurrencyModel> currencyModelList;

    public CurrencyAdapter(Context context, List<CurrencyModel> currencyModelList) {
        this.context = context;
        this.currencyModelList = currencyModelList;
    }
    private void updateData(List<CurrencyModel>newList){
        this.currencyModelList = newList;
        notifyDataSetChanged();
    }
    public void FilterList(List<CurrencyModel> filteredList) {
        this.currencyModelList = filteredList;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public CurrencyAdapter.MyCurrencyView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_currency,parent,false);
        return new MyCurrencyView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CurrencyAdapter.MyCurrencyView holder, int position) {
         CurrencyModel model = currencyModelList.get(position);

         // setting up Currency code
        holder.txtCountryCode.setText(String.format("%s - %s", model.getCurrencyCode(), model.getCurrencyName()));

        // set Countryname
        holder.txtCountryName.setText(model.getCountryName());

        // now load the Country Flag
        String flagUrl = "https://flagcdn.com/w160/" + model.getCountryCode().toLowerCase() + ".png";

        // glide
        Glide.with(context)
                .load(flagUrl)
                .placeholder(R.drawable.currency_calculator)
                .error(R.drawable.ic_scientific)
                .into(holder.imgCountry);
    }


    @Override
    public int getItemCount() {
        return currencyModelList.size();
    }
    public static class MyCurrencyView extends RecyclerView.ViewHolder{

        ShapeableImageView imgCountry;
        TextView txtCountryName;
        TextView txtCountryCode;

        public MyCurrencyView(@NonNull View itemView) {
            super(itemView);

            imgCountry = itemView.findViewById(R.id.img_flag);
            txtCountryName = itemView.findViewById(R.id.text_country_name);
            txtCountryCode = itemView.findViewById(R.id.text_currency_code_name);

        }
    }
}