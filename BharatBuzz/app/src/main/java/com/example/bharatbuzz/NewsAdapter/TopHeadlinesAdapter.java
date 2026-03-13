package com.example.bharatbuzz.NewsAdapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.bharatbuzz.Database.ArticleDatabase;
import com.example.bharatbuzz.NewsActivity.FullScreenNewsActivity;
import com.example.bharatbuzz.NewsModel.Article;
import com.example.bharatbuzz.R;

import java.util.ArrayList;

public class TopHeadlinesAdapter extends RecyclerView.Adapter<TopHeadlinesAdapter.TopNewsViewHolder> {

    private Context context;
    private ArrayList<Article> topNewsList;
    private ArticleDatabase database;

    public TopHeadlinesAdapter(Context context, ArrayList<Article> topNewsList) {
        this.context = context;
        this.topNewsList = topNewsList;
        this.database = ArticleDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public TopNewsViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.top_news_item, parent, false);
        return new TopNewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopNewsViewHolder holder, int position) {
        Article article = topNewsList.get(position);

        holder.txtTopTitle.setText(article.getTitle());

        String source = (article.getSource() != null) ? article.getSource().getName() : "News";
        String time = (article.getPublishedAt() != null && article.getPublishedAt().length() >= 10) 
                ? article.getPublishedAt().substring(0, 10) : "";
        holder.txtTopSourceAndTime.setText(source + " • " + time);

        // Check if bookmarked
        boolean isBookmarked = database.articleDao().isBookmarked(article.getUrl());
        holder.imgBookmark.setImageResource(isBookmarked ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border);

        // Load Main Image
        Glide.with(context)
                .load(article.getUrlToImage())
                .placeholder(R.color.light_gray)
                .into(holder.imgTopNews);

        holder.imgBookmark.setOnClickListener(v -> {
            if (database.articleDao().isBookmarked(article.getUrl())) {
                database.articleDao().deleteBookmark(article);
                holder.imgBookmark.setImageResource(R.drawable.ic_bookmark_border);
                Toast.makeText(context, "Removed from Bookmarks", Toast.LENGTH_SHORT).show();
            } else {
                database.articleDao().insertBookmark(article);
                holder.imgBookmark.setImageResource(R.drawable.ic_bookmark_filled);
                Toast.makeText(context, "Added to Bookmarks", Toast.LENGTH_SHORT).show();
            }
        });
        
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, FullScreenNewsActivity.class);
            intent.putExtra("article", article);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return (topNewsList != null) ? topNewsList.size() : 0;
    }

    public void updateData(ArrayList<Article> newList) {
        this.topNewsList = newList;
        notifyDataSetChanged();
    }

    public static class TopNewsViewHolder extends RecyclerView.ViewHolder {
        ImageView imgTopNews;
        ImageView imgBookmark;
        TextView txtTopTitle;
        TextView txtTopSourceAndTime;

        public TopNewsViewHolder(@NonNull View itemView) {
            super(itemView);
            imgTopNews = itemView.findViewById(R.id.imgTopNews);
            imgBookmark = itemView.findViewById(R.id.imgBookmark);
            txtTopTitle = itemView.findViewById(R.id.txtTopTitle);
            txtTopSourceAndTime = itemView.findViewById(R.id.txtTopSourceAndTime);
        }
    }
}
