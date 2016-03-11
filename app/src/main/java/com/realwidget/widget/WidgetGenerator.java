package com.realwidget.widget;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RemoteViews;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.util.Utils;

import java.net.URISyntaxException;
import java.util.List;

public abstract class WidgetGenerator {
    public abstract RemoteViews buildRemoteViews(Button[] buttons);

    public abstract View buildViews(ViewGroup parent, Button[] button);

    protected Bitmap getBackImg(Context context, Button btn) {
        if (btn.backFile != null && !btn.backFile.equals("")) {
            return Utils.getBitmapFromFile(context, btn.backFile);
        } else {
            return Utils.createOnePixyBitmap(btn.backColor);
        }
    }

    protected Bitmap getIconFromRes(Context context, Button btn, int defIcoRes) {
        // 使用默认图标
        if (btn.iconFile == null || btn.iconFile.equals("")) {
            if (btn.iconColor == -1) {
                return Utils.getBitmapFromResource(context, defIcoRes);
            } else {
                return Utils.setBitmapColor(context, defIcoRes, 255, btn.iconColor);
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

    protected Bitmap getAppIco(Context context, Button btn) {

        if (btn.iconFile == null || btn.iconFile.equals("")) {
            Bitmap ico = null;

            try {
                List<Utils.AppListItem> appList = Utils.makeListItems(context, Intent.parseUri(btn.intent, 0));

                if (appList.size() > 0) {
                    ico = ((BitmapDrawable) appList.get(0).icon).getBitmap();
                }
            } catch (URISyntaxException e) {
                return null;
            }

            if (btn.iconColor != -1 && ico != null) {
                return Utils.setBitmapColor(context, ico, 255, btn.iconColor);
            } else {
                return ico;
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

    protected Bitmap getIconFromFile(Context context, Button btn) {
        long icoIndex = Long.parseLong(btn.iconFile);

        // 说明是数字，使用的是额外提供的图标
        if (icoIndex > 0 && icoIndex < 100) {
            int icoId = Utils.getIconResId((int) icoIndex);
            return Utils.getBitmapFromResource(context, icoId);
        } else {
            return Utils.getBitmapFromFile(context, btn.iconFile);
        }
    }

    protected String getStatusStr(Context context, boolean b) {
        return b ? context.getString(R.string.on) : context.getString(R.string.off);
    }
}
