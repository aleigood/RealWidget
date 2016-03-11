package com.realwidget.widget.weather;

import android.app.ListActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.util.Utils;
import com.realwidget.widget.weather.WeatherWidget.Condition;

import java.util.List;

public class ForecastActivity extends ListActivity {
    public static final String WEATHER_FORECAST_ACTION = "com.realwidget.WEATHER_FORECAST";
    private List<Condition> mConditions;
    private MainReceiver mReceiver;
    private Button btnRefresh;
    private ProgressBar progressBar;
    private ConditionAdapter adapter;
    private TextView text;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mConditions = WeatherWidget.getInstance(this).getForecast();
        setTitle(R.string.weather);
        adapter = new ConditionAdapter();
        getListView().setAdapter(adapter);

        View emptyView = getLayoutInflater().inflate(R.layout.item_weather_forecast_embed, null);
        ((ViewGroup) getListView().getParent()).addView(emptyView);
        getListView().setEmptyView(emptyView);

        text = (TextView) findViewById(R.id.text);
        btnRefresh = (Button) findViewById(R.id.btn_refresh);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);
        btnRefresh.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(ForecastActivity.this);
                String source = sp.getString(Constants.PREF_WEATHER_SOURCE, "0");
                final String location = sp.getString(Constants.PREF_WEATHER_LOCATION, "");

                if (source.equals("0")) {
                    btnRefresh.setVisibility(View.GONE);

                    if (!Utils.isAppExist(ForecastActivity.this, "com.google.android.apps.genie.geniewidget")) {
                        String s = "<html><body><br>Need \"News & Weather\"(by Google) app, please install it first."
                                + "<br> You can download it from <a href=\"http://realwidget.googlecode.com/files/GenieWidget_1.3.11.apk\">here</a>.<br></body></html>";
                        text.setText(Html.fromHtml(s));
                        text.setMovementMethod(LinkMovementMethod.getInstance());
                        text.setVisibility(View.VISIBLE);
                    } else {
                        progressBar.setVisibility(View.VISIBLE);
                        WeatherWidget.getInstance(ForecastActivity.this).updateContent();
                    }
                } else {
                    if (location.equals("")) {
                        Toast.makeText(ForecastActivity.this, R.string.weather_location_error, Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    if (!Utils.isNetworkAvaliable(ForecastActivity.this)) {
                        Toast.makeText(ForecastActivity.this, R.string.weather_network_error, Toast.LENGTH_SHORT)
                                .show();
                        return;
                    }

                    btnRefresh.setVisibility(View.GONE);
                    progressBar.setVisibility(View.VISIBLE);
                    WeatherWidget.getInstance(ForecastActivity.this).updateContent();
                }
            }
        });

        mReceiver = new MainReceiver();
        registerReceiver(mReceiver, new IntentFilter(WEATHER_FORECAST_ACTION));
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    class ConditionAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return mConditions.size();
        }

        @Override
        public Object getItem(int arg0) {
            return mConditions.get(arg0);
        }

        @Override
        public long getItemId(int arg0) {
            return arg0;
        }

        @Override
        public View getView(int arg0, View convertView, ViewGroup arg2) {
            Condition condition = mConditions.get(arg0);
            View view = getLayoutInflater().inflate(R.layout.item_weather_forecast, null);
            ((TextView) view.findViewById(R.id.day)).setText(condition.day);
            ((ImageView) view.findViewById(R.id.icon)).setImageResource(condition.iconRes);

            String desc = condition.condition + "\n" + condition.lowTemp + "°/" + condition.highTemp + "°";

            if (condition.chancePrecipitation != null && !condition.chancePrecipitation.equals("")) {
                desc += "\n" + condition.chancePrecipitation;
            }
            if (condition.humidity != null && !condition.humidity.equals("")) {
                desc += "\n" + condition.humidity;
            }
            if (condition.wind != null && !condition.wind.equals("")) {
                desc += "\n" + condition.wind;
            }

            ((TextView) view.findViewById(R.id.desc)).setText(desc);

            return view;
        }
    }

    class MainReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            btnRefresh.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.GONE);
            mConditions = WeatherWidget.getInstance(ForecastActivity.this).getForecast();
            adapter.notifyDataSetChanged();
        }
    }
}
