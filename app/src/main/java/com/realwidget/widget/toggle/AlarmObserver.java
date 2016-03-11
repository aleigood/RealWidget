package com.realwidget.widget.toggle;

import android.content.Context;
import android.database.ContentObserver;
import android.net.Uri;
import com.realwidget.util.Utils;

public class AlarmObserver extends ContentObserver {
    private static AlarmObserver instance;
    private Context mContext;

    private AlarmObserver(Context context) {
        super(null);
        mContext = context;
    }

    public static AlarmObserver getInstance(Context context) {
        if (instance == null) {
            instance = new AlarmObserver(context);
        }

        return instance;
    }

    /**
     * 数据变化时更新
     */
    @Override
    public void onChange(boolean selfChange) {
        super.onChange(selfChange);
        Utils.updateWidgets(AlarmObserver.class.getName(), mContext);
    }

    public void registerListener() {
        // 监听闹钟的改变
        mContext.getApplicationContext().getContentResolver()
                .registerContentObserver(Uri.parse("content://com.android.deskclock/alarm"), true, instance);
    }

    public void UnRegisterListener() {
        mContext.getApplicationContext().getContentResolver().unregisterContentObserver(instance);
    }

}
