package com.example.quicknotes.Activity;

import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.example.quicknotes.Adapter.WidgetNoteSelectionAdapter;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteViewModel;
import com.example.quicknotes.Receiver.PinnedNotesWidgetProvider;
import com.example.quicknotes.Receiver.SingleNoteWidgetProvider;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivitySelectNoteBinding;
import java.util.stream.Collectors;
import com.example.quicknotes.R;

public class ActivitySelectNote extends AppCompatActivity {

    private ActivitySelectNoteBinding binding;
    private int appWidgetsId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private NoteViewModel noteViewModel;
    private WidgetNoteSelectionAdapter noteSelectionAdapter;

    private static final String PREFS_NAME = "com.example.quicknotes.Receiver.WidgetPrefs";
    private static final String PREF_PREFIX_KEY = "appwidget_";

    @Override
    protected void attachBaseContext(Context newBase) {
        Context langContext = LanguageHelper.onAttach(newBase);
        Context finalContext = FontSizeHelper.onAttach(langContext);
        super.attachBaseContext(finalContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySelectNoteBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setResult(RESULT_CANCELED);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();

        if (extras != null){
            appWidgetsId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (appWidgetsId == AppWidgetManager.INVALID_APPWIDGET_ID){
            finish();
            return;
        }

        noteViewModel = new ViewModelProvider(this).get(NoteViewModel.class);
        SetUpRecycerview();
        ObserveNotes();
    }

    private void SetUpRecycerview(){
        noteSelectionAdapter = new WidgetNoteSelectionAdapter(this::onNoteSelected);
        binding.rvConfigNotes.setLayoutManager(new LinearLayoutManager(this));
        binding.rvConfigNotes.setAdapter(noteSelectionAdapter);
    }

    private void ObserveNotes(){
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetsId);
        String providerClassName = (info != null && info.provider != null) ? info.provider.getClassName() : "";
        
        noteViewModel.getAllNotes().observe(this, notes -> {
            if (providerClassName.contains("PinnedNotesWidgetProvider")) {
                binding.txtConfigTitle.setText(R.string.select_pinned_note);
                noteSelectionAdapter.setNotes(notes.stream()
                        .filter(Note::isPinned)
                        .filter(n -> !n.isDeleted() && !n.isArchived())
                        .collect(Collectors.toList()));
            } else {
                binding.txtConfigTitle.setText(R.string.select_note);
                noteSelectionAdapter.setNotes(notes.stream()
                        .filter(n -> !n.isDeleted() && !n.isArchived())
                        .collect(Collectors.toList()));
            }
        });
    }

    private void onNoteSelected(Note note) {
        saveNoteIdPref(this, appWidgetsId, note.getId());

        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(this);
        AppWidgetProviderInfo info = appWidgetManager.getAppWidgetInfo(appWidgetsId);
        String providerClassName = (info != null && info.provider != null) ? info.provider.getClassName() : "";

        if (providerClassName.contains("PinnedNotesWidgetProvider")) {
            PinnedNotesWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetsId);
        } else {
            SingleNoteWidgetProvider.updateAppWidget(this, appWidgetManager, appWidgetsId);
        }

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetsId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    public static void saveNoteIdPref(Context context, int appWidgetId, int noteId) {
        SharedPreferences.Editor prefs = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefs.putInt(PREF_PREFIX_KEY + appWidgetId, noteId);
        prefs.apply();
    }

    public static int loadNoteIdPref(Context context, int appWidgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        return prefs.getInt(PREF_PREFIX_KEY + appWidgetId, -1);
    }
}