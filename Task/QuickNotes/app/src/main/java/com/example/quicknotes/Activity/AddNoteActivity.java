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
import androidx.appcompat.widget.AppCompatTextView;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import com.example.quicknotes.Adapter.CategorySpinnerAdapter;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivityAddNoteBinding;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class AddNoteActivity extends AppCompatActivity {

    private ActivityAddNoteBinding binding;
    private NoteViewModel noteViewModel;
    private String selectedCategory = "All"; // Default
    private String selectedColor = "#1A73E8";
    private boolean isPinned = false;
    private boolean isCompleted = false;
    private boolean isLocked = false;
    private boolean isArchived = false;
    private boolean isDeleted = false;
    private int noteId = -1;
    private long createdTime = -1;
    private int currentSearchIndex = -1;
    private List<Integer> searchMatches = new java.util.ArrayList<>();
    private final List<String> categories = Arrays.asList(
            "All", "Personal", "Work", "Others",
            "Untitled_Red", "Untitled_Orange", "Untitled_Pink",
            "Untitled_Purple", "Untitled_DarkGray", "Untitled_Gray"
    );

    @Override
    protected void attachBaseContext(Context newBase) {
        Context langContext = LanguageHelper.onAttach(newBase);
        Context finalContext = FontSizeHelper.onAttach(langContext);

        super.attachBaseContext(finalContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityAddNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, Math.max(systemBars.bottom, ime.bottom));
            return insets;
        });

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        binding.toolbarAddNote.setOverflowIcon(ContextCompat.getDrawable(this, R.drawable.ic_more));
        setSupportActionBar(binding.toolbarAddNote);
        getSupportActionBar().setDisplayShowTitleEnabled(false);
        binding.toolbarAddNote.setNavigationOnClickListener(v -> {
            saveNote(true);
            finish();
        });

        initViews();
        setClickListeners();
        setSearchListeners();
        updateDateTime();

        if (getIntent().hasExtra("category")) {
            selectedCategory = getIntent().getStringExtra("category");
            updateCategoryUI(selectedCategory);
            int index = categories.indexOf(selectedCategory);
            if (index != -1) binding.categorySpinner.setSelection(index);
        }

        if (getIntent().hasExtra("note_id")) {
            noteId = getIntent().getIntExtra("note_id", -1);
            loadNoteData(noteId);
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
                saveNote(true);
                finish();
            }
        });
    }

    private void loadNoteData(int id) {
        Note note = noteViewModel.getNoteById(id);
        if (note != null) {
            binding.edtTitle.setText(note.getTitle());
            binding.edtContent.setText(note.getDescription());
            selectedCategory = note.getCategory();
            
            int index = categories.indexOf(selectedCategory);
            if (index != -1) binding.categorySpinner.setSelection(index);
            
            selectedColor = note.getNoteColor();
            isPinned = note.isPinned();
            isCompleted = note.isCompleted();
            isLocked = note.isLocked();
            isArchived = note.isArchived();
            isDeleted = note.isDeleted();
            createdTime = note.getCreatedTime();

            if (selectedColor != null && !selectedColor.isEmpty()) {
                binding.main.setBackgroundColor(Color.parseColor(selectedColor));
            }

            invalidateOptionsMenu(); // Refresh pin icon
            updateCategoryUI(selectedCategory);
            updateReminderUI();
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

    private void initViews() {
        CategorySpinnerAdapter adapter = new CategorySpinnerAdapter(this, categories);
        binding.categorySpinner.setAdapter(adapter);

        int index = categories.indexOf(selectedCategory);
        if (index != -1) binding.categorySpinner.setSelection(index);
        
        updateCategoryUI(selectedCategory);
    }

    private void setClickListeners() {
        binding.categorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                selectedCategory = categories.get(position);
                updateCategoryUI(selectedCategory);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        binding.imgDone.setOnClickListener(v -> {
            if (noteId == -1) {
                saveNote(false); // Save new note
            } else {
                updateNote(); // Explicitly update existing note
            }
            finish();
        });

        binding.imgReminder.setOnClickListener(v -> {
            saveNote(true);
            Intent intent = new Intent(this, ReminderActivity.class);
            intent.putExtra("note_id", noteId);
            startActivity(intent);
        });
        binding.imgPalette.setOnClickListener(v -> Toast.makeText(this, "Change Background", Toast.LENGTH_SHORT).show());

        binding.imgAdd.setOnClickListener(v -> Toast.makeText(this, "Add Media", Toast.LENGTH_SHORT).show());
        binding.imgFont.setOnClickListener(v -> {
            showFormatDialog();
            // Ensure keyboard stays/opens
            binding.edtContent.requestFocus();
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.showSoftInput(binding.edtContent, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
            }
        });
        binding.imgUndo.setOnClickListener(v -> Toast.makeText(this, "Undo", Toast.LENGTH_SHORT).show());
        binding.imgRedo.setOnClickListener(v -> Toast.makeText(this, "Redo", Toast.LENGTH_SHORT).show());

        binding.imgCheckReminder.setOnClickListener(v -> {
            isCompleted = !isCompleted;
            updateReminderUI();
            saveNote(true);
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
                binding.edtContent.setPaintFlags(binding.edtContent.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                binding.txtReminderDateTime.setPaintFlags(binding.txtReminderDateTime.getPaintFlags() | android.graphics.Paint.STRIKE_THRU_TEXT_FLAG);
                binding.txtReminderDateTime.setAlpha(0.5f);
                binding.imgCheckReminder.setImageResource(R.drawable.ic_done); // Or a checked icon if you have one
                binding.layoutRemindedBar.setAlpha(0.8f);
            } else {
                binding.edtTitle.setPaintFlags(binding.edtTitle.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
                binding.edtContent.setPaintFlags(binding.edtContent.getPaintFlags() & (~android.graphics.Paint.STRIKE_THRU_TEXT_FLAG));
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
        binding.textEsitedTime.setText(currentTime);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean isNewNote = (noteId == -1);

        MenuItem archiveItem = menu.findItem(R.id.note_archive);
        MenuItem widgetItem = menu.findItem(R.id.note_widget);
        MenuItem deleteItem = menu.findItem(R.id.note_delete);
        MenuItem lockItem = menu.findItem(R.id.note_lock);

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

        // Tint icons gray for the popup menu
        int grayColor = ContextCompat.getColor(this, R.color.gray_icon);
        MenuItem moreItem = menu.findItem(R.id.menu_more);
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
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
        
        if (menu instanceof MenuBuilder) {
            ((MenuBuilder) menu).setOptionalIconsVisible(true);
        }

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
            Toast.makeText(this, isPinned ? "Note Pinned" : "Note Unpinned", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.note_checks) {
            isCompleted = !isCompleted;
            updateReminderUI();
            saveNote(true);
            Toast.makeText(this, isCompleted ? "Note Checked" : "Note Unchecked", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.note_archive) {
            isArchived = true;
            isDeleted = false;
            saveNote(true);
            Toast.makeText(this, "Note Archived", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        } else if (id == R.id.note_share) {
            showShareAsDialog();
            return true;
        } else if (id == R.id.note_pdf) {
            shareNoteAsPDF();
            return true;
        } else if (id == R.id.note_widget) {
            Toast.makeText(this, "Add widget", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.note_reminder) {
            saveNote(true);
            Intent intent = new Intent(this, ReminderActivity.class);
            intent.putExtra("note_id", noteId);
            startActivity(intent);
            return true;
        } else if (id == R.id.note_delete) {
            isDeleted = true;
            isArchived = false;
            saveNote(true);
            Toast.makeText(this, "Moved to Recycle Bin", Toast.LENGTH_SHORT).show();
            finish();
            return true;
        }
else if (id == R.id.note_lock) {
            handleLockUnlock();
            return true;
        } else if (id == R.id.note_search) {
            showSearchDialog();
            return true;
        } else if (id == R.id.note_info) {
            showNoteInfoDialog();
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
            // Logic for unlocking if needed, or just toggle for now if it's already verified to enter
            isLocked = false;
            saveNote(true);
            invalidateOptionsMenu();
            Toast.makeText(this, "Note Unlocked", Toast.LENGTH_SHORT).show();
        } else {
            isLocked = true;
            saveNote(true);
            invalidateOptionsMenu();
            Toast.makeText(this, "Note Locked", Toast.LENGTH_SHORT).show();
        }
    }

    private void updateNote() {
        saveNote(false);
    }

    private void saveNote(boolean isSilent) {
        String title = binding.edtTitle.getText().toString().trim();
        String description = binding.edtContent.getText().toString().trim();

        if (title.isEmpty() && description.isEmpty()) {
            return;
        }

        String finalTitle = title.isEmpty() ? "Untitled Note" : title;
        long timestamp = System.currentTimeMillis();

        if (noteId == -1) {
            createdTime = timestamp;
            Note note = new Note(
                    finalTitle,
                    description,
                    "TEXT",
                    selectedCategory,
                    createdTime,
                    timestamp,
                    0,
                    selectedColor,
                    isPinned
            );
            note.setCompleted(isCompleted);
            note.setLocked(isLocked);
            note.setArchived(isArchived);
            note.setDeleted(isDeleted);
            noteId = (int) noteViewModel.insert(note);
            if (!isSilent) {
                Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
            }
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
                note.setLocked(isLocked);
                note.setArchived(isArchived);
                note.setDeleted(isDeleted);

                noteViewModel.update(note);
                if (!isSilent) {
                    Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();
                }
            }
        }
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
        binding.toolbarAddNote.setVisibility(View.GONE);
        binding.bottomToolbar.setVisibility(View.GONE);
        binding.imgReminder.setVisibility(View.GONE);

        View view = binding.main;
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(view.getWidth(), view.getHeight(), 1).create();
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        view.draw(canvas);

        document.finishPage(page);

        binding.toolbarAddNote.setVisibility(View.VISIBLE);
        binding.bottomToolbar.setVisibility(View.VISIBLE);
        binding.imgReminder.setVisibility(View.VISIBLE);

        try {
            File cachePath = new File(getCacheDir(), "shared_files");
            cachePath.mkdirs();
            File file = new File(cachePath, "note.pdf");
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
        // Hide UI elements that shouldn't be in the image
        binding.toolbarAddNote.setVisibility(View.GONE);
        binding.bottomToolbar.setVisibility(View.GONE);
        binding.imgReminder.setVisibility(View.GONE);

        // Capture
        View view = binding.main;
        Bitmap bitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        view.draw(canvas);

        // Restore UI
        binding.toolbarAddNote.setVisibility(View.VISIBLE);
        binding.bottomToolbar.setVisibility(View.VISIBLE);
        binding.imgReminder.setVisibility(View.VISIBLE);

        try {
            File cachePath = new File(getCacheDir(), "shared_images");
            cachePath.mkdirs();
            File file = new File(cachePath, "note_image.png");
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

    private void showNoteInfoDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_note_info, null);
        AlertDialog dialog = new MaterialAlertDialogBuilder(this)
                .setView(dialogView)
                .setBackground(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
                .create();

        AppCompatTextView txtCategory = dialogView.findViewById(R.id.txtInfoCategory);
        AppCompatTextView txtCreated = dialogView.findViewById(R.id.txtInfoCreated);
        AppCompatTextView txtModified = dialogView.findViewById(R.id.txtInfoModified);
        AppCompatTextView txtCharacters = dialogView.findViewById(R.id.txtInfoCharacters);
        AppCompatTextView txtWords = dialogView.findViewById(R.id.txtInfoWords);
        View btnClose = dialogView.findViewById(R.id.btnClose);

        if (btnClose != null) {
            btnClose.setOnClickListener(v -> dialog.dismiss());
        }

        String title = binding.edtTitle.getText().toString();
        String content = binding.edtContent.getText().toString();
        String fullText = title + content;

        int charCount = fullText.length();
        int wordCount = fullText.trim().isEmpty() ? 0 : fullText.trim().split("\\s+").length;

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd h:mm:ss a", Locale.getDefault());
        
        txtCategory.setText(selectedCategory);
        txtCreated.setText(createdTime == -1 ? "Just now" : sdf.format(new Date(createdTime)));
        
        Note note = noteViewModel.getNoteById(noteId);
        long modTime = (note != null) ? note.getModifiedTime() : System.currentTimeMillis();
        txtModified.setText(sdf.format(new Date(modTime)));
        
        txtCharacters.setText(String.valueOf(charCount));
        txtWords.setText(String.valueOf(wordCount));

        dialog.show();
    }

    private void setSearchListeners() {
        binding.btnSearchClose.setOnClickListener(v -> {
            binding.layoutSearch.setVisibility(View.GONE);
            binding.bottomToolbar.setVisibility(View.VISIBLE);
            binding.edtSearchQuery.setText("");
            clearHighlight();
            // Hide keyboard
            android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(binding.edtSearchQuery.getWindowToken(), 0);
        });

        binding.edtSearchQuery.addTextChangedListener(new android.text.TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performSearch(s.toString());
            }

            @Override
            public void afterTextChanged(android.text.Editable s) {}
        });

        binding.btnSearchUp.setOnClickListener(v -> navigateSearch(-1));
        binding.btnSearchDown.setOnClickListener(v -> navigateSearch(1));
    }

    private void performSearch(String query) {
        searchMatches.clear();
        currentSearchIndex = -1;
        
        if (query.isEmpty()) {
            clearHighlight();
            binding.txtSearchCount.setText("0/0");
            return;
        }

        String content = binding.edtContent.getText().toString();
        String lowerContent = content.toLowerCase();
        String lowerQuery = query.toLowerCase();

        int index = lowerContent.indexOf(lowerQuery);
        while (index >= 0) {
            searchMatches.add(index);
            index = lowerContent.indexOf(lowerQuery, index + lowerQuery.length());
        }

        if (!searchMatches.isEmpty()) {
            currentSearchIndex = 0;
            highlightSearch(query);
            updateSearchCount();
        } else {
            clearHighlight();
            binding.txtSearchCount.setText("0/0");
        }
    }

    private void navigateSearch(int direction) {
        if (searchMatches.isEmpty()) return;

        currentSearchIndex += direction;
        if (currentSearchIndex < 0) currentSearchIndex = searchMatches.size() - 1;
        else if (currentSearchIndex >= searchMatches.size()) currentSearchIndex = 0;

        highlightSearch(binding.edtSearchQuery.getText().toString());
        updateSearchCount();
        
        // Scroll to the current match if it was a TextView or similar, 
        // but since it's an EditText, we can use selection to bring it into view
        binding.edtContent.setSelection(searchMatches.get(currentSearchIndex));
    }

    private void updateSearchCount() {
        binding.txtSearchCount.setText((currentSearchIndex + 1) + "/" + searchMatches.size());
    }

    private void highlightSearch(String query) {
        String content = binding.edtContent.getText().toString();
        android.text.SpannableString spannableString = new android.text.SpannableString(content);
        
        for (int i = 0; i < searchMatches.size(); i++) {
            int start = searchMatches.get(i);
            int end = start + query.length();
            int color = (i == currentSearchIndex) ? Color.parseColor("#FFA500") : Color.YELLOW; // Orange for current, Yellow for others
            spannableString.setSpan(new android.text.style.BackgroundColorSpan(color),
                    start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
        binding.edtContent.setText(spannableString);
    }

    private void showSearchDialog() {
        binding.layoutSearch.setVisibility(View.VISIBLE);
        binding.bottomToolbar.setVisibility(View.GONE);
        binding.edtSearchQuery.requestFocus();
        android.view.inputmethod.InputMethodManager imm = (android.view.inputmethod.InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(binding.edtSearchQuery, android.view.inputmethod.InputMethodManager.SHOW_IMPLICIT);
    }

    private void clearHighlight() {
        String content = binding.edtContent.getText().toString();
        binding.edtContent.setText(content);
    }

    private void showFormatDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this, R.style.RoundedBottomSheetDialog);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_format_text, null);
        bottomSheetDialog.setContentView(dialogView);

        // Optional: Remove dim to make it feel more integrated with the editor
        if (bottomSheetDialog.getWindow() != null) {
            bottomSheetDialog.getWindow().clearFlags(android.view.WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        }

        dialogView.findViewById(R.id.btnClose).setOnClickListener(v -> bottomSheetDialog.dismiss());

        dialogView.findViewById(R.id.btnHeading1).setOnClickListener(v -> applyHeadingStyle(1));
        dialogView.findViewById(R.id.btnHeading2).setOnClickListener(v -> applyHeadingStyle(2));
        dialogView.findViewById(R.id.btnHeading3).setOnClickListener(v -> applyHeadingStyle(3));
        dialogView.findViewById(R.id.btnNormalText).setOnClickListener(v -> applyHeadingStyle(0));

        dialogView.findViewById(R.id.btnBold).setOnClickListener(v -> toggleStyle(android.graphics.Typeface.BOLD));
        dialogView.findViewById(R.id.btnItalic).setOnClickListener(v -> toggleStyle(android.graphics.Typeface.ITALIC));
        dialogView.findViewById(R.id.btnUnderline).setOnClickListener(v -> toggleUnderline());

        dialogView.findViewById(R.id.btnBulletList).setOnClickListener(v -> applyListStyle("bullet"));
        dialogView.findViewById(R.id.btnNumberedList).setOnClickListener(v -> applyListStyle("numbered"));

        bottomSheetDialog.show();
    }

    private void applyHeadingStyle(int level) {
        int start = binding.edtContent.getSelectionStart();
        int end = binding.edtContent.getSelectionEnd();
        if (start == -1 || end == -1) return;

        android.text.Editable spannable = binding.edtContent.getText();
        String text = spannable.toString();

        // Expand selection to full lines
        int lineStart = text.lastIndexOf('\n', start - 1) + 1;
        int lineEnd = text.indexOf('\n', end);
        if (lineEnd == -1) lineEnd = text.length();

        // Remove existing size and style spans in this range
        android.text.style.RelativeSizeSpan[] sizeSpans = spannable.getSpans(lineStart, lineEnd, android.text.style.RelativeSizeSpan.class);
        for (android.text.style.RelativeSizeSpan span : sizeSpans) spannable.removeSpan(span);
        
        android.text.style.StyleSpan[] styleSpans = spannable.getSpans(lineStart, lineEnd, android.text.style.StyleSpan.class);
        for (android.text.style.StyleSpan span : styleSpans) {
            if (span.getStyle() == android.graphics.Typeface.BOLD) spannable.removeSpan(span);
        }

        float size;
        switch (level) {
            case 1: size = 2.0f; break; // Heading 1 (e.g. 22sp -> 44sp)
            case 2: size = 1.6f; break; // Heading 2 (e.g. 22sp -> 35sp)
            case 3: size = 1.3f; break; // Heading 3 (e.g. 22sp -> 28sp)
            default: size = 1.0f; break; // TXT (Normal)
        }

        if (size > 1.0f) {
            spannable.setSpan(new android.text.style.RelativeSizeSpan(size), lineStart, lineEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            spannable.setSpan(new android.text.style.StyleSpan(android.graphics.Typeface.BOLD), lineStart, lineEnd, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void toggleStyle(int typeface) {
        int start = binding.edtContent.getSelectionStart();
        int end = binding.edtContent.getSelectionEnd();
        if (start == -1 || end == -1 || start == end) return;

        android.text.Editable spannable = binding.edtContent.getText();
        android.text.style.StyleSpan[] spans = spannable.getSpans(start, end, android.text.style.StyleSpan.class);
        
        boolean exists = false;
        for (android.text.style.StyleSpan span : spans) {
            if (span.getStyle() == typeface) {
                spannable.removeSpan(span);
                exists = true;
            }
        }

        if (!exists) {
            spannable.setSpan(new android.text.style.StyleSpan(typeface), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void toggleUnderline() {
        int start = binding.edtContent.getSelectionStart();
        int end = binding.edtContent.getSelectionEnd();
        if (start == -1 || end == -1 || start == end) return;

        android.text.Editable spannable = binding.edtContent.getText();
        android.text.style.UnderlineSpan[] spans = spannable.getSpans(start, end, android.text.style.UnderlineSpan.class);
        
        if (spans.length > 0) {
            for (android.text.style.UnderlineSpan span : spans) spannable.removeSpan(span);
        } else {
            spannable.setSpan(new android.text.style.UnderlineSpan(), start, end, android.text.Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
    }

    private void applyListStyle(String type) {
        int start = binding.edtContent.getSelectionStart();
        int end = binding.edtContent.getSelectionEnd();
        if (start == -1) return;

        android.text.Editable spannable = binding.edtContent.getText();
        String currentText = spannable.toString();
        
        // Simple list implementation: prepend to the current line
        int lineStart = currentText.lastIndexOf('\n', start - 1) + 1;
        if (lineStart < 0) lineStart = 0;
        
        String prefix = type.equals("bullet") ? "• " : "1. ";
        spannable.insert(lineStart, prefix);
    }

    private void shareNoteAsText() {
        String title = binding.edtTitle.getText().toString();
        String content = binding.edtContent.getText().toString();
        String shareBody = title + "\n\n" + content;

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNote(true);
    }
}