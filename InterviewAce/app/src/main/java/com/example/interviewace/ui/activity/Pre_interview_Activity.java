package com.example.interviewace.ui.activity;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.interviewace.R;
import com.example.interviewace.databinding.ActivityPreInterviewBinding;

public class Pre_interview_Activity extends AppCompatActivity {

    ActivityPreInterviewBinding binding;
    private String roleName;
    private String difficulty;
    private String iconName;


    // Permission Launcher for Mic Permission for the user
    private final ActivityResultLauncher<String> micPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted ->{
               if (isGranted){
                   Toast.makeText(this, "Microphone Permission Granted", Toast.LENGTH_SHORT).show();

               }
               else {
                   Toast.makeText(this, "Microphone Permission Denied", Toast.LENGTH_SHORT).show();
               }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPreInterviewBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());


        // first getting data from the PracticeFragment
        getIntentData();
        // setting up data to the UI
        SetDataUI();
        // now setup Click Event
        SetUpClicks();

    }
    private void getIntentData(){
        Intent i = getIntent();

        if (i != null){
            roleName = i.getStringExtra("roleName");
            difficulty = i.getStringExtra("difficulty");
            iconName = i.getStringExtra("iconName");
        }
    }
    private void SetDataUI(){
       // title
        binding.textInterviewTitle.setText(roleName);
        binding.tvRoleName.setText(roleName);


        // Difficulty
        binding.tvLevel.setText(difficulty);

        // Update Question Count and Minutes based on difficulty
        if (difficulty != null) {
            if (difficulty.equalsIgnoreCase("Easy")) {
                binding.tvQuestionCount.setText("10");
                binding.tvMinuteCount.setText("3");
            } else if (difficulty.equalsIgnoreCase("Medium")) {
                binding.tvQuestionCount.setText("15");
                binding.tvMinuteCount.setText("3");
            } else if (difficulty.equalsIgnoreCase("Hard")) {
                binding.tvQuestionCount.setText("20");
                binding.tvMinuteCount.setText("3");
            }
        }

        // Icon Name

        int iconResourceId = getResources()
                .getIdentifier(iconName, "drawable"
                        , getPackageName());

        if (iconResourceId == 0){
            binding.ivRoleIcon.setImageResource(R.drawable.ic_android);
        } else {
            binding.ivRoleIcon.setImageResource(iconResourceId);
        }

    }
    private void SetUpClicks(){

        // back button to move back to the Practice fragment
        binding.imageBackInterview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });
        // allowing mic Permission
        binding.tvAllowAccess.setOnClickListener(v->{
            RequestPermissionNic();
        });
        // starting to the Interview
        binding.btnStart.setOnClickListener(v->{
            if (!isMicPermissionGranted()){
                Toast.makeText(this, "Please allow microphone permission", Toast.LENGTH_SHORT).show();
                return;
            }
            // next Activity to take Interview
            Intent intent = new Intent(this, InterViewActivity.class);
            intent.putExtra("roleName", roleName);
            intent.putExtra("difficulty", difficulty);
            intent.putExtra("iconName", iconName);
            startActivity(intent);

        });
    }
    // Permission
    private void RequestPermissionNic(){
        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);

    }
    private boolean isMicPermissionGranted(){
        return ContextCompat
                .checkSelfPermission
                        (this, Manifest.permission.RECORD_AUDIO) ==
                android.content.pm.PackageManager.PERMISSION_GRANTED;
    }

}
