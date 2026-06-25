package com.example.quicknotes.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quicknotes.Activity.AddCheckListActivity;
import com.example.quicknotes.Activity.AddNoteActivity;
import com.example.quicknotes.Adapter.CategoryAdapter;
import com.example.quicknotes.Adapter.NotesAdapter;
import com.example.quicknotes.BottomSheet.AddNoteBottomSheet;
import com.example.quicknotes.BottomSheet.EditCategoriesBottomSheet;
import com.example.quicknotes.BottomSheet.SortBottomSheet;
import com.example.quicknotes.BottomSheet.ViewSelectionBottomSheet;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.Model.CategoryModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.CategoryPrefs;
import com.example.quicknotes.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private NoteViewModel noteViewModel;
    private NotesAdapter notesAdapter;
    private CategoryAdapter categoryAdapter;
    private LiveData<List<Note>> currentNotesLiveData;
    private String selectedCategory = "All";
    private ViewSelectionBottomSheet.ViewType currentViewType = ViewSelectionBottomSheet.ViewType.DETAILS;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        noteViewModel = new ViewModelProvider(requireActivity()).get(NoteViewModel.class);

        NotesAdapterInit();
        CategoryAdapterInit();

        // Initial observation
        observeNotes(noteViewModel.getAllNotes());

        // Click events
        binding.toolbarHome.setOverflowIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_more));
        binding.toolbarHome.setOnMenuItemClickListener(this::onOptionsItemSelected);
        binding.toolbarHome.inflateMenu(R.menu.home_popoup);
        



        binding.imgEdit.setOnClickListener(v -> showEditCategoriesBottomSheet());
        binding.fabAdd.setOnClickListener(v -> {
            AddNoteBottomSheet addNoteBottomSheet = new AddNoteBottomSheet(selectedCategory);
            addNoteBottomSheet.show(getChildFragmentManager(), "AddNoteBottomSheet");
        });

        binding.imgCloseSelection.setOnClickListener(v -> setSelectionMode(false));
        binding.btnDeleteSelection.setOnClickListener(v -> showDeleteConfirmDialog());
        binding.btnMoreSelection.setOnClickListener(this::showSelectionMoreMenu);
        binding.btnArchiveSelection.setOnClickListener(v -> {
            List<Integer> selectedIds = notesAdapter.getSelectedNoteIds();
            List<Note> archivedNotes = new ArrayList<>();
            for (int id : selectedIds) {
                Note note = noteViewModel.getNoteById(id);
                if (note != null) {
                    archivedNotes.add(note);
                    noteViewModel.moveToArchive(note);
                }
            }
            setSelectionMode(false);
            showUndoSnackbar(archivedNotes, getString(R.string.note_archived), true);
        });
        binding.btnFolderSelection.setOnClickListener(v -> showMoveToFolderBottomSheet());

        binding.imgPinSelection.setOnClickListener(v -> {
            List<Integer> selectedIds = notesAdapter.getSelectedNoteIds();
            for (int id : selectedIds) {
                Note note = noteViewModel.getNoteById(id);
                if (note != null) {
                    note.setPinned(!note.isPinned());
                    noteViewModel.update(note);
                }
            }
            setSelectionMode(false);
            Toast.makeText(requireContext(), "Updated Pin status", Toast.LENGTH_SHORT).show();
        });

        binding.imgCalendarSelection.setOnClickListener(v -> Toast.makeText(requireContext(), "Calendar clicked", Toast.LENGTH_SHORT).show());
    }

    private void setSelectionMode(boolean isSelectionMode) {
        if (isSelectionMode) {
            binding.layoutNormalHeader.setVisibility(View.GONE);
            binding.layoutSelectionHeader.setVisibility(View.VISIBLE);
            binding.layoutSelectionBottom.setVisibility(View.VISIBLE);
            binding.txtNotesLabel.setVisibility(View.VISIBLE);
            binding.fabAdd.setVisibility(View.GONE);
            notesAdapter.setSelectionMode(true);
            
            // Initial count update
            int count = notesAdapter.getSelectedNoteIds().size();
            binding.txtSelectionCount.setText(getString(R.string.selected_count, count));
        } else {
            binding.layoutNormalHeader.setVisibility(View.VISIBLE);
            binding.layoutSelectionHeader.setVisibility(View.GONE);
            binding.layoutSelectionBottom.setVisibility(View.GONE);
            binding.txtNotesLabel.setVisibility(View.GONE);
            binding.fabAdd.setVisibility(View.VISIBLE);
            notesAdapter.setSelectionMode(false);
        }
    }

    private void showSelectionMoreMenu(View view) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(requireContext(), view);
        popupMenu.getMenu().add(getString(R.string.select_all));
        popupMenu.getMenu().add(getString(R.string.pin));
        popupMenu.getMenu().add(getString(R.string.lock));

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().equals(getString(R.string.select_all))) {
                notesAdapter.selectAll();
            } else {
                Toast.makeText(requireContext(), item.getTitle() + " clicked", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popupMenu.show();
    }

    private void showDeleteConfirmDialog() {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(requireContext(), R.style.CustomDialog)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            List<Integer> selectedIds = notesAdapter.getSelectedNoteIds();
            List<Note> deletedNotes = new ArrayList<>();
            for (int id : selectedIds) {
                Note note = noteViewModel.getNoteById(id);
                if (note != null) {
                    deletedNotes.add(note);
                    noteViewModel.moveToRecycleBin(note);
                }
            }
            setSelectionMode(false);
            dialog.dismiss();
            showUndoSnackbar(deletedNotes, getString(R.string.moved_to_recycle), false);
        });

        dialog.show();
    }

    private void observeNotes(LiveData<List<Note>> liveData) {
        if (currentNotesLiveData != null) {
            currentNotesLiveData.removeObservers(getViewLifecycleOwner());
        }
        currentNotesLiveData = liveData;
        currentNotesLiveData.observe(getViewLifecycleOwner(), notes -> {
            notesAdapter.setNotes(notes);
            updateEmptyState(notes);
        });
    }

    private void updateEmptyState(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            binding.layoutEmpty.setVisibility(View.VISIBLE);
            binding.recyerviewNotes.setVisibility(View.GONE);
        } else {
            binding.layoutEmpty.setVisibility(View.GONE);
            binding.recyerviewNotes.setVisibility(View.VISIBLE);
        }
    }

    private void showSortBottomSheet() {
        SortBottomSheet sortBottomSheet = new SortBottomSheet();
        sortBottomSheet.setOnSortOptionSelectedListener(id -> {
            LiveData<List<Note>> sortedLiveData;
            
            if (id == R.id.radio_b_Folder) {
                sortedLiveData = noteViewModel.sortByCategory();
            } else if (id == R.id.radio_b_NameAZ) {
                sortedLiveData = selectedCategory.equals("All") ? noteViewModel.sortNameAZ() : noteViewModel.sortNameAZFilter(selectedCategory);
            } else if (id == R.id.radio_b_NameZA) {
                sortedLiveData = selectedCategory.equals("All") ? noteViewModel.sortNameZA() : noteViewModel.sortNameZAFilter(selectedCategory);
            } else if (id == R.id.radio_b_Reminder) {
                sortedLiveData = selectedCategory.equals("All") ? noteViewModel.sortReminderTime() : noteViewModel.sortReminderFilter(selectedCategory);
            } else if (id == R.id.radio_b_ModifiedNew) {
                sortedLiveData = selectedCategory.equals("All") ? noteViewModel.lastModifiedNewToOld() : noteViewModel.lastModifiedNewFilter(selectedCategory);
            } else if (id == R.id.radio_b_ModifiedOld) {
                sortedLiveData = selectedCategory.equals("All") ? noteViewModel.lastModifiedOldToNew() : noteViewModel.lastModifiedOldFilter(selectedCategory);
            } else if (id == R.id.radio_b_CreatedNew) {
                sortedLiveData = selectedCategory.equals("All") ? noteViewModel.creationNewToOld() : noteViewModel.creationNewFilter(selectedCategory);
            } else if (id == R.id.radio_b_CreatedOld) {
                sortedLiveData = selectedCategory.equals("All") ? noteViewModel.creationOldToNew() : noteViewModel.creationOldFilter(selectedCategory);
            } else {
                sortedLiveData = noteViewModel.getAllNotes();
            }
            
            observeNotes(sortedLiveData);
        });
        sortBottomSheet.show(getChildFragmentManager(), "SortBottomSheet");
    }

    private void showViewSelectionBottomSheet() {
        ViewSelectionBottomSheet bottomSheet = new ViewSelectionBottomSheet();
        bottomSheet.setCurrentViewType(currentViewType);
        bottomSheet.setOnViewTypeSelectedListener(viewType -> {
            currentViewType = viewType;
            updateRecyclerViewLayout();
        });
        bottomSheet.show(getChildFragmentManager(), "ViewSelectionBottomSheet");
    }

    private void updateRecyclerViewLayout() {
        if (binding == null) return;

        if (currentViewType == ViewSelectionBottomSheet.ViewType.GRID) {
            binding.recyerviewNotes.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(requireContext(), 2));
        } else {
            binding.recyerviewNotes.setLayoutManager(new LinearLayoutManager(requireContext()));
        }
        
        notesAdapter.setViewType(currentViewType);
    }

    private void showEditCategoriesBottomSheet() {
        EditCategoriesBottomSheet bottomSheet = new EditCategoriesBottomSheet(categoryAdapter.getCategoryList(), EditCategoriesBottomSheet.Mode.EDIT);
        bottomSheet.setOnCategoriesUpdatedListener(updatedList -> {
            CategoryPrefs prefs = new CategoryPrefs(requireContext());
            ArrayList<CategoryModel> fullList = new ArrayList<>();
            fullList.add(new CategoryModel("All", 
                prefs.getCategoryName("All", "All"),
                ContextCompat.getColor(requireContext(), R.color.primary_blue), 
                ContextCompat.getColor(requireContext(), R.color.light_sky_blue), 
                true));
            fullList.addAll(updatedList);
            categoryAdapter.setCategories(fullList);
        });
        bottomSheet.show(getChildFragmentManager(), "EditCategoriesBottomSheet");
    }

    private void showMoveToFolderBottomSheet() {
        EditCategoriesBottomSheet bottomSheet = new EditCategoriesBottomSheet(categoryAdapter.getCategoryList(), EditCategoriesBottomSheet.Mode.MOVE);
        bottomSheet.setOnCategorySelectedListener(category -> {
            List<Integer> selectedIds = notesAdapter.getSelectedNoteIds();
            for (int id : selectedIds) {
                Note note = noteViewModel.getNoteById(id);
                if (note != null) {
                    note.setCategory(category.getCategoryKey());
                    noteViewModel.update(note);
                }
            }
            setSelectionMode(false);
            Toast.makeText(requireContext(), "Moved to " + category.getCategoryName(), Toast.LENGTH_SHORT).show();
        });
        bottomSheet.show(getChildFragmentManager(), "MoveToFolderBottomSheet");
    }

    private void showUndoSnackbar(List<Note> notes, String message, boolean isArchive) {
        com.google.android.material.snackbar.Snackbar snackbar = com.google.android.material.snackbar.Snackbar.make(binding.getRoot(), message, com.google.android.material.snackbar.Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", v -> {
            for (Note note : notes) {
                if (isArchive) {
                    note.setArchived(false);
                } else {
                    note.setDeleted(false);
                }
                noteViewModel.update(note);
            }
        });
        snackbar.setActionTextColor(ContextCompat.getColor(requireContext(), R.color.primary_blue));
        snackbar.show();
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.home_popoup, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_search) {
            SearchFragment searchFragment = SearchFragment.newInstance(SearchFragment.SearchMode.NORMAL);
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fram_container, searchFragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.menu_sort) {
            showSortBottomSheet();
            return true;
        } else if (id == R.id.menu_select) {
            setSelectionMode(true);
            return true;
        } else if (id == R.id.menu_view) {
            showViewSelectionBottomSheet();
            return true;
        } else if (id == R.id.menu_feedback) {
            Toast.makeText(requireContext(), "Feedback clicked", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void NotesAdapterInit() {
        binding.recyerviewNotes.setLayoutManager(
                new LinearLayoutManager(requireContext()));

        notesAdapter = new NotesAdapter(new NotesAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                Intent intent;
                if ("CHECKLIST".equals(note.getNoteType())) {
                    intent = new Intent(requireContext(), AddCheckListActivity.class);
                } else {
                    intent = new Intent(requireContext(), AddNoteActivity.class);
                }
                // Pass note ID for editing existing note
                intent.putExtra("note_id", note.getId());
                startActivity(intent);
            }

            @Override
            public void onNoteLongClick(Note note) {
                setSelectionMode(true);
            }

            @Override
            public void onSelectionChanged(int count) {
                if (count == 0) {
                    setSelectionMode(false);
                } else {
                    binding.txtSelectionCount.setText(getString(R.string.selected_count, count));
                }
            }
        });
        binding.recyerviewNotes.setAdapter(notesAdapter);
    }

    private void CategoryAdapterInit() {
        binding.recyclerviewCategory.setLayoutManager(
                new LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false));

        categoryAdapter = new CategoryAdapter(requireContext());
        binding.recyclerviewCategory.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(model -> {
            selectedCategory= model.getCategoryKey();
            if (selectedCategory.equalsIgnoreCase("All")) {
                observeNotes(noteViewModel.getAllNotes());
            } else {
                observeNotes(noteViewModel.getNotesByCategory(selectedCategory));
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
