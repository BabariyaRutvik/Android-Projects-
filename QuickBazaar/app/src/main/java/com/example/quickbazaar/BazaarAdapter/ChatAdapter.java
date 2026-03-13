package com.example.quickbazaar.BazaarAdapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.quickbazaar.BazaarModel.ChatMessage;
import com.example.quickbazaar.R;

import java.util.List;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private final List<ChatMessage> chatMessages;

    public ChatAdapter(List<ChatMessage> chatMessages) {
        this.chatMessages = chatMessages;
    }

    @Override
    public int getItemViewType(int position) {
        return chatMessages.get(position).getType();
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == ChatMessage.TYPE_USER) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_user, parent, false);
            return new UserViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_ai, parent, false);
            return new AiViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ChatMessage message = chatMessages.get(position);
        if (holder instanceof UserViewHolder) {
            ((UserViewHolder) holder).tvUserMsg.setText(message.getText());
        } else {
            ((AiViewHolder) holder).tvAiMsg.setText(message.getText());
        }
    }

    @Override
    public int getItemCount() {
        return chatMessages.size();
    }

    static class UserViewHolder extends RecyclerView.ViewHolder {
        TextView tvUserMsg;

        public UserViewHolder(@NonNull View itemView) {
            super(itemView);
            tvUserMsg = itemView.findViewById(R.id.tvUserMsg);
        }
    }

    static class AiViewHolder extends RecyclerView.ViewHolder {
        TextView tvAiMsg;

        public AiViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAiMsg = itemView.findViewById(R.id.tvAiMsg);
        }
    }
}
