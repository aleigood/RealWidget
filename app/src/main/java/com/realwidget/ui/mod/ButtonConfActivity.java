package com.realwidget.ui.mod;

import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceScreen;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.ui.comm.ColorPreference;
import com.realwidget.ui.comm.OnSelectListener;

import java.io.FileOutputStream;

public class ButtonConfActivity extends PreferenceActivity {
    public static final int REQUST_CODE_SEL_ICO = 1;
    public static final int REQUST_CODE_SEL_BACK = 2;
    public static final int REQUST_CODE_SEL_CROP = 3;
    private EditTextPreference titlePref;
    private CustomIconPreference iconPref;
    private Button mButton;
    private ColorPreference lableColorPref;
    private ColorPreference iconColorPref;
    private CustomBackPreference backColorPref;
    private ListPreference sizePref;
    private ChangeAppPreference destAppPref;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_button_conf);
        setContentView(R.layout.activity_button_config);
        Intent intent = getIntent();
        mButton = intent.getParcelableExtra("button");

        titlePref = (EditTextPreference) findPreference("title");
        titlePref.setText(mButton.label);
        titlePref.setSummary(mButton.label);
        titlePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newLabel = newValue.toString();
                titlePref.setSummary(newLabel);
                titlePref.setText(newLabel);
                mButton.label = newLabel;
                return false;
            }
        });

        iconPref = (CustomIconPreference) findPreference("cus_icon");
        iconPref.init(new OnSelectListener() {
            @Override
            public void onSelected(int icoId) {
                if (icoId == Constants.ICON_NONE) {
                    mButton.iconFile = "none";
                } else {
                    mButton.iconFile = icoId + "";
                }

                iconPref.update(mButton.iconFile);
            }
        }, this, mButton.iconFile);

        sizePref = (ListPreference) findPreference("btn_size");
        sizePref.setValue(mButton.size + "");
        sizePref.setSummary(mButton.size + "");
        sizePref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String newSize = newValue.toString();
                sizePref.setSummary(newSize);
                mButton.size = Integer.parseInt(newSize);
                return false;
            }
        });

        // 音乐部件不能设置大小
        if (mButton.type == Constants.BUTTON_MUSIC) {
            sizePref.setEnabled(false);
        }

        lableColorPref = (ColorPreference) findPreference("cus_label_color");
        lableColorPref.init(new OnSelectListener() {
            @Override
            public void onSelected(int color) {
                mButton.labelColor = color;
            }
        }, this, mButton.labelColor);
        iconColorPref = (ColorPreference) findPreference("cus_icon_color");
        iconColorPref.init(new OnSelectListener() {
            @Override
            public void onSelected(int color) {
                mButton.iconColor = color;
            }
        }, this, mButton.iconColor);
        backColorPref = (CustomBackPreference) findPreference("cus_back_color");
        backColorPref.init(new OnSelectListener() {
            @Override
            public void onSelected(int color) {
                mButton.backColor = color;
                // 如果选择了背景颜色，需要将背景文件清除掉
                mButton.backFile = "";
            }
        }, this, mButton.backColor, mButton.backFile, true);

        destAppPref = (ChangeAppPreference) findPreference("dest_app");
        destAppPref.init(new ChangeAppPreference.OnSelectListener() {
            @Override
            public void onSelected(String pkgName, String clsName) {
                Intent appIntent = new Intent(Intent.ACTION_MAIN);
                appIntent.addCategory("android.intent.category.LAUNCHER");
                appIntent.setClassName(pkgName, clsName);
                mButton.intent = appIntent.toURI();
            }
        }, this, mButton);

        if (mButton.type != Constants.BUTTON_ALARM && mButton.type != Constants.BUTTON_GMAIL
                && mButton.type != Constants.BUTTON_PHONE && mButton.type != Constants.BUTTON_WEATHER
                && mButton.type != Constants.BUTTON_BETTERY && mButton.type != Constants.BUTTON_CALENDAR
                && mButton.type != Constants.BUTTON_SMS) {
            PreferenceScreen cate = (PreferenceScreen) findPreference("btn_config");
            cate.removePreference(destAppPref);
        }

        findViewById(R.id.button_apply).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 重新设置新的参数
                Intent retIntent = new Intent();
                retIntent.putExtra("Button", mButton);
                setResult(RESULT_OK, retIntent);
                finish();
            }
        });
        findViewById(R.id.button_cancel).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }

    // 需要考虑到如果没有选择的图片的应用如何返回，和没选择图片如何返回
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data == null) {
            return;
        }

        switch (requestCode) {
            case REQUST_CODE_SEL_ICO:
                try {
                    Uri localUri = data.getData();
                    ContentResolver localContentResolver = getContentResolver();
                    Cursor cursor = localContentResolver.query(localUri, new String[]{"_data"}, null, null, null);
                    cursor.moveToFirst();
                    String fileName = cursor.getString(0);
                    cursor.close();

                    Bitmap arrayOfBitmap = makeDockIcon(BitmapFactory.decodeFile(fileName));

                    String newFileName = System.currentTimeMillis() + "";
                    saveImageFile(arrayOfBitmap, newFileName);
                    arrayOfBitmap.recycle();

                    // 先删除原来的文件
                    deleteFile(mButton.iconFile);
                    mButton.iconFile = newFileName;
                    iconPref.update(mButton.iconFile);
                } catch (Exception e) {
                    Toast.makeText(this, R.string.select_img_error, Toast.LENGTH_LONG).show();
                }
                break;
            case REQUST_CODE_SEL_BACK:
                int heigth = getResources().getDimensionPixelSize(R.dimen.button_width)
                        + getResources().getDimensionPixelSize(R.dimen.divider_width);
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setData(data.getData());
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", mButton.size);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", heigth * mButton.size);
                intent.putExtra("outputY", heigth);
                intent.putExtra("noFaceDetection", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, REQUST_CODE_SEL_CROP);
                break;
            case REQUST_CODE_SEL_CROP:
                Bundle extras = data.getExtras();

                if (extras != null) {
                    Bitmap backBitmap = extras.getParcelable("data");
                    String newFileName = System.currentTimeMillis() + "";
                    saveImageFile(backBitmap, newFileName);
                    backBitmap.recycle();

                    // 先删除原来的文件
                    deleteFile(mButton.backFile);
                    mButton.backFile = newFileName;
                    backColorPref.update(mButton.backFile);
                }
                break;

            default:
                break;
        }
    }

    /**
     * 生成图标
     *
     * @param paramBitmap
     * @return
     */
    private Bitmap makeDockIcon(Bitmap paramBitmap) {
        // 采用48dip的大小
        int scaledSize = (int) Math.abs(48.0F * getResources().getDisplayMetrics().density);
        int width = paramBitmap.getWidth();
        int height = paramBitmap.getHeight();
        int newWidth = width;
        int newHeight = height;

        if ((width > scaledSize) || (height > scaledSize)) {
            if (width > height) {
                // 使图像不会拉伸
                newWidth = scaledSize;
                newHeight = scaledSize * height / width;
            } else {
                newHeight = scaledSize;
                newWidth = scaledSize * width / height;
            }
        }

        int left = (scaledSize - newWidth) / 2;
        int right = (scaledSize - newHeight) / 2;
        Rect srcRect = new android.graphics.Rect(0, 0, width, height);
        // 如果图像本身小于48，使图像居中
        Rect dstRect = new android.graphics.Rect(left, right, left + newWidth, right + newHeight);

        Canvas canvas = new Canvas();
        Paint paint = new Paint();
        paint.setFilterBitmap(true);
        paint.setAntiAlias(true);

        Bitmap bitmap = Bitmap.createBitmap(scaledSize, scaledSize, Bitmap.Config.ARGB_8888);
        canvas.setBitmap(bitmap);
        canvas.drawBitmap(paramBitmap, srcRect, dstRect, paint);

        return bitmap;
    }

    private void saveImageFile(Bitmap paramArrayOfBitmap, String fileName) {
        try {
            FileOutputStream fileOutputStream = openFileOutput(fileName, 0);
            paramArrayOfBitmap.compress(Bitmap.CompressFormat.PNG, 100, fileOutputStream);
            fileOutputStream.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public void deleteIconFile() {
        deleteFile(mButton.iconFile);
        mButton.iconFile = "";
        iconPref.update(mButton.iconFile);
    }
}
