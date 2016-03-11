package com.realwidget.ui.mod;

import android.app.Activity;
import android.app.AlertDialog;
import android.appwidget.AppWidgetManager;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.SparseArray;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;
import com.realwidget.Constants;
import com.realwidget.MyApplication;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.ui.add.ButtonCreateActivity;
import com.realwidget.util.Utils;
import com.realwidget.util.XmlUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class WidgetConfigureActivity extends Activity {
    public static final int REQUEST_CODE_ADD_BUTTON = 1;
    public static final int REQUEST_CODE_CONFIG_BUTTON = 2;
    public static final int REQUEST_CODE_ADD_SHORTCUT = 3;
    public static final int REQUEST_CODE_CONFIG_ALL_BUTTON = 4;
    public static final int REQUEST_CODE_LOAD_WIDGET = 5;
    private int mAppWidgetId = AppWidgetManager.INVALID_APPWIDGET_ID;
    private List<Button> mBtns;
    private DraggableGridView mGridView;
    private int defaultBackColor;
    private int defaultIconColor;
    private int defaultTextColor;
    private boolean autofit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_widget_conf);
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);

        defaultBackColor = sp.getInt(Constants.PREF_DEF_BACK_COLOR, getResources().getColor(R.color.bg_blue));
        defaultIconColor = sp.getInt(Constants.PREF_DEF_ICON_COLOR, getResources().getColor(R.color.white));
        defaultTextColor = sp.getInt(Constants.PREF_DEF_LABLE_COLOR, getResources().getColor(R.color.white));

        // 获取正在创建的widgetID
        Intent intent = getIntent();
        boolean isModify = intent.getBooleanExtra("isModify", false);

        if (!isModify) {
            Bundle extras = intent.getExtras();

            if (extras != null) {
                mAppWidgetId = extras
                        .getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);

                if (mAppWidgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
                    setResult(RESULT_CANCELED);
                    finish();
                } else {
                    initButtonAction(false);
                    initData();
                }
            }
        } else {
            mAppWidgetId = intent.getIntExtra("widgetId", -1);

            if (mAppWidgetId != -1) {
                SparseArray<List<Button>> sa = Utils.getExistWidget(this);

                for (int i = 0; i < sa.size(); i++) {
                    if (mAppWidgetId == sa.keyAt(i)) {
                        mBtns = sa.get(mAppWidgetId);
                        mGridView = (DraggableGridView) findViewById(R.id.btn_list);
                        mGridView.init(this, mBtns);
                        break;
                    }
                }

                initButtonAction(true);
            }
        }

        autofit = sp.getBoolean(String.format(Constants.PREFS_AUTOFIT_FIELD_PATTERN, mAppWidgetId), true);
    }

    private void initButtonAction(final boolean isModify) {
        findViewById(R.id.apply).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setClickable(false);
                applyAction(isModify);
            }
        });

        findViewById(R.id.add).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(WidgetConfigureActivity.this, ButtonCreateActivity.class);
                startActivityForResult(intent, REQUEST_CODE_ADD_BUTTON);
            }
        });

        findViewById(R.id.save).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                saveConf();
            }
        });

        findViewById(R.id.load).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                loadConf();
            }
        });

        findViewById(R.id.settings).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                intent.putExtra("autofit", autofit);
                intent.putExtra("defBackColor", defaultBackColor);
                intent.putExtra("defIconColor", defaultIconColor);
                intent.putExtra("defTextColor", defaultTextColor);
                intent.setClass(WidgetConfigureActivity.this, ButtonGlobalConfActivity.class);
                startActivityForResult(intent, WidgetConfigureActivity.REQUEST_CODE_CONFIG_ALL_BUTTON);
            }
        });
    }

    private void applyAction(boolean isModify) {
        PreferenceManager.getDefaultSharedPreferences(this).edit()
                .putBoolean(String.format(Constants.PREFS_AUTOFIT_FIELD_PATTERN, mAppWidgetId), autofit).commit();

        if (isModify) {
            MyApplication.getInstance().getDataOper().deleteWidgetById(mAppWidgetId);
            MyApplication.getInstance().getDataOper().insertButtons(mAppWidgetId, mBtns);
            Utils.updateWidgets(WidgetConfigureActivity.class.getName(), this);
            finish();
        } else {
            MyApplication.getInstance().getDataOper().insertButtons(mAppWidgetId, mBtns);
            Intent result = new Intent();
            result.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, mAppWidgetId);
            setResult(RESULT_OK, result);
            finish();
        }
    }

    private void initData() {
        // 默认按钮初始化
        mBtns = new ArrayList<Button>();
        mBtns.add(new Button(0, Constants.BUTTON_PHONE, 1, getText(R.string.phone).toString(), defaultBackColor,
                defaultIconColor, defaultTextColor));
        mBtns.add(new Button(1, Constants.BUTTON_SMS, 1, getText(R.string.sms).toString(), defaultBackColor,
                defaultIconColor, defaultTextColor));

        mGridView = (DraggableGridView) findViewById(R.id.btn_list);
        mGridView.init(this, mBtns);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == Activity.RESULT_OK) {
            switch (requestCode) {
                case REQUEST_CODE_ADD_BUTTON:
                    int btnType = data.getIntExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, -1);
                    String pkgName = data.getStringExtra("pkgName");
                    String clsName = data.getStringExtra("clsName");

                    switch (btnType) {
                        case Constants.BUTTON_APP:
                            String appLabel = data.getStringExtra(Constants.TABLE_WIDGET.COLUMN_LABEL);
                            Button appButton = new Button(mBtns.size(), btnType, 1, appLabel, defaultBackColor,
                                    defaultIconColor, defaultTextColor);
                            Intent appIntent = new Intent(Intent.ACTION_MAIN);
                            appIntent.addCategory("android.intent.category.LAUNCHER");
                            appIntent.setClassName(pkgName, clsName);
                            appButton.intent = appIntent.toURI();
                            mBtns.add(appButton);
                            break;
                        case Constants.BUTTON_SETTING:
                            String settingLabel = data.getStringExtra(Constants.TABLE_WIDGET.COLUMN_LABEL);
                            Button settingButton = new Button(mBtns.size(), btnType, 1, settingLabel, defaultBackColor,
                                    defaultIconColor, defaultTextColor);
                            Intent settingIntent = new Intent(Intent.ACTION_MAIN);
                            settingIntent.addCategory("com.android.settings.SHORTCUT");
                            settingIntent.setClassName(pkgName, clsName);
                            settingButton.intent = settingIntent.toURI();
                            mBtns.add(settingButton);
                            break;
                        case Constants.BUTTON_SHORTCUT:
                            Intent shortcutIntent = new Intent("android.intent.action.CREATE_SHORTCUT");
                            shortcutIntent.setClassName(pkgName, clsName);
                            startActivityForResult(shortcutIntent, REQUEST_CODE_ADD_SHORTCUT);
                            break;
                        default:
                            Button widget = new Button(mBtns.size(), btnType, data.getIntExtra(
                                    Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE, 1),
                                    data.getStringExtra(Constants.TABLE_WIDGET.COLUMN_LABEL), defaultBackColor,
                                    defaultIconColor, defaultTextColor);
                            mBtns.add(widget);
                            break;
                    }
                    break;
                case REQUEST_CODE_CONFIG_BUTTON:
                    Button newButton = data.getParcelableExtra("Button");
                    mBtns.set(newButton.btnId, newButton);
                    break;
                case REQUEST_CODE_ADD_SHORTCUT:
                    Intent shortcutIntent = data.getParcelableExtra(Intent.EXTRA_SHORTCUT_INTENT);
                    Button shortcutButton = new Button(mBtns.size(), Constants.BUTTON_SHORTCUT, 1,
                            data.getStringExtra(Intent.EXTRA_SHORTCUT_NAME), defaultBackColor, defaultIconColor,
                            defaultTextColor);
                    shortcutButton.intent = shortcutIntent.toURI();
                    mBtns.add(shortcutButton);
                    break;
                case REQUEST_CODE_CONFIG_ALL_BUTTON:
                    int defBackColor = data.getIntExtra("defBackColor", 0);
                    int defIconColor = data.getIntExtra("defIconColor", 0);
                    int defTextColor = data.getIntExtra("defTextColor", 0);
                    defaultBackColor = defBackColor;
                    defaultIconColor = defIconColor;
                    defaultTextColor = defTextColor;
                    autofit = data.getBooleanExtra("autofit", true);

                    for (int i = 0; i < mBtns.size(); i++) {
                        mBtns.get(i).backColor = defBackColor;
                        mBtns.get(i).iconColor = defIconColor;
                        mBtns.get(i).labelColor = defTextColor;
                    }
                    break;
                case REQUEST_CODE_LOAD_WIDGET:
                    ArrayList<Button> btns = data.getParcelableArrayListExtra("buttons");

                    for (int i = 0; i < btns.size(); i++) {
                        Button button = btns.get(i);

                        if (button.backFile != null && !button.backFile.equals("")) {
                            Utils.copyFileToRomFromSD(this, button.backFile);
                        }

                        if (button.iconFile != null && !button.iconFile.equals("")) {
                            Utils.copyFileToRomFromSD(this, button.backFile);
                        }
                    }

                    mBtns.clear();
                    mBtns.addAll(btns);
                    break;
                default:
                    break;
            }
        }

        mGridView.refreshData();
    }

    private void loadConf() {
        Map<Long, List<Button>> map = XmlUtil.parseWidgetCfg(Constants.WIDGET_XML_NAME);

        if (map.size() == 0) {
            (new AlertDialog.Builder(this)).setTitle(R.string.title_load_widget)
                    .setPositiveButton(android.R.string.cancel, null).setMessage(R.string.no_saved_setting).create()
                    .show();
        } else {
            Intent intent = new Intent(this, LoadWidgetSettingsActivity.class);
            startActivityForResult(intent, REQUEST_CODE_LOAD_WIDGET);
        }
    }

    private void saveConf() {
        new AlertDialog.Builder(this).setTitle(getString(R.string.title_save_widget))
                .setMessage(getString(R.string.continue_confirm))
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface d, int w) {
                        Map<Long, List<Button>> widgets = XmlUtil.parseWidgetCfg(Constants.WIDGET_XML_NAME);
                        widgets.put(System.currentTimeMillis(), mBtns);

                        saveImgFile();

                        if (!XmlUtil.writeWidgetXml(widgets, Constants.WIDGET_XML_NAME)) {
                            Toast.makeText(WidgetConfigureActivity.this, R.string.save_conf_error, Toast.LENGTH_SHORT)
                                    .show();
                        } else {
                            Toast.makeText(WidgetConfigureActivity.this, R.string.save_conf_succ, Toast.LENGTH_SHORT)
                                    .show();
                        }
                    }

                    private void saveImgFile() {
                        for (int i = 0; i < mBtns.size(); i++) {
                            Button btn = mBtns.get(i);

                            if (btn.backFile != null && !btn.backFile.equals("")) {
                                Utils.copyFileToSDFromRom(WidgetConfigureActivity.this, btn.backFile);
                            }

                            if (btn.iconFile != null && !btn.iconFile.equals("")) {
                                Utils.copyFileToSDFromRom(WidgetConfigureActivity.this, btn.iconFile);
                            }
                        }
                    }
                }).setNegativeButton(android.R.string.cancel, null).show();
    }
}
