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

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.quicknotes.Adapter.CategorySpinnerAdapter;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivityAddNoteBinding;

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
    private int noteId = -1;
    private long createdTime = -1;
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
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
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
        binding.imgFont.setOnClickListener(v -> Toast.makeText(this, "Font Style", Toast.LENGTH_SHORT).show());
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
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.add_note_menu, menu);
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
        }
        else if (id == R.id.note_checks) {
            Toast.makeText(this, "Checked Note", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.note_share) {
            Toast.makeText(this, "Share Note", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.note_pdf){
            Toast.makeText(this, "PDF", Toast.LENGTH_SHORT).show();
        }
        else if (id == R.id.note_reminder){
            saveNote(true);
            Intent intent = new Intent(this, ReminderActivity.class);
            intent.putExtra("note_id", noteId);
            startActivity(intent);
        }
        else if (id == R.id.note_info){
            Toast.makeText(this, "Note info", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateNote() {
        String title = binding.edtTitle.getText().toString().trim();
        String description = binding.edtContent.getText().toString().trim();

        if (title.isEmpty() && description.isEmpty()) {
            return;
        }

        String finalTitle = title.isEmpty() ? "Untitled Note" : title;
        long timestamp = System.currentTimeMillis();

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
        note.setId(noteId);
        note.setCompleted(isCompleted);
        noteViewModel.update(note);
        Toast.makeText(this, "Note Updated successfully", Toast.LENGTH_SHORT).show();
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
            noteId = (int) noteViewModel.insert(note);
            if (!isSilent) {
                Toast.makeText(this, "Note Saved", Toast.LENGTH_SHORT).show();
            }
        } else {
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
            note.setId(noteId);
            note.setCompleted(isCompleted);
            noteViewModel.update(note);
            if (!isSilent) {
                Toast.makeText(this, "Note Updated", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveNote(true);
    }
}