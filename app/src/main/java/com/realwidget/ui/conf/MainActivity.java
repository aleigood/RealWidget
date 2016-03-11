package com.realwidget.ui.conf;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceCategory;
import android.preference.PreferenceManager;
import com.realwidget.Constants;
import com.realwidget.MyApplication;
import com.realwidget.R;
import com.realwidget.util.Utils;
import com.realwidget.widget.calendar.CalendarWidget;
import com.realwidget.widget.gmail.GmailWidget;
import com.realwidget.widget.weather.WeatherWidget;

public class MainActivity extends PreferenceActivity implements Preference.OnPreferenceChangeListener {
    private Preference mGmailRefreshInterval;
    private Preference mWeatherRefreshInterval;
    private Preference mWeatherSource;
    private Preference mWeatherLocation;
    private Preference mWeatherTempUnit;
    private Preference mTrafficStatisticsCycle;
    private Preference mBatteryTempUnit;
    private Preference mTrafficStatisticsFirstDay;
    private Preference mBatteryTemp;
    private Preference mTrafficStatistics;
    private Preference mMaxDaysOfEvent;
    private CharSequence[] mMaxDaysEntry;
    private CharSequence[] mTrafficStatisticsFirstDayEntry;

    private boolean refreshWeather;
    private boolean updateCalendar;
    private boolean updateGmailAlert;
    private boolean updateWeatherAlert;
    private boolean updateWidget;

    @Override
    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.pref_conf_main);
        mMaxDaysEntry = getResources().getTextArray(R.array.max_days_entry);
        mTrafficStatisticsFirstDayEntry = getResources().getTextArray(R.array.traffic_statistics_first_day_entry);

        mGmailRefreshInterval = findPreference(Constants.PREF_GMAIL_REFRESH_INTERVAL);
        mGmailRefreshInterval.setOnPreferenceChangeListener(this);
        mWeatherSource = findPreference(Constants.PREF_WEATHER_SOURCE);
        mWeatherSource.setOnPreferenceChangeListener(this);
        mWeatherRefreshInterval = findPreference(Constants.PREF_WEATHER_REFRESH_INTERVAL);
        mWeatherRefreshInterval.setOnPreferenceChangeListener(this);
        mWeatherLocation = findPreference(Constants.PREF_WEATHER_LOCATION);
        mWeatherLocation.setOnPreferenceChangeListener(this);
        mWeatherTempUnit = findPreference(Constants.PREF_WEATHER_TEMP_UNIT);
        mWeatherTempUnit.setOnPreferenceChangeListener(this);
        mBatteryTemp = findPreference(Constants.PREF_BATTERY_TEMPERATURE);
        mBatteryTemp.setOnPreferenceChangeListener(this);
        mBatteryTempUnit = findPreference(Constants.PREF_BATTERY_TEMPERATURE_UNIT);
        mBatteryTempUnit.setOnPreferenceChangeListener(this);
        mTrafficStatistics = findPreference(Constants.PREF_TRAFFIC_STATISTICS);
        mTrafficStatistics.setOnPreferenceChangeListener(this);
        mTrafficStatisticsCycle = findPreference(Constants.PREF_TRAFFIC_STATISTICS_CYCLE);
        mTrafficStatisticsCycle.setOnPreferenceChangeListener(this);
        mTrafficStatisticsFirstDay = findPreference(Constants.PREF_TRAFFIC_STATISTICS_FIRST_DAY);
        mTrafficStatisticsFirstDay.setOnPreferenceChangeListener(this);
        mMaxDaysOfEvent = findPreference(Constants.PREF_MAX_DAYS_OF_EVENT);
        mMaxDaysOfEvent.setOnPreferenceChangeListener(this);

        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        mGmailRefreshInterval.setSummary(sp.getString(Constants.PREF_GMAIL_REFRESH_INTERVAL, "") + " Minute");

        String weatherSource = sp.getString(Constants.PREF_WEATHER_SOURCE, "0");
        mWeatherSource.setSummary(weatherSource.equals("0") ? "News & Weather" : "Google Weather");
        mWeatherRefreshInterval.setSummary(sp.getString(Constants.PREF_WEATHER_REFRESH_INTERVAL, "30") + " Minute");
        mWeatherRefreshInterval.setEnabled(weatherSource.equals("0"));
        mWeatherLocation.setSummary(sp.getString(Constants.PREF_WEATHER_LOCATION, ""));
        mWeatherLocation.setEnabled(weatherSource.equals("0"));
        mWeatherTempUnit.setSummary(sp.getString(Constants.PREF_WEATHER_TEMP_UNIT, "0").equals("0") ? "Celsius"
                : "Fahrenheit");
        mWeatherTempUnit.setEnabled(weatherSource.equals("0"));

        mBatteryTempUnit.setEnabled(sp.getBoolean(Constants.PREF_BATTERY_TEMPERATURE, false));
        mBatteryTempUnit.setSummary(sp.getString(Constants.PREF_BATTERY_TEMPERATURE_UNIT, "0").equals("0") ? "Celsius"
                : "Fahrenheit");

        mTrafficStatisticsCycle.setEnabled(sp.getBoolean(Constants.PREF_TRAFFIC_STATISTICS, false));
        mTrafficStatisticsFirstDay.setEnabled(sp.getBoolean(Constants.PREF_TRAFFIC_STATISTICS, false));

        String statisticsCycle = sp.getString(Constants.PREF_TRAFFIC_STATISTICS_CYCLE, "0");
        String statisticsFirstDay = sp.getString(Constants.PREF_TRAFFIC_STATISTICS_FIRST_DAY, "1");
        mTrafficStatisticsCycle.setSummary(statisticsCycle.equals("0") ? "Month" : "Week");

        if (statisticsCycle.equals("0")) {
            mTrafficStatisticsFirstDay.setSummary(statisticsFirstDay);
        } else {
            mTrafficStatisticsFirstDay
                    .setSummary(mTrafficStatisticsFirstDayEntry[Integer.parseInt(statisticsFirstDay) - 1]);
        }

        mMaxDaysOfEvent
                .setSummary(mMaxDaysEntry[Integer.parseInt(sp.getString(Constants.PREF_MAX_DAYS_OF_EVENT, "1")) - 1]);

        // 临时删除选项，等需要增加天气源的时候放开
        PreferenceCategory cate = (PreferenceCategory) findPreference("widget_config");
        cate.removePreference(findPreference("weather_setting"));
    }

    /**
     * 此时的值还没保存
     */
    public boolean onPreferenceChange(Preference preference, Object objValue) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final String key = preference.getKey();

        if (Constants.PREF_GMAIL_REFRESH_INTERVAL.equals(key)) {
            mGmailRefreshInterval.setSummary(objValue + " Minute");
            updateGmailAlert = true;
        } else if (Constants.PREF_WEATHER_SOURCE.equals(key)) {
            mWeatherSource.setSummary(objValue.equals("0") ? "News & Weather" : "Google Weather");
            WeatherWidget weather = WeatherWidget.getInstance(MyApplication.getInstance());

            if (objValue.equals("0")) {
                if (!Utils.isAppExist(this, "com.google.android.apps.genie.geniewidget")) {
                    return false;
                }

                weather.disableAlert();
                weather.registerListener();
                mWeatherRefreshInterval.setEnabled(false);
                mWeatherLocation.setEnabled(false);
                mWeatherTempUnit.setEnabled(false);
            } else {
                weather.enableAlert();
                weather.UnRegisterListener();
                mWeatherRefreshInterval.setEnabled(true);
                mWeatherLocation.setEnabled(true);
                mWeatherTempUnit.setEnabled(true);
            }

            refreshWeather = true;
        } else if (Constants.PREF_WEATHER_REFRESH_INTERVAL.equals(key)) {
            mWeatherRefreshInterval.setSummary(objValue + " Minute");
            updateWeatherAlert = true;
        } else if (Constants.PREF_WEATHER_LOCATION.equals(key)) {
            mWeatherLocation.setSummary(objValue.toString());
            refreshWeather = true;
        } else if (Constants.PREF_WEATHER_TEMP_UNIT.equals(key)) {
            mWeatherTempUnit.setSummary(objValue.equals("0") ? "Celsius" : "Fahrenheit");
            refreshWeather = true;
        } else if (Constants.PREF_BATTERY_TEMPERATURE.equals(key)) {
            if ((Boolean) objValue) {
                mBatteryTempUnit.setEnabled(true);
            } else {
                mBatteryTempUnit.setEnabled(false);
            }

            updateWidget = true;
        } else if (Constants.PREF_BATTERY_TEMPERATURE_UNIT.equals(key)) {
            mBatteryTempUnit.setSummary(objValue.equals("0") ? "Celsius" : "Fahrenheit");
            updateWidget = true;
        } else if (Constants.PREF_TRAFFIC_STATISTICS.equals(key)) {
            if ((Boolean) objValue) {
                mTrafficStatisticsCycle.setEnabled(true);
                mTrafficStatisticsFirstDay.setEnabled(true);
            } else {
                mTrafficStatisticsCycle.setEnabled(false);
                mTrafficStatisticsFirstDay.setEnabled(false);
            }

            updateWidget = true;
        } else if (Constants.PREF_TRAFFIC_STATISTICS_CYCLE.equals(key)) {
            mTrafficStatisticsCycle.setSummary(objValue.equals("0") ? "Month" : "Week");
            sp.edit().putString(Constants.PREF_TRAFFIC_STATISTICS_FIRST_DAY, "1").commit();

            if (objValue.equals("0")) {
                mTrafficStatisticsFirstDay.setSummary("1");
            } else {
                mTrafficStatisticsFirstDay.setSummary(mTrafficStatisticsFirstDayEntry[0]);
            }

            updateWidget = true;
        } else if (Constants.PREF_TRAFFIC_STATISTICS_FIRST_DAY.equals(key)) {
            String statisticsCycle = sp.getString(Constants.PREF_TRAFFIC_STATISTICS_CYCLE, "0");
            mTrafficStatisticsCycle.setSummary(statisticsCycle.equals("0") ? "Month" : "Week");

            if (statisticsCycle.equals("0")) {
                mTrafficStatisticsFirstDay.setSummary(objValue.toString());
            } else {
                mTrafficStatisticsFirstDay.setSummary(mTrafficStatisticsFirstDayEntry[Integer.parseInt(objValue
                        .toString()) - 1]);
            }

            updateWidget = true;
        } else if (Constants.PREF_MAX_DAYS_OF_EVENT.equals(key)) {
            mMaxDaysOfEvent.setSummary(mMaxDaysEntry[Integer.parseInt(objValue.toString()) - 1]);
            updateCalendar = true;
        }

        return true;
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (refreshWeather) {
            WeatherWidget.getInstance(this).updateContent();
        }

        if (updateCalendar) {
            CalendarWidget.getInstance(this).updateContent();
        }

        if (updateWidget) {
            Utils.updateWidgets("MainActivity", this);
        }

        if (updateGmailAlert) {
            GmailWidget.getInstance(this).disableAlert();
            GmailWidget.getInstance(this).enableAlert();
        }

        if (updateWeatherAlert) {
            WeatherWidget.getInstance(this).disableAlert();
            WeatherWidget.getInstance(this).enableAlert();
        }
    }
}
