package com.realwidget.ui.comm;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.preference.Preference;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import com.realwidget.R;
import com.realwidget.ui.comm.ColorPickerView.OnColorChangedListener;
import com.realwidget.util.Utils;

public class ColorPreference extends Preference {
    private Dialog dlg;
    private Activity mParentActivity;
    private int mColor;
    private ImageView previewImg;
    private OnSelectListener mColorSelectedListener;

    public ColorPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorPreference(Context context) {
        super(context);
    }

    public void init(OnSelectListener callback, Activity parent, int defColor) {
        mParentActivity = parent;
        mColorSelectedListener = callback;
        mColor = defColor;
    }

    @Override
    protected void onClick() {
        super.onClick();
        dlg = createDialog();
        dlg.show();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        previewImg = (ImageView) view.findViewById(R.id.pref_preview);
        updateView(mColor);
    }

    private Dialog createDialog() {
        final AlertDialog.Builder builder = new AlertDialog.Builder(mParentActivity);
        builder.setTitle(R.string.back_color);

        OnColorChangedListener listener = new OnColorChangedListener() {
            public void colorChanged(int color) {
                applyColorAction(color);
                dlg.dismiss();
            }
        };

        // 主要是设置 调色板的布局
        LinearLayout layout = new LinearLayout(getContext());
        layout.setPadding(0, 0, 0, 0);
        layout.setOrientation(LinearLayout.VERTICAL);

        Display display = mParentActivity.getWindowManager().getDefaultDisplay();
        int width = display.getWidth();

        if (display.getWidth() > display.getHeight()) {
            width = display.getHeight();
        }

        int x = ((width - width / 3) / 2);

        final ColorPickerView mCPView = new ColorPickerView(getContext(), listener, mColor, x, true);
        LinearLayout.LayoutParams params1 = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT,
                LinearLayout.LayoutParams.FILL_PARENT);
        params1.gravity = Gravity.CENTER;
        mCPView.setLayoutParams(params1);
        mCPView.setFocusable(true);
        layout.addView(mCPView);

        // 添加一个隐藏的编辑框，为了可以打开键盘，否则无法显示软键盘
        EditText hideEdit = new EditText(mParentActivity);
        hideEdit.setVisibility(View.GONE);
        layout.addView(hideEdit);
        layout.setId(android.R.id.widget_frame);

        // 设置对话框的背景图片
        Bitmap bitmap = BitmapFactory.decodeResource(mParentActivity.getResources(), R.drawable.trans_bg);
        BitmapDrawable drawable = new BitmapDrawable(bitmap);
        drawable.setTileModeXY(TileMode.REPEAT, TileMode.REPEAT);
        drawable.setDither(true);
        layout.setBackgroundDrawable(drawable);

        final ScrollView hsv = new ScrollView(getContext());
        hsv.setFadingEdgeLength(0);
        hsv.addView(layout);
        hsv.setMinimumWidth(width);
        builder.setView(hsv);

        builder.setPositiveButton(mParentActivity.getResources().getString(android.R.string.ok), new OnClickListener() {
            public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                // 保存设置好的颜色
                applyColorAction(mCPView.getColor());
                dlg.dismiss();
            }
        });
        builder.setNegativeButton(mParentActivity.getResources().getString(android.R.string.cancel),
                new OnClickListener() {
                    public void onClick(DialogInterface paramDialogInterface, int paramInt) {
                        dlg.dismiss();
                    }
                });

        final AlertDialog dialog = builder.create();
        View dlgView = dialog.getLayoutInflater().inflate(R.layout.view_color_dlg_title, null, false);
        final EditText editText = (EditText) dlgView.findViewById(R.id.color_code_editor);
        editText.setText((Integer.toHexString(mColor) + "").toUpperCase());

        mCPView.setOnColorChangingListener(new ColorPickerView.onColorChangingListener() {
            public void onChange(int color) {
                editText.setText((Integer.toHexString(color) + "").toUpperCase());
            }
        });

        // 在输入框输入颜色时动态改变选择器颜色
        editText.addTextChangedListener(new TextWatcher() {
            public void onTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
                setColor();
            }

            public void beforeTextChanged(CharSequence paramCharSequence, int paramInt1, int paramInt2, int paramInt3) {
                setColor();
            }

            public void afterTextChanged(Editable paramEditable) {
                setColor();
            }

            private void setColor() {
                try {
                    int color = Color.parseColor("#" + editText.getText().toString());
                    mCPView.setColor(color);
                } catch (Exception e) {
                }
            }
        });

        dialog.setCustomTitle(dlgView);
        return dialog;
    }

    private void applyColorAction(int currentColor) {
        mColor = currentColor;
        updateView(mColor);
        mColorSelectedListener.onSelected(currentColor);
    }

    public void updateView(int color) {
        // 如果有背景图，优先显示
        if (previewImg != null) {
            previewImg.setImageBitmap(Utils.createOnePixyBitmap(color));
        }

        setSummary("#" + (Integer.toHexString(color) + "").toUpperCase());
    }
}
