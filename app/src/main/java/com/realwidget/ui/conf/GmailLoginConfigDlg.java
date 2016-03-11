package com.realwidget.ui.conf;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.widget.gmail.GmailWidget;

public class GmailLoginConfigDlg extends Preference {
    private MainActivity parent;
    private EditText mTxtUserName;
    private EditText mTxtPwd;

    public GmailLoginConfigDlg(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public GmailLoginConfigDlg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        parent = (MainActivity) context;
        setSummary(PreferenceManager.getDefaultSharedPreferences(parent).getString(Constants.PREF_GMAIL_USER_NAME, ""));
    }

    public void onClick() {
        View dlgView = parent.getLayoutInflater().inflate(R.layout.view_gmail_login_dialog, null, false);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(parent);
        mTxtUserName = ((EditText) dlgView.findViewById(R.id.user_name));
        mTxtUserName.setText(sp.getString(Constants.PREF_GMAIL_USER_NAME, ""));
        mTxtPwd = ((EditText) dlgView.findViewById(R.id.pwd));
        mTxtPwd.setText(sp.getString(Constants.PREF_GMAIL_PASSWORD, ""));

        (new AlertDialog.Builder(parent)).setTitle(getTitle()).setView(dlgView)
                .setPositiveButton(android.R.string.cancel, null)
                .setNeutralButton(android.R.string.ok, new OnClickListener() {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        String userName = mTxtUserName.getText().toString();
                        sp.edit().putString(Constants.PREF_GMAIL_USER_NAME, userName)
                                .putString(Constants.PREF_GMAIL_PASSWORD, mTxtPwd.getText().toString()).commit();
                        GmailWidget.getInstance(parent).updateContent();
                        setSummary(userName);
                    }
                }).create().show();
    }
}
