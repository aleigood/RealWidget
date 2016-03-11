package com.realwidget.ui.add;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ExpandableListView;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.ui.add.SubListAdapter.ListItem;

public class ButtonCreateActivity extends Activity implements ExpandableListView.OnChildClickListener {
    private ExpandableListView mListCategory;
    private SubListAdapter mCategoryAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_button);

        mListCategory = (ExpandableListView) findViewById(R.id.list_category);
        mCategoryAdapter = new SubListAdapter(this, mListCategory);
        mListCategory.setAdapter(mCategoryAdapter);
        mListCategory.setGroupIndicator(null);
        mListCategory.setDividerHeight(0);
        mListCategory.setOnChildClickListener(this);

        findViewById(R.id.back).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });
    }

    @Override
    public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
        ListItem item = (ListItem) mCategoryAdapter.getChild(groupPosition, childPosition);
        Intent intent = new Intent();

        switch (groupPosition) {
            case SubListAdapter.INDEX_WIDGET:
                switch (item.btnType) {
                    // 这三个默认是大尺寸
                    case Constants.BUTTON_MUSIC:
                    case Constants.BUTTON_CALENDAR:
                    case Constants.BUTTON_WEATHER:
                        intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE, 2);
                        break;
                    default:
                        intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE, 1);
                        break;
                }

                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, item.btnType);
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_LABEL, item.label);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case SubListAdapter.INDEX_APP:
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, Constants.BUTTON_APP);
                intent.putExtra("pkgName", item.packageName);
                intent.putExtra("clsName", item.className);
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_LABEL, item.label);
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE, 1);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case SubListAdapter.INDEX_SETTING:
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, Constants.BUTTON_SETTING);
                intent.putExtra("pkgName", item.packageName);
                intent.putExtra("clsName", item.className);
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_LABEL, item.label);
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE, 1);
                setResult(RESULT_OK, intent);
                finish();
                break;
            case SubListAdapter.INDEX_SHORTCUT:
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE, Constants.BUTTON_SHORTCUT);
                intent.putExtra("pkgName", item.packageName);
                intent.putExtra("clsName", item.className);
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_LABEL, item.label);
                intent.putExtra(Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE, 1);
                setResult(RESULT_OK, intent);
                finish();
                break;

            default:
                break;
        }
        return false;
    }

}
