package com.example.quicknotes.Receiver;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.quicknotes.Activity.ActivitySelectNote;
import com.example.quicknotes.Activity.MainActivity;
import com.example.quicknotes.Database.Note;
import com.example.quicknotes.Database.NoteDatabase;
import com.example.quicknotes.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PinnedNotesWidgetProvider extends AppWidgetProvider {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        int noteId = ActivitySelectNote.loadNoteIdPref(context, appWidgetId);
        if (noteId == -1) return;

        final Context appContext = context.getApplicationContext();
        RemoteViews remoteViews = new RemoteViews(appContext.getPackageName(), R.layout.widget_2x2);

        executor.execute(() -> {
            Note note = NoteDatabase.getInstance(appContext).noteDao().getNoteById(noteId);
            if (note != null) {
                remoteViews.setTextViewText(R.id.widgetTitle, note.getTitle());
                remoteViews.setTextViewText(R.id.widgetContent, note.getDescription());

                Intent intent = new Intent(appContext, MainActivity.class);
                intent.putExtra("target_fragment", "home");
                PendingIntent pendingIntent = PendingIntent.getActivity(appContext, appWidgetId, intent,
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                remoteViews.setOnClickPendingIntent(R.id.widgetTitle, pendingIntent);
                remoteViews.setOnClickPendingIntent(R.id.widgetContent, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, remoteViews);
            }
        });
    }
}