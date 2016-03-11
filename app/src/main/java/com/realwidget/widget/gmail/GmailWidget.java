package com.realwidget.widget.gmail;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.StyleSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.util.Utils;
import com.realwidget.widget.WidgetGenerator;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Authenticator;
import java.net.PasswordAuthentication;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class GmailWidget extends WidgetGenerator {
    public static final String GMAIL_ALERT_ACTION = "com.realwidget.GMAIL_ALERT";
    public static PendingIntent pendingIntent;
    private static GmailWidget instance;
    private Context mContext;
    private GmailInfo mMailInfo;

    private GmailWidget(Context context) {
        mContext = context;
        updateContent();
    }

    public static GmailWidget getInstance(Context context) {
        if (instance == null) {
            instance = new GmailWidget(context);
        }

        return instance;
    }

    public RemoteViews buildRemoteViews(Button[] button) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_sms);
        views.setTextViewText(R.id.label, button[0].label);
        views.setTextColor(R.id.label, button[0].labelColor);
        views.setImageViewBitmap(R.id.bg, getBackImg(mContext, button[0]));

        if (mMailInfo != null && mMailInfo.unread != 0) {
            views.setTextViewText(R.id.title, mMailInfo.name);
            SpannableString title = new SpannableString(mMailInfo.title + " - " + mMailInfo.summary);
            title.setSpan(new StyleSpan(Typeface.BOLD), 0, mMailInfo.title.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            views.setTextViewText(R.id.content, title);

            Date date = null;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            try {
                date = formatter.parse(mMailInfo.time);
            } catch (ParseException e) {
                Log.d("ParseException", mMailInfo.time);
                e.printStackTrace();
            }

            if (date != null) {
                views.setTextViewText(R.id.time, Utils.formatTimeStampString(mContext, date.getTime(), false));
            }

            views.setTextColor(R.id.title, button[0].labelColor);
            views.setTextColor(R.id.content, button[0].labelColor);
            views.setTextColor(R.id.time, button[0].labelColor);
        } else {
            views.setTextViewText(R.id.title, "");
            views.setTextViewText(R.id.content, "");
            views.setTextViewText(R.id.time, "");
        }

        views.setOnClickFillInIntent(R.id.btn, new Intent(Constants.ACTION_BUTTON_CLICK).putExtra("button", button[0]));
        return views;
    }

    @Override
    public View buildViews(ViewGroup parent, Button[] button) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_widget_sms, parent, false);
        view.findViewById(R.id.pos1).setTag(button[0]);
        ((ImageView) view.findViewById(R.id.bg)).setImageBitmap(getBackImg(mContext, button[0]));

        TextView label = ((TextView) view.findViewById(R.id.label));
        TextView title = ((TextView) view.findViewById(R.id.title));
        TextView content = ((TextView) view.findViewById(R.id.content));
        TextView time = ((TextView) view.findViewById(R.id.time));

        label.setText(button[0].label);
        label.setTextColor(button[0].labelColor);

        if (mMailInfo != null && mMailInfo.unread != 0) {
            title.setText(mMailInfo.name);
            content.setText(mMailInfo.title + " - " + mMailInfo.summary);

            Date date = null;
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");

            try {
                date = formatter.parse(mMailInfo.time);
            } catch (ParseException e) {
                Log.d("ParseException", mMailInfo.time);
                e.printStackTrace();
            }

            if (date != null) {
                time.setText(Utils.formatTimeStampString(mContext, date.getTime(), false));
            }

            title.setTextColor(button[0].labelColor);
            content.setTextColor(button[0].labelColor);
            time.setTextColor(button[0].labelColor);
        } else {
            title.setText("");
            content.setText("");
            time.setText("");
        }

        // 去除点击效果
        view.findViewById(R.id.btn).setBackgroundResource(R.drawable.trans_selector);
        return view;
    }

    public void updateContent() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        final String userName = sp.getString(Constants.PREF_GMAIL_USER_NAME, "");
        final String pwd = sp.getString(Constants.PREF_GMAIL_PASSWORD, "");

        if (userName.equals("") || pwd.equals("")) {
            return;
        }

        if (!Utils.isNetworkAvaliable(mContext)) {
            return;
        }

        new Thread() {
            @Override
            public void run() {
                parse(userName, pwd);
            }
        }.start();
    }

    public GmailInfo getMailInfo() {
        return mMailInfo;
    }

    public void performAction() {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setClassName("com.google.android.gm", "com.google.android.gm.GmailActivity");
            PendingIntent.getActivity(mContext, 0, intent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    private XmlHandler parse(final String name, final String password) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        XmlHandler handler = new XmlHandler();

        Authenticator.setDefault(new Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(name, password.toCharArray());
            }
        });

        try {
            SAXParser sp = spf.newSAXParser();
            XMLReader reader = sp.getXMLReader();
            reader.setContentHandler(handler);

            URL url = new URL("https://mail.google.com/mail/feed/atom/");
            InputStream is = url.openStream();
            reader.parse(new InputSource(new InputStreamReader(is, "utf-8")));
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return handler;
    }

    public void enableAlert() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int gmailFreshInterval = Integer.parseInt(sp.getString(Constants.PREF_GMAIL_REFRESH_INTERVAL, "5")) * 60 * 1000;
        Intent intent = new Intent(GMAIL_ALERT_ACTION);
        pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), gmailFreshInterval, pendingIntent);
    }

    public void disableAlert() {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    class XmlHandler extends DefaultHandler {
        private int entryCount;
        private String mTagName;

        @Override
        public void characters(char[] ch, int start, int length) throws SAXException {
            super.characters(ch, start, length);

            if (mTagName != null && mTagName.equals("fullcount")) {
                mMailInfo = new GmailInfo();
                String unread = new String(ch, start, length);
                mMailInfo.unread = (unread.equals("") || unread.equals("0")) ? 0 : Integer.parseInt(unread);
            }

            // 只读第一个邮件内容
            if (entryCount == 1 && mTagName != null) {
                if (mTagName.equals("title")) {
                    mMailInfo.title = new String(ch, start, length);
                } else if (mTagName.equals("summary")) {
                    mMailInfo.summary = new String(ch, start, length);
                } else if (mTagName.equals("issued")) {
                    mMailInfo.time = new String(ch, start, length);
                } else if (mTagName.equals("name")) {
                    mMailInfo.name = new String(ch, start, length);
                } else if (mTagName.equals("email")) {
                    mMailInfo.email = new String(ch, start, length);
                }
            }
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String tagName = localName.length() != 0 ? localName : qName;
            mTagName = tagName.toLowerCase();

            if (mTagName.equals("entry")) {
                entryCount++;
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String tagName = localName.length() != 0 ? localName : qName;
            tagName = tagName.toLowerCase();
            mTagName = null;
        }

        @Override
        public void endDocument() throws SAXException {
            // 通知widget进行更新
            Utils.updateWidgets(GmailWidget.class.getName(), mContext);
        }
    }

    public class GmailInfo {
        public int unread;
        public String title;
        public String summary;
        public String time;
        public String name;
        public String email;
    }

}
