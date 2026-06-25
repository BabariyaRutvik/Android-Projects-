package com.example.quicknotes.BottomSheet;


import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.quicknotes.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

public class ViewSelectionBottomSheet extends BottomSheetDialogFragment {

   public enum ViewType{
       LIST , GRID , DETAILS
   }
    private ViewType currentViewType = ViewType.DETAILS;
    private OnViewTypeSelectedListener listener;

    public interface OnViewTypeSelectedListener {
        void onViewTypeSelected(ViewType viewType);
    }

    public void setOnViewTypeSelectedListener(OnViewTypeSelectedListener listener) {
        this.listener = listener;
    }

    public void setCurrentViewType(ViewType viewType) {
        this.currentViewType = viewType;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_view_selection, container, false);

        ImageView imgCheckList = view.findViewById(R.id.img_check_list);
        ImageView imgCheckGrid = view.findViewById(R.id.img_check_grid);
        ImageView imgCheckDetails = view.findViewById(R.id.img_check_details);

        // setting up initial state
        imgCheckList.setVisibility(currentViewType == ViewType.LIST ? View.VISIBLE : View.GONE);
        imgCheckGrid.setVisibility(currentViewType == ViewType.GRID ? View.VISIBLE : View.GONE);
        imgCheckDetails.setVisibility(currentViewType == ViewType.DETAILS ? View.VISIBLE : View.GONE);


        view.findViewById(R.id.layout_list).setOnClickListener( v->{
            if (listener != null){
                listener.onViewTypeSelected(ViewType.LIST);
                dismiss();
            }
        });
        view.findViewById(R.id.layout_grid).setOnClickListener(v->{
            if (listener != null){
                listener.onViewTypeSelected(ViewType.GRID);
                dismiss();
            }
        });
        view.findViewById(R.id.layout_details).setOnClickListener(v->{
            if (listener != null){
                listener.onViewTypeSelected(ViewType.DETAILS);
                dismiss();
            }
        });

        return  view;
    }
}
