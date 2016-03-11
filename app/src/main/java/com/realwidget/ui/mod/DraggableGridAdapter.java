package com.realwidget.ui.mod;

import android.content.Context;
import android.database.Cursor;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import com.realwidget.Constants;
import com.realwidget.db.Button;
import com.realwidget.widget.calendar.CalendarWidget;
import com.realwidget.widget.gmail.GmailWidget;
import com.realwidget.widget.music.MusicWidget;
import com.realwidget.widget.sms.SMSWidget;
import com.realwidget.widget.toggle.ToggleWidget;
import com.realwidget.widget.weather.WeatherWidget;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DraggableGridAdapter extends BaseAdapter {
    private List<Button[]> mSortedButtons;
    private List<Button> mSourceButtons;

    private Context mContext;

    // 修改widget时调用
    public DraggableGridAdapter(WidgetConfigureActivity context, Cursor cursor) {
        super();
        mContext = context;
        mSourceButtons = initData(cursor);
        mSortedButtons = getSortedData(mContext, mSourceButtons);
    }

    // 创建widget时调用
    public DraggableGridAdapter(WidgetConfigureActivity context, List<Button> buttons) {
        super();
        mContext = context;
        mSourceButtons = buttons;
        mSortedButtons = getSortedData(mContext, mSourceButtons);
    }

    /**
     * 获取未排序好的List
     *
     * @param context
     * @param cursor
     * @return
     */
    public static List<Button> initData(Cursor cursor) {
        List<Button> buttons = new ArrayList<Button>();

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

                    buttons.add(btn);
                }
                while (cursor.moveToNext());
            }

            cursor.close();
            cursor = null;
        }

        return buttons;
    }

    @Override
    public int getCount() {
        return mSortedButtons.size();
    }

    @Override
    public Object getItem(int position) {
        return mSortedButtons.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Button[] buttons = mSortedButtons.get(position);

        // 尺寸是1的都是toggle类型
        if (buttons[0] != null && buttons[0].size == 1) {
            return ((ToggleWidget.getInstance(mContext)).buildViews(parent, buttons));
        } else {
            // 尺寸是2的使用特殊layout
            switch (buttons[0].type) {
                case Constants.BUTTON_CALENDAR:
                    return CalendarWidget.getInstance(mContext).buildViews(parent, buttons);
                case Constants.BUTTON_MUSIC:
                    return MusicWidget.getInstance(mContext).buildViews(parent, buttons);
                case Constants.BUTTON_SMS:
                    return SMSWidget.getInstance(mContext).buildViews(parent, buttons);
                case Constants.BUTTON_WEATHER:
                    return WeatherWidget.getInstance(mContext).buildViews(parent, buttons);
                case Constants.BUTTON_GMAIL:
                    return GmailWidget.getInstance(mContext).buildViews(parent, buttons);
                // 这种是toggle类型设置成尺寸2的情况
                default:
                    return ((ToggleWidget.getInstance(mContext)).buildViews(parent, buttons));
            }
        }
    }

    public void setItemsData(List<Button[]> buttons) {
        mSortedButtons = buttons;
        notifyDataSetChanged();
    }

    public List<Button> getSourceData() {
        return mSourceButtons;
    }

    public List<Button[]> getSortedData() {
        return mSortedButtons;
    }

    public void refresh() {
        mSortedButtons = getSortedData(mContext, mSourceButtons);
        notifyDataSetChanged();
    }

    public void refresh(List<Button> data) {
        mSortedButtons = getSortedData(mContext, data);
        notifyDataSetChanged();
    }

    /**
     * 在进行Widget配置时调用
     *
     * @param context
     * @param btns
     * @return
     */
    private List<Button[]> getSortedData(Context context, List<Button> btns) {
        List<Button[]> buttons = new ArrayList<Button[]>();
        // 先进行排序
        Collections.sort(btns);

        for (int i = 0; i < btns.size(); i++) {
            int listSize = buttons.size();
            Button btn = btns.get(i);

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

        return buttons;
    }

    public Context getmContext() {
        return mContext;
    }

}
