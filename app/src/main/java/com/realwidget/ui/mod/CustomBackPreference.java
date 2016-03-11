package com.realwidget.ui.mod;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Shader.TileMode;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.preference.Preference;
import android.provider.MediaStore;
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
import android.widget.SimpleAdapter;
import com.realwidget.R;
import com.realwidget.ui.comm.ColorPickerView;
import com.realwidget.ui.comm.ColorPickerView.OnColorChangedListener;
import com.realwidget.ui.comm.OnSelectListener;
import com.realwidget.ui.mod.ColorAdapter.ListItem;
import com.realwidget.util.Utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CustomBackPreference extends Preference {
    private Dialog dlg;
    private Activity mParentActivity;
    private int mBackColor;
    private String mBackFile;
    private ImageView previewImg;
    private OnSelectListener mColorSelectedListener;
    private boolean mHasImageOption;

    public CustomBackPreference(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public CustomBackPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CustomBackPreference(Context context) {
        super(context);
    }

    public void init(OnSelectListener callback, Activity parent, int defColor, String defImage, boolean hasImageOption) {
        mHasImageOption = hasImageOption;
        mParentActivity = parent;
        mColorSelectedListener = callback;
        mBackColor = defColor;
        mBackFile = defImage == null ? "" : defImage;
    }

    @Override
    protected void onClick() {
        super.onClick();

        if (mHasImageOption) {
            Map<String, Object> item1 = new HashMap<String, Object>();
            item1.put("title", mParentActivity.getString(R.string.custom_color));
            Map<String, Object> item2 = new HashMap<String, Object>();
            item2.put("title", mParentActivity.getString(R.string.custom_image));

            final ArrayList<Map<String, Object>> items = new ArrayList<Map<String, Object>>();
            items.add(item1);
            items.add(item2);

            new AlertDialog.Builder(mParentActivity)
                    .setTitle(R.string.back_color)
                    .setAdapter(
                            new SimpleAdapter(mParentActivity, items, R.layout.item_custom_back_option,
                                    new String[]{"title"}, new int[]{R.id.back_txt}), new OnClickListener() {
                                public void onClick(DialogInterface arg0, int arg1) {
                                    if (items.get(arg1).get("title")
                                            .equals(mParentActivity.getString(R.string.custom_color))) {
                                        showColorDlg();
                                    } else {
                                        Uri localUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                                        Intent localIntent = new Intent("android.intent.action.PICK", localUri);
                                        mParentActivity.startActivityForResult(localIntent,
                                                ButtonConfActivity.REQUST_CODE_SEL_BACK);
                                    }
                                }
                            }).setInverseBackgroundForced(true).create().show();
        } else {
            showColorDlg();
        }
    }

    private void showColorDlg() {
        final ColorAdapter mAdapter = new ColorAdapter(mParentActivity);

        (new AlertDialog.Builder(mParentActivity)).setTitle("背景颜色").setAdapter(mAdapter, new OnClickListener() {
            public void onClick(DialogInterface arg0, int imgId) {
                ListItem item = (ListItem) mAdapter.getItem(imgId);

                if (item.itemId == -1) {
                    dlg = createDialog();
                    dlg.show();
                } else {
                    applyColorAction(item.color);
                }
            }
        }).setInverseBackgroundForced(true).create().show();
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        previewImg = (ImageView) view.findViewById(R.id.pref_preview);

        if (mBackFile != null && !mBackFile.equals("")) {
            updateView(mBackFile);
        } else {
            updateView(mBackColor);
        }
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

        final ColorPickerView mCPView = new ColorPickerView(getContext(), listener, mBackColor, x, true);
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
        editText.setText((Integer.toHexString(mBackColor) + "").toUpperCase());

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
        // 如果背景文件存在要先删除
        mParentActivity.deleteFile(mBackFile);
        mBackFile = "";

        mBackColor = currentColor;
        update(mBackColor);
        mColorSelectedListener.onSelected(currentColor);
    }

    private void updateView(String backFile) {
        Bitmap backBitmap = Utils.getBitmapFromFile(mParentActivity, backFile);

        if (backBitmap != null) {
            BitmapDrawable cusIcon = new BitmapDrawable(backBitmap);
            previewImg.setImageDrawable(cusIcon);
        }
    }

    private void updateView(int color) {
        if (previewImg != null) {
            previewImg.setImageBitmap(Utils.createOnePixyBitmap(color));
        }

        switch (color) {
            case 0xFF1BA1E2:
                setSummary(mParentActivity.getText(R.string.bg_blue));
                break;
            case 0xFFA05000:
                setSummary(mParentActivity.getText(R.string.bg_brown));
                break;
            case 0xFF339933:
                setSummary(mParentActivity.getText(R.string.bg_green));
                break;
            case 0xFFA2C139:
                setSummary(mParentActivity.getText(R.string.bg_lime));
                break;
            case 0xFFD80073:
                setSummary(mParentActivity.getText(R.string.bg_magenta));
                break;
            case 0xFFF09609:
                setSummary(mParentActivity.getText(R.string.bg_mango));
                break;
            case 0xFFE671B8:
                setSummary(mParentActivity.getText(R.string.bg_pink));
                break;
            case 0xFFA200FF:
                setSummary(mParentActivity.getText(R.string.bg_purple));
                break;
            case 0xFFE51400:
                setSummary(mParentActivity.getText(R.string.bg_red));
                break;
            case 0xFF00ABA9:
                setSummary(mParentActivity.getText(R.string.bg_teal));
                break;
            default:
                setSummary("#" + (Integer.toHexString(color) + "").toUpperCase());
                break;
        }
    }

    public void update(String backFile) {
        mBackFile = backFile;
        notifyChanged();
    }

    public void update(int color) {
        mBackColor = color;
        notifyChanged();
    }
}
