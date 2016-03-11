package com.realwidget.ui.mod;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.realwidget.R;
import com.realwidget.util.Utils;
import com.realwidget.util.Utils.AppListItem;

import java.util.List;

public class AppAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final List<AppListItem> mItems;

    public AppAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        Intent targetIntent = new Intent(Intent.ACTION_MAIN, null);
        targetIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        mItems = Utils.makeListItems(context, targetIntent);
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.item_image, parent, false);
        } else {
            view = convertView;
        }

        bindView(view, mItems.get(position));
        return view;
    }

    private void bindView(View view, AppListItem item) {
        TextView text = (TextView) view.findViewById(R.id.pref_current_txt);
        text.setText(item.label);
        ImageView image = (ImageView) view.findViewById(R.id.pref_current_img);
        image.setImageDrawable(item.icon);
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

}
