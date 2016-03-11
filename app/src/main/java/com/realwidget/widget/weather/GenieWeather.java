package com.realwidget.widget.weather;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.format.DateFormat;
import com.realwidget.R;
import com.realwidget.widget.weather.WeatherWidget.Callback;
import com.realwidget.widget.weather.WeatherWidget.Condition;

import java.util.ArrayList;
import java.util.List;

public class GenieWeather {
    public static Uri CONTENT_URI = Uri.parse("content://com.google.android.apps.genie.geniewidget.weather/weather");
    private static final Uri CURRENT_WEATHER_URI = CONTENT_URI.buildUpon().appendPath("current").build();
    private static final Uri DAILY_WEATHER_URI = CONTENT_URI.buildUpon().appendPath("daily").build();
    private Context mContext;

    protected GenieWeather(Context context, final String local, final Callback listener) {
        mContext = context;

        new Thread() {
            @Override
            public void run() {
                listener.onDataChange(getCurrentWeather(), getForecastWeather());
            }
        }.start();
    }

    /**
     * "_id, fakeLocation, location, timestamp, begins, ends, pointInTime,
     * description, temperature, highTemperature,lowTemperature,
     * chancePrecipitation, wind, humidity, sunrise, sunset, iconUrl, iconResId"
     *
     * @param context
     * @param local
     * @return
     */
    public Condition getCurrentWeather() {
        Condition condition = null;

        try {
            Cursor cursor = mContext.getContentResolver().query(
                    CURRENT_WEATHER_URI.buildUpon().appendPath(System.currentTimeMillis() + "").build(), null, null,
                    null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    condition = new Condition();
                    condition.city = cursor.getString(2);
                    condition.condition = cursor.getString(7);
                    condition.temperature = cursor.getInt(8) + "";
                    int hTemp = cursor.getInt(9);
                    condition.highTemp = hTemp == 0 ? "--" : "" + hTemp;
                    int lTemp = cursor.getInt(10);
                    condition.lowTemp = lTemp == 0 ? "--" : "" + lTemp;
                    condition.chancePrecipitation = cursor.getInt(11) + "";
                    condition.wind = cursor.getString(12);
                    condition.humidity = cursor.getString(13);
                    condition.iconRes = getWeatherIconResource(cursor.getString(16));
                }

                cursor.close();
                cursor = null;
            }

            return condition;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return condition;
    }

    public List<Condition> getForecastWeather() {
        List<Condition> list = new ArrayList<Condition>();

        try {
            Cursor cursor = mContext.getContentResolver().query(
                    DAILY_WEATHER_URI.buildUpon().appendPath(System.currentTimeMillis() + "").build(), null, null,
                    null, null);

            if (cursor != null) {
                if (cursor.moveToFirst()) {
                    do {
                        Condition condition = new Condition();
                        condition.day = DateFormat.format("EEE", cursor.getLong(4)).toString();
                        condition.condition = cursor.getString(7);
                        int hTemp = cursor.getInt(9);
                        condition.highTemp = hTemp == 0 ? "--" : "" + hTemp;
                        int lTemp = cursor.getInt(10);
                        condition.lowTemp = lTemp == 0 ? "--" : "" + lTemp;
                        condition.iconRes = getWeatherIconResource(cursor.getString(16));
                        list.add(condition);
                    }
                    while (cursor.moveToNext());
                }

                cursor.close();
                cursor = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return list;
    }

    public int getWeatherIconResource(String paramString) {
        String str = paramString.substring(1 + paramString.lastIndexOf('/'));

        if (str.equals("ic_weather_chance_of_rain_SIZE.png")) {
            return R.drawable.ic_weather_chance_of_rain;
        } else if (str.equals("ic_weather_chance_of_rain_night_SIZE.png")) {
            return R.drawable.ic_weather_chance_of_rain_night;
        } else if (str.equals("ic_weather_chance_snow_SIZE.png")) {
            return R.drawable.ic_weather_snow;
        } else if (str.equals("ic_weather_chance_storm_SIZE.png")) {
            return R.drawable.ic_weather_thunderstorms;
        } else if (str.equals("ic_weather_clear_day_SIZE.png")) {
            return R.drawable.ic_weather_sunny;
        } else if (str.equals("ic_weather_clear_SIZE.png")) {
            return R.drawable.ic_weather_sunny;
        } else if (str.equals("ic_weather_clear_night_SIZE.png")) {
            return R.drawable.ic_weather_clear_night;
        } else if (str.equals("ic_weather_cloudy_SIZE.png")) {
            return R.drawable.ic_weather_cloudy;
        } else if (str.equals("ic_weather_flurries_SIZE.png")) {
            return R.drawable.ic_weather_snow;
        } else if (str.equals("ic_weather_fog_SIZE.png")) {
            return R.drawable.ic_weather_fog;
        } else if (str.equals("ic_weather_heavy_rain_SIZE.png")) {
            return R.drawable.ic_weather_rain;
        } else if (str.equals("ic_weather_icy_sleet_SIZE.png")) {
            return R.drawable.ic_weather_sleet;
        } else if (str.equals("ic_weather_mist_SIZE.png")) {
            return R.drawable.ic_weather_mist;
        } else if (str.equals("ic_weather_partly_cloudy_SIZE.png")) {
            return R.drawable.ic_weather_partly_cloudy_day;
        } else if (str.equals("ic_weather_rain_day_SIZE.png")) {
            return R.drawable.ic_weather_chance_of_rain;
        } else if (str.equals("ic_weather_rain_SIZE.png")) {
            return R.drawable.ic_weather_rain_light;
        } else if (str.equals("ic_weather_rain_night_SIZE.png")) {
            return R.drawable.ic_weather_chance_of_rain_night;
        } else if (str.equals("ic_weather_snow_SIZE.png")) {
            return R.drawable.ic_weather_snow;
        } else if (str.equals("ic_weather_snow_rain_SIZE.png")) {
            return R.drawable.ic_weather_snow;
        } else if (str.equals("ic_weather_sunny_SIZE.png")) {
            return R.drawable.ic_weather_sunny;
        } else if (str.equals("ic_weather_thunderstorm_SIZE.png")) {
            return R.drawable.ic_weather_thunderstorms;
        } else if (str.equals("ic_weather_windy_SIZE.png")) {
            return R.drawable.ic_weather_cloudy;
        } else {
            return R.drawable.ic_weather_unknown;
        }
    }
}
