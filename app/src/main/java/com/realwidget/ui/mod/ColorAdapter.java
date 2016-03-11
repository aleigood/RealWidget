package com.realwidget.ui.mod;

import android.content.Context;
import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.realwidget.R;
import com.realwidget.util.Utils;

import java.util.ArrayList;

public class ColorAdapter extends BaseAdapter {
    private final LayoutInflater mInflater;
    private final ArrayList<ListItem> mItems = new ArrayList<ListItem>();

    public ColorAdapter(Context context) {
        mInflater = LayoutInflater.from(context);
        Resources res = context.getResources();

        mItems.add(new ListItem(0, res.getString(R.string.bg_blue), res.getColor(R.color.bg_blue)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_brown), res.getColor(R.color.bg_brown)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_green), res.getColor(R.color.bg_green)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_lime), res.getColor(R.color.bg_lime)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_magenta), res.getColor(R.color.bg_magenta)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_mango), res.getColor(R.color.bg_mango)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_pink), res.getColor(R.color.bg_pink)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_purple), res.getColor(R.color.bg_purple)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_red), res.getColor(R.color.bg_red)));
        mItems.add(new ListItem(0, res.getString(R.string.bg_teal), res.getColor(R.color.bg_teal)));
        mItems.add(new ListItem(-1, res.getString(R.string.bg_custom) + "...", 0));
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

    private void bindView(View view, ListItem item) {
        TextView text = (TextView) view.findViewById(R.id.pref_current_txt);
        text.setText(item.text);
        ImageView image = (ImageView) view.findViewById(R.id.pref_current_img);

        if (item.color != 0) {
            image.setVisibility(View.VISIBLE);
            image.setImageBitmap(Utils.createOnePixyBitmap(item.color));
        } else {
            image.setVisibility(View.GONE);
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

    public static class ListItem {
        public final int itemId;
        public final String text;
        public final int color;

        public ListItem(int itemId, String text, int color) {
            this.itemId = itemId;
            this.text = text;
            this.color = color;
        }
    }

}
