package com.example.quicknotes.Fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quicknotes.Activity.AddCheckListActivity;
import com.example.quicknotes.Activity.AddNoteActivity;
import com.example.quicknotes.Activity.ReminderActivity;
import com.example.quicknotes.Adapter.CalendarTaskAdapter;
import com.example.quicknotes.BottomSheet.AddNoteBottomSheet;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.R;
import com.example.quicknotes.databinding.CalenderDayBinding;
import com.example.quicknotes.databinding.FragmentCalendarBinding;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.WeekDay;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.view.WeekDayBinder;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class CalendarFragment extends Fragment {

    private FragmentCalendarBinding binding;
    private NoteViewModel noteViewModel;
    private CalendarTaskAdapter calendarTaskAdapter;
    private YearMonth visibleMonth = YearMonth.now();
    private Note selectedNote = null;
    private DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;

    private ActivityResultLauncher<Intent> lockLauncher;
    private Note noteToOpen;
    private boolean isUnlockingSingle = false;
    private Note pendingDeleteNote;

    private LocalDate selectedDate = LocalDate.now();
    private boolean isProgrammaticChange = false;
    private final String selectedCategory = "All";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());

    private final Map<LocalDate, List<Note>> notesByDate = new HashMap<>();

    public CalendarFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentCalendarBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        lockLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                if (noteToOpen != null) {
                    openNoteActivity(noteToOpen);
                    noteToOpen = null;
                } else if (isUnlockingSingle && selectedNote != null) {
                    performLockUnlock(selectedNote, false);
                    isUnlockingSingle = false;
                } else if (pendingDeleteNote != null) {
                    noteViewModel.moveToRecycleBin(pendingDeleteNote);
                    Toast.makeText(requireContext(), getString(R.string.moved_to_recycle), Toast.LENGTH_SHORT).show();
                    pendingDeleteNote = null;
                    clearSelection();
                }
            } else {
                noteToOpen = null;
                isUnlockingSingle = false;
                pendingDeleteNote = null;
            }
        });

        SharedPreferences prefs = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        String startDay = prefs.getString("start_of_week", "Sunday");
        firstDayOfWeek = "Monday".equals(startDay) ? DayOfWeek.MONDAY : DayOfWeek.SUNDAY;

        setupRecyclerView();
        observeAllNotes();
        setupSelectionBarActions();
        setupCalendarHeader();

        YearMonth currentMonth = YearMonth.now();

        binding.calendarView.setup(currentMonth.minusMonths(120),
                currentMonth.plusMonths(120),
                firstDayOfWeek);
        binding.calendarView.scrollToMonth(currentMonth);

        binding.textTodayDate.setText(String.valueOf(LocalDate.now().getDayOfMonth()));

        binding.weekCalenderView.setup(currentMonth.minusMonths(120).atDay(1),
                currentMonth.plusMonths(120).atDay(currentMonth.plusMonths(120).lengthOfMonth()),
                firstDayOfWeek);
        binding.weekCalenderView.scrollToWeek(LocalDate.now());

        binding.calendarView.setDayBinder(new MonthDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, CalendarDay calendarDay) {
                bindDay(container, calendarDay.getDate(), calendarDay.getPosition() == DayPosition.MonthDate);
            }
        });

        binding.weekCalenderView.setDayBinder(new WeekDayBinder<DayViewContainer>() {
            @NonNull
            @Override
            public DayViewContainer create(@NonNull View view) {
                return new DayViewContainer(view);
            }

            @Override
            public void bind(@NonNull DayViewContainer container, WeekDay weekDay) {
                bindDay(container, weekDay.getDate(), true);
            }
        });

        binding.calendarView.setVisibility(View.VISIBLE);
        binding.weekCalenderView.setVisibility(View.GONE);
        setUpMonthNavigation();
        setUpCollapsingBehaviour();
        setupViewSelector();

        binding.fabAddCalendarNote.setOnClickListener(v -> {
            long selectedMillis = selectedDate.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli();
            AddNoteBottomSheet addNoteBottomSheet = new AddNoteBottomSheet(selectedCategory, selectedMillis);
            addNoteBottomSheet.show(getChildFragmentManager(), "AddNoteBottomSheet");
        });

        binding.calendarView.notifyDateChanged(selectedDate);
        binding.weekCalenderView.notifyDateChanged(selectedDate);
        updateHeaderColors(selectedDate);
    }

    private void bindDay(DayViewContainer container, LocalDate date, boolean isCurrentMonth) {
        container.binding.txtDay.setText(String.valueOf(date.getDayOfMonth()));

        // Always reset these to avoid state issues during recycling
        container.binding.viewReminderBar.setVisibility(View.GONE);
        container.binding.layoutIndicators.removeAllViews();

        List<Note> notesOnThisDay = notesByDate.get(date);
        if (notesOnThisDay != null && !notesOnThisDay.isEmpty()) {
            for (Note note : notesOnThisDay) {
                TextView indicator = new TextView(requireContext());
                indicator.setText(note.getTitle());
                indicator.setTextColor(Color.WHITE);
                indicator.setTextSize(TypedValue.COMPLEX_UNIT_SP, 8);
                indicator.setGravity(Gravity.CENTER);
                indicator.setSingleLine(true);
                indicator.setPadding(12, 0, 12, 0);

                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.WRAP_CONTENT,
                        (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 14, getResources().getDisplayMetrics()));

                int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, getResources().getDisplayMetrics());
                params.setMargins(0, margin, 0, margin);
                indicator.setLayoutParams(params);

                int color = getCategoryColor(note.getCategory());
                GradientDrawable shape = new GradientDrawable();
                shape.setShape(GradientDrawable.RECTANGLE);
                shape.setCornerRadius(12f);
                shape.setColor(color);

                if (note.isCompleted()) {
                    shape.setAlpha(80);
                    indicator.setTextColor(Color.argb(120, 255, 255, 255));
                } else {
                    shape.setAlpha(255);
                    indicator.setTextColor(Color.WHITE);
                }

                indicator.setBackground(shape);
                container.binding.layoutIndicators.addView(indicator);
            }
        }

        if (date.equals(selectedDate)) {
            container.binding.txtDay.setBackgroundResource(R.drawable.bg_circle_primary);
            container.binding.txtDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
        } else if (date.equals(LocalDate.now())) {
            container.binding.txtDay.setBackground(null);
            container.binding.txtDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue));
        } else {
            container.binding.txtDay.setBackground(null);
            if (isCurrentMonth) {
                container.binding.txtDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.black));
            } else {
                container.binding.txtDay.setTextColor(ContextCompat.getColor(requireContext(), R.color.badge_untitled_dark_gray_text));
            }
        }

        // Dividers (viewReminderBar) are hidden by default at the start of this method
        // as per user request to remove extra dividers.
        container.binding.viewReminderBar.setVisibility(View.GONE);

        container.binding.getRoot().setOnClickListener(v -> selectDate(date));
    }

    private void selectDate(LocalDate date) {
        if (selectedDate.equals(date)) {
            updateNotesForSelectedDate();
            return;
        }
        LocalDate oldDate = selectedDate;
        selectedDate = date;

        binding.calendarView.notifyDateChanged(oldDate);
        binding.calendarView.notifyDateChanged(date);
        binding.weekCalenderView.notifyDateChanged(oldDate);
        binding.weekCalenderView.notifyDateChanged(date);

        visibleMonth = YearMonth.from(date);
        binding.textCurrentMonth.setText(visibleMonth.format(formatter));

        updateHeaderColors(date);
        updateNotesForSelectedDate();

        if (binding.calendarView.getVisibility() == View.VISIBLE) {
            binding.weekCalenderView.scrollToWeek(date);
        } else {
            binding.calendarView.scrollToMonth(visibleMonth);
        }
    }

    private void setupRecyclerView() {
        calendarTaskAdapter = new CalendarTaskAdapter(new CalendarTaskAdapter.OnTaskClickListener() {
            @Override
            public void onTaskClick(Note note) {
                if (selectedNote != null) {
                    if (selectedNote.getId() == note.getId()) {
                        clearSelection();
                    } else {
                        selectNoteForAction(note);
                    }
                    return;
                }
                
                if (note.isLocked()) {
                    noteToOpen = note;
                    Intent intent = new Intent(requireContext(), com.example.quicknotes.Activity.PatternLockActivity.class);
                    intent.putExtra("extra_mode", "mode_verify");
                    lockLauncher.launch(intent);
                } else {
                    openNoteActivity(note);
                }
            }

            @Override
            public void onTaskLongClick(Note note, View view) {
                selectNoteForAction(note);
            }

            @Override
            public void onTaskStatusChanged(Note note, boolean isCompleted) {
                note.setCompleted(isCompleted);
                noteViewModel.update(note);
            }
        });
        binding.rvCalendarNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.rvCalendarNotes.setAdapter(calendarTaskAdapter);
    }

    private void openNoteActivity(Note note) {
        Intent intent;
        if ("CHECKLIST".equals(note.getNoteType())) {
            intent = new Intent(requireContext(), AddCheckListActivity.class);
        } else {
            intent = new Intent(requireContext(), AddNoteActivity.class);
        }
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    private void selectNoteForAction(Note note) {
        selectedNote = note;
        calendarTaskAdapter.setSelectedNoteId(note.getId());
        binding.layoutSelectionBar.setVisibility(View.VISIBLE);
        binding.fabAddCalendarNote.setVisibility(View.GONE);
    }

    @SuppressLint("RestrictedApi")
    private void showSelectionMoreMenu(View view) {
        if (selectedNote == null) return;
        Note note = selectedNote;

        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.selection_more_menu, popupMenu.getMenu());

        MenuItem checkItem = popupMenu.getMenu().findItem(R.id.menu_check);
        if (note.isCompleted()) {
            checkItem.setTitle(R.string.uncheck);
            checkItem.setIcon(R.drawable.ic_checkbox_blank);
        } else {
            checkItem.setTitle(R.string.check);
            checkItem.setIcon(R.drawable.ic_check_menu);
        }

        MenuItem lockItem = popupMenu.getMenu().findItem(R.id.menu_lock);
        lockItem.setTitle(note.isLocked() ? R.string.unlock : R.string.lock);
        lockItem.setIcon(R.drawable.ic_lock_settings);

        MenuItem shareItem = popupMenu.getMenu().findItem(R.id.menu_share);
        if (note.isLocked()) {
            shareItem.setEnabled(false);
            if (shareItem.getIcon() != null) {
                shareItem.getIcon().setAlpha(130);
            }
        }

        MenuBuilder menuBuilder = (MenuBuilder) popupMenu.getMenu();
        menuBuilder.setOptionalIconsVisible(true);

        popupMenu.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.menu_check) {
                handleCheck(note, !note.isCompleted());
                return true;
            } else if (itemId == R.id.menu_lock) {
                handleLock(note, !note.isLocked());
                return true;
            } else if (itemId == R.id.menu_add_widget) {
                Toast.makeText(requireContext(), R.string.add_widget, Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_share) {
                if (note.isLocked()) {
                    Toast.makeText(requireContext(), "Locked notes cannot be shared", Toast.LENGTH_SHORT).show();
                } else {
                    showShareAsDialog(note);
                }
                return true;
            }
            return false;
        });

        MenuPopupHelper optionsMenu = new MenuPopupHelper(requireContext(), menuBuilder, view);
        optionsMenu.setForceShowIcon(true);
        optionsMenu.show();
    }

    private void handleCheck(Note note, boolean isChecking) {
        note.setCompleted(isChecking);
        noteViewModel.update(note);
        clearSelection();
        String message = isChecking ? getString(R.string.check) : getString(R.string.uncheck);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void handleLock(Note note, boolean isLocking) {
        SharedPreferences prefs = requireContext().getSharedPreferences("security_prefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("is_enabled", false)) {
            Toast.makeText(requireContext(), R.string.set_password_first, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLocking) {
            performLockUnlock(note, true);
        } else {
            isUnlockingSingle = true;
            selectedNote = note;
            Intent intent = new Intent(requireContext(), com.example.quicknotes.Activity.PatternLockActivity.class);
            intent.putExtra("extra_mode", "mode_verify");
            lockLauncher.launch(intent);
        }
    }

    private void performLockUnlock(Note note, boolean isLocking) {
        note.setLocked(isLocking);
        noteViewModel.update(note);
        clearSelection();
        String msg = isLocking ? getString(R.string.lock) : getString(R.string.unlock);
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }

    private void showShareAsDialog(Note note) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_share_as, null);
        AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setBackground(new ColorDrawable(Color.TRANSPARENT))
                .create();

        dialogView.findViewById(R.id.btnShareImage).setOnClickListener(v -> {
            openNoteActivityForShare(note, "IMAGE");
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnSharePDF).setOnClickListener(v -> {
            openNoteActivityForShare(note, "PDF");
            dialog.dismiss();
        });

        dialogView.findViewById(R.id.btnShareText).setOnClickListener(v -> {
            shareNoteAsText(note);
            dialog.dismiss();
            clearSelection();
        });

        dialog.show();
    }

    private void shareNoteAsText(Note note) {
        String shareBody;
        if ("CHECKLIST".equals(note.getNoteType())) {
            StringBuilder sb = new StringBuilder(note.getTitle()).append("\n\n");
            String[] lines = note.getDescription().split("\n");
            for (String line : lines) {
                String[] parts = line.split("\\|", 2);
                if (parts.length == 2) {
                    sb.append("1".equals(parts[0]) ? "☑ " : "☐ ").append(parts[1]).append("\n");
                }
            }
            shareBody = sb.toString();
        } else {
            shareBody = note.getTitle() + "\n\n" + note.getDescription();
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, note.getTitle());
        intent.putExtra(Intent.EXTRA_TEXT, shareBody);
        startActivity(Intent.createChooser(intent, getString(R.string.share)));
    }

    private void openNoteActivityForShare(Note note, String type) {
        Intent intent;
        if ("CHECKLIST".equals(note.getNoteType())) {
            intent = new Intent(requireContext(), com.example.quicknotes.Activity.AddCheckListActivity.class);
        } else {
            intent = new Intent(requireContext(), com.example.quicknotes.Activity.AddNoteActivity.class);
        }
        intent.putExtra("note_id", note.getId());
        intent.putExtra("extra_share_type", type);
        startActivity(intent);
        clearSelection();
    }

    private void setupSelectionBarActions() {
        binding.btnDeleteSelection.setOnClickListener(v -> {
            if (selectedNote != null) {
                showDeleteConfirmDialog(selectedNote);
            }
        });

        binding.btnArchiveSelection.setOnClickListener(v -> {
            if (selectedNote != null) {
                noteViewModel.moveToArchive(selectedNote);
                Toast.makeText(requireContext(), getString(R.string.note_archived), Toast.LENGTH_SHORT).show();
                clearSelection();
            }
        });

        binding.btnReminderSelection.setOnClickListener(v -> {
           if (selectedNote == null) {
               Toast.makeText(requireContext(), "Please First Enter Note", Toast.LENGTH_SHORT).show();
           } else {
               Intent intent = new Intent(getContext(), ReminderActivity.class);
               intent.putExtra("note_id", selectedNote.getId());
               startActivity(intent);
               clearSelection();
           }
        });

        binding.btnMoreSelection.setOnClickListener(this::showSelectionMoreMenu);
    }

    private void showDeleteConfirmDialog(Note note) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialog)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            if (note.isLocked()) {
                pendingDeleteNote = note;
                Intent intent = new Intent(requireContext(), com.example.quicknotes.Activity.PatternLockActivity.class);
                intent.putExtra("extra_mode", "mode_verify");
                lockLauncher.launch(intent);
            } else {
                noteViewModel.moveToRecycleBin(note);
                Toast.makeText(requireContext(), getString(R.string.moved_to_recycle), Toast.LENGTH_SHORT).show();
                clearSelection();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void clearSelection() {
        selectedNote = null;
        calendarTaskAdapter.setSelectedNoteId(-1);
        binding.layoutSelectionBar.setVisibility(View.GONE);
        binding.fabAddCalendarNote.setVisibility(View.VISIBLE);
    }

    private void observeAllNotes() {
        noteViewModel.getAllNotes().observe(getViewLifecycleOwner(), notes -> {
            notesByDate.clear();
            if (notes != null) {
                for (Note note : notes) {
                    long dateToUse = note.getCalendarDate();
                    if (dateToUse == 0) dateToUse = note.getCreatedTime();

                    LocalDate calendarDate = new Date(dateToUse).toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    addNoteToDate(calendarDate, note);

                    if (note.getReminderTime() > 0 && note.isReminderEnabled()) {
                        LocalDate startDate = new Date(note.getReminderTime()).toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        
                        String repeatType = note.getRepeatType();
                        if (repeatType == null || "None".equals(repeatType)) {
                            addNoteToDate(startDate, note);
                        } else {
                            LocalDate endDate = startDate.plusYears(2);
                            LocalDate current = startDate;

                            Set<DayOfWeek> targetDays = new HashSet<>();
                            if (repeatType.contains("Weekly")) {
                                if (note.getRepeatDays() != null && !note.getRepeatDays().isEmpty()) {
                                    String[] days = note.getRepeatDays().split(",");
                                    for (String d : days) {
                                        try { targetDays.add(DayOfWeek.valueOf(d.trim().toUpperCase())); } catch (Exception ignored) {}
                                    }
                                }
                                if (targetDays.isEmpty()) targetDays.add(startDate.getDayOfWeek());
                            }

                            while (!current.isAfter(endDate)) {
                                boolean isReminderDay = false;
                                if (repeatType.contains("Daily")) isReminderDay = true;
                                else if (repeatType.contains("Weekly")) isReminderDay = targetDays.contains(current.getDayOfWeek());
                                else if (repeatType.contains("Monthly")) isReminderDay = (current.getDayOfMonth() == startDate.getDayOfMonth());

                                if (isReminderDay) {
                                    addNoteToDate(current, note);
                                }

                                if (repeatType.contains("Daily")) current = current.plusDays(note.getRepeatInterval() > 0 ? note.getRepeatInterval() : 1);
                                else if (repeatType.contains("Weekly")) current = current.plusDays(1);
                                else if (repeatType.contains("Monthly")) current = current.plusMonths(note.getRepeatInterval() > 0 ? note.getRepeatInterval() : 1);
                                else break;
                            }
                        }
                    }
                }
            }
            binding.calendarView.notifyCalendarChanged();
            binding.weekCalenderView.notifyCalendarChanged();
            updateNotesForSelectedDate();
        });
    }

    private void addNoteToDate(LocalDate date, Note note) {
        if (!notesByDate.containsKey(date)) {
            notesByDate.put(date, new ArrayList<>());
        }
        List<Note> list = notesByDate.get(date);
        boolean exists = false;
        for (Note n : list) {
            if (n.getId() == note.getId()) {
                exists = true;
                break;
            }
        }
        if (!exists) {
            list.add(note);
        }
    }

    private void updateNotesForSelectedDate() {
        List<Note> notes = notesByDate.get(selectedDate);

        List<Note> activeTasks = new ArrayList<>();
        List<Note> doneTasks = new ArrayList<>();

        if (notes != null) {
            for (Note note : notes) {
                if (note.isCompleted()) {
                    doneTasks.add(note);
                } else {
                    activeTasks.add(note);
                }
            }
        }

        DateTimeFormatter headerFormatter = DateTimeFormatter.ofPattern("d MMM", Locale.getDefault());
        String headerLabel = selectedDate.format(headerFormatter);

        calendarTaskAdapter.setTasks(activeTasks, doneTasks, headerLabel);

        // Always show the RecyclerView so the date header is visible
        binding.rvCalendarNotes.setVisibility(View.VISIBLE);

        if (activeTasks.isEmpty() && doneTasks.isEmpty()) {
            binding.layoutEmptyCalendar.setVisibility(View.VISIBLE);
            // Optional: Adjust padding or height of empty state when header is present
        } else {
            binding.layoutEmptyCalendar.setVisibility(View.GONE);
        }
    }

    private int getCategoryColor(String category) {
        if (category == null) return ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_text);
        switch (category) {
            case "Personal": return ContextCompat.getColor(requireContext(), R.color.badge_personal_text);
            case "Work": return ContextCompat.getColor(requireContext(), R.color.badge_work_text);
            case "Others": return ContextCompat.getColor(requireContext(), R.color.badge_others_text);
            case "All": return ContextCompat.getColor(requireContext(), R.color.primary_blue);
            case "Untitled_Red": return ContextCompat.getColor(requireContext(), R.color.badge_untitled_red_text);
            case "Untitled_Orange": return ContextCompat.getColor(requireContext(), R.color.badge_untitled_orange_text);
            case "Untitled_Pink": return ContextCompat.getColor(requireContext(), R.color.badge_untitled_pink_text);
            case "Untitled_Purple": return ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_text);
            case "Untitled_DarkGray": return ContextCompat.getColor(requireContext(), R.color.badge_untitled_dark_gray_text);
            case "Untitled_Gray": return ContextCompat.getColor(requireContext(), R.color.badge_untitled_gray_text);
            default: return ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_text);
        }
    }

    private void setSpinnerSelectionProgrammatically(int position) {
        if (binding.calendarViewSpinner.getSelectedItemPosition() == position) return;
        isProgrammaticChange = true;
        binding.calendarViewSpinner.setSelection(position);
        isProgrammaticChange = false;
    }

    private void setupViewSelector() {
        String[] options = {getString(R.string.month), getString(R.string.week)};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), R.layout.spinner_item_calendar, options);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.calendarViewSpinner.setAdapter(adapter);

        binding.calendarViewSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                if (isProgrammaticChange) return;

                if (position == 0) {
                    binding.appBarCalendar.setExpanded(true, true);
                } else {
                    binding.appBarCalendar.setExpanded(false, true);
                    binding.weekCalenderView.post(() -> binding.weekCalenderView.scrollToWeek(selectedDate));
                }
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void setUpCollapsingBehaviour() {
        binding.appBarCalendar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int scrollRange = appBarLayout.getTotalScrollRange();
            if (scrollRange == 0) return;

            float percentage = (float) Math.abs(verticalOffset) / scrollRange;

            if (percentage > 0.5f) {
                if (binding.weekCalenderView.getVisibility() != View.VISIBLE) {
                    binding.weekCalenderView.setVisibility(View.VISIBLE);
                    binding.calendarView.setVisibility(View.INVISIBLE);
                    setSpinnerSelectionProgrammatically(1);
                    binding.weekCalenderView.post(() -> binding.weekCalenderView.scrollToWeek(selectedDate));
                    updateNotesForSelectedDate();
                }
            } else {
                if (binding.calendarView.getVisibility() != View.VISIBLE) {
                    binding.calendarView.setVisibility(View.VISIBLE);
                    binding.weekCalenderView.setVisibility(View.GONE);
                    setSpinnerSelectionProgrammatically(0);
                    updateNotesForSelectedDate();
                }
            }
        });
    }

    private void setUpMonthNavigation() {
        binding.calendarView.setMonthScrollListener(calendarMonth -> {
            visibleMonth = calendarMonth.getYearMonth();
            binding.textCurrentMonth.setText(visibleMonth.format(formatter));
            return null;
        });

        binding.weekCalenderView.setWeekScrollListener(week -> {
            visibleMonth = YearMonth.from(week.getDays().get(3).getDate());
            binding.textCurrentMonth.setText(visibleMonth.format(formatter));
            return null;
        });

        binding.imgPrevious.setOnClickListener(v -> {
            if (binding.calendarView.getVisibility() == View.VISIBLE) {
                visibleMonth = visibleMonth.minusMonths(1);
                binding.calendarView.smoothScrollToMonth(visibleMonth);
                selectDate(selectedDate.minusMonths(1));
            } else {
                LocalDate target = selectedDate.minusWeeks(1);
                selectDate(target);
                binding.weekCalenderView.smoothScrollToWeek(target);
            }
        });

        binding.imgNext.setOnClickListener(v -> {
            if (binding.calendarView.getVisibility() == View.VISIBLE) {
                visibleMonth = visibleMonth.plusMonths(1);
                binding.calendarView.smoothScrollToMonth(visibleMonth);
                selectDate(selectedDate.plusMonths(1));
            } else {
                LocalDate target = selectedDate.plusWeeks(1);
                selectDate(target);
                binding.weekCalenderView.smoothScrollToWeek(target);
            }
        });

        binding.imageCalendarToday.setOnClickListener(v -> {
            visibleMonth = YearMonth.now();
            LocalDate today = LocalDate.now();
            selectDate(today);
            binding.calendarView.smoothScrollToMonth(visibleMonth);
            binding.weekCalenderView.smoothScrollToWeek(today);
        });
    }

    private void setupCalendarHeader() {
        TextView[] days = {
                binding.calendarHeader.tvMon, binding.calendarHeader.tvTue,
                binding.calendarHeader.tvWed, binding.calendarHeader.tvThu,
                binding.calendarHeader.tvFri, binding.calendarHeader.tvSat,
                binding.calendarHeader.tvSun
        };

        String[] dayLabels = firstDayOfWeek == DayOfWeek.MONDAY
                ? new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"}
                : new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (int i = 0; i < 7; i++) {
            days[i].setText(dayLabels[i]);
        }
    }

    private void updateHeaderColors(LocalDate date) {
        int selectedColor = ContextCompat.getColor(requireContext(), R.color.primary_blue);
        int defaultColor = Color.parseColor("#9E9E9E");

        TextView[] days = {
                binding.calendarHeader.tvMon, binding.calendarHeader.tvTue,
                binding.calendarHeader.tvWed, binding.calendarHeader.tvThu,
                binding.calendarHeader.tvFri, binding.calendarHeader.tvSat,
                binding.calendarHeader.tvSun
        };

        for (TextView tv : days) {
            tv.setTextColor(defaultColor);
        }

        int dayIndex = firstDayOfWeek == DayOfWeek.MONDAY
                ? date.getDayOfWeek().getValue() - 1
                : date.getDayOfWeek().getValue() % 7;

        if (dayIndex >= 0 && dayIndex < 7) {
            days[dayIndex].setTextColor(selectedColor);
        }
    }

    public static class DayViewContainer extends ViewContainer {
        public final CalenderDayBinding binding;
        public DayViewContainer(@NonNull View view) {
            super(view);
            binding = CalenderDayBinding.bind(view);
        }
    }

    @Override
    public void onDestroyView() {
        if (selectedNote != null) {
            clearSelection();
        }
        super.onDestroyView();
        binding = null;
    }
}
