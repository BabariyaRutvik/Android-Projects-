package com.example.bharatbuzz.Service;

import com.example.bharatbuzz.NewsModel.NewsResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiInterface {

    @GET("top-headlines")
    Call<NewsResponse> getNews(
            @Query("country") String country,
            @Query("pageSize") int pageSize,
            @Query("apiKey") String apiKey
    );

    @GET("top-headlines")
    Call<NewsResponse> getCategoryNews(
            @Query("country") String country,
            @Query("category") String category,
            @Query("page") int page,
            @Query("pageSize") int pageSize,
            @Query("apiKey") String apiKey
    );
}
