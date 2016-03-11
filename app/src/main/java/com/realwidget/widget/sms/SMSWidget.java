package com.realwidget.widget.sms;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetFileDescriptor;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.DisplayPhoto;
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

import java.io.IOException;
import java.io.InputStream;

public class SMSWidget extends WidgetGenerator {
    public static final Uri sAllThreadsUri = Uri.parse("content://mms-sms/conversations").buildUpon()
            .appendQueryParameter("simple", "true").build();
    private static SmsInfo mLastSms;
    private static ContactInfo mContactInfo;
    private static String mUnreadSmsCount = "";
    private static SMSWidget instance;
    private Context mContext;
    private SMSContentObserver mObserver;

    private SMSWidget(Context context) {
        mContext = context;
        // 第一次创建时更新
        updateContent();
        mObserver = new SMSContentObserver();
    }

    public static SMSWidget getInstance(Context context) {
        if (instance == null) {
            instance = new SMSWidget(context);
        }

        return instance;
    }

    public RemoteViews buildRemoteViews(Button[] button) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_sms);
        views.setTextViewText(R.id.content, mLastSms.body);
        views.setTextViewText(R.id.label, button[0].label);
        views.setImageViewBitmap(R.id.bg, getBackImg(mContext, button[0]));
        String title = "";

        if (mContactInfo != null) {
            title = mContactInfo.name;
        } else {
            title = mLastSms.address;
        }

        views.setTextViewText(R.id.title, title);

        if (mLastSms.date != 0) {
            views.setTextViewText(R.id.time, Utils.formatTimeStampString(mContext, mLastSms.date, false));
        }

        views.setTextColor(R.id.content, button[0].labelColor);
        views.setTextColor(R.id.label, button[0].labelColor);
        views.setTextColor(R.id.title, button[0].labelColor);
        views.setTextColor(R.id.time, button[0].labelColor);

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
        TextView content = ((TextView) view.findViewById(R.id.content));
        TextView titleView = ((TextView) view.findViewById(R.id.title));
        TextView time = ((TextView) view.findViewById(R.id.time));

        label.setText(button[0].label);
        content.setText(mLastSms.body);
        String title = "";

        if (mContactInfo != null) {
            title = mContactInfo.name;
        } else {
            title = mLastSms.address;
        }

        titleView.setText(title);

        if (mLastSms.date != 0) {
            time.setText(Utils.formatTimeStampString(mContext, mLastSms.date, false));
        }

        content.setTextColor(button[0].labelColor);
        label.setTextColor(button[0].labelColor);
        titleView.setTextColor(button[0].labelColor);
        time.setTextColor(button[0].labelColor);

        // 去除点击效果
        view.findViewById(R.id.btn).setBackgroundResource(R.drawable.trans_selector);
        return view;
    }

    public void updateContent() {
        new Thread() {
            @Override
            public void run() {
                mLastSms = new SmsInfo();
                Cursor cursor = mContext.getContentResolver().query(Uri.parse("content://sms/inbox"),
                        new String[]{"_id", "address", "date", "body", "thread_id"}, null, null, "date DESC");

                if (cursor != null) {
                    if (cursor.moveToFirst()) {
                        mLastSms.id = cursor.getLong(0);
                        mLastSms.address = cursor.getString(1);
                        mLastSms.date = cursor.getLong(2);
                        mLastSms.body = cursor.getString(3);
                        mLastSms.thread_id = cursor.getLong(4);

                        mContactInfo = null;

                        Cursor contactCursor = mContext.getContentResolver().query(
                                Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI,
                                        Uri.encode(mLastSms.address)),
                                new String[]{"_id", "lookup", "display_name"}, null, null, null);

                        if (contactCursor != null) {
                            if (contactCursor.moveToFirst()) {
                                mContactInfo = new ContactInfo();
                                mContactInfo.id = contactCursor.getLong(0);
                                mContactInfo.lookupKey = contactCursor.getString(1);
                                mContactInfo.name = contactCursor.getString(2);
                            }

                            contactCursor.close();
                        }
                    }

                    cursor.close();
                    cursor = null;
                }

                int count = queryUnreadCount();
                mUnreadSmsCount = count == 0 ? "" : "" + count;
                Utils.updateWidgets(SMSWidget.class.getName(), mContext);
            }
        }.start();
    }

    private int queryUnreadCount() {
        Cursor cursor = null;
        int unreadCount = 0;

        try {
            cursor = mContext.getContentResolver().query(sAllThreadsUri, new String[]{"_id"}, "read=0", null, null);

            if (cursor != null) {
                unreadCount = cursor.getCount();
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return unreadCount;
    }

    public InputStream openDisplayPhoto(long photoFileId) {
        Uri displayPhotoUri = ContentUris.withAppendedId(DisplayPhoto.CONTENT_URI, photoFileId);

        try {
            AssetFileDescriptor fd = mContext.getContentResolver().openAssetFileDescriptor(displayPhotoUri, "r");
            return fd.createInputStream();
        } catch (IOException e) {
            return null;
        }
    }

    public void registerListener() {
        // 监听短信的改变，比如新增或删除短信
        mContext.getApplicationContext().getContentResolver()
                .registerContentObserver(Uri.parse("content://sms/"), true, mObserver);

        // 监听会话的改变，比如标记为已读，或删除会话
        mContext.getApplicationContext().getContentResolver()
                .registerContentObserver(Uri.parse("content://mms-sms/conversations"), true, mObserver);
    }

    public void UnRegisterListener() {
        mContext.getApplicationContext().getContentResolver().unregisterContentObserver(mObserver);
    }

    public String getUnreadSmsCount() {
        return mUnreadSmsCount;
    }

    public SmsInfo getLastSms() {
        return mLastSms;
    }

    public void performAction(int widgetSize) {
        if (widgetSize == 1) {
            Intent shortcutIntent1 = new Intent(Intent.ACTION_MAIN);
            shortcutIntent1.addCategory(Intent.CATEGORY_LAUNCHER);
            shortcutIntent1.setClassName("com.android.mms", "com.android.mms.ui.ConversationList");
            shortcutIntent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

            try {
                PendingIntent.getActivity(mContext, 0, shortcutIntent1, 0).send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        } else {
            // Intent msmX2Intent = new Intent(Intent.ACTION_MAIN);
            // msmX2Intent.setClassName("com.android.mms",
            // "com.android.mms.ui.ComposeMessageActivity");
            // msmX2Intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            // msmX2Intent.setData(Uri.parse("content://mms-sms/conversations/"
            // + mLastSms.thread_id));

            Intent msmX2Intent = new Intent(Intent.ACTION_VIEW);
            msmX2Intent.setType("vnd.android-dir/mms-sms");
            msmX2Intent.putExtra("thread_id", mLastSms.thread_id);

            try {
                PendingIntent.getActivity(mContext, 0, msmX2Intent, 0).send();
            } catch (CanceledException e) {
                e.printStackTrace();
            }
        }
    }

    private class SMSContentObserver extends ContentObserver {
        public SMSContentObserver() {
            super(null);
        }

        /**
         * 短信数据变化时更新
         */
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateContent();
        }
    }
}

class SmsInfo {
    String address;
    String body;
    long date;
    long id;
    long thread_id;
}

class ContactInfo {
    long id;
    String lookupKey;
    String name;
}
