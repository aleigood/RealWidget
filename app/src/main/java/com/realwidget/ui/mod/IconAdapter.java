package com.realwidget.ui.mod;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.util.Utils;

import java.util.ArrayList;

public class IconAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();
    private Context mContext;

    public IconAdapter(Context context) {
        mContext = context;
        mInflater = LayoutInflater.from(context);
        Resources res = context.getResources();

        mItems.add(new ListItem(Constants.ICON_NONE, "None"));
        mItems.add(new ListItem(Constants.ICON_GMAIL, res.getString(R.string.gmail)));
        mItems.add(new ListItem(Constants.ICON_CALENDAR, res.getString(R.string.calendar)));
        mItems.add(new ListItem(Constants.ICON_PHONE, res.getString(R.string.phone)));
        mItems.add(new ListItem(Constants.ICON_SMS, res.getString(R.string.sms)));
        mItems.add(new ListItem(Constants.ICON_ALARM, res.getString(R.string.alarm)));
        mItems.add(new ListItem(Constants.ICON_AUTO_ROTATE, res.getString(R.string.auto_rotate)));
        mItems.add(new ListItem(Constants.ICON_BLUETOOTH, res.getString(R.string.bluetooth)));
        mItems.add(new ListItem(Constants.ICON_BRIGHTNESS, res.getString(R.string.brightness)));
        mItems.add(new ListItem(Constants.ICON_CALENDAR, res.getString(R.string.calendar)));
        mItems.add(new ListItem(Constants.ICON_DATA, res.getString(R.string.data)));
        mItems.add(new ListItem(Constants.ICON_SYNC, res.getString(R.string.sync)));
        mItems.add(new ListItem(Constants.ICON_WIFI, res.getString(R.string.wifi)));
        mItems.add(new ListItem(Constants.ICON_CUSTOM, res.getString(R.string.bg_custom) + "..."));
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.activity_list_item, parent, false);
        } else {
            view = convertView;
        }

        bindView(view, mItems.get(position));
        return view;
    }

    private void bindView(View view, ListItem item) {
        TextView text = (TextView) view;
        text.setText(item.name);
        int icoId = Utils.getIconResId(item.icoId);

        if (icoId != -1) {
            text.setCompoundDrawablesWithIntrinsicBounds(mContext.getResources().getDrawable(icoId), null, null, null);
        } else {
            text.setCompoundDrawablesWithIntrinsicBounds(null, null, null, null);
        }
    }

    public int getCount() {
        return mItems.size();
    }

    public Object getItem(int position) {
        return mItems.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public class ListItem {
        public final int icoId;
        public final String name;

        public ListItem(int icoId, String name) {
            this.icoId = icoId;
            this.name = name;
        }
    }
}
