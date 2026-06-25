package com.example.quicknotes.Fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.LinearLayoutCompat;
import android.widget.Toast;
import android.graphics.drawable.ColorDrawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.quicknotes.Activity.AddCheckListActivity;
import com.example.quicknotes.Activity.AddNoteActivity;
import com.example.quicknotes.Activity.ReminderActivity;
import com.example.quicknotes.Adapter.CalendarTaskAdapter;
import com.example.quicknotes.BottomSheet.AddNoteBottomSheet;
import com.example.quicknotes.BottomSheet.ReminderBottomSheet;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.R;
import com.example.quicknotes.databinding.FragmentCalendarBinding;
import com.example.quicknotes.databinding.CalenderDayBinding;
import com.kizitonwose.calendar.core.CalendarDay;
import com.kizitonwose.calendar.core.DayPosition;
import com.kizitonwose.calendar.core.WeekDay;
import com.kizitonwose.calendar.view.MonthDayBinder;
import com.kizitonwose.calendar.view.ViewContainer;
import com.kizitonwose.calendar.view.WeekDayBinder;
import android.view.Gravity;
import android.widget.ArrayAdapter;
import android.graphics.Color;
import static java.lang.Math.abs;
import android.widget.AdapterView;
import android.widget.LinearLayout;
import android.widget.TextView;

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
    private java.time.DayOfWeek firstDayOfWeek = DayOfWeek.MONDAY;

   
    private LocalDate selectedDate = LocalDate.now();
    private boolean isProgrammaticChange = false;
    private String selectedCategory = "All";
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("MMMM yyyy", Locale.getDefault());

    // Cache for notes by date to show indicators
    private Map<LocalDate, List<Note>> notesByDate = new HashMap<>();
    // Cache for active reminder firing dates to show thick colored bars
    private Map<LocalDate, Integer> reminderOccurrences = new HashMap<>();

    public CalendarFragment() {
        // Required empty public constructor
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

        SharedPreferences prefs = requireContext().getSharedPreferences("theme_prefs", Context.MODE_PRIVATE);
        String startDay = prefs.getString("start_of_week", "Sunday");
        firstDayOfWeek = startDay.equals("Monday") ? java.time.DayOfWeek.MONDAY : java.time.DayOfWeek.SUNDAY;

        setupRecyclerView();
        observeAllNotes();
        setupSelectionBarActions();
        setupCalendarHeader();

        YearMonth currentMonth = YearMonth.now();

        // Setup Month Calendar View
        binding.calendarView.setup(currentMonth.minusMonths(120),
                currentMonth.plusMonths(120),
                firstDayOfWeek);
        binding.calendarView.scrollToMonth(currentMonth);

        // Set up the persistent current date dynamic indicator label context
        binding.textTodayDate.setText(String.valueOf(LocalDate.now().getDayOfMonth()));

        // Setup Week Calendar View
        binding.weekCalenderView.setup(currentMonth.minusMonths(120).atDay(1),
                currentMonth.plusMonths(120).atDay(currentMonth.plusMonths(120).lengthOfMonth()),
                firstDayOfWeek);
        binding.weekCalenderView.scrollToWeek(LocalDate.now());

        //  Setup Month Binder
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

        //  Setup Week Binder
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

        // Initialize component configurations
        SetUpMonthNavigation();
        SetUpCollapsingBehaviour();
        setupViewSelector();

        // add notes
        binding.fabAddCalendarNote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AddNoteBottomSheet addNoteBottomSheet = new AddNoteBottomSheet(selectedCategory);
                addNoteBottomSheet.show(getChildFragmentManager(), "AddNoteBottomSheet");
            }
        });


        binding.calendarView.notifyDateChanged(selectedDate);
        binding.weekCalenderView.notifyDateChanged(selectedDate);
        updateHeaderColors(selectedDate);
    }

    private void bindDay(DayViewContainer container, LocalDate date, boolean isCurrentMonth) {
        container.binding.txtDay.setText(String.valueOf(date.getDayOfMonth()));

        if (!isCurrentMonth) {
            container.binding.viewReminderBar.setVisibility(View.GONE);
            container.binding.layoutIndicators.removeAllViews();
            container.binding.txtDay.setBackground(null);
            container.binding.txtDay.setTextColor(getResources().getColor(R.color.badge_untitled_dark_gray_text, null));
            container.binding.getRoot().setOnClickListener(v -> selectDate(date));
            return;
        }

        // Hide the reminder bar divider as requested to avoid the "two dividers" look.
        // The notes are already shown as chips (indicators), so the bar is redundant.
        container.binding.viewReminderBar.setVisibility(View.GONE);

        // Handle Indicators (Chips)
        List<Note> notesOnThisDay = notesByDate.get(date);
        container.binding.layoutIndicators.removeAllViews();
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
                    shape.setAlpha(80); // Light color for completed tasks
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
            // today date
            container.binding.txtDay.setBackgroundResource(R.drawable.bg_circle_primary);
            container.binding.txtDay.setTextColor(getResources().getColor(R.color.white, null));
        } else if (date.equals(LocalDate.now())) {

            container.binding.txtDay.setBackground(null);
            container.binding.txtDay.setTextColor(getResources().getColor(R.color.primary_blue, null));
        } else {
            // Normal unselected calendar cells
            container.binding.txtDay.setBackground(null);
            container.binding.txtDay.setTextColor(getResources().getColor(R.color.black, null));
        }

        container.binding.getRoot().setOnClickListener(v -> selectDate(date));
    }

    private void selectDate(LocalDate date) {
        if (selectedDate.equals(date)) {
            // Even if same date, update the notes list (in case of new/deleted notes)
            updateNotesForSelectedDate();
            return;
        }
        LocalDate oldDate = selectedDate;
        selectedDate = date;

        binding.calendarView.notifyDateChanged(oldDate);
        binding.calendarView.notifyDateChanged(date);
        binding.weekCalenderView.notifyDateChanged(oldDate);
        binding.weekCalenderView.notifyDateChanged(date);

        // Keep header titles systematically synchronized across navigation operations
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
                Intent intent;
                if ("CHECKLIST".equals(note.getNoteType())) {
                    intent = new Intent(requireContext(), AddCheckListActivity.class);
                } else {
                    intent = new Intent(requireContext(), AddNoteActivity.class);
                }
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            }

            @Override
            public void onTaskLongClick(Note note) {
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

    private void selectNoteForAction(Note note) {
        selectedNote = note;
        calendarTaskAdapter.setSelectedNoteId(note.getId());
        binding.layoutSelectionBar.setVisibility(View.VISIBLE);
        binding.fabAddCalendarNote.setVisibility(View.GONE);
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
           }
           else {
               Intent intent = new Intent(getContext(), ReminderActivity.class);
               intent.putExtra("note_id", selectedNote.getId());
               startActivity(intent);
               clearSelection(); // Hide selection bar after clicking
           }
        });
    }

    private void showDeleteConfirmDialog(Note note) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialog)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(android.graphics.Color.TRANSPARENT));
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            noteViewModel.moveToRecycleBin(note);
            Toast.makeText(requireContext(), getString(R.string.moved_to_recycle), Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            clearSelection();
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
            reminderOccurrences.clear();
            if (notes != null) {
                LocalDate today = LocalDate.now();
                for (Note note : notes) {
                    // 1. Always show note chip on its creation date
                    LocalDate createdDate = new Date(note.getCreatedTime()).toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate();
                    addNoteToDate(createdDate, note);

                    // 2. Handle reminders
                    if (note.getReminderTime() > 0 && note.isReminderEnabled()) {
                        LocalDate startDate = new Date(note.getReminderTime()).toInstant()
                                .atZone(ZoneId.systemDefault())
                                .toLocalDate();
                        
                        String repeatType = note.getRepeatType();
                        if (repeatType == null || repeatType.equals("None")) {
                            // Single reminder: show chip and divider
                            addNoteToDate(startDate, note);
                            if (!startDate.isBefore(today)) {
                                addReminderOccurrence(startDate, note);
                            }
                        } else {
                            // Repeating reminders: populate range for 2 years (e.g., June 2026 to 2028)
                            LocalDate endDate = startDate.plusYears(2);
                            LocalDate current = startDate;

                            // For Weekly repeat, identify target days
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
                                    // Only show divider bar for today onwards
                                    if (!current.isBefore(today)) {
                                        addReminderOccurrence(current, note);
                                    }
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

    private void addReminderOccurrence(LocalDate date, Note note) {
        if (!reminderOccurrences.containsKey(date)) {
            reminderOccurrences.put(date, getCategoryColor(note.getCategory()));
        }
    }

    private void addNoteToDate(LocalDate date, Note note) {
        if (!notesByDate.containsKey(date)) {
            notesByDate.put(date, new ArrayList<>());
        }
        // Avoid duplicates if same note added twice for same day
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
        String dateLabel;
        if (selectedDate.equals(LocalDate.now())) {
            dateLabel = getString(R.string.today);
        } else {
            DateTimeFormatter labelFormatter = DateTimeFormatter.ofPattern("dd MMMM", Locale.getDefault());
            dateLabel = selectedDate.format(labelFormatter);
        }

        List<Note> notes = notesByDate.get(selectedDate);
        boolean isWeekView = binding.weekCalenderView.getVisibility() == View.VISIBLE;

        if (notes == null || notes.isEmpty()) {
            binding.rvCalendarNotes.setVisibility(View.GONE);
            // Only show empty state in Week View, hide it in Month View
            binding.layoutEmptyCalendar.setVisibility(isWeekView ? View.VISIBLE : View.GONE);
        } else {
            binding.rvCalendarNotes.setVisibility(View.VISIBLE);
            binding.layoutEmptyCalendar.setVisibility(View.GONE);

            List<Note> activeTasks = new ArrayList<>();
            List<Note> doneTasks = new ArrayList<>();
            for (Note note : notes) {
                if (note.isCompleted()) {
                    doneTasks.add(note);
                } else {
                    activeTasks.add(note);
                }
            }
            calendarTaskAdapter.setTasks(activeTasks, doneTasks, dateLabel);
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

                if (position == 0) { // Month Expanded
                    binding.appBarCalendar.setExpanded(true, true);
                    binding.calendarView.setVisibility(View.VISIBLE);
                    binding.weekCalenderView.setVisibility(View.INVISIBLE);
                } else { // Week Collapsed
                    binding.appBarCalendar.setExpanded(false, true);
                    binding.calendarView.setVisibility(View.INVISIBLE);
                    binding.weekCalenderView.setVisibility(View.VISIBLE);
                    binding.weekCalenderView.scrollToWeek(selectedDate);
                }
                updateNotesForSelectedDate();
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
    }

    private void SetUpCollapsingBehaviour() {
        binding.appBarCalendar.addOnOffsetChangedListener((appBarLayout, verticalOffset) -> {
            int scrollRange = appBarLayout.getTotalScrollRange();
            if (scrollRange == 0) return;

            float percentage = (float) Math.abs(verticalOffset) / scrollRange;

            // Swiping Up -> Transition to Week View smoothly
            if (percentage > 0.55) {
                if (binding.weekCalenderView.getVisibility() != View.VISIBLE) {
                    binding.weekCalenderView.setVisibility(View.VISIBLE);
                    binding.calendarView.setVisibility(View.INVISIBLE);
                    setSpinnerSelectionProgrammatically(1);
                    binding.weekCalenderView.scrollToWeek(selectedDate);
                    updateNotesForSelectedDate();
                }
            }
            // Swiping Down -> Return to Expanded Month View
            else {
                if (binding.calendarView.getVisibility() != View.VISIBLE) {
                    binding.calendarView.setVisibility(View.VISIBLE);
                    binding.weekCalenderView.setVisibility(View.INVISIBLE);
                    setSpinnerSelectionProgrammatically(0);
                    updateNotesForSelectedDate();
                }
            }
        });
    }

    private void SetUpMonthNavigation() {
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

        String[] dayLabels;
        if (firstDayOfWeek == java.time.DayOfWeek.MONDAY) {
            dayLabels = new String[]{"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
        } else {
            dayLabels = new String[]{"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};
        }

        for (int i = 0; i < 7; i++) {
            days[i].setText(dayLabels[i]);
        }
    }

    private void updateHeaderColors(LocalDate date) {
        int selectedColor = getResources().getColor(R.color.primary_blue, null);
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

        int dayIndex;
        if (firstDayOfWeek == java.time.DayOfWeek.MONDAY) {
            dayIndex = date.getDayOfWeek().getValue() - 1;
        } else {
            dayIndex = date.getDayOfWeek().getValue() % 7;
        }

        if (dayIndex >= 0 && dayIndex < 7) {
            days[dayIndex].setTextColor(selectedColor);
        }
    }

    public class DayViewContainer extends ViewContainer {
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
