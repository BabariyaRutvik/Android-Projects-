package com.example.interviewace.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.interviewace.R;
import com.example.interviewace.model.OnBoardingItem;

import java.util.List;


public class OnBoardingAdapter extends RecyclerView.Adapter<OnBoardingView> {


    // listing of onboarding item
    private List<OnBoardingItem> onboardingItems;


    // constructor
    public OnBoardingAdapter(List<OnBoardingItem> onboardingItems) {
        this.onboardingItems = onboardingItems;
    }



    @NonNull
    @Override
    public OnBoardingView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_onboarding_page, parent, false);
        OnBoardingView onBoardingView = new OnBoardingView(view);
        return onBoardingView;
    }

    @Override
    public void onBindViewHolder(@NonNull OnBoardingView holder, int position) {
        OnBoardingItem item = onboardingItems.get(position);

        // onboarding image
        holder.image_illustrations.setImageResource(item.getImageRes());

        // onboarding title
        holder.text_title.setText(item.getTitle());

        // onboarding description
        holder.text_description.setText(item.getDescription());


    }

    @Override
    public int getItemCount() {
        return onboardingItems.size();
    }
}
// viewholder class
class OnBoardingView extends RecyclerView.ViewHolder{

    ImageView image_illustrations;
    TextView text_title;
    TextView text_description;

    public OnBoardingView(@NonNull View itemView) {
        super(itemView);

        image_illustrations = itemView.findViewById(R.id.ivIllustration);
        text_title = itemView.findViewById(R.id.text_title_on);
        text_description = itemView.findViewById(R.id.tvDescription);


    }
}
