package com.realwidget.widget.weather;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.preference.PreferenceManager;
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

import java.util.ArrayList;
import java.util.List;

public class WeatherWidget extends WidgetGenerator {
    public static final String WEATHER_ALERT_ACTION = "com.realwidget.WEATHER_ALERT";
    private static WeatherWidget instance;
    private Context mContext;
    private Condition mCurrent;
    private List<Condition> mForecast = new ArrayList<Condition>();

    private PendingIntent pendingIntent;
    private WeatherContentObserver mWeatherObserver = new WeatherContentObserver();

    private WeatherWidget(Context context) {
        mContext = context;
    }

    public static WeatherWidget getInstance(Context context) {
        if (instance == null) {
            instance = new WeatherWidget(context);
        }

        return instance;
    }

    public Condition getCurrent() {
        return mCurrent;
    }

    public List<Condition> getForecast() {
        return mForecast;
    }

    public RemoteViews buildRemoteViews(Button[] button) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_weather);
        views.setTextViewText(R.id.label, button[0].label);
        views.setTextColor(R.id.label, button[0].labelColor);
        views.setImageViewBitmap(R.id.bg, getBackImg(mContext, button[0]));

        if (mCurrent != null) {
            views.setTextViewText(R.id.temp, mCurrent.temperature + "°");
            views.setTextViewText(R.id.desc, mCurrent.city + "\n" + mCurrent.condition + "\n" + mCurrent.lowTemp + "°/"
                    + mCurrent.highTemp + "°");
            views.setImageViewResource(R.id.icon, mCurrent.iconRes);
            views.setTextColor(R.id.desc, button[0].labelColor);
            views.setTextColor(R.id.temp, button[0].labelColor);
        }

        views.setOnClickFillInIntent(R.id.btn, new Intent(Constants.ACTION_BUTTON_CLICK).putExtra("button", button[0]));
        return views;
    }

    @Override
    public View buildViews(ViewGroup parent, Button[] button) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_widget_weather, parent, false);
        view.findViewById(R.id.pos1).setTag(button[0]);

        ((ImageView) view.findViewById(R.id.bg)).setImageBitmap(getBackImg(mContext, button[0]));
        TextView label = ((TextView) view.findViewById(R.id.label));
        TextView temp = ((TextView) view.findViewById(R.id.temp));
        TextView desc = ((TextView) view.findViewById(R.id.desc));
        ImageView icon = ((ImageView) view.findViewById(R.id.icon));
        label.setText(button[0].label);
        label.setTextColor(button[0].labelColor);

        if (mCurrent != null) {
            temp.setText(mCurrent.temperature + "°");
            desc.setText(mCurrent.city + "\n" + mCurrent.condition + "\n" + mCurrent.lowTemp + "°/" + mCurrent.highTemp
                    + "°");
            icon.setImageResource(mCurrent.iconRes);
            desc.setTextColor(button[0].labelColor);
            temp.setTextColor(button[0].labelColor);
        }

        // 去除点击效果
        view.findViewById(R.id.btn).setBackgroundResource(R.drawable.trans_selector);
        return view;
    }

    public void updateContent() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String source = sp.getString(Constants.PREF_WEATHER_SOURCE, "0");
        final String location = sp.getString(Constants.PREF_WEATHER_LOCATION, "");

        if (source.equals("0")) {
            new GenieWeather(mContext, location, new Callback() {
                @Override
                public void onDataChange(Condition current, List<Condition> forecast) {
                    // 通知widget进行更新
                    mCurrent = current;
                    mForecast = forecast;
                    Utils.updateWidgets(WeatherWidget.class.getName(), mContext);
                    mContext.sendBroadcast(new Intent(ForecastActivity.WEATHER_FORECAST_ACTION));
                }
            });
        } else {
            if (location.equals("")) {
                return;
            }

            if (!Utils.isNetworkAvaliable(mContext)) {
                return;
            }

            new GoolgeWeather(mContext, location, new Callback() {
                @Override
                public void onDataChange(Condition current, List<Condition> forecast) {
                    // 通知widget进行更新
                    mCurrent = current;
                    mForecast = forecast;
                    Utils.updateWidgets(WeatherWidget.class.getName(), mContext);
                    mContext.sendBroadcast(new Intent(ForecastActivity.WEATHER_FORECAST_ACTION));
                }
            });
        }
    }

    public void enableAlert() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        int weatherFreshInterval = Integer.parseInt(sp.getString(Constants.PREF_WEATHER_REFRESH_INTERVAL, "30")) * 60 * 1000;
        Intent intent = new Intent(WEATHER_ALERT_ACTION);
        pendingIntent = PendingIntent.getBroadcast(mContext, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), weatherFreshInterval, pendingIntent);
        Log.d("WeatherWidget", "enableAlert");
    }

    public void disableAlert() {
        AlarmManager am = (AlarmManager) mContext.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
        Log.d("WeatherWidget", "disableAlert");
    }

    public void registerListener() {
        mContext.getApplicationContext().getContentResolver()
                .registerContentObserver(GenieWeather.CONTENT_URI, false, mWeatherObserver);
        Log.d("WeatherWidget", "registerListener");
    }

    public void UnRegisterListener() {
        mContext.getApplicationContext().getContentResolver().unregisterContentObserver(mWeatherObserver);
        Log.d("WeatherWidget", "UnRegisterListener");
    }

    public void performAction() {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        String source = sp.getString(Constants.PREF_WEATHER_SOURCE, "0");
        Intent weatherIntent = new Intent();

        if (source.equals("0")) {
            if (!Utils.isAppExist(mContext, "com.google.android.apps.genie.geniewidget")) {
                weatherIntent.setClass(mContext, DownloadActivity.class);
            } else {
                weatherIntent.setClassName("com.google.android.apps.genie.geniewidget",
                        "com.google.android.apps.genie.geniewidget.activities.NewsActivity");
            }
        } else {
            weatherIntent.setClass(mContext, ForecastActivity.class);
        }

        try {
            PendingIntent.getActivity(mContext, 0, weatherIntent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    public interface Callback {
        public void onDataChange(Condition current, List<Condition> forecast);
    }

    public static class Condition {
        public int isForecast;
        // 星期几
        public String day;
        // 城市名称
        public String city;
        // 天气状态
        public String condition;
        // 当前温度
        public String temperature;
        // 最高温度 c
        public String highTemp;
        // 最低温度 c
        public String lowTemp;
        // 湿度
        public String humidity;
        // 图标资源
        public int iconRes;
        // 降水概率
        public String chancePrecipitation;
        // 风
        public String wind;
    }

    private class WeatherContentObserver extends ContentObserver {
        public WeatherContentObserver() {
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
