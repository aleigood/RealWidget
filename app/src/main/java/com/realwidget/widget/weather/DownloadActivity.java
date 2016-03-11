package com.realwidget.widget.weather;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.DialogInterface.OnClickListener;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.realwidget.R;

public class DownloadActivity extends ListActivity {
    private static final int DLG_DOWNLOAD = 1;

    @Override
    protected void onStart() {
        super.onStart();
        showDialog(DLG_DOWNLOAD);
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DLG_DOWNLOAD:
                String s = "<html><body><br>Need \"News & Weather\"(by Google) app, please install it first."
                        + "<br> You can download it from <a href=\"http://realwidget.googlecode.com/files/GenieWidget_1.3.11.apk\">here</a>.<br></body></html>";
                LayoutInflater inflater = getLayoutInflater();
                View dlgView = inflater.inflate(R.layout.view_weather_download, null, false);
                TextView view = (TextView) dlgView.findViewById(R.id.txt);
                view.setText(Html.fromHtml(s));
                view.setMovementMethod(LinkMovementMethod.getInstance());
                return (new AlertDialog.Builder(this)).setTitle("Download").setView(dlgView)
                        .setNegativeButton(android.R.string.ok, new OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }
                        }).setOnCancelListener(new OnCancelListener() {
                            @Override
                            public void onCancel(DialogInterface dialog) {
                                finish();
                            }
                        }).create();
            default:
                return null;
        }
    }
}
