package com.example.interviewace.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.interviewace.R;
import com.example.interviewace.ViewModel.InterviewViewModel;
import com.example.interviewace.adapter.RoleCardAdapter;
import com.example.interviewace.adapter.SessionAdapter;
import com.example.interviewace.databinding.FragmentHomeBinding;
import com.example.interviewace.model.RoleItem;
import com.example.interviewace.model.SessionItem;
import com.example.interviewace.ui.activity.ActivitySessionDetails;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class HomeFragment extends Fragment implements SessionAdapter.OnSessionClickListener, RoleCardAdapter.OnRoleCardClickListener {

    private FragmentHomeBinding binding;
    private InterviewViewModel viewModel;
    private SessionAdapter sessionAdapter;
    private RoleCardAdapter roleAdapter;
    private List<SessionItem> sessionList;
    private List<RoleItem> roleList;
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault());

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(InterviewViewModel.class);

        setupRecyclerViews();
        setupUserInfo();
        observeViewModel();
        setupClickListeners();

        String currentUserId = FirebaseAuth.getInstance().getUid();
        if (currentUserId != null) {
            viewModel.loadSessions(currentUserId);
            viewModel.loadUnreadNotificationsCount(currentUserId);
        }

        viewModel.loadRoles();
    }

    private void setupClickListeners() {
        binding.btnStartInterview.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.nav_practice);
        });

        binding.relativeNotification.setOnClickListener(v -> {
            // Reset badge when clicked (simulating reading notifications)
            binding.tvNotificationBadge.setVisibility(View.GONE);
            Toast.makeText(getContext(), "Notifications coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void setupUserInfo() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String name = user.getDisplayName();
            String email = user.getEmail();

            if (name == null || name.trim().isEmpty()) {
                if (email != null && email.contains("@")) {
                    name = email.split("@")[0];
                } else {
                    name = "User";
                }
            }

            binding.textGreetingUser.setText("Hi, " + name);
            binding.tvProfileInitial.setText(String.valueOf(name.charAt(0)).toUpperCase());
        }
    }

    private void setupRecyclerViews() {
        sessionList = new ArrayList<>();
        sessionAdapter = new SessionAdapter(sessionList, this);
        binding.rvSessions.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.rvSessions.setNestedScrollingEnabled(false);
        binding.rvSessions.setAdapter(sessionAdapter);

        roleList = new ArrayList<>();
        roleAdapter = new RoleCardAdapter(roleList, this);
        binding.rvRoles.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.rvRoles.setNestedScrollingEnabled(false);
        binding.rvRoles.setAdapter(roleAdapter);
    }

    private void observeViewModel() {
        viewModel.getSessions().observe(getViewLifecycleOwner(), sessions -> {
            if (sessions == null || sessions.isEmpty()) {
                sessionList.clear();
                sessionAdapter.notifyDataSetChanged();
                binding.pbGoal.setProgress(0);
                binding.tvGoalStatus.setText("0 of 3 sessions completed");
                binding.tvStreakCount.setText("0");
                binding.tvStreakTitle.setText("0 Day Streak");
                return;
            }

            List<SessionItem> sortedSessions = new ArrayList<>(sessions);
            sortSessionsByDate(sortedSessions);

            sessionList.clear();
            for (int i = 0; i < Math.min(sortedSessions.size(), 5); i++) {
                sessionList.add(sortedSessions.get(i));
            }
            sessionAdapter.notifyDataSetChanged();

            int completedToday = countCompletedToday(sortedSessions);
            binding.pbGoal.setProgress(Math.min((completedToday * 100) / 3, 100));
            binding.tvGoalStatus.setText(completedToday + " of 3 sessions completed");

            int streak = calculateStreak(sortedSessions);
            binding.tvStreakCount.setText(String.valueOf(streak));
            binding.tvStreakTitle.setText(streak + " Day Streak");
        });

        viewModel.getRoles().observe(getViewLifecycleOwner(), roles -> {
            if (roles != null) {
                roleList.clear();
                roleList.addAll(roles);
                roleAdapter.notifyDataSetChanged();
            }
        });

        viewModel.getUnreadNotificationsCount().observe(getViewLifecycleOwner(), count -> {
            if (count != null && count > 0) {
                binding.tvNotificationBadge.setVisibility(View.VISIBLE);
                binding.tvNotificationBadge.setText(String.valueOf(count));
            } else {
                binding.tvNotificationBadge.setVisibility(View.GONE);
            }
        });

        viewModel.getErrorMessage().observe(getViewLifecycleOwner(), error -> {
            if (error != null) {
                Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sortSessionsByDate(List<SessionItem> sessions) {
        Collections.sort(sessions, (s1, s2) -> {
            try {
                Date d1 = dateFormat.parse(s1.getDate());
                Date d2 = dateFormat.parse(s2.getDate());
                return d2.compareTo(d1);
            } catch (ParseException e) {
                return 0;
            }
        });
    }

    private int countCompletedToday(List<SessionItem> sessions) {
        String today = dateFormat.format(Calendar.getInstance().getTime());
        int count = 0;
        for (SessionItem item : sessions) {
            if (today.equals(item.getDate())) {
                count++;
            }
        }
        return count;
    }

    private int calculateStreak(List<SessionItem> sessions) {
        if (sessions == null || sessions.isEmpty()) return 0;
        Set<String> sessionDates = new HashSet<>();
        for (SessionItem item : sessions) {
            sessionDates.add(item.getDate());
        }
        Calendar cal = Calendar.getInstance();
        int streak = 0;
        if (sessionDates.contains(dateFormat.format(cal.getTime()))) {
            streak++;
            cal.add(Calendar.DATE, -1);
            while (sessionDates.contains(dateFormat.format(cal.getTime()))) {
                streak++;
                cal.add(Calendar.DATE, -1);
            }
        } else {
            cal.add(Calendar.DATE, -1);
            if (sessionDates.contains(dateFormat.format(cal.getTime()))) {
                streak++;
                cal.add(Calendar.DATE, -1);
                while (sessionDates.contains(dateFormat.format(cal.getTime()))) {
                    streak++;
                    cal.add(Calendar.DATE, -1);
                }
            }
        }
        return streak;
    }

    @Override
    public void onSessionClick(int position) {
        if (sessionList != null && position < sessionList.size()) {
            SessionItem clickedItem = sessionList.get(position);
            Intent intent = new Intent(getContext(), ActivitySessionDetails.class);
            intent.putExtra("role", clickedItem.getRoleName());
            intent.putExtra("date", clickedItem.getDate());
            intent.putExtra("score", clickedItem.getScore());
            intent.putExtra("streak", Integer.parseInt(binding.tvStreakCount.getText().toString()));
            ArrayList<String> skills = clickedItem.getSkills() != null ? new ArrayList<>(clickedItem.getSkills()) : new ArrayList<>();
            intent.putStringArrayListExtra("skills", skills);
            startActivity(intent);
        }
    }

    @Override
    public void onRoleCardClick(RoleItem roleItem) {
        Bundle bundle = new Bundle();
        bundle.putString("roleName", roleItem.getRoleName());
        Navigation.findNavController(requireView()).navigate(R.id.nav_practice, bundle);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
