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
import com.google.android.material.imageview.ShapeableImageView;

import java.util.ArrayList;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.MyNews> {
    private Context context;
    private ArrayList<Article> newsList;
    private ArrayList<Article> newsListFull;
    private ArticleDatabase database;

    public NewsAdapter(Context context, ArrayList<Article> newsList) {
        this.context = context;
        this.newsList = newsList;
        this.newsListFull = new ArrayList<>(newsList);
        this.database = ArticleDatabase.getInstance(context);
    }

    @NonNull
    @Override
    public MyNews onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.news_item, parent, false);
        return new MyNews(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MyNews holder, int position) {
        Article article = newsList.get(position);
        
        holder.txtTitle.setText(article.getTitle());
        
        String source = (article.getSource() != null) ? article.getSource().getName() : "News";
        String time = (article.getPublishedAt() != null && article.getPublishedAt().length() >= 10) 
                ? article.getPublishedAt().substring(0, 10) : "";
        holder.txtSourceAndTime.setText(source + " • " + time);

        // Check if bookmarked
        boolean isBookmarked = database.articleDao().isBookmarked(article.getUrl());
        holder.imgFav.setImageResource(isBookmarked ? R.drawable.ic_bookmark_filled : R.drawable.ic_bookmark_border);

        // Loading News Image using Glide
        Glide.with(context)
                .load(article.getUrlToImage())
                .placeholder(R.color.light_gray)
                .error(R.color.light_gray)
                .into(holder.imgNews);

        holder.imgFav.setOnClickListener(v -> {
            if (database.articleDao().isBookmarked(article.getUrl())) {
                database.articleDao().deleteBookmark(article);
                holder.imgFav.setImageResource(R.drawable.ic_bookmark_border);
                Toast.makeText(context, "Removed from Bookmarks", Toast.LENGTH_SHORT).show();
            } else {
                database.articleDao().insertBookmark(article);
                holder.imgFav.setImageResource(R.drawable.ic_bookmark_filled);
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
        return (newsList != null) ? newsList.size() : 0;
    }

    public void updateData(ArrayList<Article> newList) {
        this.newsList = newList;
        this.newsListFull = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String text) {
        ArrayList<Article> filteredList = new ArrayList<>();
        if (text == null || text.isEmpty()) {
            filteredList.addAll(newsListFull);
        } else {
            text = text.toLowerCase().trim();
            for (Article item : newsListFull) {
                if (item.getTitle().toLowerCase().contains(text)) {
                    filteredList.add(item);
                }
            }
        }
        this.newsList = filteredList;
        notifyDataSetChanged();
    }

    public static class MyNews extends RecyclerView.ViewHolder {
        ShapeableImageView imgNews;
        TextView txtTitle, txtSourceAndTime;
        ImageView imgFav;

        public MyNews(@NonNull View itemView) {
            super(itemView);
            imgNews = itemView.findViewById(R.id.imgNews);
            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtSourceAndTime = itemView.findViewById(R.id.txtSourceAndTime);
            imgFav = itemView.findViewById(R.id.imgFav);
        }
    }
}
