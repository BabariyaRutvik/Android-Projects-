package com.example.calculator.Currency.Networking;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface ApiService {

    // Dynamically pass the Currency code to the endpoint path
    @GET("v4/latest/{base}")
    Call<ExchangeResponse>getLatestExchangeRates(@Path("base") String baseCurrency);
}
