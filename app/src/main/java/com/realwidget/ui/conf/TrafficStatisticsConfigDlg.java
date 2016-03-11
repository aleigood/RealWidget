package com.realwidget.ui.conf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.ListPreference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.widget.NumberPicker;
import com.realwidget.Constants;
import com.realwidget.R;

public class TrafficStatisticsConfigDlg extends ListPreference {
    private Activity parent;

    public TrafficStatisticsConfigDlg(Context context) {
        super(context);
        parent = (Activity) context;
        init();
    }

    public TrafficStatisticsConfigDlg(Context context, AttributeSet attrs) {
        super(context, attrs);
        parent = (Activity) context;
        init();
    }

    private void init() {
        setEntries(R.array.traffic_statistics_first_day_entry);
        setEntryValues(R.array.traffic_statistics_first_day_value);
    }

    public void onClick() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(parent);
        String cycle = sp.getString(Constants.PREF_TRAFFIC_STATISTICS_CYCLE, "0");
        String firstDay = sp.getString(Constants.PREF_TRAFFIC_STATISTICS_FIRST_DAY, "1");

        if (cycle.equals("0")) {
            final NumberPicker dlgView = new NumberPicker(parent);
            dlgView.setMinValue(1);
            dlgView.setMaxValue(31);
            dlgView.setValue(Integer.parseInt(firstDay));
            (new AlertDialog.Builder(parent)).setTitle(getTitle()).setView(dlgView)
                    .setPositiveButton(android.R.string.cancel, null)
                    .setNeutralButton(android.R.string.ok, new OnClickListener() {
                        public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                            PreferenceManager.getDefaultSharedPreferences(parent).edit()
                                    .putString(Constants.PREF_TRAFFIC_STATISTICS_FIRST_DAY, dlgView.getValue() + "")
                                    .commit();
                            setSummary(dlgView.getValue() + "");
                        }
                    }).create().show();
        } else {
            super.onClick();
        }
    }
}
