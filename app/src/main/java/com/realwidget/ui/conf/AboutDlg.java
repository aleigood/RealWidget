package com.realwidget.ui.conf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.preference.Preference;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;
import com.realwidget.R;

public class AboutDlg extends Preference {
    private Activity parent;
    private String s = "<html><body>"
            + "<b>1.</b> Do not install on SD card. <br><br>"
            + "<b>2.</b> Do not delete app from running list, otherwise it cannot be updated. <br><br>"
            + "<b>3.</b> If you have any questions, please <a href=\"mailto:realwidget101@gmail.com\">contact me</a>.<br><br>"
            + "Copyright &copy; 2011 Leo &lt;<a href=\"https://play.google.com/store/apps/details?id=alei.switchpro\">More app by me</a>&gt;</body></html>";

    public AboutDlg(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public AboutDlg(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(context);
    }

    private void init(Context context) {
        parent = (Activity) context;
        setSummary(context.getText(R.string.app_name) + " " + context.getText(R.string.version));
    }

    public void onClick() {
        // 设置对话框的View
        View dlgView = parent.getLayoutInflater().inflate(R.layout.view_about_dialog, null, false);
        TextView aboutTxt = ((TextView) dlgView.findViewById(R.id.about_txt));
        aboutTxt.setText(Html.fromHtml(s));
        aboutTxt.setMovementMethod(LinkMovementMethod.getInstance());

        (new AlertDialog.Builder(parent)).setTitle(R.string.about).setView(dlgView)
                .setNeutralButton(android.R.string.ok, null).create().show();
    }

}
