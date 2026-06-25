package com.example.quicknotes.Activity;

import android.content.Context;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.quicknotes.Adapter.DeletedNotesAdapter;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.Fragments.SearchFragment;
import com.example.quicknotes.R;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivityRecycleBinBinding;

import java.util.List;

public class RecycleBinActivity extends AppCompatActivity {

    private ActivityRecycleBinBinding binding;
    private NoteViewModel noteViewModel;
    private DeletedNotesAdapter adapter;

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
        binding = ActivityRecycleBinBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);

        getSupportFragmentManager().addOnBackStackChangedListener(() -> {
            if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
                binding.fragmentContainer.setVisibility(View.GONE);
            }
        });

        setupToolbar();
        setupRecyclerView();
        observeDeletedNotes();
        setupSearch();
        setupSelectionActions();
    }

    private void setupToolbar() {
        binding.toolbarRecycleBin.setNavigationOnClickListener(v -> finish());
        binding.imgEmptyBin.setOnClickListener(v -> showEmptyBinDialog());

        binding.toolbarSelectionBin.setNavigationOnClickListener(v -> setSelectionMode(false));
        
        binding.cbSelectAll.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                adapter.selectAll();
            } else {
                if (adapter.getSelectedNoteIds().size() == adapter.getItemCount()) {
                    adapter.clearSelection();
                }
            }
        });
    }

    private void setupRecyclerView() {
        adapter = new DeletedNotesAdapter(new DeletedNotesAdapter.OnNoteClickListener() {
            @Override
            public void onNoteClick(Note note) {
                showRestoreDeleteDialog(note);
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
                    binding.txtSelectionCount.setText(count + " Selected");
                    binding.cbSelectAll.setChecked(count == adapter.getItemCount());
                }
            }
        });
        binding.rvRecycleBin.setLayoutManager(new LinearLayoutManager(this));
        binding.rvRecycleBin.setAdapter(adapter);
    }

    private void setSelectionMode(boolean isSelectionMode) {
        if ((binding.toolbarSelectionBin.getVisibility() == View.VISIBLE) == isSelectionMode) return;

        if (isSelectionMode) {
            binding.toolbarRecycleBin.setVisibility(View.GONE);
            binding.toolbarSelectionBin.setVisibility(View.VISIBLE);
            binding.layoutSelectionActions.setVisibility(View.VISIBLE);
            adapter.setSelectionMode(true);
        } else {
            binding.toolbarRecycleBin.setVisibility(View.VISIBLE);
            binding.toolbarSelectionBin.setVisibility(View.GONE);
            binding.layoutSelectionActions.setVisibility(View.GONE);
            adapter.setSelectionMode(false);
            binding.cbSelectAll.setChecked(false);
        }
    }

    private void setupSelectionActions() {
        binding.btnRestoreBin.setOnClickListener(v -> {
            List<Integer> ids = adapter.getSelectedNoteIds();
            for (int id : ids) {
                Note note = noteViewModel.getNoteById(id);
                if (note != null) {
                    note.setDeleted(false);
                    noteViewModel.update(note);
                }
            }
            Toast.makeText(this, ids.size() + " notes restored", Toast.LENGTH_SHORT).show();
            setSelectionMode(false);
        });

        binding.btnDeleteBin.setOnClickListener(v -> {
            View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirm, null);
            AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialog)
                    .setView(dialogView)
                    .create();

            if (dialog.getWindow() != null) {
                dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            }

            TextView txtMessage = dialogView.findViewById(R.id.txtMessage);
            txtMessage.setText("Are you sure you want to delete the note(s) permanently?");

            dialogView.findViewById(R.id.btnCancel).setOnClickListener(v1 -> dialog.dismiss());
            dialogView.findViewById(R.id.btnDelete).setOnClickListener(v1 -> {
                List<Integer> ids = adapter.getSelectedNoteIds();
                for (int id : ids) {
                    Note note = noteViewModel.getNoteById(id);
                    if (note != null) {
                        noteViewModel.delete(note);
                    }
                }
                Toast.makeText(this, ids.size() + " notes deleted", Toast.LENGTH_SHORT).show();
                setSelectionMode(false);
                dialog.dismiss();
            });

            dialog.show();
        });
    }

    private void observeDeletedNotes() {
        noteViewModel.getDeletedNotes().observe(this, this::updateUI);
    }

    private void updateUI(List<Note> notes) {
        adapter.setNotes(notes);
        if (notes == null || notes.isEmpty()) {
            binding.layoutEmptyBin.setVisibility(View.VISIBLE);
            binding.rvRecycleBin.setVisibility(View.GONE);
            binding.imgEmptyBin.setVisibility(View.GONE);
        } else {
            binding.layoutEmptyBin.setVisibility(View.GONE);
            binding.rvRecycleBin.setVisibility(View.VISIBLE);
            binding.imgEmptyBin.setVisibility(View.VISIBLE);
        }
    }

    private void setupSearch() {
        binding.etSearchBin.setOnClickListener(v -> {
            binding.fragmentContainer.setVisibility(View.VISIBLE);
            SearchFragment searchFragment = SearchFragment.newInstance(SearchFragment.SearchMode.DELETED);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, searchFragment)
                    .addToBackStack(null)
                    .commit();
        });
        
        // Also keep the existing listener if they don't want to use the fragment yet, 
        // but since they asked for fragment like archived screen:
        binding.etSearchBin.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.etSearchBin.callOnClick();
            }
        });
    }

    private void showRestoreDeleteDialog(Note note) {
        if (adapter.isSelectionMode()) return;
        
        String[] options = {"Restore", "Delete permanently"};
        new AlertDialog.Builder(this)
                .setTitle(note.getTitle())
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        note.setDeleted(false);
                        noteViewModel.update(note);
                        Toast.makeText(this, "Note restored", Toast.LENGTH_SHORT).show();
                    } else {
                        showDeleteConfirmDialog(note);
                    }
                })
                .show();
    }

    private void showDeleteConfirmDialog(Note note) {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_delete_confirm, null);
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.CustomDialog)
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
            Toast.makeText(this, "Note deleted permanently", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    private void showEmptyBinDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Empty Recycle bin?")
                .setMessage("All notes in Recycle bin will be permanently deleted.")
                .setPositiveButton("Empty", (dialog, which) -> {
                    noteViewModel.emptyRecycleBin();
                    Toast.makeText(this, "Recycle bin emptied", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
