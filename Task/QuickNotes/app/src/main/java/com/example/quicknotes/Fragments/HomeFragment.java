package com.example.quicknotes.Fragments;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.menu.MenuBuilder;
import androidx.appcompat.view.menu.MenuPopupHelper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quicknotes.Activity.AddCheckListActivity;
import com.example.quicknotes.Activity.AddNoteActivity;
import com.example.quicknotes.Activity.ReminderActivity;
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

import com.example.quicknotes.databinding.FragmentHomeBinding;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private NoteViewModel noteViewModel;
    private NotesAdapter notesAdapter;
    private CategoryAdapter categoryAdapter;
    private LiveData<List<Note>> currentNotesLiveData;
    private String selectedCategory = "All";
    private ViewSelectionBottomSheet.ViewType currentViewType = ViewSelectionBottomSheet.ViewType.DETAILS;
    private ActivityResultLauncher<Intent> lockLauncher;
    private Note noteToOpen;
    private boolean isUnlockingSelection = false;
    private List<Note> pendingDeleteNotes;

    public HomeFragment() {
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

        lockLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == Activity.RESULT_OK) {
                if (noteToOpen != null) {
                    openNoteActivity(noteToOpen);
                    noteToOpen = null;
                } else if (isUnlockingSelection) {
                    performLockUnlock(false);
                    isUnlockingSelection = false;
                } else if (pendingDeleteNotes != null) {
                    performMoveToRecycleBin(pendingDeleteNotes);
                    pendingDeleteNotes = null;
                }
            } else {
                noteToOpen = null;
                isUnlockingSelection = false;
                pendingDeleteNotes = null;
            }
        });

        NotesAdapterInit();
        CategoryAdapterInit();

        observeNotes(noteViewModel.getAllNotes());

        binding.toolbarHome.setOverflowIcon(ContextCompat.getDrawable(requireContext(), R.drawable.ic_more));
        binding.toolbarHome.setOnMenuItemClickListener(this::onOptionsItemSelected);
        binding.toolbarHome.inflateMenu(R.menu.home_popoup);

        binding.imgReminderNotes.setOnClickListener(v->{
            Intent intent = new Intent(getActivity(), ReminderActivity.class);
            startActivity(intent);
        });
        



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
            Toast.makeText(requireContext(), R.string.updated_pin_status, Toast.LENGTH_SHORT).show();
        });

       
    }

    private void setSelectionMode(boolean isSelectionMode) {
        if (isSelectionMode) {
            binding.layoutNormalHeader.setVisibility(View.GONE);
            binding.layoutSelectionHeader.setVisibility(View.VISIBLE);
            binding.layoutSelectionBottom.setVisibility(View.VISIBLE);
            binding.txtNotesLabel.setVisibility(View.GONE);
            binding.fabAdd.setVisibility(View.GONE);

            binding.imgPinSelection.setVisibility(View.VISIBLE);
            binding.imgReminderNotes.setVisibility(View.VISIBLE);
            binding.btnMoreSelection.setVisibility(View.VISIBLE);

            int lightGray = ContextCompat.getColor(requireContext(), R.color.light_gray);
            binding.layoutSelectionHeader.setBackgroundColor(lightGray);
            binding.layoutSelectionBottom.setBackgroundColor(lightGray);

            notesAdapter.setSelectionMode(true);
            
            int count = notesAdapter.getSelectedNoteIds().size();
            binding.txtSelectionCount.setText(getString(R.string.selected_count, count));
            updateSelectionUI(count);
        } else {
            binding.layoutNormalHeader.setVisibility(View.VISIBLE);
            binding.layoutSelectionHeader.setVisibility(View.GONE);
            binding.layoutSelectionBottom.setVisibility(View.GONE);
            binding.txtNotesLabel.setVisibility(View.GONE);
            binding.fabAdd.setVisibility(View.VISIBLE);

            binding.imgPinSelection.setVisibility(View.VISIBLE);
            binding.imgReminderNotes.setVisibility(View.VISIBLE);
            binding.btnMoreSelection.setVisibility(View.VISIBLE);
            
            binding.imgPinSelection.setColorFilter(null);
            binding.imgReminderNotes.setColorFilter(null);
            if (binding.btnMoreSelection.getChildCount() > 0) {
                View child = binding.btnMoreSelection.getChildAt(0);
                if (child instanceof ImageView) {
                    ((ImageView) child).setColorFilter(null);
                }
            }

            int surfaceColor = ContextCompat.getColor(requireContext(), R.color.surface_bg);
            binding.layoutSelectionHeader.setBackgroundColor(surfaceColor);
            binding.layoutSelectionBottom.setBackgroundColor(surfaceColor);

            notesAdapter.setSelectionMode(false);
        }
    }

    private void updateSelectionUI(int count) {
        boolean isSingleSelection = (count == 1);
        int activeColor = ContextCompat.getColor(requireContext(), R.color.primary_blue);
        
        binding.imgPinSelection.setVisibility(isSingleSelection ? View.VISIBLE : View.GONE);
        binding.imgPinSelection.setEnabled(isSingleSelection);
        binding.imgPinSelection.setColorFilter(activeColor);
        
        binding.imgReminderNotes.setVisibility(isSingleSelection ? View.VISIBLE : View.GONE);
        binding.imgReminderNotes.setEnabled(isSingleSelection);
        binding.imgReminderNotes.setColorFilter(activeColor);
        
        binding.btnMoreSelection.setEnabled(count > 0);
        if (binding.btnMoreSelection.getChildCount() > 0) {
            View child = binding.btnMoreSelection.getChildAt(0);
            if (child instanceof ImageView) {
                ((ImageView) child).setColorFilter(activeColor);
            }
        }
    }

    @SuppressLint("RestrictedApi")
    private void showSelectionMoreMenu(View view) {
        androidx.appcompat.widget.PopupMenu popupMenu = new androidx.appcompat.widget.PopupMenu(requireContext(), view);
        popupMenu.getMenuInflater().inflate(R.menu.selection_more_menu, popupMenu.getMenu());

        boolean allSelectedCompleted = true;
        List<Integer> selectedIds = notesAdapter.getSelectedNoteIds();
        for (int id : selectedIds) {
            Note note = noteViewModel.getNoteById(id);
            if (note != null && !note.isCompleted()) {
                allSelectedCompleted = false;
                break;
            }
        }

        boolean allSelectedLocked = true;
        for (int id : selectedIds) {
            Note note = noteViewModel.getNoteById(id);
            if (note != null && !note.isLocked()) {
                allSelectedLocked = false;
                break;
            }
        }

        final boolean isChecking = !allSelectedCompleted;
        final boolean isLocking = !allSelectedLocked;

        MenuItem checkItem = popupMenu.getMenu().findItem(R.id.menu_check);
        if (allSelectedCompleted) {
            checkItem.setTitle(R.string.uncheck);
            checkItem.setIcon(R.drawable.ic_checkbox_blank);
        } else {
            checkItem.setTitle(R.string.check);
            checkItem.setIcon(R.drawable.ic_check_menu);
        }

        MenuItem lockItem = popupMenu.getMenu().findItem(R.id.menu_lock);
        if (allSelectedLocked) {
            lockItem.setTitle(R.string.unlock);
            lockItem.setIcon(R.drawable.ic_lock_settings); 
        } else {
            lockItem.setTitle(R.string.lock);
            lockItem.setIcon(R.drawable.ic_lock_settings);
        }

        MenuItem shareItem = popupMenu.getMenu().findItem(R.id.menu_share);
        shareItem.setVisible(true);
        if (selectedIds.size() == 1) {
            Note note = noteViewModel.getNoteById(selectedIds.get(0));
            if (note != null && note.isLocked()) {
                shareItem.setEnabled(false);
                if (shareItem.getIcon() != null) {
                    shareItem.getIcon().setAlpha(130);
                }
            } else {
                shareItem.setEnabled(true);
                if (shareItem.getIcon() != null) {
                    shareItem.getIcon().setAlpha(255);
                }
            }
        } else {
            // Visible but disabled for multiple selection
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
                handleCheckSelection(isChecking);
                return true;
            } else if (itemId == R.id.menu_lock) {
                handleLockSelection(isLocking);
                return true;
            } else if (itemId == R.id.menu_add_widget) {
                Toast.makeText(requireContext(), R.string.add_widget, Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.menu_share) {
                if (!selectedIds.isEmpty()) {
                    Note note = noteViewModel.getNoteById(selectedIds.get(0));
                    if (note != null) {
                        showShareAsDialog(note);
                    }
                }
                return true;
            }
            return false;
        });

        MenuPopupHelper optionsMenu = new MenuPopupHelper(requireContext(), menuBuilder, view);
        optionsMenu.setForceShowIcon(true);
        optionsMenu.show();
    }

    private void showShareAsDialog(Note note) {
        View dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_share_as, null);
        AlertDialog dialog = new com.google.android.material.dialog.MaterialAlertDialogBuilder(requireContext())
                .setView(dialogView)
                .setBackground(new android.graphics.drawable.ColorDrawable(android.graphics.Color.TRANSPARENT))
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
            setSelectionMode(false);
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
                    sb.append(parts[0].equals("1") ? "☑ " : "☐ ").append(parts[1]).append("\n");
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
        setSelectionMode(false);
    }

    private void handleCheckSelection(boolean isChecking) {
        List<Integer> selectedIds = notesAdapter.getSelectedNoteIds();
        for (int id : selectedIds) {
            Note note = noteViewModel.getNoteById(id);
            if (note != null) {
                note.setCompleted(isChecking);
                noteViewModel.update(note);
            }
        }
        setSelectionMode(false);
        String message = isChecking ? getString(R.string.check) : getString(R.string.uncheck);
        Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
    }

    private void handleLockSelection(boolean isLocking) {
        android.content.SharedPreferences prefs = requireContext().getSharedPreferences("security_prefs", android.content.Context.MODE_PRIVATE);
        if (!prefs.getBoolean("is_enabled", false)) {
            Toast.makeText(requireContext(), R.string.set_password_first, Toast.LENGTH_SHORT).show();
            return;
        }

        if (isLocking) {
            performLockUnlock(true);
        } else {
            isUnlockingSelection = true;
            Intent intent = new Intent(requireContext(), com.example.quicknotes.Activity.PatternLockActivity.class);
            intent.putExtra("extra_mode", "mode_verify");
            lockLauncher.launch(intent);
        }
    }

    private void performLockUnlock(boolean isLocking) {
        List<Integer> selectedIds = notesAdapter.getSelectedNoteIds();
        for (int id : selectedIds) {
            Note note = noteViewModel.getNoteById(id);
            if (note != null) {
                note.setLocked(isLocking);
                noteViewModel.update(note);
            }
        }
        setSelectionMode(false);
        String msg = isLocking ? getString(R.string.lock) : getString(R.string.unlock);
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
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
            boolean hasLocked = false;
            for (int id : selectedIds) {
                Note note = noteViewModel.getNoteById(id);
                if (note != null) {
                    deletedNotes.add(note);
                    if (note.isLocked()) hasLocked = true;
                }
            }

            if (hasLocked) {
                pendingDeleteNotes = deletedNotes;
                Intent intent = new Intent(requireContext(), com.example.quicknotes.Activity.PatternLockActivity.class);
                intent.putExtra("extra_mode", "mode_verify");
                lockLauncher.launch(intent);
            } else {
                performMoveToRecycleBin(deletedNotes);
            }
            
            setSelectionMode(false);
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performMoveToRecycleBin(List<Note> notes) {
        for (Note note : notes) {
            noteViewModel.moveToRecycleBin(note);
        }
        showUndoSnackbar(notes, getString(R.string.moved_to_recycle), false);
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
            Toast.makeText(requireContext(), getString(R.string.moved_to_folder, category.getCategoryName()), Toast.LENGTH_SHORT).show();
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
            Toast.makeText(requireContext(), R.string.feedback_suggestion, Toast.LENGTH_SHORT).show();
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
            public void onNoteLongClick(Note note) {
                setSelectionMode(true);
            }

            @Override
            public void onSelectionChanged(int count) {
                if (count == 0) {
                    setSelectionMode(false);
                } else {
                    binding.txtSelectionCount.setText(getString(R.string.selected_count, count));
                    updateSelectionUI(count);
                }
            }
        });
        binding.recyerviewNotes.setAdapter(notesAdapter);
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
