package com.realwidget;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import com.realwidget.db.Button;
import com.realwidget.util.Utils;
import com.realwidget.widget.calendar.CalendarWidget;
import com.realwidget.widget.gmail.GmailWidget;
import com.realwidget.widget.music.MusicWidget;
import com.realwidget.widget.sms.SMSWidget;
import com.realwidget.widget.toggle.ToggleWidget;
import com.realwidget.widget.weather.WeatherWidget;

import java.util.ArrayList;
import java.util.List;

public class RealWidgetService extends RemoteViewsService {
    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        int widgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, -1);

        if (widgetId < 0) {
            return null;
        }

        return new ButtonFactory(getApplicationContext(), widgetId);
    }

    static class ButtonFactory implements RemoteViewsFactory {
        private Context mContext;
        private int mWidgetId;
        private List<RemoteViews> viewsList;
        private List<Button[]> sortedBtns;

        public ButtonFactory(Context context, int widgetId) {
            mContext = context.getApplicationContext();
            mWidgetId = widgetId;
        }

        @Override
        public int getCount() {
            return viewsList.size();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public RemoteViews getLoadingView() {
            return new RemoteViews(mContext.getPackageName(), R.layout.item_widget_toggle);
        }

        @Override
        public RemoteViews getViewAt(int position) {
            return viewsList.get(position);
        }

        @Override
        public int getViewTypeCount() {
            // 返回1时，在resize时报错
            return 6;
        }

        /*
         * 做什么用的？
         */
        @Override
        public boolean hasStableIds() {
            return true;
        }

        @Override
        public void onCreate() {
        }

        @Override
        public void onDestroy() {
            Utils.deleteAllButtonImage(mContext, sortedBtns);
            MyApplication.getInstance().getDataOper().deleteWidgetById(mWidgetId);
        }

        @Override
        public void onDataSetChanged() {
            Cursor cursor = MyApplication.getInstance().getDataOper().queryWidgetById(mWidgetId);
            sortedBtns = getSortedData(cursor);
            buildViews(sortedBtns);
        }

        /**
         * 直接获取排序好的List 直接显示widget使用调用
         *
         * @param context
         * @param cursor
         * @return
         */
        public List<Button[]> getSortedData(Cursor cursor) {
            List<Button[]> buttons = new ArrayList<Button[]>();

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Button btn = new Button();
                        btn.btnId = cursor.getInt(Constants.TABLE_WIDGET.INDEX_BUTTON_ID);
                        btn.type = cursor.getInt(Constants.TABLE_WIDGET.INDEX_BUTTON_TYPE);
                        btn.size = cursor.getInt(Constants.TABLE_WIDGET.INDEX_BUTTON_SIZE);
                        btn.backColor = cursor.getInt(Constants.TABLE_WIDGET.INDEX_BACK_COLOR);
                        btn.iconColor = cursor.getInt(Constants.TABLE_WIDGET.INDEX_ICON_COLOR);
                        btn.labelColor = cursor.getInt(Constants.TABLE_WIDGET.INDEX_LABEL_COLOR);
                        btn.label = cursor.getString(Constants.TABLE_WIDGET.INDEX_LABEL);
                        btn.intent = cursor.getString(Constants.TABLE_WIDGET.INDEX_INTENT);
                        btn.iconFile = cursor.getString(Constants.TABLE_WIDGET.INDEX_ICON_FILE);
                        btn.backFile = cursor.getString(Constants.TABLE_WIDGET.INDEX_BACK_FILE);

                        int listSize = buttons.size();

                        if (btn.size == 1) {
                            if (listSize != 0) {
                                // 找到最后一项
                                Button[] tmp = buttons.get(listSize - 1);

                                // 如果最后一项只有一个按钮，且为小按钮,则放到第二个位置
                                if (tmp[0] != null && tmp[0].size == 1 && tmp[1] == null) {
                                    tmp[1] = btn;
                                } else {
                                    buttons.add(new Button[]{btn, null});
                                }
                            }
                            // 新增一行，加到第一个位置
                            else {
                                buttons.add(new Button[]{btn, null});
                            }
                        } else {
                            buttons.add(new Button[]{btn, null});
                        }
                    }
                    while (cursor.moveToNext());
                }

                cursor.close();
                cursor = null;
            }

            return buttons;
        }

        private void buildViews(List<Button[]> btn) {
            viewsList = new ArrayList<RemoteViews>();

            for (Button[] buttons : btn) {
                // 小按钮
                if (buttons[0] != null) {
                    viewsList.add(buildWidget(buttons));
                }
            }
        }

        private RemoteViews buildWidget(Button[] btn) {
            RemoteViews view = null;

            switch (btn[0].type) {
                case Constants.BUTTON_CALENDAR:
                    view = btn[0].size == 2 ? CalendarWidget.getInstance(mContext).buildRemoteViews(btn) : ToggleWidget
                            .getInstance(mContext).buildRemoteViews(btn);
                    break;
                case Constants.BUTTON_MUSIC:
                    view = MusicWidget.getInstance(mContext).buildRemoteViews(btn);
                    break;
                case Constants.BUTTON_SMS:
                    // 只有第一个按钮尺寸是2的才用大尺寸,其他都用小按钮
                    view = btn[0].size == 2 ? SMSWidget.getInstance(mContext).buildRemoteViews(btn) : ToggleWidget
                            .getInstance(mContext).buildRemoteViews(btn);
                    break;
                case Constants.BUTTON_WEATHER:
                    view = btn[0].size == 2 ? WeatherWidget.getInstance(mContext).buildRemoteViews(btn) : ToggleWidget
                            .getInstance(mContext).buildRemoteViews(btn);
                    break;
                case Constants.BUTTON_GMAIL:
                    view = btn[0].size == 2 ? GmailWidget.getInstance(mContext).buildRemoteViews(btn) : ToggleWidget
                            .getInstance(mContext).buildRemoteViews(btn);
                    break;
                default:
                    view = ToggleWidget.getInstance(mContext).buildRemoteViews(btn);
                    break;
            }

            return view;
        }
    }

}
