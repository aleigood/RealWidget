package com.realwidget.widget.toggle;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Environment;
import android.os.StatFs;
import android.preference.PreferenceManager;
import android.text.format.DateFormat;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RemoteViews;
import android.widget.TextView;
import com.realwidget.Constants;
import com.realwidget.MainBroadcastReceiver;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.util.MemInfoReader;
import com.realwidget.util.SwitchUtils;
import com.realwidget.util.Utils;
import com.realwidget.widget.WidgetGenerator;
import com.realwidget.widget.calendar.CalendarWidget;
import com.realwidget.widget.gmail.GmailWidget;
import com.realwidget.widget.gmail.GmailWidget.GmailInfo;
import com.realwidget.widget.sms.SMSWidget;
import com.realwidget.widget.toggle.BookmarkUtils.Bookmark;
import com.realwidget.widget.toggle.ContactUtils.Contact;
import com.realwidget.widget.weather.WeatherWidget;
import com.realwidget.widget.weather.WeatherWidget.Condition;

import java.net.URISyntaxException;
import java.util.Date;

public class ToggleWidget extends WidgetGenerator {
    private static ToggleWidget instance;
    private Context mContext;
    private MemInfoReader mMemInfoReader;

    private ToggleWidget(Context context) {
        mContext = context;
        mMemInfoReader = new MemInfoReader();
    }

    public static ToggleWidget getInstance(Context context) {
        if (instance == null) {
            instance = new ToggleWidget(context);
        }

        return instance;
    }

    @Override
    public RemoteViews buildRemoteViews(Button[] button) {
        Button leftButton = button[0];
        Button rightbutton = button[1];
        RemoteViews view = null;

        if (leftButton.size == 1) {
            view = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_toggle);
        } else {
            view = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_toggle_large);
        }

        view.setImageViewBitmap(R.id.bg1, getBackImg(mContext, leftButton));
        Toggle leftToggle = getTogglePropertis(mContext, leftButton);

        if (leftToggle.icon != null) {
            view.setViewVisibility(R.id.icon1, View.VISIBLE);
            view.setImageViewBitmap(R.id.icon1, leftToggle.icon);
        } else {
            view.setViewVisibility(R.id.icon1, View.INVISIBLE);
        }

        if (leftToggle.thumbnail != null) {
            view.setViewVisibility(R.id.bgImg1, View.VISIBLE);
            view.setImageViewBitmap(R.id.bgImg1, leftToggle.thumbnail);
        } else {
            view.setViewVisibility(R.id.bgImg1, View.INVISIBLE);
        }

        view.setTextViewText(R.id.label1, leftToggle.lable);
        view.setTextViewText(R.id.info1, leftToggle.info);
        view.setTextViewText(R.id.top_info1, leftToggle.topInfo);
        view.setTextViewText(R.id.title1, leftToggle.title);
        view.setTextViewText(R.id.count1, leftToggle.count);

        view.setTextColor(R.id.label1, leftButton.labelColor);
        view.setTextColor(R.id.info1, leftButton.labelColor);
        view.setTextColor(R.id.top_info1, leftButton.labelColor);
        view.setTextColor(R.id.title1, leftButton.labelColor);
        view.setTextColor(R.id.count1, leftButton.labelColor);

        view.setOnClickFillInIntent(R.id.btn1, new Intent(Constants.ACTION_BUTTON_CLICK).putExtra("button", leftButton));

        if (rightbutton != null) {
            view.setViewVisibility(R.id.pos2, View.VISIBLE);
            view.setImageViewBitmap(R.id.bg2, getBackImg(mContext, rightbutton));
            Toggle rightToggle = getTogglePropertis(mContext, rightbutton);

            if (rightToggle.icon != null) {
                view.setViewVisibility(R.id.icon2, View.VISIBLE);
                view.setImageViewBitmap(R.id.icon2, rightToggle.icon);
            } else {
                view.setViewVisibility(R.id.icon2, View.INVISIBLE);
            }

            if (rightToggle.thumbnail != null) {
                view.setViewVisibility(R.id.bgImg2, View.VISIBLE);
                view.setImageViewBitmap(R.id.bgImg2, rightToggle.thumbnail);
            } else {
                view.setViewVisibility(R.id.bgImg2, View.INVISIBLE);
            }

            view.setTextViewText(R.id.label2, rightToggle.lable);
            view.setTextViewText(R.id.info2, rightToggle.info);
            view.setTextViewText(R.id.top_info2, rightToggle.topInfo);
            view.setTextViewText(R.id.title2, rightToggle.title);
            view.setTextViewText(R.id.count2, rightToggle.count);

            view.setTextColor(R.id.label2, rightbutton.labelColor);
            view.setTextColor(R.id.info2, rightbutton.labelColor);
            view.setTextColor(R.id.top_info2, rightbutton.labelColor);
            view.setTextColor(R.id.title2, rightbutton.labelColor);
            view.setTextColor(R.id.count2, rightbutton.labelColor);

            view.setOnClickFillInIntent(R.id.btn2,
                    new Intent(Constants.ACTION_BUTTON_CLICK).putExtra("button", rightbutton));
        } else {
            view.setViewVisibility(R.id.pos2, View.INVISIBLE);
        }

        return view;
    }

    @Override
    public View buildViews(ViewGroup parent, Button[] button) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        Button leftButton = button[0];
        Button rightbutton = button[1];

        Toggle toggle1 = getTogglePropertis(mContext, leftButton);

        View view = null;

        if (leftButton.size == 1) {
            view = inflater.inflate(R.layout.item_widget_toggle, parent, false);
        } else {
            view = inflater.inflate(R.layout.item_widget_toggle_large, parent, false);
        }

        ((ImageView) view.findViewById(R.id.bg1)).setImageBitmap(getBackImg(mContext, leftButton));

        // 去除点击效果
        view.findViewById(R.id.btn1).setBackgroundResource(R.drawable.trans_selector);
        view.findViewById(R.id.pos1).setTag(leftButton);

        ImageView ico1 = (ImageView) view.findViewById(R.id.icon1);

        if (toggle1.icon != null) {
            ico1.setImageBitmap(toggle1.icon);
            ico1.setVisibility(View.VISIBLE);
        } else {
            ico1.setVisibility(View.INVISIBLE);
        }

        ImageView bgImg1 = (ImageView) view.findViewById(R.id.bgImg1);

        if (toggle1.thumbnail != null) {
            bgImg1.setVisibility(View.VISIBLE);
            bgImg1.setImageBitmap(toggle1.thumbnail);
        } else {
            bgImg1.setVisibility(View.INVISIBLE);
        }

        TextView textView1 = null;
        textView1 = ((TextView) view.findViewById(R.id.label1));
        textView1.setText(toggle1.lable);
        textView1.setTextColor(leftButton.labelColor);

        textView1 = ((TextView) view.findViewById(R.id.info1));
        textView1.setText(toggle1.info);
        textView1.setTextColor(leftButton.labelColor);

        textView1 = ((TextView) view.findViewById(R.id.top_info1));
        textView1.setText(toggle1.topInfo);
        textView1.setTextColor(leftButton.labelColor);

        textView1 = ((TextView) view.findViewById(R.id.title1));
        textView1.setText(toggle1.title);
        textView1.setTextColor(leftButton.labelColor);

        textView1 = ((TextView) view.findViewById(R.id.count1));
        textView1.setText(toggle1.count);
        textView1.setTextColor(leftButton.labelColor);

        View pos2 = view.findViewById(R.id.pos2);

        if (pos2 != null) {
            if (rightbutton != null) {
                pos2.setVisibility(View.VISIBLE);
                pos2.setTag(rightbutton);

                Toggle toggle2 = getTogglePropertis(mContext, rightbutton);
                ((ImageView) view.findViewById(R.id.bg2)).setImageBitmap(getBackImg(mContext, rightbutton));
                // 去除点击效果
                view.findViewById(R.id.btn2).setBackgroundResource(R.drawable.trans_selector);
                ImageView ico2 = (ImageView) view.findViewById(R.id.icon2);

                if (toggle2.icon != null) {
                    ico2.setImageBitmap(toggle2.icon);
                    ico2.setVisibility(View.VISIBLE);
                } else {
                    ico2.setVisibility(View.INVISIBLE);
                }

                ImageView bgImg2 = (ImageView) view.findViewById(R.id.bgImg2);

                if (toggle2.thumbnail != null) {
                    bgImg2.setVisibility(View.VISIBLE);
                    bgImg2.setImageBitmap(toggle2.thumbnail);
                } else {
                    bgImg2.setVisibility(View.INVISIBLE);
                }

                TextView textView2 = null;
                textView2 = ((TextView) view.findViewById(R.id.label2));
                textView2.setText(toggle2.lable);
                textView2.setTextColor(rightbutton.labelColor);

                textView2 = ((TextView) view.findViewById(R.id.info2));
                textView2.setText(toggle2.info);
                textView2.setTextColor(rightbutton.labelColor);

                textView2 = ((TextView) view.findViewById(R.id.top_info2));
                textView2.setText(toggle2.topInfo);
                textView2.setTextColor(rightbutton.labelColor);

                textView2 = ((TextView) view.findViewById(R.id.title2));
                textView2.setText(toggle2.title);
                textView2.setTextColor(rightbutton.labelColor);

                textView2 = ((TextView) view.findViewById(R.id.count2));
                textView2.setText(toggle2.count);
                textView2.setTextColor(rightbutton.labelColor);
            } else {
                pos2.setVisibility(View.INVISIBLE);
            }
        }
        return view;
    }

    public Toggle getTogglePropertis(Context context, Button btn) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        Toggle toggle = new Toggle();

        switch (btn.type) {
            case Constants.BUTTON_ALARM:
                String nextAlarm = SwitchUtils.getNextAlarm(context);
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_alarm);
                toggle.lable = nextAlarm == null ? context.getString(R.string.alarm) : nextAlarm;
                break;
            case Constants.BUTTON_BETTERY:
                toggle.info = MainBroadcastReceiver.batteryLevel + "%";
                toggle.lable = btn.label;

                if (sp.getBoolean(Constants.PREF_BATTERY_TEMPERATURE, false)) {
                    String unit = sp.getString(Constants.PREF_BATTERY_TEMPERATURE_UNIT, "0");

                    if (unit.equals("0")) {
                        toggle.topInfo = (MainBroadcastReceiver.batteryTemperatureC / 10) + "°";
                    } else {
                        toggle.topInfo = Utils.celsiusToFahrenheit(MainBroadcastReceiver.batteryTemperatureC / 10) + "°";
                    }
                }

                toggle.icon = getBatteryIcon(btn);
                break;
            case Constants.BUTTON_BLUETOOTH:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_bluetooth);
                toggle.lable = btn.label;
                toggle.info = getStatusStr(mContext, SwitchUtils.getBluetooth(context));
                break;
            case Constants.BUTTON_BRIGHTNESS:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_brightness);
                toggle.lable = btn.label;
                toggle.info = SwitchUtils.getBrightness(context) + "";
                break;
            case Constants.BUTTON_CALENDAR:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_calendar);
                toggle.lable = DateFormat.format("EEE, MMM d", new Date()).toString();
                int eventCount = CalendarWidget.getInstance(mContext).getEventCount();
                toggle.count = eventCount == 0 ? "" : eventCount + "";
                break;
            case Constants.BUTTON_WEATHER:
                Condition weather = WeatherWidget.getInstance(mContext).getCurrent();

                if (weather != null) {
                    toggle.icon = getIconFromRes(mContext, btn, weather.iconRes);
                    toggle.lable = weather.condition + ", " + weather.temperature + "°";
                } else {
                    toggle.lable = btn.label;
                }
                break;
            case Constants.BUTTON_GMAIL:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_gmail);
                toggle.lable = btn.label;
                GmailInfo mailInfo = GmailWidget.getInstance(mContext).getMailInfo();
                toggle.count = (mailInfo != null && mailInfo.unread != 0) ? mailInfo.unread + "" : "";
                break;
            case Constants.BUTTON_PHONE:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_phone);
                String opratorName = SwitchUtils.getOpratorName();
                toggle.lable = opratorName == null ? context.getString(R.string.phone) : opratorName;
                toggle.count = CallObserver.getInstance(context).getMissingCallCount();
                break;
            case Constants.BUTTON_SMS:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_sms);
                toggle.lable = btn.label;
                toggle.count = SMSWidget.getInstance(context).getUnreadSmsCount();
                break;
            case Constants.BUTTON_SYNC:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_sync);
                toggle.lable = btn.label;
                toggle.info = getStatusStr(mContext, SwitchUtils.getSync(context));
                break;
            case Constants.BUTTON_WIFI:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_wifi);
                WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                String currentSsid = wifiManager.getConnectionInfo().getSSID();

                if (currentSsid != null && !currentSsid.equals("")) {
                    toggle.lable = currentSsid;
                    toggle.info = "";
                } else {
                    toggle.lable = btn.label;
                    toggle.info = getStatusStr(mContext, SwitchUtils.getWifiState(context) == SwitchUtils.STATE_ENABLED);
                }
                break;
            case Constants.BUTTON_DATA:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_data);
                toggle.lable = btn.label;
                boolean dataState = SwitchUtils.getDataState(context);
                toggle.info = getStatusStr(mContext, dataState);

                if (sp.getBoolean(Constants.PREF_TRAFFIC_STATISTICS, false)) {
                    toggle.topInfo = Formatter.formatFileSize(mContext,
                            sp.getLong(Constants.PREF_TRAFFICSTATS_COUNT_BYTES, 0));
                }
                break;
            case Constants.BUTTON_MEMORY:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_data);
                toggle.lable = btn.label;
                mMemInfoReader.readMemInfo();
                long freeSize = mMemInfoReader.getFreeSize() + mMemInfoReader.getCachedSize();
                long total = mMemInfoReader.getTotalSize();
                toggle.icon = getUsageIcon(context, btn, ((float) total - (float) freeSize) / (float) total);
                toggle.info = Formatter.formatShortFileSize(mContext, freeSize);
                break;
            case Constants.BUTTON_STORAGE:
                toggle.lable = btn.label;
                StatFs statFs = new StatFs(Environment.getExternalStorageDirectory().getPath());
                long blockSize = statFs.getBlockSize();
                long totalBlocks = statFs.getBlockCount();
                long availableBlocks = statFs.getAvailableBlocks();
                toggle.icon = getUsageIcon(context, btn, ((float) totalBlocks - (float) availableBlocks)
                        / (float) totalBlocks);
                toggle.info = Formatter.formatFileSize(mContext, availableBlocks * blockSize);
                break;
            case Constants.BUTTON_AUTO_ROTATE:
                toggle.icon = getIconFromRes(mContext, btn, R.drawable.ic_auto_rotate);
                toggle.lable = btn.label;
                toggle.info = getStatusStr(mContext, SwitchUtils.getAutoRotateState(context));
                break;
            case Constants.BUTTON_APP:
            case Constants.BUTTON_SETTING:
                toggle.lable = btn.label;
                toggle.icon = getAppIco(context, btn);
                break;
            case Constants.BUTTON_SHORTCUT:
                try {
                    Intent shortcutIntent = Intent.parseUri(btn.intent, 0);
                    String data = shortcutIntent.getDataString();
                    String action = shortcutIntent.getAction();

                    if (data != null && (data.startsWith("https://") || data.startsWith("http://"))) {
                        Bookmark bookmark = BookmarkUtils.builderBookmark(context, shortcutIntent.getData());
                        toggle.title = btn.label;
                        toggle.thumbnail = bookmark.mBitmapData;
                    } else if (action != null && action.equals("com.android.contacts.action.QUICK_CONTACT"))
                    // || action.equals("android.intent.action.SENDTO") ||
                    // action
                    // .equals("android.intent.action.CALL")))
                    {
                        Contact contact = ContactUtils.builderContact(context, shortcutIntent.getData());
                        toggle.title = btn.label;
                        toggle.thumbnail = contact.mBitmapData;
                    } else {
                        toggle.lable = btn.label;
                        toggle.icon = getAppIco(context, btn);
                    }
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }

        return toggle;
    }

    protected Bitmap getUsageIcon(Context context, Button btn, float percentage) {
        // 使用默认图标
        if (btn.iconFile == null || btn.iconFile.equals("")) {
            if (btn.iconColor == -1) {
                return Utils.createUsageIcon(context, percentage, Color.WHITE);
            } else {
                return Utils.createUsageIcon(context, percentage, btn.iconColor);
            }
        } else if ("none".equals(btn.iconFile)) {
            return null;
        } else {
            if (btn.iconColor == -1) {
                return getIconFromFile(context, btn);
            } else {
                return Utils.setBitmapColor(context, getIconFromFile(context, btn), 255, btn.iconColor);
            }
        }
    }

    /**
     * @param btn
     * @return
     */
    private Bitmap getBatteryIcon(Button btn) {
        switch (MainBroadcastReceiver.batteryStatus) {
            // 表示是充电状态
            case BatteryManager.BATTERY_STATUS_CHARGING:
                return getIconFromRes(mContext, btn, R.drawable.ic_battery_charging);
            default:
                if (MainBroadcastReceiver.batteryLevel < 15) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_10);
                } else if (MainBroadcastReceiver.batteryLevel >= 15 && MainBroadcastReceiver.batteryLevel < 25) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_20);
                } else if (MainBroadcastReceiver.batteryLevel >= 25 && MainBroadcastReceiver.batteryLevel < 35) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_30);
                } else if (MainBroadcastReceiver.batteryLevel >= 35 && MainBroadcastReceiver.batteryLevel < 45) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_40);
                } else if (MainBroadcastReceiver.batteryLevel >= 45 && MainBroadcastReceiver.batteryLevel < 55) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_50);
                } else if (MainBroadcastReceiver.batteryLevel >= 55 && MainBroadcastReceiver.batteryLevel < 65) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_60);
                } else if (MainBroadcastReceiver.batteryLevel >= 65 && MainBroadcastReceiver.batteryLevel < 75) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_70);
                } else if (MainBroadcastReceiver.batteryLevel >= 75 && MainBroadcastReceiver.batteryLevel < 85) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_80);
                } else if (MainBroadcastReceiver.batteryLevel >= 85 && MainBroadcastReceiver.batteryLevel < 95) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_90);
                } else if (MainBroadcastReceiver.batteryLevel >= 95 && MainBroadcastReceiver.batteryLevel <= 100) {
                    return getIconFromRes(mContext, btn, R.drawable.ic_battery_100);
                }
                break;
        }

        return getIconFromRes(mContext, btn, R.drawable.ic_battery_100);
    }
}

class Toggle {
    public String lable = "";
    public String info = "";
    public String topInfo = "";
    public String title = "";
    public String count = "";
    public Bitmap icon;
    public Bitmap thumbnail;
    public Bitmap bgImage;
}
