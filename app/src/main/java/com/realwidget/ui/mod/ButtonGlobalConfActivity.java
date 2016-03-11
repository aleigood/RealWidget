package com.realwidget.ui.mod;

import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.ui.comm.ColorPreference;
import com.realwidget.ui.comm.OnSelectListener;

public class ButtonGlobalConfActivity extends PreferenceActivity {
    public int backColor;
    public int iconColor;
    public int textColor;
    public int size;
    public String iconFile;
    public String label;
    private CheckBoxPreference autofit;
    private CustomBackPreference backColorPref;
    private ColorPreference lableColorPref;
    private ColorPreference iconColorPref;

    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pref_button_global_conf);
        setContentView(R.layout.activity_button_config);
        final SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(this);
        final Intent intent = getIntent();

        backColor = intent.getIntExtra("defBackColor", 0);
        iconColor = intent.getIntExtra("defIconColor", 0);
        textColor = intent.getIntExtra("defTextColor", 0);

        autofit = (CheckBoxPreference) findPreference("autofit");
        autofit.setChecked(intent.getBooleanExtra("autofit", true));

        lableColorPref = (ColorPreference) findPreference("cus_label_color");
        lableColorPref.init(new OnSelectListener() {
            @Override
            public void onSelected(int color) {
                textColor = color;
                sp.edit().putInt(Constants.PREF_DEF_LABLE_COLOR, color).commit();
            }
        }, this, textColor);
        iconColorPref = (ColorPreference) findPreference("cus_icon_color");
        iconColorPref.init(new OnSelectListener() {
            @Override
            public void onSelected(int color) {
                iconColor = color;
                sp.edit().putInt(Constants.PREF_DEF_ICON_COLOR, color).commit();
            }
        }, this, iconColor);
        backColorPref = (CustomBackPreference) findPreference("cus_back_color");
        backColorPref.init(new OnSelectListener() {
            @Override
            public void onSelected(int color) {
                backColor = color;
                sp.edit().putInt(Constants.PREF_DEF_BACK_COLOR, color).commit();
            }
        }, this, backColor, null, false);

        findViewById(R.id.button_apply).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                // 重新设置新的参数
                intent.putExtra("autofit", autofit.isChecked());
                intent.putExtra("defBackColor", backColor);
                intent.putExtra("defIconColor", iconColor);
                intent.putExtra("defTextColor", textColor);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        findViewById(R.id.button_cancel).setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                finish();
            }
        });
    }
}
