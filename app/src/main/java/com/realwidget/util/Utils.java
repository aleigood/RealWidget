package com.realwidget.util;

import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ComponentInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Environment;
import android.text.format.DateUtils;
import android.text.format.Time;
import android.util.SparseArray;
import com.realwidget.Constants;
import com.realwidget.MainBrocastReceiver;
import com.realwidget.MyApplication;
import com.realwidget.R;
import com.realwidget.RealWidgetProvider;
import com.realwidget.db.Button;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class Utils {
    private static final float SATURATION_ADJUST = 1.3f;
    private static final float INTENSITY_ADJUST = 0.8f;
    private static boolean isUpdating = false;

    /**
     * 延迟1.5秒更新，1.5秒内如果再有更新请求，就会被忽略，防止频繁刷新
     *
     * @param context
     */
    public static void updateWidgets(final String tag, final Context context) {
        if (!isUpdating) {
            isUpdating = true;

            Timer timer = new Timer();
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    isUpdating = false;
                    context.sendBroadcast(new Intent(Constants.ACTION_WIDGET_UPDATE, null, context,
                            MainBrocastReceiver.class).putExtra("tag", tag));
                    this.cancel();
                }
            }, 1500);
        }
    }

    public static Bitmap createOnePixyBitmap(int color) {
        Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
        return Bitmap.createBitmap(new int[]{color}, 1, 1, localConfig);
    }

    public static Bitmap getBitmapFromResource(Context context, int resId) {
        InputStream is = context.getResources().openRawResource(resId);
        BitmapDrawable bmpDraw = new BitmapDrawable(is);
        return bmpDraw.getBitmap();
    }

    public static int setAlpha(int color, boolean trans) {
        int alpha = Color.alpha(trans ? 0x00000000 : 0xFF000000);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    public static Bitmap getBitmapFromFile(Context context, String fileName) {
        try {
            return BitmapFactory.decodeStream(context.openFileInput(fileName));
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    public static void deleteAllButtonImage(Context context, List<Button[]> sortedBtns) {
        if (sortedBtns == null) {
            return;
        }

        // 删除保存的数据和文件
        for (int i = 0; i < sortedBtns.size(); i++) {
            Button button0 = sortedBtns.get(i)[0];
            Button button1 = sortedBtns.get(i)[1];

            String iconFile0 = button0.iconFile;
            String backFile0 = button0.backFile;

            if (iconFile0 != null && !iconFile0.equals("")) {
                context.deleteFile(iconFile0);
            }

            if (backFile0 != null && !backFile0.equals("")) {
                context.deleteFile(backFile0);
            }

            if (button1 != null) {
                String iconFile1 = button1.iconFile;
                String backFile1 = button1.backFile;

                if (iconFile1 != null && !iconFile1.equals("")) {
                    context.deleteFile(iconFile1);
                }

                if (backFile1 != null && !backFile1.equals("")) {
                    context.deleteFile(backFile1);
                }
            }
        }
    }

    /**
     * Color 可以为空，为空时表示不加颜色滤镜
     */
    public static Bitmap setBitmapColor(Context context, int resId, Integer alpha, Integer color) {
        return setBitmapColor(context, getBitmapFromResource(context, resId), alpha, color);
    }

    public static Bitmap setBitmapColor(Context context, Bitmap source, Integer alpha, Integer color) {
        if (source == null) {
            return null;
        }

        Bitmap.Config localConfig = Bitmap.Config.ARGB_8888;
        int width = source.getWidth();
        int height = source.getHeight();
        Bitmap retBitmap = Bitmap.createBitmap(width, height, localConfig);
        Canvas localCanvas = new Canvas(retBitmap);
        BitmapDrawable drawable = new BitmapDrawable(source);

        if (color != null) {
            ColorFilter filter = new PorterDuffColorFilter(color, PorterDuff.Mode.MULTIPLY);
            drawable.setColorFilter(filter);
        } else {
            drawable.clearColorFilter();
        }

        if (alpha != null) {
            drawable.setAlpha(alpha);
        } else {
            drawable.setAlpha(255);
        }

        drawable.setBounds(0, 0, width, height);
        drawable.draw(localCanvas);
        return retBitmap;
    }

    public static int getIconResId(int icoId) {
        switch (icoId) {
            case Constants.ICON_GMAIL:
                return R.drawable.ic_gmail;
            case Constants.ICON_CALENDAR:
                return R.drawable.ic_calendar;
            case Constants.ICON_PHONE:
                return R.drawable.ic_phone;
            case Constants.ICON_SMS:
                return R.drawable.ic_sms;
            case Constants.ICON_ALARM:
                return R.drawable.ic_alarm;
            case Constants.ICON_AUTO_ROTATE:
                return R.drawable.ic_auto_rotate;
            case Constants.ICON_BLUETOOTH:
                return R.drawable.ic_bluetooth;
            case Constants.ICON_BRIGHTNESS:
                return R.drawable.ic_brightness;
            case Constants.ICON_DATA:
                return R.drawable.ic_data;
            case Constants.ICON_SYNC:
                return R.drawable.ic_sync;
            case Constants.ICON_WIFI:
                return R.drawable.ic_wifi;
            default:
                return -1;
        }
    }

    public static int celsiusToFahrenheit(int paramInt) {
        return (int) Math.round(32.0D + 1.8D * paramInt);
    }

    public static int fahrenheitToCelsius(int paramInt) {
        return (int) Math.round((5.0D / 9D) * (paramInt - 32));
    }

    public static String inputStream2String(InputStream in) throws IOException {
        StringBuffer out = new StringBuffer();
        byte[] b = new byte[4096];

        for (int n; (n = in.read(b)) != -1; ) {
            out.append(new String(b, 0, n));
        }
        return out.toString();
    }

    /**
     * 判断网络是否可用
     *
     * @param context
     * @return
     */
    public static boolean isNetworkAvaliable(Context context) {
        ConnectivityManager connMgr = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        if (connMgr.getActiveNetworkInfo() != null) {
            return connMgr.getActiveNetworkInfo().isAvailable();
        }

        return false;
    }

    public static boolean isBtnTypeExist(Context context, int type) {
        // 获取当前已经创建的部件
        int[] widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(context.getPackageName(), RealWidgetProvider.class.getName()));

        for (int i = 0; i < widgetIds.length; i++) {
            Cursor cursor = MyApplication.getInstance().getDataOper().queryWidgetByType(widgetIds[i], type);

            if (cursor != null) {
                if (cursor.getCount() != 0) {
                    cursor.close();
                    cursor = null;
                    return true;
                }

                cursor.close();
                cursor = null;
            }
        }

        return false;
    }

    public static SparseArray<List<Button>> getExistWidget(Context context) {
        SparseArray<List<Button>> sa = new SparseArray<List<Button>>();
        // 获取当前已经创建的部件
        int[] widgetIds = AppWidgetManager.getInstance(context).getAppWidgetIds(
                new ComponentName(context.getPackageName(), RealWidgetProvider.class.getName()));

        for (int i = 0; i < widgetIds.length; i++) {
            Cursor cursor = MyApplication.getInstance().getDataOper().queryWidgetById(widgetIds[i]);

            if (cursor != null) {
                if (cursor.moveToFirst()) {

                    List<Button> btns = new ArrayList<Button>();

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
                        btns.add(btn);
                    }
                    while (cursor.moveToNext());

                    sa.put(widgetIds[i], btns);
                }

                cursor.close();
                cursor = null;
            }
        }

        return sa;
    }

    public static boolean copyFile(File source, File dest) {
        FileInputStream sourceFile = null;
        FileOutputStream destFile = null;

        try {
            sourceFile = new FileInputStream(source);
            destFile = null;

            destFile = new FileOutputStream(dest);
            byte buffer[] = new byte[1024];
            int nbLecture;

            while ((nbLecture = sourceFile.read(buffer)) != -1) {
                destFile.write(buffer, 0, nbLecture);
            }

            destFile.flush();
            destFile.close();
            sourceFile.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        return true;
    }

    /**
     * 从SD卡恢复文件到手机内存
     *
     * @param fileName
     * @return
     */
    public static boolean copyFileToRomFromSD(Context context, String fileName) {
        File srcFile = null;

        // 从SD卡拷到手机目录
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File sdCardDir = Environment.getExternalStorageDirectory();
            File dir = new File(sdCardDir.getPath() + File.separator + Constants.DIR_NAME);

            if (!dir.exists()) {
                dir.mkdir();
            }

            srcFile = new File(dir.getPath(), fileName);
        }

        if (srcFile != null && srcFile.exists()) {
            try {
                FileInputStream sourceFile = new FileInputStream(srcFile);
                FileOutputStream fileOutputStream = context.openFileOutput(srcFile.getName(), 0);

                byte buffer[] = new byte[1024];
                int nbLecture;

                while ((nbLecture = sourceFile.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, nbLecture);
                }

                fileOutputStream.flush();
                fileOutputStream.close();
                sourceFile.close();
                return true;
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        return false;
    }

    public static boolean copyFileToSDFromRom(Context context, String fileName) {
        File file = context.getFileStreamPath(fileName);

        if (file.exists()) {
            if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                File sdCardDir = Environment.getExternalStorageDirectory();
                File dir = new File(sdCardDir.getPath() + File.separator + Constants.DIR_NAME);

                if (!dir.exists()) {
                    dir.mkdir();
                }

                File destFile = new File(dir.getPath(), file.getName());
                return Utils.copyFile(file, destFile);
            }
        }

        return false;
    }

    public static boolean isAppExist(Context context, String packageName) {
        if (packageName == null || "".equals(packageName)) {
            return false;
        }

        try {
            context.getPackageManager().getApplicationInfo(packageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    public static String formatTimeStampString(Context context, long when, boolean fullFormat) {
        Time then = new Time();
        then.set(when);
        Time now = new Time();
        now.setToNow();

        // Basic settings for formatDateTime() we want for all cases.
        int format_flags = DateUtils.FORMAT_NO_NOON_MIDNIGHT | DateUtils.FORMAT_ABBREV_ALL | DateUtils.FORMAT_CAP_AMPM;

        // If the message is from a different year, show the date and year.
        if (then.year != now.year) {
            format_flags |= DateUtils.FORMAT_SHOW_YEAR | DateUtils.FORMAT_SHOW_DATE;
        } else if (then.yearDay != now.yearDay) {
            // If it is from a different day than today, show only the date.
            format_flags |= DateUtils.FORMAT_SHOW_DATE;
        } else {
            // Otherwise, if the message is from today, show the time.
            format_flags |= DateUtils.FORMAT_SHOW_TIME;
        }

        // If the caller has asked for full details, make sure to show the date
        // and time no matter what we've determined above (but still make
        // showing
        // the year only happen if it is a different year from today).
        if (fullFormat) {
            format_flags |= (DateUtils.FORMAT_SHOW_DATE | DateUtils.FORMAT_SHOW_TIME);
        }

        return DateUtils.formatDateTime(context, when, format_flags);
    }

    public static int getDisplayColorFromColor(int color) {
        // STOPSHIP - Finalize color adjustment algorithm before shipping
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[1] = Math.min(hsv[1] * SATURATION_ADJUST, 1.0f);
        hsv[2] = hsv[2] * INTENSITY_ADJUST;
        return Color.HSVToColor(hsv);
    }

    /**
     * Perform the query to determine which results to show and return a list of
     * them.
     */
    public static List<AppListItem> makeListItems(Context context, Intent targetIntent) {
        PackageManager mPackageManager = context.getPackageManager();
        List<ResolveInfo> list = mPackageManager.queryIntentActivities(targetIntent, 0);
        Collections.sort(list, new ResolveInfo.DisplayNameComparator(mPackageManager));

        ArrayList<AppListItem> result = new ArrayList<AppListItem>(list.size());
        int listSize = list.size();

        for (int i = 0; i < listSize; i++) {
            ResolveInfo resolveInfo = list.get(i);
            result.add(new AppListItem(mPackageManager, resolveInfo, new IconResizer(context)));
        }

        return result;
    }

    public static boolean isNull(Object obj) {
        if (obj == null) {
            return true;
        }

        if (obj instanceof String) {
            return obj.equals("");
        }

        return true;
    }

    public static Bitmap createUsageIcon(Context context, float percentage, int color) {
        int width = context.getResources().getDimensionPixelSize(R.dimen.button_width) / 3;
        int halfStrokeWidth = context.getResources().getDimensionPixelSize(R.dimen.stroke_width);
        Bitmap bitmap = Bitmap.createBitmap(width, width, Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Paint paints = new Paint();
        paints.setAntiAlias(true);
        paints.setColor(color);
        paints.setStrokeWidth(halfStrokeWidth * 2);
        paints.setStyle(Paint.Style.STROKE);
        RectF rect = new RectF(halfStrokeWidth, halfStrokeWidth, width - halfStrokeWidth, width - halfStrokeWidth);
        canvas.drawArc(rect, 0, 360, false, paints);
        paints.setStrokeWidth(0);
        paints.setStyle(Paint.Style.FILL);
        canvas.drawArc(rect, 180, 360 * percentage, true, paints);
        return bitmap;
    }

    /**
     * An item in the list
     */
    public static class AppListItem {
        public ResolveInfo resolveInfo;
        public CharSequence label;
        public Drawable icon;
        public String packageName;
        public String className;

        AppListItem(PackageManager pm, ResolveInfo resolveInfo, IconResizer resizer) {
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
    }
}
