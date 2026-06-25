package com.example.retrofitapi.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.retrofitapi.Activity.FullDetailsActivity;
import com.example.retrofitapi.Model.User;
import com.example.retrofitapi.R;
import com.google.android.material.button.MaterialButton;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserView> {

    private final Context context;
    private final List<User> userList;

    public UserAdapter(List<User> userList, Context context) {
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public UserView onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.design, parent, false);
        return new UserView(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserView holder, int position) {
        User user = userList.get(position);

        holder.textId.setText("ID: " + user.getId());
        holder.textName.setText(user.getName());

        holder.btnDetails.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullDetailsActivity.class);
            intent.putExtra("user_data", user);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return userList != null ? userList.size() : 0;
    }

    public static class UserView extends RecyclerView.ViewHolder {
        TextView textId, textName;
        MaterialButton btnDetails;

        public UserView(@NonNull View itemView) {
            super(itemView);
            textId = itemView.findViewById(R.id.text_id);
            textName = itemView.findViewById(R.id.text_name);
            btnDetails = itemView.findViewById(R.id.btn_details);
        }
    }
}
