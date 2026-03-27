package com.example.interviewace.fragment;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.interviewace.R;
import com.example.interviewace.ViewModel.ProgressViewModel;
import com.example.interviewace.adapter.SessionAdapter;
import com.example.interviewace.databinding.FragmentProcessBinding;
import com.example.interviewace.model.SessionItem;
import com.example.interviewace.ui.activity.ActivitySessionDetails;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.android.material.chip.Chip;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ProcessFragment extends Fragment {

    private FragmentProcessBinding binding;
    private ProgressViewModel viewModel;
    private SessionAdapter adapter;

    public ProcessFragment() {
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProcessBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(ProgressViewModel.class);

        SetUpRecyclerview();
        ObserveViewModel();
    }

    private void SetUpRecyclerview() {
        adapter = new SessionAdapter(position -> {
            // Fix: Safe access to adapter list
            if (adapter.getSessionList() != null && position < adapter.getSessionList().size()) {
                SessionItem clickedItem = adapter.getSessionList().get(position);
                
                Intent intent = new Intent(getContext(), ActivitySessionDetails.class);
                intent.putExtra("role", clickedItem.getRoleName());
                intent.putExtra("date", clickedItem.getDate());
                intent.putExtra("score", clickedItem.getScore());
                intent.putExtra("streak", calculateStreak(viewModel.getSessions().getValue()));
                
                // Fix: Check if skills is null before creating ArrayList
                ArrayList<String> skills = clickedItem.getSkills() != null ? 
                        new ArrayList<>(clickedItem.getSkills()) : new ArrayList<>();
                intent.putStringArrayListExtra("skills", skills);
                
                startActivity(intent);
            }
        });
        binding.rvRecentSessions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvRecentSessions.setAdapter(adapter);
    }

    private void ObserveViewModel() {
        viewModel.getSessions().observe(getViewLifecycleOwner(), sessionItems -> {
            if (sessionItems != null) {
                UpdateUI(sessionItems);
                UpdateSkillOverview(sessionItems);
                UpdatePracticeStreak(sessionItems);
                if (!sessionItems.isEmpty()) {
                    SetUpChart(sessionItems);
                } else {
                    binding.lineChart.clear();
                    binding.lineChart.setNoDataText("No session data available");
                }
                adapter.setSessionList(sessionItems);
            }
        });
        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void UpdateUI(List<SessionItem> sessionItems) {
        binding.tvSessionsCount.setText(String.valueOf(sessionItems.size()));

        if (sessionItems.isEmpty()) {
            binding.tvAvgScore.setText("0%");
            binding.tvBestScore.setText("0%");
            return;
        }

        int totalScore = 0;
        int maxScore = 0;

        for (SessionItem item : sessionItems) {
            totalScore += item.getScore();

            if (item.getScore() > maxScore) {
                maxScore = item.getScore();
            }
        }
        double avg = (double) totalScore / sessionItems.size();
        binding.tvAvgScore.setText(String.format("%.1f%%", avg));
        binding.tvBestScore.setText(String.format("%d%%", maxScore));
    }

    private void UpdateSkillOverview(List<SessionItem> sessionItems) {
        if (binding.chipGroupSkills == null || getContext() == null) return;

        binding.chipGroupSkills.removeAllViews();
        Set<String> uniqueSkills = new HashSet<>();

        for (SessionItem item : sessionItems) {
            if (item.getSkills() != null) {
                uniqueSkills.addAll(item.getSkills());
            }
        }

        if (uniqueSkills.isEmpty()) {
            Chip chip = new Chip(getContext());
            chip.setText("No skills data");
            binding.chipGroupSkills.addView(chip);
            return;
        }

        for (String skill : uniqueSkills) {
            Chip chip = new Chip(getContext());
            chip.setText(skill);
            chip.setChipBackgroundColorResource(R.color.light_main_bg);
            chip.setTextColor(Color.BLACK);
            binding.chipGroupSkills.addView(chip);
        }
    }

    private void UpdatePracticeStreak(List<SessionItem> sessionItems) {
        if (sessionItems == null || sessionItems.isEmpty()) {
            binding.tvStreakCount.setText("0 days");
            updateStreakCircles(0);
            return;
        }

        int streakCount = calculateStreak(sessionItems);
        binding.tvStreakCount.setText(streakCount + " days");
        updateStreakCircles(streakCount);
    }

    private int calculateStreak(List<SessionItem> sessionItems) {
        if (sessionItems == null) return 0;
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());
        Set<String> sessionDates = new HashSet<>();
        for (SessionItem item : sessionItems) {
            sessionDates.add(item.getDate());
        }

        Calendar cal = Calendar.getInstance();
        int streak = 0;

        String today = sdf.format(cal.getTime());
        if (sessionDates.contains(today)) {
            streak++;
            cal.add(Calendar.DATE, -1);

            while (true) {
                String prevDay = sdf.format(cal.getTime());
                if (sessionDates.contains(prevDay)) {
                    streak++;
                    cal.add(Calendar.DATE, -1);
                } else {
                    break;
                }
            }
        } else {
            cal.add(Calendar.DATE, -1);
            String yesterday = sdf.format(cal.getTime());
            if (sessionDates.contains(yesterday)) {
                streak++;
                cal.add(Calendar.DATE, -1);
                while (true) {
                    String prevDay = sdf.format(cal.getTime());
                    if (sessionDates.contains(prevDay)) {
                        streak++;
                        cal.add(Calendar.DATE, -1);
                    } else {
                        break;
                    }
                }
            }
        }
        return streak;
    }

    private void updateStreakCircles(int streakCount) {
        ViewGroup layout = binding.layoutStreakCircles;
        if (layout == null) return;
        int childCount = layout.getChildCount();

        for (int i = 0; i < childCount; i++) {
            View circle = layout.getChildAt(i);
            if (i < streakCount) {
                circle.setBackgroundResource(R.drawable.ic_streak_active);
            } else {
                circle.setBackgroundResource(R.drawable.ic_streak_inactive);
            }
        }
    }

    private void SetUpChart(List<SessionItem> sessionItems) {
        List<Entry> entries = new ArrayList<>();
        List<SessionItem> chronologicalSessions = new ArrayList<>(sessionItems);
        Collections.reverse(chronologicalSessions);

        for (int i = 0; i < chronologicalSessions.size(); i++) {
            entries.add(new Entry(i, chronologicalSessions.get(i).getScore()));
        }

        LineDataSet lineDataSet = new LineDataSet(entries, "Performance");
        lineDataSet.setColor(Color.parseColor("#4A69FF"));
        lineDataSet.setCircleColor(Color.parseColor("#4A69FF"));
        lineDataSet.setLineWidth(2f);
        lineDataSet.setCircleRadius(4f);
        lineDataSet.setDrawValues(false);
        lineDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData lineData = new LineData(lineDataSet);
        binding.lineChart.setData(lineData);

        binding.lineChart.getDescription().setEnabled(false);
        binding.lineChart.getLegend().setEnabled(false);
        binding.lineChart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        binding.lineChart.getXAxis().setDrawGridLines(false);
        binding.lineChart.getAxisLeft().setDrawGridLines(true);
        binding.lineChart.getAxisRight().setEnabled(false);
        binding.lineChart.animateX(1000);
        binding.lineChart.invalidate();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
