package com.realwidget.util;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.wifi.WifiManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.telephony.ServiceState;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;
import com.realwidget.Constants;
import com.realwidget.widget.toggle.BrightnessActivity;
import com.realwidget.widget.toggle.NetStateListener;

import java.lang.reflect.Method;
import java.util.Calendar;
import java.util.Date;

public class SwitchUtils {
    // 开关状态
    public static final int STATE_OTHER = -1;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_INTERMEDIATE = 2;
    private static PendingIntent pendingIntent;

    public static boolean getBluetooth(Context context) {
        try {
            Class<?> localClass = ClassLoader.getSystemClassLoader().loadClass("android.bluetooth.BluetoothAdapter");
            Method localMethod = localClass.getMethod("getDefaultAdapter", new Class[0]);
            Object device1 = localMethod.invoke(null, new Object[0]);

            if (device1 != null) {
                Method isEnabledMethod = localClass.getMethod("isEnabled", new Class[0]);
                isEnabledMethod.setAccessible(true);
                return ((Boolean) isEnabledMethod.invoke(device1, new Object[]{})).booleanValue();
            }
        } catch (Exception e) {
            Log.d("getBluetooth", e.toString());
        }

        return false;
    }

    public static void toggleBluetooth(Context context) {
        try {
            Class<?> localClass = ClassLoader.getSystemClassLoader().loadClass("android.bluetooth.BluetoothAdapter");
            Method localMethod = localClass.getMethod("getDefaultAdapter", new Class[0]);
            Object device = localMethod.invoke(null, new Object[0]);

            if (getBluetooth(context)) {
                Method disableMethod = localClass.getMethod("disable", new Class[0]);
                disableMethod.setAccessible(true);
                disableMethod.invoke(device, new Object[]{});
            } else {
                Method enableMethod = localClass.getMethod("enable", new Class[0]);
                enableMethod.setAccessible(true);
                enableMethod.invoke(device, new Object[]{});
            }
        } catch (Exception e) {
            Log.d("toggleBluetooth", e.toString());
        }
    }

    /**
     * @return A formatted string of the next alarm (for showing on the lock
     * screen), or null if there is no next alarm.
     */
    public static String getNextAlarm(Context context) {
        String nextAlarm = Settings.System
                .getString(context.getContentResolver(), Settings.System.NEXT_ALARM_FORMATTED);
        if (nextAlarm == null || TextUtils.isEmpty(nextAlarm)) {
            return null;
        }
        return nextAlarm;
    }

    public static void toggleBirghtness(Context context) {
        Intent intent = new Intent(context, BrightnessActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0, intent, 0);

        try {
            pendingIntent.send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    /**
     * Gets state of brightness.
     *
     * @param context
     * @return true if more than moderately bright.
     */
    public static String getBrightness(Context context) {
        int brightness = Settings.System.getInt(context.getContentResolver(),
                android.provider.Settings.System.SCREEN_BRIGHTNESS, BrightnessActivity.BRIGHT_LEVEL_30);
        int mode = Settings.System.getInt(context.getContentResolver(), BrightnessActivity.BRIGHT_MODE, 1);

        if (mode == BrightnessActivity.BRIGHT_MODE_AUTO) {
            return "Auto";
        } else {
            return (int) ((float) brightness / 255f * 100f) + "%";
        }
    }

    /**
     * 有广播，不需要手动更新
     *
     * @param context
     */
    public static void toggleSync(Context context) {
        boolean backgroundData = getBackgroundDataState(context);
        boolean sync = getMasterSyncAutomatically();

        // need open background data
        if (!backgroundData) {
            Toast.makeText(context, "need_open_backdata", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!sync) {
            setMasterSyncAutomatically(true);
        } else {
            setMasterSyncAutomatically(false);
        }
    }

    public static boolean getSync(Context context) {
        boolean backgroundData = getBackgroundDataState(context);
        boolean sync = getMasterSyncAutomatically();
        return backgroundData && sync;
    }

    /**
     * Gets the state of background data.
     *
     * @param context
     * @return true if enabled
     */
    public static boolean getBackgroundDataState(Context context) {
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        return connManager.getBackgroundDataSetting();
    }

    private static boolean getMasterSyncAutomatically() {
        try {
            return ((Boolean) (ContentResolver.class.getMethod("getMasterSyncAutomatically", new Class[]{}).invoke(
                    null, new Object[]{}))).booleanValue();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    private static void setMasterSyncAutomatically(boolean b) {
        try {
            ContentResolver.class.getMethod("setMasterSyncAutomatically", new Class[]{boolean.class}).invoke(null,
                    new Object[]{b});
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Toggles the state of Wi-Fi
     *
     * @param context
     */
    public static void toggleWifi(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = getWifiState(context);

        if (wifiState == STATE_ENABLED) {
            wifiManager.setWifiEnabled(false);
        } else if (wifiState == STATE_DISABLED) {
            wifiManager.setWifiEnabled(true);
        }
    }

    /**
     * Gets the state of Wi-Fi
     *
     * @param context
     * @return STATE_ENABLED, STATE_DISABLED, or STATE_INTERMEDIATE
     */
    public static int getWifiState(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int wifiState = wifiManager.getWifiState();

        if (wifiState == WifiManager.WIFI_STATE_DISABLED) {
            return STATE_DISABLED;
        } else if (wifiState == WifiManager.WIFI_STATE_ENABLED) {
            return STATE_ENABLED;
        } else {
            return STATE_INTERMEDIATE;
        }
    }

    /**
     * Toggle grayvity
     *
     * @param context
     */
    public static void toggleAutoRotate(Context context) {
        ContentResolver cr = context.getContentResolver();
        int autoRotate = Settings.System.getInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0);

        if (autoRotate == 1) {
            Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 0);
        } else {
            Settings.System.putInt(cr, Settings.System.ACCELEROMETER_ROTATION, 1);
        }

        // 无广播，需要手动更新
        Utils.updateWidgets("AutoRotate", context);
    }

    /**
     * Gets state of gravity.
     *
     * @param context
     * @return true if more than moderately bright.
     */
    public static boolean getAutoRotateState(Context context) {
        int autoRotate = Settings.System
                .getInt(context.getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0);

        if (autoRotate == 1) {
            return true;
        }

        return false;
    }

    public static void toggleData(Context context) {
        ConnectivityManager sConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");

        try {
            Method setMethod = ConnectivityManager.class.getMethod("setMobileDataEnabled",
                    new Class[]{boolean.class});

            if (getDataState(context)) {
                setMethod.invoke(sConnectivityManager, new Object[]{false});
            } else {
                setMethod.invoke(sConnectivityManager, new Object[]{true});
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 无广播，需要手动更新
        Utils.updateWidgets("AutoRotate", context);
    }

    public static boolean getDataState(Context context) {
        ConnectivityManager sConnectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        NetworkInfo info = sConnectivityManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if (info == null) {
            return false;
        }

        if (info.getState() == NetworkInfo.State.DISCONNECTED) {
            return false;
        } else if (info.getState() == NetworkInfo.State.CONNECTED) {
            return true;
        }

        return false;
    }

    public static String getOpratorName() {
        ServiceState serviceState = NetStateListener.serviceState;

        if (serviceState == null) {
            return null;
        }

        String operatorName = serviceState.getOperatorAlphaShort();

        if (operatorName == null || operatorName.equals("") || operatorName.equalsIgnoreCase("(N/A)")) {
            operatorName = serviceState.getOperatorAlphaLong();
        }

        return operatorName;
    }

    public static void trafficStatis(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String cycle = sp.getString(Constants.PREF_TRAFFIC_STATISTICS_CYCLE, "0");
        String firstDay = sp.getString(Constants.PREF_TRAFFIC_STATISTICS_FIRST_DAY, "1");
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());

        // 如果是循环日期的凌晨1点,则重置计数为0
        if (((cycle.equals("0") && cal.get(Calendar.DAY_OF_MONTH) == Integer.parseInt(firstDay)) || (cycle.equals("1") && cal
                .get(Calendar.DAY_OF_WEEK) == Integer.parseInt(firstDay))) && cal.get(Calendar.HOUR_OF_DAY) == 1) {
            sp.edit().putLong(Constants.PREF_TRAFFICSTATS_COUNT_BYTES, 0).commit();
        }

        long curIn = TrafficStats.getMobileRxBytes();
        long curOut = TrafficStats.getMobileTxBytes();

        // 获取上一次保存的总流量
        long countIn = sp.getLong(Constants.PREF_TRAFFICSTATS_RXBYTES, curIn);
        long countOut = sp.getLong(Constants.PREF_TRAFFICSTATS_TXBYTES, curOut);

        long count = sp.getLong(Constants.PREF_TRAFFICSTATS_COUNT_BYTES, 0);

        // 当前总流量如果比上一次还小,说明已经重启过
        if (curIn < countIn) {
            // 把当前总数当作使用量
            count = count + curIn + curOut;
        } else {
            count = count + (curIn - countIn) + (curOut - countOut);
        }

        // 保存当前的流量
        sp.edit().putLong(Constants.PREF_TRAFFICSTATS_RXBYTES, curIn)
                .putLong(Constants.PREF_TRAFFICSTATS_TXBYTES, curOut)
                .putLong(Constants.PREF_TRAFFICSTATS_COUNT_BYTES, count).commit();
    }

    public static void toggleAlarm(Context context) {
        Intent alarmIntent = new Intent(Intent.ACTION_MAIN);
        alarmIntent.addCategory("android.intent.category.LAUNCHER");
        alarmIntent.setClassName("com.google.android.deskclock", "com.android.deskclock.DeskClock");

        if (context.getPackageManager().queryIntentActivities(alarmIntent, 0).size() == 0) {
            alarmIntent.setClassName("com.android.deskclock", "com.android.deskclock.DeskClock");
        }

        try {
            PendingIntent.getActivity(context, 0, alarmIntent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void enableTrafficStatisAlert(Context context) {
        Intent intent = new Intent(Constants.TRAFFIC_STATISTICS_ALERT);
        pendingIntent = PendingIntent.getBroadcast(context, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 15 * 60 * 1000, pendingIntent);
    }

    public static void disableTrafficStatisAlert(Context context) {
        AlarmManager am = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.cancel(pendingIntent);
    }

    public static void toggleMemory(Context context) {
        Intent alarmIntent = new Intent(Intent.ACTION_MAIN);
        alarmIntent.setClassName("com.android.settings", "com.android.settings.Settings$RunningServicesActivity");

        try {
            PendingIntent.getActivity(context, 0, alarmIntent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    public static void toggleStorage(Context context) {
        Intent alarmIntent = new Intent(Intent.ACTION_MAIN);
        alarmIntent.setClassName("com.android.settings", "com.android.settings.Settings$StorageSettingsActivity");

        try {
            PendingIntent.getActivity(context, 0, alarmIntent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }
}
