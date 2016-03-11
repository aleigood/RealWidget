package com.realwidget.ui.add;

import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.TextView;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.util.IconResizer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SubListAdapter extends BaseExpandableListAdapter {
    public static final int INDEX_WIDGET = 0;
    public static final int INDEX_APP = 1;
    public static final int INDEX_SETTING = 2;
    public static final int INDEX_SHORTCUT = 3;
    private ButtonCreateActivity mActivity;
    private LayoutInflater mInflater;
    private List<String> mGroupList;
    private SparseArray<List<ListItem>> mChilds;
    private Handler mHandler;
    private PackageManager mPackageManager;

    public SubListAdapter(ButtonCreateActivity activity, ExpandableListView listView) {
        mActivity = activity;
        mPackageManager = mActivity.getPackageManager();
        mInflater = LayoutInflater.from(mActivity);
        mHandler = new Handler();

        // 顺序固定
        mGroupList = new ArrayList<String>();
        mGroupList.add("Widget");
        mGroupList.add("Application");
        mGroupList.add("Setting");
        mGroupList.add("Other");

        mChilds = new SparseArray<List<ListItem>>();

        // 初始化widget列表
        ArrayList<ListItem> widgetList = new ArrayList<ListItem>();
        widgetList.add(new ListItem(Constants.BUTTON_ALARM, mActivity.getText(R.string.alarm).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_AUTO_ROTATE, mActivity.getText(R.string.auto_rotate).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_BETTERY, mActivity.getText(R.string.battery).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_BLUETOOTH, mActivity.getText(R.string.bluetooth).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_BRIGHTNESS, mActivity.getText(R.string.brightness).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_CALENDAR, mActivity.getText(R.string.calendar).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_DATA, mActivity.getText(R.string.data).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_GMAIL, mActivity.getText(R.string.gmail).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_MEMORY, mActivity.getText(R.string.memory).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_PHONE, mActivity.getText(R.string.phone).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_SMS, mActivity.getText(R.string.sms).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_STORAGE, mActivity.getText(R.string.storage).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_SYNC, mActivity.getText(R.string.sync).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_WIFI, mActivity.getText(R.string.wifi).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_WEATHER, mActivity.getText(R.string.weather).toString()));
        widgetList.add(new ListItem(Constants.BUTTON_MUSIC, mActivity.getText(R.string.music).toString()));
        mChilds.put(INDEX_WIDGET, widgetList);
    }

    /**
     * Perform the query to determine which results to show and return a list of
     * them.
     */
    public List<ListItem> makeListItems(Intent targetIntent) {
        List<ResolveInfo> list = mPackageManager.queryIntentActivities(targetIntent, 0);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(mPackageManager));

        ArrayList<ListItem> result = new ArrayList<ListItem>(list.size());
        int listSize = list.size();

        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            result.add(new ListItem(mPackageManager, resolveInfo, new IconResizer(mActivity)));
        }

        return result;
    }

    public void onGroupExpanded(int groupPosition) {
        switch (groupPosition) {
            case INDEX_APP:
                if (mChilds.get(INDEX_APP) == null) {
                    Intent targetIntent = new Intent(Intent.ACTION_MAIN);
                    targetIntent.addCategory(Intent.CATEGORY_LAUNCHER);
                    targetIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mChilds.put(INDEX_APP, makeListItems(targetIntent));
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            SubListAdapter.this.notifyDataSetChanged();
                        }
                    });
                }
                break;
            case INDEX_SETTING:
                if (mChilds.get(INDEX_SETTING) == null) {
                    Intent settingsIntent = new Intent(Intent.ACTION_MAIN);
                    settingsIntent.addCategory("com.android.settings.SHORTCUT");
                    settingsIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mChilds.put(INDEX_SETTING, makeListItems(settingsIntent));
                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            SubListAdapter.this.notifyDataSetChanged();
                        }
                    });
                }
                break;
            case INDEX_SHORTCUT:
                if (mChilds.get(INDEX_SHORTCUT) == null) {
                    Intent shortcutIntent = new Intent("android.intent.action.CREATE_SHORTCUT");
                    mChilds.put(INDEX_SHORTCUT, makeListItems(shortcutIntent));

                    mHandler.post(new Runnable() {
                        @Override
                        public void run() {
                            SubListAdapter.this.notifyDataSetChanged();
                        }
                    });
                }
                break;
            default:
                break;
        }
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        return mChilds.get(groupPosition) == null ? null : mChilds.get(groupPosition).get(childPosition);
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
                             ViewGroup parent) {
        View view;

        if (convertView == null) {
            view = mInflater.inflate(R.layout.activity_list_item, parent, false);
        } else {
            view = convertView;
        }

        bindView(view, mChilds.get(groupPosition).get(childPosition));
        return view;

    }

    private void bindView(View view, ListItem item) {
        TextView text = (TextView) view;
        text.setText(item.label);

        // 标识是widget
        if (item.btnType != -1) {
            text.setCompoundDrawablesWithIntrinsicBounds(mActivity.getResources().getDrawable(R.drawable.ic_launcher),
                    null, null, null);
        } else {
            text.setCompoundDrawablesWithIntrinsicBounds(item.icon, null, null, null);
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return mChilds.get(groupPosition) != null ? mChilds.get(groupPosition).size() : 0;
    }

    @Override
    public Object getGroup(int groupPosition) {
        return mGroupList.get(groupPosition);
    }

    @Override
    public int getGroupCount() {
        return mGroupList.size();
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        View groupView = mInflater.inflate(R.layout.item_category, null);
        ((TextView) groupView.findViewById(R.id.name)).setText(mGroupList.get(groupPosition));
        return groupView;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }

    /**
     * An item in the list
     */
    public static class ListItem {
        public ResolveInfo resolveInfo;
        public CharSequence label;
        public Drawable icon;
        public String packageName;
        public String className;
        // 0:shortcut 1:widget
        public int btnType;

        ListItem(PackageManager pm, ResolveInfo resolveInfo, IconResizer resizer) {
            btnType = -1;
            this.resolveInfo = resolveInfo;
            label = resolveInfo.loadLabel(pm);
            ComponentInfo ci = resolveInfo.activityInfo;
            if (ci == null)
                ci = resolveInfo.serviceInfo;
            if (label == null && ci != null) {
                label = resolveInfo.activityInfo.name;
            }

            if (resizer != null) {
                icon = resizer.createIconThumbnail(resolveInfo.loadIcon(pm));
            }
            packageName = ci.applicationInfo.packageName;
            className = ci.name;
        }

        ListItem(int btnType, String label) {
            this.btnType = btnType;
            this.label = label;
        }
    }
}