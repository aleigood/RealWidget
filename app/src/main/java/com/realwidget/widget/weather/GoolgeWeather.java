package com.realwidget.widget.weather;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.realwidget.Constants;
import com.realwidget.R;
import com.realwidget.util.Utils;
import com.realwidget.widget.weather.WeatherWidget.Callback;
import com.realwidget.widget.weather.WeatherWidget.Condition;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class GoolgeWeather {
    private Context mContext;
    private Callback mListener;

    protected GoolgeWeather(Context context, final String local, Callback listener) {
        mContext = context;
        mListener = listener;

        new Thread() {
            @Override
            public void run() {
                parse(local);
            }
        }.start();
    }

    private XmlHandler parse(String local) {
        SAXParserFactory spf = SAXParserFactory.newInstance();
        XmlHandler handler = new XmlHandler();

        try {
            SAXParser sp = spf.newSAXParser();
            XMLReader reader = sp.getXMLReader();
            reader.setContentHandler(handler);

            URL url = new URL("http://www.google.com/ig/api?weather=" + URLEncoder.encode(local));
            InputStream is = url.openStream();
            reader.parse(new InputSource(new InputStreamReader(is, "utf-8")));
            is.close();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }

        return handler;
    }

    // 2012-08-01 16:00:00 +0000
    private boolean isNight() {
        Calendar cal = Calendar.getInstance();
        cal.setTime(new Date());
        int hour = cal.get(Calendar.HOUR_OF_DAY);

        if (hour > 20 || hour < 5) {
            return true;
        }

        return false;
    }

    public int getWeatherIconResource(String paramString, boolean isNight) {
        String str = paramString.substring(1 + paramString.lastIndexOf('/')).replace(".gif", "");

        // 有时有雪
        if (str.equals("chance_of_snow")) {
            return R.drawable.ic_weather_snow_light;
        }
        // 飘雪
        else if (str.equals("flurries")) {
            return R.drawable.ic_weather_snow_light;
        }
        // 雪
        else if (str.equals("snow")) {
            return R.drawable.ic_weather_snow;
        }
        // 冻雨
        else if (str.equals("sleet")) {
            return R.drawable.ic_weather_sleet;
        } else if (str.equals("icy")) {
            return R.drawable.ic_weather_sleet;
        } else if (str.equals("rain_snow")) {
            return R.drawable.ic_weather_sleet;
        }
        // 有时有雨
        else if (str.equals("chance_of_rain")) {
            return isNight ? R.drawable.ic_weather_chance_of_rain_night : R.drawable.ic_weather_chance_of_rain;
        }
        // 有时有暴雨
        else if (str.equals("chance_of_storm")) {
            return R.drawable.ic_weather_thunderstorms;
        }
        // 阵雨
        else if (str.equals("showers")) {
            return R.drawable.ic_weather_rain_light;
        } else if (str.equals("rain")) {
            return R.drawable.ic_weather_rain;
        } else if (str.equals("storm")) {
            return R.drawable.ic_weather_thunderstorms;
        } else if (str.equals("thunderstorm")) {
            return R.drawable.ic_weather_thunderstorms;
        } else if (str.equals("sunny")) {
            return isNight ? R.drawable.ic_weather_clear_night : R.drawable.ic_weather_sunny;
        } else if (str.equals("partly_cloudy")) {
            return isNight ? R.drawable.ic_weather_partly_cloudy_night : R.drawable.ic_weather_partly_cloudy_day;
        } else if (str.equals("mostly_cloudy")) {
            return isNight ? R.drawable.ic_weather_partly_cloudy_night : R.drawable.ic_weather_partly_cloudy_day;
        } else if (str.equals("mostly_sunny")) {
            return isNight ? R.drawable.ic_weather_partly_cloudy_night : R.drawable.ic_weather_partly_cloudy_day;
        } else if (str.equals("cloudy")) {
            return R.drawable.ic_weather_cloudy;
        }
        // 雾
        else if (str.equals("mist")) {
            return R.drawable.ic_weather_mist;
        } else if (str.equals("fog")) {
            return R.drawable.ic_weather_fog;
        } else if (str.equals("smoke")) {
            return R.drawable.ic_weather_fog;
        } else if (str.equals("haze")) {
            return R.drawable.ic_weather_fog;
        } else if (str.equals("dust")) {
            return R.drawable.ic_weather_fog;
        } else {
            return R.drawable.ic_weather_unknown;
        }
    }

    private String getCelsiusStr(String param) {
        try {
            int fahrenheitS = Integer.parseInt(param);
            return "" + Utils.fahrenheitToCelsius(fahrenheitS);
        } catch (Exception e) {
            return "";
        }
    }

    class XmlHandler extends DefaultHandler {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(mContext);
        // 0: Celsius 1:Fahrenheit
        final String tmpUnit = sp.getString(Constants.PREF_WEATHER_TEMP_UNIT, "0");
        List<Condition> forecasts = new ArrayList<Condition>();
        Condition forecast;
        Condition current = new Condition();
        private boolean isCurrentConditions = false;

        public List<Condition> getForecast() {
            return forecasts;
        }

        @Override
        public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
            String tagName = localName.length() != 0 ? localName : qName;
            tagName = tagName.toLowerCase();

            if (tagName.equals("forecast_conditions")) {
                forecast = new Condition();
                forecast.isForecast = 1;
            } else if (tagName.equals("current_conditions")) {
                isCurrentConditions = true;
            } else if (tagName.equals("city")) {
                current.city = attributes.getValue("data");
            } else if (tagName.equals("condition")) {
                if (isCurrentConditions) {
                    current.condition = attributes.getValue("data");
                } else {
                    forecast.condition = attributes.getValue("data");
                }
            } else if (tagName.equals("temp_f") && tmpUnit.equals("1")) {
                current.temperature = attributes.getValue("data");
            } else if (tagName.equals("temp_c") && tmpUnit.equals("0")) {
                current.temperature = attributes.getValue("data");
            } else if (tagName.equals("humidity")) {
                current.humidity = attributes.getValue("data");
            } else if (tagName.equals("wind_condition")) {
                current.wind = attributes.getValue("data");
            } else if (tagName.equals("day_of_week")) {
                forecast.day = attributes.getValue("data");
            } else if (tagName.equals("low")) {
                if (tmpUnit.equals("0")) {
                    forecast.lowTemp = getCelsiusStr(attributes.getValue("data"));
                } else {
                    forecast.lowTemp = attributes.getValue("data");
                }
            } else if (tagName.equals("high")) {
                if (tmpUnit.equals("0")) {
                    forecast.highTemp = getCelsiusStr(attributes.getValue("data"));
                } else {
                    forecast.highTemp = attributes.getValue("data");
                }
            } else if (tagName.equals("icon")) {
                if (isCurrentConditions) {
                    current.iconRes = getWeatherIconResource(attributes.getValue("data"), isNight());
                } else {
                    forecast.iconRes = getWeatherIconResource(attributes.getValue("data"), isNight());
                }
            } else if (tagName.equals("condition")) {
                forecast.condition = attributes.getValue("data");
            }
        }

        @Override
        public void endElement(String uri, String localName, String qName) throws SAXException {
            String tagName = localName.length() != 0 ? localName : qName;
            tagName = tagName.toLowerCase();

            if (tagName.equals("forecast_conditions")) {
                forecasts.add(forecast);
            } else if (tagName.equals("current_conditions")) {
                isCurrentConditions = false;
            }
        }

        @Override
        public void endDocument() throws SAXException {
            super.endDocument();
            current.isForecast = 0;
            // 最高最低温度取自预报中的温度
            current.lowTemp = forecasts.get(0).lowTemp;
            current.highTemp = forecasts.get(0).highTemp;

            mListener.onDataChange(current, forecasts);
        }
    }
}
