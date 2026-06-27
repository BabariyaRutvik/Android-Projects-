package com.example.quicknotes.Activity;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.view.LayoutInflater;
import android.annotation.SuppressLint;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.annotation.NonNull;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quicknotes.Adapter.CategorySpinnerAdapter;
import com.example.quicknotes.Adapter.ChecklistAdapter;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.Model.ChecklistItem;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivityAddCheckListBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddCheckListActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(Context newBase) {
        Context langContext = LanguageHelper.onAttach(newBase);
        Context finalContext = FontSizeHelper.onAttach(langContext);

        super.attachBaseContext(finalContext);
    }

    private ActivityAddCheckListBinding binding;
    private NoteViewModel noteViewModel;
    private ChecklistAdapter checklistAdapter;
    private String selectedCategory = "Untitled_Purple";
    private String selectedColor = "#FFFFFF";
    private boolean isPinned = false;
    private boolean isCompleted = false;
    private boolean isArchived = false;
    private boolean isDeleted = false;
    private boolean isLocked = false;
    private int noteId = -1;
    private long createdTime = -1;
    private final List<String> categories = Arrays.asList(
            "All", "Personal", "Work", "Others",
            "Untitled_Red", "Untitled_Orange", "Untitled_Pink",
            "Untitled_Purple", "Untitled_DarkGray", "Untitled_Gray"
    );


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddCheckListBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        binding.toolbarAddChecklist.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more));

        setSupportActionBar(binding.toolbarAddChecklist);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbarAddChecklist.setNavigationOnClickListener(v -> {
            saveChecklist(true);
            finish();
        });

        initViews();
        setClickListeners();
        updateDateTime();

        if (getIntent().hasExtra("category")) {
            selectedCategory = getIntent().getStringExtra("category");
            updateCategoryUI(selectedCategory);
            int index = categories.indexOf(selectedCategory);
            if (index != -1) binding.spinnerCategory.setSelection(index);
        }

        // Check for existing note
        if (getIntent().hasExtra("note_id")) {
            noteId = getIntent().getIntExtra("note_id", -1);
            loadChecklistData(noteId);
        } else {
            // New checklist starts with one empty item
            List<ChecklistItem> items = new ArrayList<>();
            items.add(new ChecklistItem("", false));
            setupRecyclerView(items);
        }

        String shareType = getIntent().getStringExtra("extra_share_type");
        if (shareType != null) {
            binding.main.post(() -> {
                if (shareType.equals("IMAGE")) {
                    shareNoteAsImage();
                } else if (shareType.equals("PDF")) {
                    shareNoteAsPDF();
                }
            });
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                saveChecklist(true);
                finish();
            }
        });
    }

    private void initViews() {
        CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(this, categories);
        binding.spinnerCategory.setAdapter(adapter);
        
        int index = categories.indexOf(selectedCategory);
        if (index != -1) binding.spinnerCategory.setSelection(index);
        
        updateCategoryUI(selectedCategory);
    }

    private void setupRecyclerView(List<ChecklistItem> items) {
        checklistAdapter = new ChecklistAdapter(items);
        binding.rvChecklist.setLayoutManager(new LinearLayoutManager(this));
        binding.rvChecklist.setAdapter(checklistAdapter);
    }

    private void setClickListeners() {
        binding.spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                updateCategoryUI(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.imgDone.setOnClickListener(v -> {
            saveChecklist(false);
            finish();
        });

        binding.imgReminder.setOnClickListener(v -> {
            saveChecklist(true);
            Intent intent = new Intent(this, ReminderActivity.class);
            intent.putExtra("note_id", noteId);
            startActivity(intent);
        });

        binding.imgCheckReminder.setOnClickListener(v -> {
            isCompleted = !isCompleted;
            updateReminderUI();
            saveChecklist(true);
        });
    }

    private void updateReminderUI() {
        Note note = noteViewModel.getNoteById(noteId);
        if (note == null) return;

        if (note.getReminderTime() > 0) {
            binding.cardReminderInfo.setVisibility(View.VISIBLE);
            binding.layoutRemindedBar.setVisibility(View.VISIBLE);

            SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm a", Locale.getDefault());
            String dateTimeStr = sdf.format(new Date(note.getReminderTime()));

            binding.txtReminderDateTime.setText(dateTimeStr);
            binding.txtRemindedInfo.setText("Reminded: " + dateTimeStr);

            if (isCompleted) {
                binding.edtTitle.setPaintFlags(binding.edtTitle.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                binding.txtReminderDateTime.setPaintFlags(binding.txtReminderDateTime.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                binding.txtReminderDateTime.setAlpha(0.5f);
                binding.imgCheckReminder.setImageResource(R.drawable.ic_done);
                binding.layoutRemindedBar.setAlpha(0.8f);
            } else {
                binding.edtTitle.setPaintFlags(binding.edtTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                binding.txtReminderDateTime.setPaintFlags(binding.txtReminderDateTime.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                binding.txtReminderDateTime.setAlpha(1.0f);
                binding.imgCheckReminder.setImageResource(R.drawable.ic_done);
                binding.layoutRemindedBar.setAlpha(1.0f);
            }

            if (note.getRepeatType() != null && !note.getRepeatType().equals("None")) {
                binding.imgRepeatIcon.setVisibility(View.VISIBLE);
            } else {
                binding.imgRepeatIcon.setVisibility(View.GONE);
            }
        } else {
            binding.cardReminderInfo.setVisibility(View.GONE);
            binding.layoutRemindedBar.setVisibility(View.GONE);
        }
    }

    private void updateDateTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("MM-dd hh:mm a", Locale.getDefault());
        String currentTime = "Edited : " + sdf.format(new Date());
        binding.textEditedCheck.setText(currentTime);
    }

    private void loadChecklistData(int id) {
        Note note = noteViewModel.getNoteById(id);
        if (note != null) {
            binding.edtTitle.setText(note.getTitle());
            selectedCategory = note.getCategory();
            
            int index = categories.indexOf(selectedCategory);
            if (index != -1) binding.spinnerCategory.setSelection(index);
            
            selectedColor = note.getNoteColor();
            isPinned = note.isPinned();
            isCompleted = note.isCompleted();
            isArchived = note.isArchived();
            isDeleted = note.isDeleted();
            isLocked = note.isLocked();
            createdTime = note.getCreatedTime();

            if (selectedColor != null && !selectedColor.isEmpty()) {
                binding.main.setBackgroundColor(Color.parseColor(selectedColor));
            }

            invalidateOptionsMenu(); // Refresh pin icon
            updateCategoryUI(selectedCategory);
            updateReminderUI();

            // Deserialize checklist items
            List<ChecklistItem> items = deserializeChecklist(note.getDescription());
            if (items.isEmpty()) {
                items.add(new ChecklistItem("", false));
            }
            setupRecyclerView(items);
        }
    }

    private void updateCategoryUI(String category) {
        int bgColor;
        switch (category) {
            case "All":
                bgColor = ContextCompat.getColor(this, R.color.light_sky_blue);
                break;
            case "Personal":
                bgColor = ContextCompat.getColor(this, R.color.badge_personal_bg);
                break;
            case "Work":
                bgColor = ContextCompat.getColor(this, R.color.badge_work_bg);
                break;
            case "Others":
                bgColor = ContextCompat.getColor(this, R.color.badge_others_bg);
                break;
            case "Untitled_Red":
                bgColor = ContextCompat.getColor(this, R.color.badge_untitled_red_bg);
                break;
            case "Untitled_Orange":
                bgColor = ContextCompat.getColor(this, R.color.badge_untitled_orange_bg);
                break;
            case "Untitled_Pink":
                bgColor = ContextCompat.getColor(this, R.color.badge_untitled_pink_bg);
                break;
            case "Untitled_Purple":
                bgColor = ContextCompat.getColor(this, R.color.badge_untitled_purple_bg);
                break;
            case "Untitled_DarkGray":
                bgColor = ContextCompat.getColor(this, R.color.badge_untitled_dark_gray_bg);
                break;
            case "Untitled_Gray":
                bgColor = ContextCompat.getColor(this, R.color.badge_untitled_gray_bg);
                break;
            default:
                bgColor = ContextCompat.getColor(this, R.color.badge_untitled_purple_bg);
                break;
        }
        binding.main.setBackgroundColor(bgColor);
        selectedColor = String.format("#%06X", (0xFFFFFF & bgColor));
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isNewNote = (noteId == -1);

        MenuItem archiveItem = menu.findItem(R.id.check_archive);
        MenuItem widgetItem = menu.findItem(R.id.check_widget);
        MenuItem deleteItem = menu.findItem(R.id.check_delete);
        MenuItem lockItem = menu.findItem(R.id.check_lock);

        if (isNewNote) {
            if (archiveItem != null) archiveItem.setVisible(false);
            if (widgetItem != null) widgetItem.setVisible(false);
            if (deleteItem != null) deleteItem.setVisible(false);
            if (lockItem != null) lockItem.setVisible(false);
        } else {
            if (archiveItem != null) archiveItem.setVisible(true);
            if (widgetItem != null) widgetItem.setVisible(true);
            if (deleteItem != null) deleteItem.setVisible(true);
            if (lockItem != null) {
                lockItem.setVisible(true);
                lockItem.setTitle(isLocked ? "Unlock" : "Lock");
            }
        }

        // Tint icons
        int grayColor = ContextCompat.getColor(this, R.color.gray_icon);
        MenuItem moreItem = menu.findItem(R.id.check_more);
        if (moreItem != null) {
            Menu subMenu = moreItem.getSubMenu();
            if (subMenu != null) {
                for (int i = 0; i < subMenu.size(); i++) {
                    MenuItem subItem = subMenu.getItem(i);
                    if (subItem != null && subItem.getIcon() != null) {
                        subItem.getIcon().setTint(grayColor);
                    }
                }
            }
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @SuppressLint("RestrictedApi")
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_checklist_menu, menu);

        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }

        // Update pin icon
        MenuItem pinItem = menu.findItem(R.id.menu_pin);
        if (pinItem != null) {
            pinItem.setIcon(isPinned ? R.drawable.ic_pin_filled : R.drawable.ic_pin);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_pin) {
            isPinned = !isPinned;
            item.setIcon(isPinned ? R.drawable.ic_pin_filled : R.drawable.ic_pin);
            Toast.makeText(this, isPinned ? "Checklist Pinned" : "Checklist Unpinned", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.check_share_top) {
            showShareAsDialog();
            return true;
        } else if (id == R.id.check_checkboxes){
            isCompleted = !isCompleted;
            updateReminderUI();
            saveChecklist(true);
            Toast.makeText(this, isCompleted ? "Checklist Checked" : "Checklist Unchecked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.check_archive) {
            isArchived = true;
            isDeleted = false;
            saveChecklist(true);
            Toast.makeText(this, "Checklist Archived", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (id == R.id.check_pdf){
            shareNoteAsPDF();
            return true;
        } else if (id == R.id.check_widget) {
            Toast.makeText(this, "Add widget", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.check_reminder){
            saveChecklist(true);
            Intent intent = new Intent(this, ReminderActivity.class);
            intent.putExtra("note_id", noteId);
            startActivity(intent);
            return true;
        } else if (id == R.id.check_delete) {
            isDeleted = true;
            isArchived = false;
            saveChecklist(true);
            Toast.makeText(this, "Moved to Recycle Bin", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (id == R.id.check_lock) {
            handleLockUnlock();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void handleLockUnlock() {
        android.content.SharedPreferences prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("is_enabled", false)) {
            Toast.makeText(this, R.string.set_password_first, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLocked) {
            isLocked = false;
            saveChecklist(true);
            invalidateOptionsMenu();
            Toast.makeText(this, "Checklist Unlocked", Toast.LENGTH_SHORT).show();
        } else {
            isLocked = true;
            saveChecklist(true);
            invalidateOptionsMenu();
            Toast.makeText(this, "Checklist Locked", Toast.LENGTH_SHORT).show();
        }
    }

    private void saveChecklist(boolean isSilent) {
        String title = binding.edtTitle.getText().toString().trim();
        List<ChecklistItem> items = checklistAdapter.getItems();

        // Check if anything to save
        boolean hasContent = false;
        for (ChecklistItem item : items) {
            if (!item.getText().trim().isEmpty()) {
                hasContent = true;
                break;
            }
        }

        if (title.isEmpty() && !hasContent) {
            return;
        }

        String finalTitle = title.isEmpty() ? "Untitled Checklist" : title;
        String description = serializeChecklist(items);
        long timestamp = System.currentTimeMillis();

        if (noteId == -1) {
            createdTime = timestamp;
            Note note = new Note(
                    finalTitle,
                    description,
                    "CHECKLIST",
                    selectedCategory,
                    createdTime,
                    timestamp,
                    0,
                    selectedColor,
                    isPinned
            );
            note.setCompleted(isCompleted);
            note.setArchived(isArchived);
            note.setDeleted(isDeleted);
            note.setLocked(isLocked);
            noteId = (int) noteViewModel.insert(note);
            if (!isSilent) Toast.makeText(this, "Checklist Saved", Toast.LENGTH_SHORT).show();
        } else {
            Note note = noteViewModel.getNoteById(noteId);
            if (note != null) {
                note.setTitle(finalTitle);
                note.setDescription(description);
                note.setCategory(selectedCategory);
                note.setModifiedTime(timestamp);
                note.setNoteColor(selectedColor);
                note.setPinned(isPinned);
                note.setCompleted(isCompleted);
                note.setArchived(isArchived);
                note.setDeleted(isDeleted);
                note.setLocked(isLocked);

                noteViewModel.update(note);
                if (!isSilent) Toast.makeText(this, "Checklist Updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String serializeChecklist(List<ChecklistItem> items) {
        StringBuilder sb = new StringBuilder();
        for (ChecklistItem item : items) {
            sb.append(item.isChecked() ? "1" : "0")
              .append("|")
              .append(item.getText())
              .append("\n");
        }
        return sb.toString();
    }

    private List<ChecklistItem> deserializeChecklist(String data) {
        List<ChecklistItem> items = new ArrayList<>();
        if (data == null || data.isEmpty()) return items;

        String[] lines = data.split("\n");
        for (String line : lines) {
            String[] parts = line.split("\\|", 2);
            if (parts.length == 2) {
                items.add(new ChecklistItem(parts[1], parts[0].equals("1")));
            }
        }
        return items;
    }

    private void showShareAsDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_share_as, null);
        AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setBackground(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                .create();

        dialogView.findViewById(R.id.btnShareImage).setOnClickListener(v -> {
            shareNoteAsImage();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnSharePDF).setOnClickListener(v -> {
            shareNoteAsPDF();
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnShareText).setOnClickListener(v -> {
            shareNoteAsText();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void shareNoteAsPDF() {
        binding.toolbarAddChecklist.setVisibility(View.GONE);
        binding.bottomToolbar.setVisibility(View.GONE);
        binding.imgReminder.setVisibility(View.GONE);

        View view = binding.main;
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getWidth(), view.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        view.draw(canvas);

        document.finishPage(page);

        binding.toolbarAddChecklist.setVisibility(View.VISIBLE);
        binding.bottomToolbar.setVisibility(View.VISIBLE);
        binding.imgReminder.setVisibility(View.VISIBLE);

        try {
            File cachePath = new File(getCacheDir(), "shared_files");
            cachePath.mkdirs();
            File file = new File(cachePath, "checklist.pdf");
            FileOutputStream stream = new FileOutputStream(file);
            document.writeTo(stream);
            stream.close();
            document.close();

            Uri contentUri = FileProvider.getUriForFile(this, "com.example.quicknotes.fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("application/pdf");
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to share PDF", Toast.LENGTH_SHORT).show();
            document.close();
        }
    }

    private void shareNoteAsImage() {
        binding.toolbarAddChecklist.setVisibility(View.GONE);
        binding.bottomToolbar.setVisibility(View.GONE);
        binding.imgReminder.setVisibility(View.GONE);

        View view = binding.main;
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        binding.toolbarAddChecklist.setVisibility(View.VISIBLE);
        binding.bottomToolbar.setVisibility(View.VISIBLE);
        binding.imgReminder.setVisibility(View.VISIBLE);

        try {
            File cachePath = new File(getCacheDir(), "shared_images");
            cachePath.mkdirs();
            File file = new File(cachePath, "checklist_image.png");
            FileOutputStream stream = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
            stream.close();

            Uri contentUri = FileProvider.getUriForFile(this, "com.example.quicknotes.fileprovider", file);

            if (contentUri != null) {
                Intent shareIntent = new Intent();
                shareIntent.setAction(Intent.ACTION_SEND);
                shareIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                shareIntent.setDataAndType(contentUri, getContentResolver().getType(contentUri));
                shareIntent.putExtra(Intent.EXTRA_STREAM, contentUri);
                startActivity(Intent.createChooser(shareIntent, getString(R.string.share)));
            }
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to share image", Toast.LENGTH_SHORT).show();
        }
    }

    private void shareNoteAsText() {
        String title = binding.edtTitle.getText().toString();
        StringBuilder sb = new StringBuilder(title).append("\n\n");
        List<ChecklistItem> items = checklistAdapter.getItems();
        for (ChecklistItem item : items) {
            sb.append(item.isChecked() ? "☑ " : "☐ ").append(item.getText()).append("\n");
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, sb.toString());
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveChecklist(true);
    }
}
