package com.realwidget.ui.mod;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import android.util.SparseArray;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.util.Utils;

import java.util.List;

public class WidgetSelectorDlg extends ListPreference implements Preference.OnPreferenceChangeListener {
    private Activity parent;
    private SparseArray<List<Button>> widgets;

    public WidgetSelectorDlg(Context context) {
        super(context);
        parent = (Activity) context;
        init();
    }

    public WidgetSelectorDlg(Context context, AttributeSet attrs) {
        super(context, attrs);
        parent = (Activity) context;
        init();
    }

    private void init() {
        widgets = Utils.getExistWidget(parent);
        String[] entries = new String[widgets.size()];
        String[] values = new String[widgets.size()];

        for (int i = 0; i < widgets.size(); i++) {
            entries[i] = (i + 1) + "";
            values[i] = widgets.keyAt(i) + "";
        }

        setEntries(entries);
        setEntryValues(values);
        setOnPreferenceChangeListener(this);
    }

    public void onClick() {
        if (widgets.size() == 0) {
            (new AlertDialog.Builder(parent)).setTitle(getTitle()).setPositiveButton(android.R.string.cancel, null)
                    .setMessage(R.string.no_widget_to_modify).create().show();
        } else if (widgets.size() == 1) {
            Intent intent = new Intent(parent, WidgetConfigureActivity.class);
            intent.putExtra("isModify", true);
            intent.putExtra("widgetId", widgets.keyAt(0));
            parent.startActivity(intent);
        } else {
            super.onClick();
        }
    }

    @Override
    public boolean onPreferenceChange(Preference paramPreference, Object paramObject) {
        Integer widgetId = Integer.parseInt(paramObject.toString());
        Intent intent = new Intent(parent, WidgetConfigureActivity.class);
        intent.putExtra("isModify", true);
        intent.putExtra("widgetId", widgetId);
        parent.startActivity(intent);
        return false;
    }
}
