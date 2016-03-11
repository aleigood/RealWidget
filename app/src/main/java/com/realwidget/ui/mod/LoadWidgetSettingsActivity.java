package com.realwidget.ui.mod;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.db.Button;
import com.realwidget.util.XmlUtil;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class LoadWidgetSettingsActivity extends ListActivity {
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.title_load_widget);
        final Map<Long, List<Button>> widgets = XmlUtil.parseWidgetCfg(Constants.WIDGET_XML_NAME);
        final Long[] keys = widgets.keySet().toArray(new Long[]{});
        String[] items = new String[keys.length];

        for (int i = 0; i < keys.length; i++) {
            items[i] = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date(keys[i]));
        }

        setListAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_list_item_single_choice, items));
        ListView view = getListView();
        view.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                ArrayList<Button> buttons = (ArrayList<Button>) widgets.get(keys[arg2]);
                Intent intent = new Intent();
                intent.putParcelableArrayListExtra("buttons", buttons);
                setResult(RESULT_OK, intent);
                finish();
            }
        });
    }
}
