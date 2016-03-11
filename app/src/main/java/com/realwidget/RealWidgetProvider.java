package com.realwidget;

import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import com.realwidget.util.ProviderUtil;

public class RealWidgetProvider extends AppWidgetProvider {
    /**
     * Build {@link ComponentName} describing this specific
     * {@link AppWidgetProvider}
     */
    public static ComponentName getComponentName(Context context) {
        return new ComponentName(context, RealWidgetProvider.class);
    }

    /**
     * 同一个Widget在第一个widget被创建时调用，初始化用
     */
    @Override
    public void onEnabled(Context context) {
        super.onEnabled(context);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        super.onReceive(context, intent);
        ProviderUtil.onReceive(context, intent, RealWidgetProvider.class);
    }

    @Override
    public void onDeleted(Context context, int[] appWidgetIds) {
        super.onDeleted(context, appWidgetIds);
        ProviderUtil.onDeleted(context, appWidgetIds);
    }

}
