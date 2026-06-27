package com.example.quicknotes.Activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quicknotes.Adapter.ArchivedNotesAdapter;
import com.example.quicknotes.Adapter.CategoryAdapter;
import com.example.quicknotes.BottomSheet.EditCategoriesBottomSheet;
import com.example.quicknotes.BottomSheet.SortBottomSheet;
import com.example.quicknotes.BottomSheet.ViewSelectionBottomSheet;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.Fragments.SearchFragment;
import com.example.quicknotes.Model.CategoryModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.CategoryPrefs;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivityArchievedNotesBinding;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

public class ArchievedNotesActivity extends AppCompatActivity {

    private ActivityArchievedNotesBinding binding;
    private NoteViewModel noteViewModel;
    private ArchivedNotesAdapter archivedNotesAdapter;
    private CategoryAdapter categoryAdapter;
    private String selectedCategory = "All";
    private int currentSortId = R.id.radio_b_ModifiedNew;
    private LiveData<List<Note>> currentNotesLiveData;
    private ViewSelectionBottomSheet.ViewType currentViewType = ViewSelectionBottomSheet.ViewType.DETAILS;
    private androidx.activity.result.ActivityResultLauncher<Intent> lockLauncher;
    private Note noteToOpen;
    private List<Note> pendingUnarchiveNotes;
    private List<Note> pendingDeleteNotes;
    private boolean isUnlockingSelection = false;

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
        binding = ActivityArchievedNotesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setSupportActionBar(binding.toolbarArchive);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        lockLauncher = registerForActivityResult(new androidx.activity.result.contract.ActivityResultContracts.StartActivityForResult(), result -> {
            if (result.getResultCode() == android.app.Activity.RESULT_OK) {
                if (noteToOpen != null) {
                    openNoteActivity(noteToOpen);
                    noteToOpen = null;
                } else if (pendingUnarchiveNotes != null) {
                    performUnarchive(pendingUnarchiveNotes);
                    pendingUnarchiveNotes = null;
                } else if (pendingDeleteNotes != null) {
                    performMoveToRecycleBin(pendingDeleteNotes);
                    pendingDeleteNotes = null;
                } else if (isUnlockingSelection) {
                    performLockUnlock(false);
                    isUnlockingSelection = false;
                }
            } else {
                noteToOpen = null;
                pendingUnarchiveNotes = null;
                pendingDeleteNotes = null;
                isUnlockingSelection = false;
            }
        });

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                binding.fragmentContainer.setVisibility(View.GONE);
            }
        });

        setupNotesRecyclerView();
        setupCategoryRecyclerView();
        setupClickListeners();

        updateNotesList();

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (archivedNotesAdapter.isSelectionMode()) {
                    setSelectionMode(false);
                } else {
                    finish();
                }
            }
        });
    }

    private void setupNotesRecyclerView() {
        binding.rvArchivedNotes.setLayoutManager(new LinearLayoutManager(this));
        archivedNotesAdapter = new ArchivedNotesAdapter(new ArchivedNotesAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                if (note.isLocked()) {
                    noteToOpen = note;
                    Intent intent = new Intent(ArchievedNotesActivity.this, PatternLockActivity.class);
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
                    binding.txtSelectionCountArchive.setText(count + " Selected");
                    updateLockIconInSelectionBar();
                }
            }
        });
        binding.rvArchivedNotes.setAdapter(archivedNotesAdapter);
    }

    private void openNoteActivity(Note note) {
        Intent intent;
        if ("CHECKLIST".equals(note.getNoteType())) {
            intent = new Intent(ArchievedNotesActivity.this, AddCheckListActivity.class);
        } else {
            intent = new Intent(ArchievedNotesActivity.this, AddNoteActivity.class);
        }
        intent.putExtra("note_id", note.getId());
        startActivity(intent);
    }

    private void setupCategoryRecyclerView() {
        binding.rvArchiveCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        categoryAdapter = new CategoryAdapter(this);
        binding.rvArchiveCategories.setAdapter(categoryAdapter);

        categoryAdapter.setOnCategoryClickListener(model -> {
            selectedCategory = model.getCategoryKey();
            updateNotesList();
        });
    }

    private void setupClickListeners() {
        binding.toolbarArchive.setNavigationOnClickListener(v -> {
            if (archivedNotesAdapter.isSelectionMode()) {
                setSelectionMode(false);
            } else {
                finish();
            }
        });
        binding.txtEditCategoriesArchive.setOnClickListener(v -> showEditCategoriesBottomSheet());
        binding.imgCloseSelectionArchive.setOnClickListener(v -> setSelectionMode(false));
        binding.btnUnarchiveSelection.setOnClickListener(v -> unarchiveSelectedNotes());
        binding.btnDeleteSelectionArchive.setOnClickListener(v -> showDeleteConfirmDialog());
        binding.btnLockSelectionArchive.setOnClickListener(v -> toggleLockSelectedNotes());
        binding.imgPinSelectionArchive.setOnClickListener(v -> togglePinSelectedNotes());
        binding.imgReminderSelectionArchive.setOnClickListener(v -> Toast.makeText(this, "Reminder clicked", Toast.LENGTH_SHORT).show());
    }

    private void updateNotesList() {
        LiveData<List<Note>> sortedLiveData;
        boolean isAll = selectedCategory.equalsIgnoreCase("All");

        if (currentSortId == R.id.radio_b_Folder) {
            sortedLiveData = isAll ? noteViewModel.sortArchivedByCategory() : noteViewModel.sortArchivedByCategoryFilter(selectedCategory);
        } else if (currentSortId == R.id.radio_b_NameAZ) {
            sortedLiveData = isAll ? noteViewModel.sortArchivedNameAZ() : noteViewModel.sortArchivedNameAZFilter(selectedCategory);
        } else if (currentSortId == R.id.radio_b_NameZA) {
            sortedLiveData = isAll ? noteViewModel.sortArchivedNameZA() : noteViewModel.sortArchivedNameZAFilter(selectedCategory);
        } else if (currentSortId == R.id.radio_b_Reminder) {
            sortedLiveData = isAll ? noteViewModel.sortArchivedReminderTime() : noteViewModel.sortArchivedReminderFilter(selectedCategory);
        } else if (currentSortId == R.id.radio_b_ModifiedNew) {
            sortedLiveData = isAll ? noteViewModel.sortArchivedModifiedNew() : noteViewModel.sortArchivedModifiedNewFilter(selectedCategory);
        } else if (currentSortId == R.id.radio_b_ModifiedOld) {
            sortedLiveData = isAll ? noteViewModel.sortArchivedModifiedOld() : noteViewModel.sortArchivedModifiedOldFilter(selectedCategory);
        } else if (currentSortId == R.id.radio_b_CreatedNew) {
            sortedLiveData = isAll ? noteViewModel.sortArchivedCreatedNew() : noteViewModel.sortArchivedCreatedNewFilter(selectedCategory);
        } else if (currentSortId == R.id.radio_b_CreatedOld) {
            sortedLiveData = isAll ? noteViewModel.sortArchivedCreatedOld() : noteViewModel.sortArchivedCreatedOldFilter(selectedCategory);
        } else {
            sortedLiveData = isAll ? noteViewModel.getArchivedNotes() : noteViewModel.getArchivedNotesByCategory(selectedCategory);
        }
        observeNotes(sortedLiveData);
    }

    private void observeNotes(LiveData<List<Note>> liveData) {
        if (currentNotesLiveData != null) {
            currentNotesLiveData.removeObservers(this);
        }
        currentNotesLiveData = liveData;
        currentNotesLiveData.observe(this, notes -> {
            archivedNotesAdapter.setNotes(notes);
            updateEmptyState(notes);
        });
    }

    private void updateEmptyState(List<Note> notes) {
        if (notes == null || notes.isEmpty()) {
            binding.layoutEmptyArchive.setVisibility(View.VISIBLE);
            binding.rvArchivedNotes.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyArchive.setVisibility(View.GONE);
            binding.rvArchivedNotes.setVisibility(View.VISIBLE);
        }
    }

    private void setSelectionMode(boolean isSelectionMode) {
        if (isSelectionMode) {
            binding.toolbarArchive.setVisibility(View.GONE);
            binding.layoutSelectionHeaderArchive.setVisibility(View.VISIBLE);
            binding.layoutSelectionBottomArchive.setVisibility(View.VISIBLE);
            archivedNotesAdapter.setSelectionMode(true);
            int count = archivedNotesAdapter.getSelectedNoteIds().size();
            binding.txtSelectionCountArchive.setText(count + " Selected");
            updateLockIconInSelectionBar();
        } else {
            binding.toolbarArchive.setVisibility(View.VISIBLE);
            binding.layoutSelectionHeaderArchive.setVisibility(View.GONE);
            binding.layoutSelectionBottomArchive.setVisibility(View.GONE);
            archivedNotesAdapter.setSelectionMode(false);
        }
    }

    private void togglePinSelectedNotes() {
        List<Integer> selectedIds = archivedNotesAdapter.getSelectedNoteIds();
        for (int id : selectedIds) {
            Note note = noteViewModel.getNoteById(id);
            if (note != null) {
                note.setPinned(!note.isPinned());
                noteViewModel.update(note);
            }
        }
        setSelectionMode(false);
        Toast.makeText(this, "Pin status updated", Toast.LENGTH_SHORT).show();
    }

    private void toggleLockSelectedNotes() {
        android.content.SharedPreferences prefs = getSharedPreferences("security_prefs", Context.MODE_PRIVATE);
        if (!prefs.getBoolean("is_enabled", false)) {
            Toast.makeText(this, R.string.set_password_first, Toast.LENGTH_SHORT).show();
            return;
        }

        List<Integer> selectedIds = archivedNotesAdapter.getSelectedNoteIds();
        if (selectedIds.isEmpty()) return;

        // Determine if we should lock or unlock based on the first note
        Note firstNote = noteViewModel.getNoteById(selectedIds.get(0));
        if (firstNote == null) return;
        
        boolean shouldLock = !firstNote.isLocked();

        if (!shouldLock) {
            // Unlocking requires pattern
            isUnlockingSelection = true;
            Intent intent = new Intent(this, PatternLockActivity.class);
            intent.putExtra("extra_mode", "mode_verify");
            lockLauncher.launch(intent);
        } else {
            performLockUnlock(true);
        }
    }

    private void performLockUnlock(boolean shouldLock) {
        List<Integer> selectedIds = archivedNotesAdapter.getSelectedNoteIds();
        for (int id : selectedIds) {
            Note note = noteViewModel.getNoteById(id);
            if (note != null) {
                note.setLocked(shouldLock);
                noteViewModel.update(note);
            }
        }
        setSelectionMode(false);
        String msg = shouldLock ? "Notes locked" : "Notes unlocked";
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    private void updateLockIconInSelectionBar() {
        List<Integer> selectedIds = archivedNotesAdapter.getSelectedNoteIds();
        if (selectedIds.isEmpty()) return;

        // Check if all selected notes are locked
        boolean allLocked = true;
        for (int id : selectedIds) {
            Note note = noteViewModel.getNoteById(id);
            if (note != null && !note.isLocked()) {
                allLocked = false;
                break;
            }
        }

        if (allLocked) {
            // Show unlock icon
            binding.imgLockSelectionArchive.setImageResource(R.drawable.ic_unlock_custom);
        } else {
            // Show lock icon
            binding.imgLockSelectionArchive.setImageResource(R.drawable.ic_lock_settings);
        }
    }

    private void showSelectionMorePopup(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.getMenu().add("Select All");
        popupMenu.getMenu().add("Lock");

        popupMenu.setOnMenuItemClickListener(item -> {
            String title = item.getTitle() != null ? item.getTitle().toString() : "";
            if ("Select All".equals(title)) {
                archivedNotesAdapter.selectAll();
            } else if ("Lock".equals(title)) {
                Toast.makeText(this, "Lock clicked", Toast.LENGTH_SHORT).show();
            }
            return true;
        });
        popupMenu.show();
    }

    private void unarchiveSelectedNotes() {
        List<Integer> selectedIds = archivedNotesAdapter.getSelectedNoteIds();
        List<Note> notesToUnarchive = new ArrayList<>();
        boolean hasLocked = false;
        for (int id : selectedIds) {
            Note note = noteViewModel.getNoteById(id);
            if (note != null) {
                notesToUnarchive.add(note);
                if (note.isLocked()) hasLocked = true;
            }
        }

        if (hasLocked) {
            pendingUnarchiveNotes = notesToUnarchive;
            Intent intent = new Intent(this, PatternLockActivity.class);
            intent.putExtra("extra_mode", "mode_verify");
            lockLauncher.launch(intent);
        } else {
            performUnarchive(notesToUnarchive);
        }
    }

    private void performUnarchive(List<Note> notes) {
        for (Note note : notes) {
            note.setArchived(false);
            noteViewModel.update(note);
        }
        setSelectionMode(false);
        showUndoSnackbar(notes, true);
    }

    private void showDeleteConfirmDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialog)
                .setView(dialogView)
                .create();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        dialogView.findViewById(R.id.btnCancel).setOnClickListener(v -> dialog.dismiss());
        dialogView.findViewById(R.id.btnDelete).setOnClickListener(v -> {
            List<Integer> selectedIds = archivedNotesAdapter.getSelectedNoteIds();
            List<Note> notesToDelete = new ArrayList<>();
            boolean hasLocked = false;
            for (int id : selectedIds) {
                Note note = noteViewModel.getNoteById(id);
                if (note != null) {
                    notesToDelete.add(note);
                    if (note.isLocked()) hasLocked = true;
                }
            }

            if (hasLocked) {
                pendingDeleteNotes = notesToDelete;
                Intent intent = new Intent(this, PatternLockActivity.class);
                intent.putExtra("extra_mode", "mode_verify");
                lockLauncher.launch(intent);
            } else {
                performMoveToRecycleBin(notesToDelete);
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    private void performMoveToRecycleBin(List<Note> notes) {
        for (Note note : notes) {
            noteViewModel.moveToRecycleBin(note);
        }
        setSelectionMode(false);
        showUndoSnackbar(notes, false);
    }

    private void showUndoSnackbar(List<Note> notes, boolean unarchived) {
        String message = unarchived ? "Note unarchived" : "Moved to Recycle bin";
        Snackbar snackbar = Snackbar.make(binding.main, message, Snackbar.LENGTH_LONG);
        snackbar.setAction("UNDO", v -> {
            for (Note note : notes) {
                if (unarchived) {
                    note.setArchived(true);
                } else {
                    note.setDeleted(false);
                }
                noteViewModel.update(note);
            }
        });
        snackbar.setActionTextColor(ContextCompat.getColor(this, R.color.primary_blue));
        snackbar.show();
    }

    private void showEditCategoriesBottomSheet() {
        EditCategoriesBottomSheet bottomSheet = new EditCategoriesBottomSheet(categoryAdapter.getCategoryList(), EditCategoriesBottomSheet.Mode.EDIT);
        bottomSheet.setOnCategoriesUpdatedListener(updatedList -> {
            CategoryPrefs prefs = new CategoryPrefs(this);
            ArrayList<CategoryModel> fullList = new ArrayList<>();
            fullList.add(new CategoryModel("All",
                    prefs.getCategoryName("All", "All"),
                    ContextCompat.getColor(this, R.color.primary_blue),
                    ContextCompat.getColor(this, R.color.light_sky_blue),
                    true));
            fullList.addAll(updatedList);
            categoryAdapter.setCategories(fullList);
        });
        bottomSheet.show(getSupportFragmentManager(), "EditCategoriesBottomSheet");
    }

    private void showSortBottomSheet() {
        SortBottomSheet sortBottomSheet = new SortBottomSheet();
        sortBottomSheet.setOnSortOptionSelectedListener(id -> {
            currentSortId = id;
            updateNotesList();
        });
        sortBottomSheet.show(getSupportFragmentManager(), "SortBottomSheet");
    }

    private void showViewSelectionBottomSheet() {
        ViewSelectionBottomSheet bottomSheet = new ViewSelectionBottomSheet();
        bottomSheet.setCurrentViewType(currentViewType);
        bottomSheet.setOnViewTypeSelectedListener(viewType -> {
            currentViewType = viewType;
            updateRecyclerViewLayout();
        });
        bottomSheet.show(getSupportFragmentManager(), "ViewSelectionBottomSheet");
    }

    private void updateRecyclerViewLayout() {
        if (currentViewType == ViewSelectionBottomSheet.ViewType.GRID) {
            binding.rvArchivedNotes.setLayoutManager(new androidx.recyclerview.widget.GridLayoutManager(this, 2));
        } else {
            binding.rvArchivedNotes.setLayoutManager(new LinearLayoutManager(this));
        }
        archivedNotesAdapter.setViewType(currentViewType);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.archive_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_search_archive) {
            binding.fragmentContainer.setVisibility(View.VISIBLE);
            SearchFragment searchFragment = SearchFragment.newInstance(SearchFragment.SearchMode.ARCHIVED);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .addToBackStack(null)
                    .commit();
            return true;
        } else if (id == R.id.menu_sort_archive) {
            showSortBottomSheet();
            return true;
        } else if (id == R.id.menu_select_archive) {
            setSelectionMode(true);
            return true;
        } else if (id == R.id.menu_view_archive) {
            showViewSelectionBottomSheet();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
