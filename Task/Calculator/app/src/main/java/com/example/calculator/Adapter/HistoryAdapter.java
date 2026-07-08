package com.example.calculator.Adapter;


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.calculator.Database.HistoryItem;
import com.example.calculator.R;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public  class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.HistoryView>
{
    private List<HistoryItem> historyItemList;
    private  boolean isSelectionMode = false;
    private Set<Integer> selectedIds = new HashSet<>();
    private OnSelectionChangeListener selectionChangeListener;

    public interface  OnSelectionChangeListener{
        void onSelectionChanged(int selectedCount);
    }
    // constructor
    public HistoryAdapter(List<HistoryItem>historyItemList, OnSelectionChangeListener listener){
        this.historyItemList = historyItemList;
        this.selectionChangeListener = listener;
    }

    @NonNull
    @Override
    public HistoryView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history,parent,false);
        return new HistoryView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull HistoryView holder, int position) {
         HistoryItem item = historyItemList.get(position);
         holder.textExpression.setText(item.getExpression());
         holder.textResult.setText(item.getResult());

         holder.checkBox.setVisibility(isSelectionMode ? View.VISIBLE : View.GONE);
         holder.checkBox.setChecked(selectedIds.contains(item.getId()));

         // selection process
         holder.itemView.setOnClickListener(v->{
             if (isSelectionMode){
                 toggleSelection(item.getId());
             }
         });
         // selecting long click event item
        holder.itemView.setOnLongClickListener(v->{

            if (!isSelectionMode){
                isSelectionMode = true;
                toggleSelection(item.getId());
                notifyDataSetChanged();
                return true;
            }
           return false;
        });
        holder.checkBox.setOnClickListener(v->{
            toggleSelection(item.getId());
        });


    }
    // toggle selection method
    private void toggleSelection(int id){
        if (selectedIds.contains(id)){
            selectedIds.remove(id);

        }
        else {
            selectedIds.add(id);
        }
        notifyDataSetChanged();
        if (selectionChangeListener != null){
            selectionChangeListener.onSelectionChanged(selectedIds.size());
        }

    }
    public void setSelectionMode(boolean selectionMode){
        isSelectionMode = selectionMode;

        if (!selectionMode){
            selectedIds.clear();
        }
        notifyDataSetChanged();
    }
    public boolean isSelectionMode(){
        return isSelectionMode;
    }
    public void selectAll(boolean select){
        selectedIds.clear();

        if (select){
            for (HistoryItem item : historyItemList){
                selectedIds.add(item.getId());

            }
        }
        notifyDataSetChanged();
        if (selectionChangeListener != null){
            selectionChangeListener.onSelectionChanged(selectedIds.size());
        }
    }
    public List<Integer>getSelectedIds(){
        return new ArrayList<>(selectedIds);
    }

    @Override
    public int getItemCount() {
        return historyItemList.size();
    }
    public void updateList(List<HistoryItem> newList){
        this.historyItemList = newList;
        notifyDataSetChanged();
    }


    public static  class HistoryView extends RecyclerView.ViewHolder{
        TextView textExpression , textResult;
        CheckBox checkBox;


        public HistoryView(@NonNull View itemView) {
            super(itemView);

            textExpression = itemView.findViewById(R.id.text_expression);
            textResult = itemView.findViewById(R.id.text_result);
            checkBox = itemView.findViewById(R.id.checkbox);

        }
    }
}

