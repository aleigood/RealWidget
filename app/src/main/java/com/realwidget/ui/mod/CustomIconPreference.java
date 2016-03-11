package com.realwidget.ui.mod;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.net.Uri;
import android.preference.Preference;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import com.realwidget.R;
import com.realwidget.ui.comm.OnSelectListener;
import com.realwidget.ui.mod.IconAdapter.ListItem;

public class CustomIconPreference extends Preference {
    private ButtonConfActivity mParentActivity;
    private String mIconFile;
    private OnSelectListener mIconSelectedListener;

    public CustomIconPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomIconPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomIconPreference(Context context) {
        super(context);
    }

    public void init(OnSelectListener onColorSelectListener, ButtonConfActivity parent, String iconFile) {
        mIconSelectedListener = onColorSelectListener;
        mParentActivity = parent;
        mIconFile = iconFile;
    }

    @Override
    public void onBindView(View view) {
        super.onBindView(view);
        Button btn = (Button) view.findViewById(R.id.pref_button);
        btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mParentActivity.deleteIconFile();
            }
        });

        if (mIconFile != null && !mIconFile.equals("")) {
            btn.setVisibility(View.VISIBLE);
        } else {
            btn.setVisibility(View.INVISIBLE);
        }
    }

    private void showColorDlg() {
        final IconAdapter mAdapter = new IconAdapter(mParentActivity);

        (new AlertDialog.Builder(mParentActivity)).setTitle("Icon").setAdapter(mAdapter, new OnClickListener() {
            public void onClick(DialogInterface arg0, int imgId) {
                ListItem item = (ListItem) mAdapter.getItem(imgId);

                if (item.icoId == -1) {
                    pickIcon();
                } else {
                    mIconSelectedListener.onSelected(item.icoId);
                }
            }
        }).setInverseBackgroundForced(true).create().show();
    }

    @Override
    protected void onClick() {
        super.onClick();
        showColorDlg();
    }

    private void pickIcon() {
        Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        Intent localIntent = new Intent("android.intent.action.PICK", localUri);
        mParentActivity.startActivityForResult(localIntent, ButtonConfActivity.REQUST_CODE_SEL_ICO);
    }

    public void update(String iconFile) {
        mIconFile = iconFile;
        notifyChanged();
    }

}
