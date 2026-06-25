package com.example.quicknotes.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quicknotes.Activity.AddCheckListActivity;
import com.example.quicknotes.Activity.AddNoteActivity;
import com.example.quicknotes.Adapter.CategoryAdapter;
import com.example.quicknotes.Adapter.NotesAdapter;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.Model.CategoryModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.CategoryPrefs;
import com.example.quicknotes.databinding.FragmentSearchBinding;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    public enum SearchMode {
        NORMAL, ARCHIVED, DELETED
    }

    private FragmentSearchBinding binding;
    private CategoryAdapter categoryAdapter;
    private NotesAdapter notesAdapter;
    private NoteViewModel noteViewModel;
    private LiveData<List<Note>> currentSearchLiveData;
    private SearchMode searchMode = SearchMode.NORMAL;

    public SearchFragment() {
        // Required empty public constructor
    }

    public static SearchFragment newInstance(SearchMode mode) {
        SearchFragment fragment = new SearchFragment();
        Bundle args = new Bundle();
        args.putSerializable("search_mode", mode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            searchMode = (SearchMode) getArguments().getSerializable("search_mode");
            if (searchMode == null) searchMode = SearchMode.NORMAL;
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        binding.imgBack.setOnClickListener(v -> {
            if (getActivity() != null) {
                getActivity().getSupportFragmentManager().popBackStack();
            }
        });

        setupAdapters();

        binding.etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String query = s.toString().trim();
                if (query.isEmpty()) {
                    showFilters(true);
                } else {
                    showFilters(false);
                    binding.txtResultHeader.setText("Search results for \"" + query + "\"");
                    switch (searchMode) {
                        case ARCHIVED:
                            observeNotes(noteViewModel.searchArchivedNotes(query));
                            break;
                        case DELETED:
                            observeNotes(noteViewModel.searchDeletedNotes(query));
                            break;
                        default:
                            observeNotes(noteViewModel.searchNotes(query));
                            break;
                    }
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        // Click listeners for types
        binding.cardTxt.setOnClickListener(v -> {
            showFilters(false);
            binding.txtResultHeader.setText("Note Type: TXT");
            switch (searchMode) {
                case ARCHIVED:
                    observeNotes(noteViewModel.getArchivedNotesByType("TXT"));
                    break;
                case DELETED:
                    // Note: If you have getDeletedNotesByType, use it. Otherwise, use a general search or filter.
                    // For now, let's assume we use regular getNotesByType but the DAO should filter by isDeleted.
                    // But our NoteViewModel doesn't have getDeletedNotesByType yet.
                    // Let's just use search if specific type isn't available, or fallback.
                    observeNotes(noteViewModel.getNotesByType("TXT")); 
                    break;
                default:
                    observeNotes(noteViewModel.getNotesByType("TXT"));
                    break;
            }
        });

        binding.cardChecklist.setOnClickListener(v -> {
            showFilters(false);
            binding.txtResultHeader.setText("Note Type: Checklist");
            switch (searchMode) {
                case ARCHIVED:
                    observeNotes(noteViewModel.getArchivedNotesByType("CHECKLIST"));
                    break;
                case DELETED:
                    observeNotes(noteViewModel.getNotesByType("CHECKLIST"));
                    break;
                default:
                    observeNotes(noteViewModel.getNotesByType("CHECKLIST"));
                    break;
            }
        });

        // If in DELETED mode, we might want to hide folders as they might not be relevant or supported in recycle bin the same way
        if (searchMode == SearchMode.DELETED) {
            binding.txtFolderHeader.setVisibility(View.GONE);
            binding.rvFolder.setVisibility(View.GONE);
        }
    }

    private void observeNotes(LiveData<List<Note>> liveData) {
        if (currentSearchLiveData != null) {
            currentSearchLiveData.removeObservers(getViewLifecycleOwner());
        }
        currentSearchLiveData = liveData;
        currentSearchLiveData.observe(getViewLifecycleOwner(), notes -> {
            notesAdapter.setNotes(notes);
        });
    }

    private void showFilters(boolean show) {
        if (show) {
            binding.layoutFilters.setVisibility(View.VISIBLE);
            binding.layoutResults.setVisibility(View.GONE);
        } else {
            binding.layoutFilters.setVisibility(View.GONE);
            binding.layoutResults.setVisibility(View.VISIBLE);
        }
    }

    private void setupAdapters() {
        // Folder/Category Adapter
        binding.rvFolder.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(requireContext());
        
        // Fetch used categories automatically
        LiveData<List<Note>> notesLiveData;
        if (searchMode == SearchMode.ARCHIVED) {
            notesLiveData = noteViewModel.getArchivedNotes();
        } else if (searchMode == SearchMode.DELETED) {
            notesLiveData = noteViewModel.getDeletedNotes();
        } else {
            notesLiveData = noteViewModel.getAllNotes();
        }

        notesLiveData.observe(getViewLifecycleOwner(), notes -> {
            if (notes != null) {
                java.util.Set<String> usedCategories = new java.util.HashSet<>();
                for (Note note : notes) {
                    if (note.getCategory() != null) {
                        usedCategories.add(note.getCategory());
                    }
                }
                
                CategoryPrefs prefs = new CategoryPrefs(requireContext());
                ArrayList<CategoryModel> categoryModels = new ArrayList<>();
                for (String key : usedCategories) {
                    if ("All".equals(key)) continue;
                    
                    int textColor, bgColor;
                    switch (key) {
                        case "Work":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_work_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_work_bg);
                            break;
                        case "Personal":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_personal_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_personal_bg);
                            break;
                        case "Others":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_others_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_others_bg);
                            break;
                        case "Untitled_Red":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_red_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_red_bg);
                            break;
                        case "Untitled_Orange":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_orange_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_orange_bg);
                            break;
                        case "Untitled_Pink":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_pink_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_pink_bg);
                            break;
                        case "Untitled_Purple":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_bg);
                            break;
                        case "Untitled_DarkGray":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_dark_gray_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_dark_gray_bg);
                            break;
                        case "Untitled_Gray":
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_gray_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_gray_bg);
                            break;
                        default:
                            textColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_text);
                            bgColor = ContextCompat.getColor(requireContext(), R.color.badge_untitled_purple_bg);
                            break;
                    }
                    categoryModels.add(new CategoryModel(key, prefs.getCategoryName(key, key), textColor, bgColor, false));
                }
                categoryAdapter.setCategories(categoryModels);
            }
        });
        
        binding.rvFolder.setAdapter(categoryAdapter);
        
        categoryAdapter.setOnCategoryClickListener(model -> {
            showFilters(false);
            binding.txtResultHeader.setText("Folder: " + model.getCategoryName().replace("Untitled_", ""));
            if (searchMode == SearchMode.ARCHIVED) {
                observeNotes(noteViewModel.getArchivedNotesByCategory(model.getCategoryName()));
            } else if (searchMode == SearchMode.DELETED) {
                // Same here, if getDeletedNotesByCategory exists, use it.
                observeNotes(noteViewModel.getNotesByCategory(model.getCategoryName()));
            } else {
                observeNotes(noteViewModel.getNotesByCategory(model.getCategoryName()));
            }
        });

        // Results Adapter
        binding.rvResults.setLayoutManager(new LinearLayoutManager(requireContext()));
        notesAdapter = new NotesAdapter(note -> {
            if (searchMode == SearchMode.DELETED) {
                showRestoreDeleteDialog(note);
            } else {
                Intent intent;
                if ("CHECKLIST".equals(note.getNoteType())) {
                    intent = new Intent(requireContext(), AddCheckListActivity.class);
                } else {
                    intent = new Intent(requireContext(), AddNoteActivity.class);
                }
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            }
        });
        binding.rvResults.setAdapter(notesAdapter);
    }

    private void showRestoreDeleteDialog(Note note) {
        String[] options = {"Restore", "Delete permanently"};
        new AlertDialog.Builder(requireContext())
                .setTitle(note.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        note.setDeleted(false);
                        noteViewModel.update(note);
                        Toast.makeText(requireContext(), "Note restored", Toast.LENGTH_SHORT).show();
                    } else {
                        showDeleteConfirmDialog(note);
                    }
                })
                .show();
    }

    private void showDeleteConfirmDialog(Note note) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialog)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        TextView txtMessage = dialogView.findViewById(R.id.txtMessage);
        txtMessage.setText("Are you sure you want to delete this note permanently?");

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            noteViewModel.delete(note);
            Toast.makeText(requireContext(), "Note deleted permanently", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
