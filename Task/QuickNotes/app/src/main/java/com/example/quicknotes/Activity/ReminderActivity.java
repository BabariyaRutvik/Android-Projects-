package com.example.quicknotes.Activity;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;

import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.ReminderManager;
import com.example.quicknotes.databinding.ActivityReminderBinding;
import com.example.quicknotes.databinding.DialogCustomRepeatBinding;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class ReminderActivity extends AppCompatActivity {

    ActivityReminderBinding binding;
    private NoteViewModel noteViewModel;
    private Note currentNote;
    private int noteId = -1;

    private final Set<String> selectedDays = new HashSet<>();
    private final Set<String> selectedWeekDays = new HashSet<>();

    private String repeatType = "None";
    private int repeatInterval = 1;
    private String repeatUnit = "Day";
    private long selectedTimeMillis = -1;
    private Calendar calendar = Calendar.getInstance();
    private boolean isChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityReminderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        // Clear seconds and milliseconds for consistent comparison
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (getIntent().hasExtra("note_id")) {
            noteId = getIntent().getIntExtra("note_id", -1);
            loadNoteData();
            
            // Check if opened via Snooze
            if (getIntent().getBooleanExtra("is_snooze", false)) {
                openSnoozeDialogs();
            }
        } else {
            updateDateTimeLabels();
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.main, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowTitleEnabled(true);
        }

        binding.toolbar.setNavigationOnClickListener(v -> handleCancelAction());

        binding.btnClear.setOnClickListener(v -> showClearConfirmationDialog());

        binding.layoutDate.setOnClickListener(v -> {
            int year = calendar.get(Calendar.YEAR);
            int month = calendar.get(Calendar.MONTH);
            int day = calendar.get(Calendar.DAY_OF_MONTH);

            DatePickerDialog datePickerDialog = new DatePickerDialog(ReminderActivity.this,
                    (view, selectedYear, selectedMonth, selectedDay) -> {
                        calendar.set(Calendar.YEAR, selectedYear);
                        calendar.set(Calendar.MONTH, selectedMonth);
                        calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                        
                        updateDateTimeLabels();
                        selectedTimeMillis = calendar.getTimeInMillis();
                        isChanged = true;
                    }, year, month, day);
            datePickerDialog.show();
        });

        binding.layoutTime.setOnClickListener(v -> {
            int hour = calendar.get(Calendar.HOUR_OF_DAY);
            int minute = calendar.get(Calendar.MINUTE);

            TimePickerDialog timePickerDialog = new TimePickerDialog(ReminderActivity.this,
                    (view, selectedHour, selectedMinute) -> {
                        calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                        calendar.set(Calendar.MINUTE, selectedMinute);

                        updateDateTimeLabels();
                        selectedTimeMillis = calendar.getTimeInMillis();
                        isChanged = true;
                    }, hour, minute, false);
            timePickerDialog.show();
        });

        binding.layoutRepeat.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(ReminderActivity.this, binding.layoutRepeat, Gravity.END);
            popupMenu.inflate(R.menu.reminder_menu);
            popupMenu.setOnMenuItemClickListener(item -> {
                int itemId = item.getItemId();
                String oldRepeat = repeatType;
                if (itemId == R.id.repeat_none) {
                    repeatType = "None";
                } else if (itemId == R.id.repeat_daily) {
                    repeatType = "Daily";
                } else if (itemId == R.id.repeat_weekly) {
                    repeatType = "Weekly";
                } else if (itemId == R.id.repeat_monthly) {
                    repeatType = "Monthly";
                } else if (itemId == R.id.repeat_custom) {
                    ShowCustomRepeatDialog();
                    return true;
                }
                if (!repeatType.equals(oldRepeat)) {
                    isChanged = true;
                }
                binding.tvRepeat.setText(repeatType);
                return true;
            });
            popupMenu.show();
        });

        binding.btnCancel.setOnClickListener(v -> handleCancelAction());
        binding.btnSave.setOnClickListener(v -> saveReminder());
    }

    private void handleCancelAction() {
        if (isChanged || selectedTimeMillis != -1) {
            showNoticeDialog();
        } else {
            finish();
        }
    }

    private void showNoticeDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_notice_unsaved, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();
        
        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnYes).setOnClickListener(v -> {
            dialog.dismiss();
            finish();
        });

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void updateDateTimeLabels() {
        SimpleDateFormat dateSdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        binding.tvDate.setText(dateSdf.format(calendar.getTime()));
        
        SimpleDateFormat timeSdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        binding.tvTime.setText(timeSdf.format(calendar.getTime()));
    }

    private void openSnoozeDialogs() {
        // First show DatePicker
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                (view, selectedYear, selectedMonth, selectedDay) -> {
                    calendar.set(Calendar.YEAR, selectedYear);
                    calendar.set(Calendar.MONTH, selectedMonth);
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay);
                    updateDateTimeLabels();
                    
                    // Immediately show TimePicker after DatePicker
                    openSnoozeTimePicker();
                }, year, month, day);
        datePickerDialog.setTitle("Snooze: Select Date");
        datePickerDialog.show();
    }

    private void openSnoozeTimePicker() {
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        TimePickerDialog timePickerDialog = new TimePickerDialog(this,
                (view, selectedHour, selectedMinute) -> {
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour);
                    calendar.set(Calendar.MINUTE, selectedMinute);
                    calendar.set(Calendar.SECOND, 0);
                    
                    updateDateTimeLabels();
                    selectedTimeMillis = calendar.getTimeInMillis();
                    isChanged = true;
                    Toast.makeText(this, "New time set. Click SAVE to confirm.", Toast.LENGTH_SHORT).show();
                }, hour, minute, false);
        timePickerDialog.setTitle("Snooze: Select Time");
        timePickerDialog.show();
    }

    private void loadNoteData() {
        new Thread(() -> {
            currentNote = noteViewModel.getNoteById(noteId);
            if (currentNote != null) {
                runOnUiThread(() -> {
                    if (currentNote.getReminderTime() > 0) {
                        calendar.setTimeInMillis(currentNote.getReminderTime());
                        selectedTimeMillis = currentNote.getReminderTime();
                    }
                    updateDateTimeLabels();
                    repeatType = currentNote.getRepeatType();
                    repeatInterval = currentNote.getRepeatInterval();
                    repeatUnit = currentNote.getRepeatUnit();
                    
                    selectedWeekDays.clear();
                    if (currentNote.getRepeatDays() != null && !currentNote.getRepeatDays().isEmpty()) {
                        String[] days = currentNote.getRepeatDays().split(",");
                        for (String d : days) {
                            if (!d.trim().isEmpty()) {
                                selectedWeekDays.add(d.trim());
                            }
                        }
                    }

                    updateRepeatDisplay();
                    isChanged = false; // Initial data loaded, no changes yet
                });
            }
        }).start();
    }

    private void updateRepeatDisplay() {
        if (repeatType == null || repeatType.equals("None")) {
            binding.tvRepeat.setText("None");
            return;
        }

        if (repeatType.contains("Daily")) {
            if (repeatInterval <= 1) binding.tvRepeat.setText("Daily");
            else binding.tvRepeat.setText("Every " + repeatInterval + " day(s)");
        } 
        else if (repeatType.contains("Weekly")) {
            StringBuilder sb = new StringBuilder();
            if (repeatInterval <= 1) sb.append("Weekly");
            else sb.append("Every ").append(repeatInterval).append(" week(s)");

            if (!selectedWeekDays.isEmpty()) {
                sb.append(" on ");
                // Sort and join short day names
                String[] allDays = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};
                int added = 0;
                for (String d : allDays) {
                    if (selectedWeekDays.contains(d)) {
                        sb.append(d.substring(0, 3)).append(", ");
                        added++;
                    }
                }
                if (added > 0) {
                    binding.tvRepeat.setText(sb.substring(0, sb.length() - 2));
                } else {
                    binding.tvRepeat.setText(sb.toString());
                }
            } else {
                binding.tvRepeat.setText(sb.toString());
            }
        } 
        else if (repeatType.contains("Monthly")) {
            if (repeatInterval <= 1) binding.tvRepeat.setText("Monthly");
            else binding.tvRepeat.setText("Every " + repeatInterval + " month(s)");
        } 
        else {
            binding.tvRepeat.setText(repeatType);
        }
    }

    private void saveReminder() {
        if (selectedTimeMillis == -1) {
            selectedTimeMillis = calendar.getTimeInMillis();
        }

        if (selectedTimeMillis <= System.currentTimeMillis()) {
            Toast.makeText(this, "Please select a future time", Toast.LENGTH_SHORT).show();
            return;
        }

        if (noteId == -1) {
            Toast.makeText(this, "Error: Note ID not found", Toast.LENGTH_SHORT).show();
            return;
        }

        new Thread(() -> {
            currentNote = noteViewModel.getNoteById(noteId);
            if (currentNote != null) {
                currentNote.setReminderTime(selectedTimeMillis);
                currentNote.setRepeatType(repeatType);
                currentNote.setRepeatInterval(repeatInterval);
                currentNote.setRepeatUnit(repeatUnit);
                currentNote.setRepeatDays(String.join(",", selectedWeekDays));
                currentNote.setReminderEnabled(true);
                currentNote.setCompleted(false);
                
                noteViewModel.update(currentNote);
                ReminderManager.scheduleReminder(this, currentNote);
                
                runOnUiThread(() -> {
                    isChanged = false;
                    Toast.makeText(ReminderActivity.this, "Reminder Saved Successfully", Toast.LENGTH_SHORT).show();
                    finish();
                });
            } else {
                runOnUiThread(() -> Toast.makeText(ReminderActivity.this, "Error: Could not find note", Toast.LENGTH_SHORT).show());
            }
        }).start();
    }

    private void showClearConfirmationDialog() {
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_clear_reminder, null);
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            clearReminder();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void clearReminder() {
        new Thread(() -> {
            currentNote = noteViewModel.getNoteById(noteId);
            if (currentNote != null) {
                currentNote.setReminderTime(0);
                currentNote.setRepeatType("None");
                currentNote.setRepeatDays("");
                currentNote.setReminderEnabled(false);
                currentNote.setCompleted(false);
                
                noteViewModel.update(currentNote);
                ReminderManager.cancelReminder(this, noteId);
                
                runOnUiThread(() -> {
                    isChanged = false;
                    Toast.makeText(ReminderActivity.this, "Reminder Cleared", Toast.LENGTH_SHORT).show();
                    finish();
                });
            }
        }).start();
    }

    private void ShowCustomRepeatDialog() {
        selectedDays.clear();
        selectedDays.addAll(selectedWeekDays);
        
        DialogCustomRepeatBinding repeatBinding = DialogCustomRepeatBinding.inflate(getLayoutInflater());
        AlertDialog dialog = new AlertDialog.Builder(this).setView(repeatBinding.getRoot()).create();
        
        // Set initial unit and visibility based on current repeat type if it's "Repeat ..."
        if (repeatType.startsWith("Repeat")) {
            repeatBinding.txtRepeatType.setText(repeatType);
            if (repeatType.contains("Weekly")) {
                repeatBinding.txtUnit.setText("week(s)");
                repeatBinding.layoutWeekDays.setVisibility(View.VISIBLE);
            } else if (repeatType.contains("Monthly")) {
                repeatBinding.txtUnit.setText("month(s)");
                repeatBinding.layoutWeekDays.setVisibility(View.GONE);
            } else {
                repeatBinding.txtUnit.setText("day(s)");
                repeatBinding.layoutWeekDays.setVisibility(View.GONE);
            }
            repeatBinding.edtInterval.setText(String.valueOf(repeatInterval));
        } else {
            // Default to Daily if not currently custom
            repeatBinding.txtRepeatType.setText("Repeat Daily");
            repeatBinding.txtUnit.setText("day(s)");
            repeatBinding.layoutWeekDays.setVisibility(View.GONE);
            repeatBinding.edtInterval.setText("1");
        }

        dialog.show();

        setupDayButtons(repeatBinding);
        updateDayButtonsUI(repeatBinding);

        repeatBinding.layoutRepeatType.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(this, repeatBinding.layoutRepeatType);
            popupMenu.inflate(R.menu.custom_repeat_type);
            popupMenu.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_daily) {
                    repeatBinding.txtRepeatType.setText("Repeat Daily");
                    repeatBinding.txtUnit.setText("day(s)");
                    repeatBinding.layoutWeekDays.setVisibility(View.GONE);
                } else if (item.getItemId() == R.id.action_weekly) {
                    repeatBinding.txtRepeatType.setText("Repeat Weekly");
                    repeatBinding.txtUnit.setText("week(s)");
                    repeatBinding.layoutWeekDays.setVisibility(View.VISIBLE);
                } else if (item.getItemId() == R.id.action_monthly) {
                    repeatBinding.txtRepeatType.setText("Repeat Monthly");
                    repeatBinding.txtUnit.setText("month(s)");
                    repeatBinding.layoutWeekDays.setVisibility(View.GONE);
                }
                return true;
            });
            popupMenu.show();
        });

        repeatBinding.btnSave.setOnClickListener(v -> {
            repeatType = repeatBinding.txtRepeatType.getText().toString();
            String intervalStr = repeatBinding.edtInterval.getText().toString().trim();
            repeatInterval = intervalStr.isEmpty() ? 1 : Integer.parseInt(intervalStr);
            repeatUnit = repeatBinding.txtUnit.getText().toString();
            
            selectedWeekDays.clear();
            selectedWeekDays.addAll(selectedDays);
            
            updateRepeatDisplay();
            isChanged = true;
            dialog.dismiss();
        });
        repeatBinding.btnCancel.setOnClickListener(v -> dialog.dismiss());
    }

    private void updateDayButtonsUI(DialogCustomRepeatBinding rb) {
        setDayButtonStyle(rb.btnMon, "Monday");
        setDayButtonStyle(rb.btnTue, "Tuesday");
        setDayButtonStyle(rb.btnWed, "Wednesday");
        setDayButtonStyle(rb.btnThu, "Thursday");
        setDayButtonStyle(rb.btnFri, "Friday");
        setDayButtonStyle(rb.btnSat, "Saturday");
        setDayButtonStyle(rb.btnSun, "Sunday");
    }

    private void setDayButtonStyle(MaterialButton button, String day) {
        if (selectedDays.contains(day)) {
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.sky_blue)));
            button.setTextColor(ContextCompat.getColor(this, R.color.white));
        } else {
            button.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(this, R.color.search_bar_bg)));
            button.setTextColor(ContextCompat.getColor(this, R.color.text_primary));
        }
    }

    private void setupDayButtons(DialogCustomRepeatBinding rb) {
        rb.btnMon.setOnClickListener(v -> toggleDay(rb, "Monday"));
        rb.btnTue.setOnClickListener(v -> toggleDay(rb, "Tuesday"));
        rb.btnWed.setOnClickListener(v -> toggleDay(rb, "Wednesday"));
        rb.btnThu.setOnClickListener(v -> toggleDay(rb, "Thursday"));
        rb.btnFri.setOnClickListener(v -> toggleDay(rb, "Friday"));
        rb.btnSat.setOnClickListener(v -> toggleDay(rb, "Saturday"));
        rb.btnSun.setOnClickListener(v -> toggleDay(rb, "Sunday"));
    }

    private void toggleDay(DialogCustomRepeatBinding rb, String day) {
        if (selectedDays.contains(day)) {
            selectedDays.remove(day);
        } else {
            // Clear all other selections to enforce single selection
            selectedDays.clear();
            selectedDays.add(day);
        }
        // Refresh all button colors
        updateDayButtonsUI(rb);
    }
}
