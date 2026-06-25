package com.example.retrofitapi.Activity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.retrofitapi.Model.User;
import com.example.retrofitapi.Networking.ApiClient;
import com.example.retrofitapi.Networking.ApiInterface;
import com.example.retrofitapi.databinding.ActivityMainBinding;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    private List<User> userList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        setSupportActionBar(binding.toolbar);


        fetchUsersFromApi(false);

        binding.btnFetch.setOnClickListener(v -> {
            String userIdStr = binding.etUserId.getText().toString().trim();
            if (userIdStr.isEmpty()) {
                binding.etUserId.setError("Please Enter User Id");
                return;
            }

            try {
                int id = Integer.parseInt(userIdStr);
                if (id < 1 || id > 10) {
                    Toast.makeText(this, "Enter ID between 1 and 10", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (userList != null) {
                    showUserDetails(id);
                } else {
                    fetchUsersFromApi(true);
                }
            } catch (NumberFormatException e) {
                binding.etUserId.setError("Invalid ID");
            }
        });
    }

    private void fetchUsersFromApi(boolean showLoading) {
        if (showLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnFetch.setEnabled(false);
        }

        ApiInterface api = ApiClient.getClient().create(ApiInterface.class);
        api.getUsers().enqueue(new Callback<List<User>>() {
            @Override
            public void onResponse(Call<List<User>> call, Response<List<User>> response) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnFetch.setEnabled(true);

                if (response.isSuccessful() && response.body() != null) {
                    userList = response.body();
                    if (showLoading) showUserDetails(Integer.parseInt(binding.etUserId.getText().toString().trim()));
                } else if (showLoading) {
                    Toast.makeText(MainActivity.this, "Failed to load data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<User>> call, Throwable t) {
                binding.progressBar.setVisibility(View.GONE);
                binding.btnFetch.setEnabled(true);
                if (showLoading) Toast.makeText(MainActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUserDetails(int id) {
        for (User user : userList) {
            if (user.getId() == id) {
                Intent intent = new Intent(this, FullDetailsActivity.class);
                intent.putExtra("user_data", user);
                startActivity(intent);
                return;
            }
        }
        Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
    }


}
