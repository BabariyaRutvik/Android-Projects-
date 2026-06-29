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

public class SingleNoteWidgetProvider extends AppWidgetProvider {

    private static final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId);
        }
    }

    public static void updateAppWidget(Context context, AppWidgetManager appWidgetManager, int appWidgetId) {
        int noteId = ActivitySelectNote.loadNoteIdPref(context, appWidgetId);
        if (noteId == -1) return;

        final Context appContext = context.getApplicationContext();
        RemoteViews views = new RemoteViews(appContext.getPackageName(), R.layout.widget_1x1);

        executor.execute(() -> {
            Note note = NoteDatabase.getInstance(appContext).noteDao().getNoteById(noteId);
            if (note != null) {
                views.setTextViewText(R.id.widgetTitle, note.getTitle());
                views.setTextViewText(R.id.widgetContent, note.getDescription());

                Intent intent = new Intent(appContext, MainActivity.class);
                intent.putExtra("target_fragment", "home");
                PendingIntent pendingIntent = PendingIntent.getActivity(appContext, appWidgetId, intent, 
                        PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                views.setOnClickPendingIntent(R.id.widgetTitle, pendingIntent);
                views.setOnClickPendingIntent(R.id.widgetContent, pendingIntent);

                appWidgetManager.updateAppWidget(appWidgetId, views);
            }
        });
    }
}