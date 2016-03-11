package com.realwidget.util;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.widget.RemoteViews;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.RealWidgetProvider;
import com.realwidget.RealWidgetService;
import com.realwidget.db.Button;
import com.realwidget.widget.calendar.CalendarWidget;
import com.realwidget.widget.gmail.GmailWidget;
import com.realwidget.widget.music.MusicWidget;
import com.realwidget.widget.sms.SMSWidget;
import com.realwidget.widget.weather.WeatherWidget;

public class ProviderUtil {
    // 开关状态
    public static final int STATE_OTHER = -1;
    public static final int STATE_DISABLED = 0;
    public static final int STATE_ENABLED = 1;
    public static final int STATE_INTERMEDIATE = 2;

    public static void onReceive(Context context, Intent intent, Class<?> cls) {
        SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if (Constants.ACTION_BUTTON_CLICK.equals(intent.getAction())) {
            Button btn = intent.getParcelableExtra("button");
            int btnType = btn.type;

            // 执行后直接返回
            switch (btnType) {
                case Constants.BUTTON_BRIGHTNESS:
                    SwitchUtils.toggleBirghtness(context);
                    return;
                case Constants.BUTTON_BLUETOOTH:
                    SwitchUtils.toggleBluetooth(context);
                    return;
                case Constants.BUTTON_SYNC:
                    SwitchUtils.toggleSync(context);
                    return;
                case Constants.BUTTON_GMAIL:
                    performAction(context, btn);
                    return;
                case Constants.BUTTON_WIFI:
                    SwitchUtils.toggleWifi(context);
                    return;
                case Constants.BUTTON_AUTO_ROTATE:
                    SwitchUtils.toggleAutoRotate(context);
                    return;
                case Constants.BUTTON_DATA:
                    SwitchUtils.toggleData(context);
                    return;
                case Constants.BUTTON_STORAGE:
                    SwitchUtils.toggleStorage(context);
                    return;
                case Constants.BUTTON_MEMORY:
                    SwitchUtils.toggleMemory(context);
                    return;
                case Constants.BUTTON_ALARM:
                    performAction(context, btn);
                    return;
                case Constants.BUTTON_PHONE:
                    performAction(context, btn);
                    return;
                case Constants.BUTTON_WEATHER:
                    performAction(context, btn);
                    return;
                case Constants.BUTTON_BETTERY:
                    performAction(context, btn);
                    return;
                case Constants.BUTTON_CALENDAR:
                    performAction(context, btn);
                    return;
                case Constants.BUTTON_SMS:
                    performAction(context, btn);
                    return;
                case Constants.BUTTON_MUSIC:
                    MusicWidget.getInstance(context).performAction(intent.getIntExtra("cmd", -1));
                    return;
                case Constants.BUTTON_APP:
                case Constants.BUTTON_SETTING:
                case Constants.BUTTON_SHORTCUT:
                    try {
                        Intent shortcutIntent = Intent.parseUri(btn.intent, 0);

                        if (shortcutIntent.getAction().equals("com.android.contacts.action.QUICK_CONTACT")) {
                            ContactsContract.QuickContact
                                    .showQuickContact(context, intent.getSourceBounds(), shortcutIntent.getData(),
                                            ContactsContract.QuickContact.MODE_LARGE, (String[]) null);
                        } else {
                            PendingIntent.getActivity(context, 0, shortcutIntent, 0).send();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return;
                default:
                    return;
            }
        }

        // 其他更新事件。一般是接收到状态更新的消息，所以要更新所有的widget
        int[] widgetIds = appWidgetManager.getAppWidgetIds(intent.getComponent());

        for (int i = 0; i < widgetIds.length; i++) {
            // Views有可能为空，如果为空直接跳过，不更新
            RemoteViews views = buildRemoteView(context, widgetIds[i], config, cls);

            if (views != null) {
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds[i], R.id.buttons_list);
                appWidgetManager.updateAppWidget(widgetIds[i], views);
            }
        }
    }

    private static RemoteViews buildRemoteView(Context context, int appWidgetId, SharedPreferences config, Class<?> cls) {
        boolean autofit = config.getBoolean(String.format(Constants.PREFS_AUTOFIT_FIELD_PATTERN, appWidgetId), true);
        RemoteViews views = null;

        if (autofit) {
            views = new RemoteViews(context.getPackageName(), R.layout.view_widget_auto_fit);
        } else {
            views = new RemoteViews(context.getPackageName(), R.layout.view_widget);
        }

        Intent updateIntent = new Intent(context, RealWidgetService.class);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
        updateIntent.setData(Uri.parse(updateIntent.toUri(Intent.URI_INTENT_SCHEME)));

        views.setRemoteAdapter(R.id.buttons_list, updateIntent);
        Intent ic = new Intent(context, cls);
        views.setPendingIntentTemplate(R.id.buttons_list,
                PendingIntent.getBroadcast(context, 0, ic, PendingIntent.FLAG_UPDATE_CURRENT));
        return views;
    }

    /**
     * 更新全部widget
     *
     * @param context
     */
    public static void updateWidget(Context context) {
        AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);
        int[] widgetIds = appWidgetManager.getAppWidgetIds(RealWidgetProvider.getComponentName(context));
        appWidgetManager.notifyAppWidgetViewDataChanged(widgetIds, R.id.buttons_list);
    }

    /**
     * 删除每个widget对应的参数
     */
    public static void onDeleted(Context context, int[] appWidgetIds) {
        for (int appWidgetId : appWidgetIds) {
            // 删除删除每个widget实例的按钮参数,如果不删除则会在修改是显示出来
            SharedPreferences config = PreferenceManager.getDefaultSharedPreferences(context);
            SharedPreferences.Editor configEditor = config.edit();

            configEditor.remove(String.format(Constants.PREFS_AUTOFIT_FIELD_PATTERN, appWidgetId)).commit();
        }
    }

    private static void performAction(Context context, Button btn) {
        // 如果没有自定义事件，就执行默认事件
        if (!Utils.isNull(btn.intent)) {
            try {
                PendingIntent.getActivity(context, 0, Intent.parseUri(btn.intent, 0), 0).send();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            switch (btn.type) {
                case Constants.BUTTON_GMAIL:
                    GmailWidget.getInstance(context).performAction();
                    return;
                case Constants.BUTTON_ALARM:
                    SwitchUtils.toggleAlarm(context);
                    return;
                case Constants.BUTTON_PHONE:
                    Intent phoneIntent = new Intent(Intent.ACTION_CALL_BUTTON);

                    try {
                        PendingIntent.getActivity(context, 0, phoneIntent, 0).send();
                    } catch (CanceledException e) {
                        e.printStackTrace();
                    }
                    return;
                case Constants.BUTTON_WEATHER:
                    WeatherWidget.getInstance(context).performAction();
                    return;
                case Constants.BUTTON_BETTERY:
                    Intent shortcutIntent = new Intent(Intent.ACTION_MAIN);
                    shortcutIntent.addCategory("com.android.settings.SHORTCUT");
                    shortcutIntent.setClassName("com.android.settings",
                            "com.android.settings.Settings$PowerUsageSummaryActivity");
                    shortcutIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                    try {
                        PendingIntent.getActivity(context, 0, shortcutIntent, 0).send();
                    } catch (CanceledException e) {
                        e.printStackTrace();
                    }
                    return;
                case Constants.BUTTON_CALENDAR:
                    CalendarWidget.getInstance(context).performAction();
                    return;
                case Constants.BUTTON_SMS:
                    SMSWidget.getInstance(context).performAction(btn.size);
                    return;
            }
        }
    }
}
