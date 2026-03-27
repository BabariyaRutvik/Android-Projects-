package com.example.interviewace.fragment;

import android.Manifest;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.interviewace.R;
import com.example.interviewace.ViewModel.InterviewViewModel;
import com.example.interviewace.databinding.FragmentCertificateBinding;
import com.example.interviewace.model.SessionItem;
import com.google.firebase.auth.FirebaseAuth;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class CertificateFragment extends Fragment {

    private static final String TAG = "CertificateFragment";
    private static final int PERMISSION_REQUEST_CODE = 100;
    
    private FragmentCertificateBinding binding;
    private InterviewViewModel viewModel;

    public CertificateFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCertificateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewModel = new ViewModelProvider(this).get(InterviewViewModel.class);
        LoadLatestSessions();
        SetUpClicks();
    }

    private void LoadLatestSessions() {
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId != null) {
            viewModel.getSessions().observe(getViewLifecycleOwner(), sessions -> {
                if (sessions != null && !sessions.isEmpty()) {
                    UpdateUI(sessions.get(0));
                }
            });
            viewModel.loadSessions(userId);
        }
    }

    private void UpdateUI(SessionItem session) {
        binding.tvRoleName.setText(session.getRoleName());
        binding.tvScoreBadge.setText(getString(R.string.score_format, session.getScore()));
        binding.tvDate.setText(session.getDate());

        if (session.getScore() >= 90){
            binding.tvTierBadge.setText("PLATINUM");
        }


        else if (session.getScore() >= 75) {

            binding.tvTierBadge.setText("GOLD");
        }
        else {
            binding.tvTierBadge.setText("SILVER");
        }

        // Set User Name from Firebase Auth
        String displayName = FirebaseAuth.getInstance().getCurrentUser() != null ?
                FirebaseAuth.getInstance().getCurrentUser().getDisplayName() : "User";
        
        if (displayName == null || displayName.isEmpty()) {
            displayName = "User";
        }
        binding.tvUserName.setText(displayName);
    }

    private void SetUpClicks() {
        binding.btnDownload.setOnClickListener(v -> checkPermissionAndProceed());
        binding.btnShare.setOnClickListener(v -> ShareToLinkedIn());
    }

    private void checkPermissionAndProceed() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
                return;
            }
        }
        GenerateAndSavePDF();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                GenerateAndSavePDF();
            } else {
                Toast.makeText(getContext(), "Permission denied. Cannot download PDF.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void GenerateAndSavePDF() {
        Toast.makeText(getContext(), "Generating PDF...", Toast.LENGTH_SHORT).show();
        byte[] pdfBytes = generatePDFBytes();
        if (pdfBytes != null) {
            SavePDFTODownloads(pdfBytes);
        }
    }

    private byte[] generatePDFBytes() {
        if (binding.layoutCertificateCard.getWidth() <= 0) {
            Toast.makeText(getContext(), "UI not ready yet", Toast.LENGTH_SHORT).show();
            return null;
        }

        Bitmap bitmap = createBitmapFromView(binding.layoutCertificateCard);
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(bitmap.getWidth(), bitmap.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        canvas.drawBitmap(bitmap, 0, 0, null);
        document.finishPage(page);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            document.writeTo(outputStream);
            document.close();
            return outputStream.toByteArray();
        } catch (IOException e) {
            Log.e(TAG, "PDF Error", e);
            Toast.makeText(getContext(), "PDF Generation Failed", Toast.LENGTH_SHORT).show();
            return null;
        }
    }

    private void SavePDFTODownloads(byte[] pdfBytes) {
        String fileName = "InterviewAce_Certificate_" + System.currentTimeMillis() + ".pdf";
        
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentValues values = new ContentValues();
                values.put(MediaStore.Downloads.DISPLAY_NAME, fileName);
                values.put(MediaStore.Downloads.MIME_TYPE, "application/pdf");
                values.put(MediaStore.Downloads.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);

                ContentResolver resolver = requireContext().getContentResolver();
                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values);

                if (uri != null) {
                    try (OutputStream os = resolver.openOutputStream(uri)) {
                        if (os != null) {
                            os.write(pdfBytes);
                            Toast.makeText(getContext(), "PDF Saved to Downloads folder", Toast.LENGTH_LONG).show();
                        }
                    }
                }
            } else {
                File downloadDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                if (!downloadDir.exists()) downloadDir.mkdirs();
                File file = new File(downloadDir, fileName);
                try (FileOutputStream fos = new FileOutputStream(file)) {
                    fos.write(pdfBytes);
                    Toast.makeText(getContext(), "PDF Saved to: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Save Error", e);
            Toast.makeText(getContext(), "Error saving PDF: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap createBitmapFromView(View view) {
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);
        return bitmap;
    }

    private void ShareToLinkedIn() {
        // Convert view to Bitmap for sharing as image
        Bitmap bitmap = createBitmapFromView(binding.layoutCertificateCard);
        
        // Save to gallery temporarily to get a URI
        String path = MediaStore.Images.Media.insertImage(requireContext().getContentResolver(), bitmap, "Certificate", null);
        if (path == null) {
            Toast.makeText(getContext(), "Failed to create share image", Toast.LENGTH_SHORT).show();
            return;
        }
        Uri uri = Uri.parse(path);

        Intent shareIntent = new Intent(Intent.ACTION_SEND);
        shareIntent.setType("image/*");
        shareIntent.putExtra(Intent.EXTRA_STREAM, uri);
        String shareBody = "I just earned a certificate for " + binding.tvRoleName.getText() + " on InterviewAce!";
        shareIntent.putExtra(Intent.EXTRA_TEXT, shareBody);
        shareIntent.setPackage("com.linkedin.android");

        try {
            startActivity(shareIntent);
        } catch (Exception e) {
            shareIntent.setPackage(null);
            startActivity(Intent.createChooser(shareIntent, "Share Certificate"));
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
