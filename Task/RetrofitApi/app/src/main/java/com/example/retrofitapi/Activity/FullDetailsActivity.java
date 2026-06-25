package com.example.retrofitapi.Activity;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.retrofitapi.Model.User;
import com.example.retrofitapi.databinding.ActivityFullDetailsBinding;

public class FullDetailsActivity extends AppCompatActivity {

    private ActivityFullDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullDetailsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // setup the toolbar
        setSupportActionBar(binding.toolbar);

        if (getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        // get the user data
        User user = (User) getIntent().getSerializableExtra("user_data");


        if (user != null){
            PopulateUser(user);
        }


    }
    private void PopulateUser(User user){
        // initials and name
        StringBuilder initials = new StringBuilder();

        if (user.getName() != null && !user.getName().trim().isEmpty()){
            String [] parts = user.getName().trim().split("\\s+");

            if (parts.length > 0 && !parts[0].isEmpty()) initials.append(parts[0].charAt(0));
            if (parts.length > 1 && !parts[1].isEmpty()) initials.append(parts[1].charAt(0));
        }
        binding.txtInitials.setText(initials.toString().toUpperCase());
        binding.textName.setText(user.getName());
        binding.txtUsername.setText("@" + user.getUsername());

        // Basic Info
        binding.txtId.setText(String.valueOf(user.getId()));
        binding.txtUser.setText(user.getUsername());

        // Contact info
        binding.txtEmail.setText(user.getEmail());
        binding.txtPhone.setText(user.getPhone());
        binding.txtWebsite.setText(user.getWebsite());

        // Address info
        if (user.getAddress() != null){
            String fullAddress = user.getAddress().getStreet() + "\n" +
                    user.getAddress().getSuite() + "\n" +
                    user.getAddress().getCity() + "\n" +
                    user.getAddress().getZipcode();

            binding.txtAddress.setText(fullAddress);
        }
        // company info
        if (user.getCompany() != null){
            binding.txtCompanyName.setText(user.getCompany().getName());

        }



    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home){
            finish();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
