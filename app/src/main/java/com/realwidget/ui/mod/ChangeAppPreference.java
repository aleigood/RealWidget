package com.realwidget.ui.mod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.util.Utils;
import com.realwidget.util.Utils.AppListItem;

public class ChangeAppPreference extends Preference {
    private ButtonConfActivity mParentActivity;
    private OnSelectListener mIconSelectedListener;
    private Button mBtn;

    public ChangeAppPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ChangeAppPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChangeAppPreference(Context context) {
        super(context);
    }

    public void init(OnSelectListener onColorSelectListener, ButtonConfActivity parent, Button btn) {
        mIconSelectedListener = onColorSelectListener;
        mParentActivity = parent;
        mBtn = btn;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        android.widget.Button btn = (android.widget.Button) view.findViewById(R.id.pref_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mIconSelectedListener.onSelected("", "");
                notifyChanged();
            }
        });

        if (!Utils.isNull(mBtn.intent)) {
            btn.setVisibility(View.VISIBLE);
        } else {
            btn.setVisibility(View.INVISIBLE);
        }
    }

    @Override
    protected void onClick() {
        final AppAdapter adapter = new AppAdapter(mParentActivity);
        new AlertDialog.Builder(mParentActivity).setTitle("Change application")
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface arg0, int arg1) {
                        AppListItem item = (AppListItem) adapter.getItem(arg1);
                        mIconSelectedListener.onSelected(item.packageName, item.className);
                        notifyChanged();
                    }
                }).setInverseBackgroundForced(true).create().show();
    }

    public static interface OnSelectListener {
        public void onSelected(String pkgName, String clsName);
    }
}
