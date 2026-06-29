package com.example.quicknotes.Activity;

import android.app.Activity;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import com.example.quicknotes.Adapter.WidgetAdapter;
import com.example.quicknotes.Model.WidgetModel;
import com.example.quicknotes.R;
import com.example.quicknotes.Receiver.PinnedNotesWidgetProvider;
import com.example.quicknotes.Receiver.ShortcutWidgetProvider;
import com.example.quicknotes.Receiver.SingleNoteWidgetProvider;
import com.example.quicknotes.Utils.FontSizeHelper;
import com.example.quicknotes.Utils.LanguageHelper;
import com.example.quicknotes.databinding.ActivityWidgetSelectionBinding;
import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class WidgetSelectionActivity extends AppCompatActivity {

    private ActivityWidgetSelectionBinding binding;
    private WidgetAdapter widgetAdapter;
    private ActivityResultLauncher<Intent> tutorialLauncher;

    @Override
    protected void attachBaseContext(Context newBase) {
        Context langContext = LanguageHelper.onAttach(newBase);
        Context finalContext = FontSizeHelper.onAttach(langContext);
        super.attachBaseContext(finalContext);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityWidgetSelectionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tutorialLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        requestPinWidgets();
                    }
                }
        );
        SetupRecyclerView();
        SetupListeners();
    }

    private void SetupRecyclerView() {
        List<WidgetModel> widgetList = new ArrayList<>();
        widgetList.add(new WidgetModel("4 X 1", R.drawable.preview_widget_4x1));
        widgetList.add(new WidgetModel("2 X 2", R.drawable.preview_widget_2x2));
        widgetList.add(new WidgetModel("1 X 1", R.drawable.preview_widget_1x1));

        widgetAdapter = new WidgetAdapter(widgetList);
        widgetAdapter.setOnWidgetSelectedListener(widget -> {
            binding.btnAddWidget.setText(R.string.confirm);
        });
        binding.rvWidgets.setLayoutManager(new GridLayoutManager(this, 2));
        binding.rvWidgets.setAdapter(widgetAdapter);
    }

    private void SetupListeners() {
        binding.toolbarWidgets.setNavigationOnClickListener(v -> finish());

        binding.btnTutorial.setOnClickListener(v -> {
            startActivity(new Intent(this, WidgetTutorialActivity.class));
        });

        binding.btnAddWidget.setOnClickListener(v -> {
            WidgetModel selectedWidget = widgetAdapter.getSelectedWidget();
            if (selectedWidget != null) {
                Intent intent = new Intent(this, WidgetTutorialActivity.class);
                intent.putExtra("from_selection", true);
                intent.putExtra("widget_size", selectedWidget.getSize());
                tutorialLauncher.launch(intent);
            } else {
                Toast.makeText(this, R.string.please_select_widget, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestPinWidgets() {
        WidgetModel selectedWidgets = widgetAdapter.getSelectedWidget();
        if (selectedWidgets == null) return;

        Class<?> providerClass;
        switch (selectedWidgets.getSize()) {
            case "4 X 1":
                providerClass = ShortcutWidgetProvider.class;
                break;
            case "2 X 2":
                providerClass = PinnedNotesWidgetProvider.class;
                break;
            case "1 X 1":
                providerClass = SingleNoteWidgetProvider.class;
                break;
            default:
                providerClass = ShortcutWidgetProvider.class;
                break;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            AppWidgetManager appWidgetManager = getSystemService(AppWidgetManager.class);
            ComponentName myProvider = new ComponentName(this, providerClass);

            if (appWidgetManager != null && appWidgetManager.isRequestPinAppWidgetSupported()) {
                Intent pinnedWidgetsCallbackIntent = new Intent(this, providerClass);
                PendingIntent successCallback = PendingIntent.getBroadcast(this, 0, 
                        pinnedWidgetsCallbackIntent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);

                appWidgetManager.requestPinAppWidget(myProvider, null, successCallback);
            } else {
                Toast.makeText(this, R.string.pinned_widgets_not_supported, Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, R.string.pinned_widgets_android_version_error, Toast.LENGTH_SHORT).show();
        }
    }
}