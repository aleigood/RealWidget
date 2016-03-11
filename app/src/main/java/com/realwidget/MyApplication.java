package com.realwidget;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import com.realwidget.db.DatabaseOper;
import com.realwidget.util.SwitchUtils;
import com.realwidget.widget.calendar.CalendarWidget;
import com.realwidget.widget.gmail.GmailWidget;
import com.realwidget.widget.sms.SMSWidget;
import com.realwidget.widget.toggle.AlarmObserver;
import com.realwidget.widget.toggle.CallObserver;
import com.realwidget.widget.toggle.NetStateListener;
import com.realwidget.widget.weather.WeatherWidget;

public class MyApplication extends Application {
    private static MyApplication instance;
    private static MainBroadcastReceiver mainReceiver = new MainBroadcastReceiver();
    private DatabaseOper dbOper;

    public static MyApplication getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        dbOper = new DatabaseOper(this);

        getApplicationContext().registerReceiver(mainReceiver,
                new IntentFilter("android.intent.action.BATTERY_CHANGED"));
        SMSWidget.getInstance(this).registerListener();
        CalendarWidget.getInstance(this).registerListener();
        CallObserver.getInstance(this).registerListener();
        AlarmObserver.getInstance(this).registerListener();
        GmailWidget.getInstance(this).enableAlert();

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherSource = sp.getString(Constants.PREF_WEATHER_SOURCE, "0");

        if (weatherSource.equals("0")) {
            WeatherWidget.getInstance(this).registerListener();

        } else {
            WeatherWidget.getInstance(this).enableAlert();
        }

        SwitchUtils.enableTrafficStatisAlert(this);

        // 初始化监听网络状态的监听器
        TelephonyManager mTelephonyMgr = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        mTelephonyMgr.listen(new NetStateListener(this), PhoneStateListener.LISTEN_SERVICE_STATE
                | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        dbOper.close();

        getApplicationContext().unregisterReceiver(mainReceiver);
        SMSWidget.getInstance(this).UnRegisterListener();
        CalendarWidget.getInstance(this).UnRegisterListener();
        CallObserver.getInstance(this).UnRegisterListener();
        AlarmObserver.getInstance(this).UnRegisterListener();
        GmailWidget.getInstance(this).disableAlert();
        SwitchUtils.disableTrafficStatisAlert(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        String weatherSource = sp.getString(Constants.PREF_WEATHER_SOURCE, "0");

        if (weatherSource.equals("0")) {
            WeatherWidget.getInstance(this).UnRegisterListener();
        } else {
            WeatherWidget.getInstance(this).disableAlert();
        }
    }

    public DatabaseOper getDataOper() {
        return dbOper;
    }
}
