package com.realwidget;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import com.realwidget.util.ProviderUtil;
import com.realwidget.util.SwitchUtils;
import com.realwidget.widget.gmail.GmailWidget;
import com.realwidget.widget.music.MusicWidget;
import com.realwidget.widget.weather.WeatherWidget;

public class MainBrocastReceiver extends BroadcastReceiver {
    public static int batteryLevel;
    public static int batteryTemperatureC;
    public static int batteryStatus;

    public static String currentSsid;

    @Override
    public void onReceive(final Context context, Intent intent) {
        String action = intent.getAction();

        if (Intent.ACTION_BATTERY_CHANGED.equals(action)) {
            Log.d("action", "" + action);
            int level = intent.getIntExtra("level", 0);
            int scale = intent.getIntExtra("scale", 100);
            batteryLevel = (level * 100 / scale);
            batteryStatus = intent.getIntExtra("status", 1);
            batteryTemperatureC = intent.getIntExtra("temperature", 0);
            ProviderUtil.updateWidget(context);
        } else if (Constants.ACTION_WIDGET_UPDATE.equals(action)) {
            Log.d(intent.getStringExtra("tag"), action);
            ProviderUtil.updateWidget(context);
        } else if (GmailWidget.GMAIL_ALERT_ACTION.equals(action)) {
            GmailWidget.getInstance(context).updateContent();
            Log.d(GmailWidget.GMAIL_ALERT_ACTION, action);
        } else if (WeatherWidget.WEATHER_ALERT_ACTION.equals(action)) {
            WeatherWidget.getInstance(context).updateContent();
            Log.d(WeatherWidget.WEATHER_ALERT_ACTION, action);
        } else if (Constants.TRAFFIC_STATISTICS_ALERT.equals(action)) {
            SwitchUtils.trafficStatis(context);
            Log.d(Constants.TRAFFIC_STATISTICS_ALERT, action);
        } else if (intent.getAction().equals(Intent.ACTION_MEDIA_SCANNER_FINISHED)) {
            MusicWidget.getInstance(context).updatePlaylist();
        } else {
            Log.d("other action", "" + action);
            // 通知widget更新，需要改进，要选择性的更新，如果创建的widget中没有这个按钮就不进行更新
            ProviderUtil.updateWidget(context);
        }
    }
}
