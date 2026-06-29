package com.example.quicknotes.Receiver;




import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.widget.RemoteViews;

import com.example.quicknotes.Activity.AddCheckListActivity;
import com.example.quicknotes.Activity.AddNoteActivity;
import com.example.quicknotes.Activity.MainActivity;
import com.example.quicknotes.R;


public class ShortcutWidgetProvider extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        super.onUpdate(context, appWidgetManager, appWidgetIds);

        RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.widget_4x1);

        // 1. Home
        Intent intentHome = new Intent(context, MainActivity.class);
        intentHome.putExtra("target_fragment", "home");
        PendingIntent piAddNotes = PendingIntent.getActivity(context,1,intentHome,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.btnWidgetHome, piAddNotes);

        // 2. Add notes
        Intent intent = new Intent(context, AddNoteActivity.class);
        PendingIntent piAddNotes2 = PendingIntent.getActivity(context, 2, intent,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.btnWidgetAddNote, piAddNotes2);

        // 3. Add Check List
        Intent intentAddChecklist = new Intent(context, AddCheckListActivity.class);
        PendingIntent piAddChecklist = PendingIntent.getActivity(context, 3, intentAddChecklist,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.btnWidgetAddChecklist, piAddChecklist);


        // 4. Search
        Intent intentSearch = new Intent(context, MainActivity.class);
        intentSearch.putExtra("target_fragment", "search");
        PendingIntent piSearch = PendingIntent.getActivity(context, 4, intentSearch,
                PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        remoteViews.setOnClickPendingIntent(R.id.btnWidgetSearch, piSearch);


        int white = context.getResources().getColor(android.R.color.white, null);
        remoteViews.setInt(R.id.btnWidgetHome, "setColorFilter", white);
        remoteViews.setInt(R.id.btnWidgetAddNote, "setColorFilter", white);
        remoteViews.setInt(R.id.btnWidgetAddChecklist, "setColorFilter", white);
        remoteViews.setInt(R.id.btnWidgetSearch, "setColorFilter", white);

        appWidgetManager.updateAppWidget(appWidgetIds,remoteViews);

    }


}