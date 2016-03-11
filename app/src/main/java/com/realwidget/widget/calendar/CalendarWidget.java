package com.realwidget.widget.calendar;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.CalendarContract.Calendars;
import android.provider.CalendarContract.Events;
import android.provider.CalendarContract.Instances;
import android.text.format.DateFormat;
import android.text.format.DateUtils;
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
import java.util.Date;
import java.util.List;

public class CalendarWidget extends WidgetGenerator {
    // 最多显示两条
    static final int EVENT_MAX_COUNT = 2;
    static final String[] EVENT_PROJECTION = new String[]{Instances.ALL_DAY, Instances.BEGIN, Instances.END,
            Instances.TITLE, Instances.DESCRIPTION, Instances.EVENT_LOCATION, Instances.EVENT_ID, Instances.START_DAY,
            Instances.END_DAY, Instances.CALENDAR_COLOR, Instances.SELF_ATTENDEE_STATUS,};
    private static final String EVENT_SELECTION = Calendars.VISIBLE + "=1 AND " + Instances.END + ">=?";
    private static final String EVENT_SORT_ORDER = Instances.START_DAY + " ASC, " + Instances.START_MINUTE + " ASC, "
            + Instances.END_DAY + " ASC, " + Instances.END_MINUTE + " ASC LIMIT " + EVENT_MAX_COUNT;

    // private static final String EVENT_SELECTION_HIDE_DECLINED =
    // Calendars.VISIBLE + "=1 AND " + EventsEntity.ALL_DAY
    // + "=0 AND " + Instances.SELF_ATTENDEE_STATUS + "!=" +
    // Attendees.ATTENDEE_STATUS_DECLINED;
    private static CalendarWidget instance;
    private static List<EventInfo> eventInfos;
    private Context mContext;
    private CalendarContentObserver mObserver;

    private CalendarWidget(Context context) {
        mContext = context;
        mObserver = new CalendarContentObserver();
        updateContent();
    }

    public static CalendarWidget getInstance(Context context) {
        if (instance == null) {
            instance = new CalendarWidget(context);
        }

        return instance;
    }

    public int getEventCount() {
        return eventInfos != null ? eventInfos.size() : 0;
    }

    @Override
    public RemoteViews buildRemoteViews(Button[] button) {
        RemoteViews views = new RemoteViews(mContext.getPackageName(), R.layout.item_widget_calendar);
        views.setImageViewBitmap(R.id.bg, getBackImg(mContext, button[0]));
        Date date = new Date();
        views.setTextViewText(R.id.mounth, DateFormat.format("EEE, MMM ", date).toString());
        views.setTextViewText(R.id.day, DateFormat.format("d", date).toString());
        views.setTextViewText(R.id.label, button[0].label);
        java.text.DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(mContext);

        if (eventInfos != null && eventInfos.size() != 0) {
            EventInfo event1 = eventInfos.get(0);

            String startTime = timeFormatter.format(event1.start).toString();
            String endTime = timeFormatter.format(event1.end).toString();
            String eventDate = DateFormat.format("EEE, MMM d", event1.start).toString();

            views.setTextViewText(R.id.content1,
                    event1.allDay ? "All day: " + DateFormat.format("EEE, MMM d", event1.start).toString() : eventDate
                            + ", " + startTime + " - " + endTime);
            views.setImageViewBitmap(R.id.color1,
                    Utils.createOnePixyBitmap(Utils.getDisplayColorFromColor(event1.color)));
            views.setTextViewText(R.id.title1, event1.title);
            views.setTextColor(R.id.title1, button[0].labelColor);
            views.setTextColor(R.id.content1, button[0].labelColor);

            if (eventInfos.size() > 1) {
                EventInfo event2 = eventInfos.get(1);

                String startTime2 = timeFormatter.format(event2.start).toString();
                String endTime2 = timeFormatter.format(event2.end).toString();
                String eventDate2 = DateFormat.format("EEE, MMM d", event2.start).toString();

                views.setTextViewText(R.id.content2,
                        event2.allDay ? "All day: " + DateFormat.format("EEE, MMM d", event2.start).toString()
                                : eventDate2 + ", " + startTime2 + " - " + endTime2);
                views.setImageViewBitmap(R.id.color2,
                        Utils.createOnePixyBitmap(Utils.getDisplayColorFromColor(event2.color)));
                views.setTextViewText(R.id.title2, event2.title);
                views.setTextColor(R.id.title2, button[0].labelColor);
                views.setTextColor(R.id.content2, button[0].labelColor);
            }

            if (eventInfos.size() == 1) {
                views.setViewVisibility(R.id.event1, View.VISIBLE);
                views.setViewVisibility(R.id.event2, View.GONE);
                views.setViewVisibility(R.id.no_event, View.GONE);
            } else {
                views.setViewVisibility(R.id.event1, View.VISIBLE);
                views.setViewVisibility(R.id.event2, View.VISIBLE);
                views.setViewVisibility(R.id.no_event, View.GONE);
            }
        } else {
            views.setViewVisibility(R.id.event1, View.GONE);
            views.setViewVisibility(R.id.event2, View.GONE);
            views.setViewVisibility(R.id.no_event, View.VISIBLE);
            views.setTextViewText(R.id.no_event, mContext.getString(R.string.gadget_no_events));
            views.setTextColor(R.id.no_event, button[0].labelColor);
        }

        views.setTextColor(R.id.label, button[0].labelColor);
        views.setTextColor(R.id.mounth, button[0].labelColor);
        views.setTextColor(R.id.day, button[0].labelColor);

        views.setOnClickFillInIntent(R.id.btn, new Intent(Constants.ACTION_BUTTON_CLICK).putExtra("button", button[0]));
        return views;
    }

    @Override
    public View buildViews(ViewGroup parent, Button[] button) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.item_widget_calendar, parent, false);
        view.findViewById(R.id.pos1).setTag(button[0]);

        ((ImageView) view.findViewById(R.id.bg)).setImageBitmap(getBackImg(mContext, button[0]));
        TextView label = ((TextView) view.findViewById(R.id.label));
        TextView mounth = (TextView) view.findViewById(R.id.mounth);
        TextView day = (TextView) view.findViewById(R.id.day);
        TextView content1 = (TextView) view.findViewById(R.id.content1);
        ImageView color1 = (ImageView) view.findViewById(R.id.color1);
        TextView title1 = (TextView) view.findViewById(R.id.title1);
        TextView content2 = (TextView) view.findViewById(R.id.content2);
        ImageView color2 = (ImageView) view.findViewById(R.id.color2);
        TextView title2 = (TextView) view.findViewById(R.id.title2);

        View eventView1 = view.findViewById(R.id.event1);
        View eventView2 = view.findViewById(R.id.event2);
        TextView noEventView = (TextView) view.findViewById(R.id.no_event);

        Date date = new Date();
        mounth.setText(DateFormat.format("EEE, MMM ", date).toString());
        day.setText(DateFormat.format("d", date).toString());
        label.setText(button[0].label);
        java.text.DateFormat timeFormatter = android.text.format.DateFormat.getTimeFormat(mContext);

        if (eventInfos != null && eventInfos.size() != 0) {
            EventInfo event1 = eventInfos.get(0);

            String startTime = timeFormatter.format(event1.start).toString();
            String endTime = timeFormatter.format(event1.end).toString();
            String eventDate = DateFormat.format("EEE, MMM d", event1.start).toString();

            content1.setText(event1.allDay ? "All day: " + DateFormat.format("EEE, MMM d", event1.start).toString()
                    : eventDate + ", " + startTime + " - " + endTime);
            color1.setImageBitmap(Utils.createOnePixyBitmap(Utils.getDisplayColorFromColor(event1.color)));
            title1.setText(event1.title);
            title1.setTextColor(button[0].labelColor);
            content1.setTextColor(button[0].labelColor);

            if (eventInfos.size() > 1) {
                EventInfo event2 = eventInfos.get(1);

                String startTime2 = timeFormatter.format(event2.start).toString();
                String endTime2 = timeFormatter.format(event2.end).toString();
                String eventDate2 = DateFormat.format("EEE, MMM d", event2.start).toString();

                content2.setText(event2.allDay ? "All day: " + DateFormat.format("EEE, MMM d", event2.start).toString()
                        : eventDate2 + ", " + startTime2 + " - " + endTime2);
                color2.setImageBitmap(Utils.createOnePixyBitmap(Utils.getDisplayColorFromColor(event2.color)));
                title2.setText(event2.title);
                title2.setTextColor(button[0].labelColor);
                content2.setTextColor(button[0].labelColor);
            }

            if (eventInfos.size() == 1) {
                eventView1.setVisibility(View.VISIBLE);
                eventView2.setVisibility(View.GONE);
                noEventView.setVisibility(View.GONE);
            } else {
                eventView1.setVisibility(View.VISIBLE);
                eventView2.setVisibility(View.VISIBLE);
                noEventView.setVisibility(View.GONE);
            }
        } else {
            eventView1.setVisibility(View.GONE);
            eventView2.setVisibility(View.GONE);
            noEventView.setVisibility(View.VISIBLE);
            noEventView.setText(mContext.getString(R.string.gadget_no_events));
            noEventView.setTextColor(button[0].labelColor);
        }

        label.setTextColor(button[0].labelColor);
        mounth.setTextColor(button[0].labelColor);
        day.setTextColor(button[0].labelColor);

        // 去除点击效果
        view.findViewById(R.id.btn).setBackgroundResource(R.drawable.trans_selector);

        label.setText(button[0].label);
        label.setTextColor(button[0].labelColor);
        return view;
    }

    public void updateContent() {
        new Thread() {
            @Override
            public void run() {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
                // 显示最多多少天的事件
                int maxDays = Integer.parseInt(sp.getString(Constants.PREF_MAX_DAYS_OF_EVENT, "1"));
                long now = System.currentTimeMillis();
                // Add a day on either side to catch all-day events
                long begin = now - DateUtils.DAY_IN_MILLIS;
                long end = now + maxDays * DateUtils.DAY_IN_MILLIS + DateUtils.DAY_IN_MILLIS;

                Uri uri = Uri.withAppendedPath(Instances.CONTENT_URI, Long.toString(begin) + "/" + end);
                Cursor cursor = mContext.getContentResolver().query(uri, EVENT_PROJECTION, EVENT_SELECTION,
                        new String[]{Long.toString(now)}, EVENT_SORT_ORDER);

                if (cursor != null) {
                    eventInfos = new ArrayList<EventInfo>();

                    if (cursor.moveToFirst()) {
                        do {
                            EventInfo eventInfo = new EventInfo();
                            eventInfo.allDay = cursor.getInt(0) != 0;
                            eventInfo.start = cursor.getLong(1);
                            eventInfo.end = cursor.getLong(2);
                            eventInfo.title = cursor.getString(3);
                            eventInfo.desc = cursor.getString(4);
                            eventInfo.location = cursor.getString(5);
                            eventInfo.eventId = cursor.getLong(6);
                            eventInfo.startDay = cursor.getInt(7);
                            eventInfo.endDay = cursor.getInt(8);
                            eventInfo.color = cursor.getInt(9);
                            eventInfo.selfStatus = cursor.getInt(10);
                            eventInfos.add(eventInfo);
                        }
                        while (cursor.moveToNext());
                    }

                    cursor.close();
                }

                Utils.updateWidgets(CalendarWidget.class.getName(), mContext);
            }
        }.start();
    }

    public void registerListener() {
        // 监听短信的改变，比如新增或删除短信
        mContext.getApplicationContext().getContentResolver()
                .registerContentObserver(Events.CONTENT_URI, true, mObserver);
    }

    public void UnRegisterListener() {
        mContext.getApplicationContext().getContentResolver().unregisterContentObserver(mObserver);
    }

    public void performAction() {
        try {
            Intent intent = new Intent("android.intent.action.MAIN");
            intent.addCategory("android.intent.category.LAUNCHER");
            intent.setClassName("com.google.android.calendar", "com.android.calendar.AllInOneActivity");

            if (mContext.getPackageManager().queryIntentActivities(intent, 0).size() == 0) {
                intent.setClassName("com.android.calendar", "com.android.calendar.AllInOneActivity");
            }

            PendingIntent.getActivity(mContext, 0, intent, 0).send();
        } catch (CanceledException e) {
            e.printStackTrace();
        }
    }

    private class CalendarContentObserver extends ContentObserver {
        public CalendarContentObserver() {
            super(null);
        }

        /**
         * 监听数据更新
         */
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            updateContent();
        }
    }
}

class EventInfo {
    boolean allDay;
    long start;
    long end;
    String title;
    String desc;
    String location;
    long eventId;
    int startDay;
    int endDay;
    int color;
    int selfStatus;
}