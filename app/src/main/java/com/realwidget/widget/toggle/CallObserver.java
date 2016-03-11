package com.realwidget.widget.toggle;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.provider.CallLog.Calls;
import com.realwidget.util.Utils;

public class CallObserver extends ContentObserver {
    private static String mMissCallCount;
    private static CallObserver instance;
    private Context mContext;

    private CallObserver(Context context) {
        super(null);
        mContext = context;
        // 第一次创建时更新
        updateContent();
    }

    public static CallObserver getInstance(Context context) {
        if (instance == null) {
            instance = new CallObserver(context);
        }

        return instance;
    }

    /**
     * 来电数据变化时更新
     */
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        updateContent();
    }

    public void updateContent() {
        Cursor cursor = mContext.getContentResolver().query(Calls.CONTENT_URI,
                new String[]{Calls.NUMBER, Calls.TYPE, Calls.NEW}, "type=3 and new=1", null, null);

        if (cursor != null) {
            int count = cursor.getCount();
            mMissCallCount = count == 0 ? "" : "" + count;
        }

        cursor.close();
        Utils.updateWidgets(CallObserver.class.getName(), mContext);
    }

    public void registerListener() {
        mContext.getContentResolver().registerContentObserver(Calls.CONTENT_URI, true, instance);
    }

    public void UnRegisterListener() {
        mContext.getContentResolver().unregisterContentObserver(instance);
    }

    public String getMissingCallCount() {
        // if ((str.equals("com.android.contacts.DialtactsActivity")) ||
        // (str.equals("com.android.htcdialer.Dialer")) ||
        // (str.equals("com.android.htccontacts.ViewCallHistory")) ||
        // (str.equals("com.android.phone.DialtactsContactsEntryActivity")))
        return mMissCallCount;
    }

}
