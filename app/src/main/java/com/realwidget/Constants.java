package com.realwidget;

public class Constants {
    public static final String APP_NAME = "realwidget";
    public static final String DEFAULT_ENCODING = "UTF-8";
    public static final String WIDGET_XML_NAME = "widgets";
    public static final String DIR_NAME = ".realwidget";

    public static final String PREFS_BATTERY_LEVEL = "battery_level";
    public static final String PREFS_BATTERY_STATUS = "battery_status";
    public static final String PREFS_BATTERY_TEMPERATURE = "battery_temperature";
    public static final String PREF_BRIGHT_LEVEL = "bright_level";
    public static final String DEFAULT_LEVEL = "0,50,100,-1";
    public static final String PREF_TRAFFICSTATS_RXBYTES = "trafficStatsRxbytes";
    public static final String PREF_TRAFFICSTATS_TXBYTES = "trafficStatsTxbytes";
    public static final String PREF_TRAFFICSTATS_COUNT_BYTES = "trafficStatsCountbytes";
    public static final String PREF_DEF_BACK_COLOR = "defBackColor";
    public static final String PREF_DEF_LABLE_COLOR = "defLableColor";
    public static final String PREF_DEF_ICON_COLOR = "defIconColor";
    public static final String PREF_GMAIL_USER_NAME = "gmailUserName";
    public static final String PREF_GMAIL_PASSWORD = "gmailPassword";
    public static final String PREF_GMAIL_REFRESH_INTERVAL = "gmailRefreshInterval";
    public static final String PREF_WEATHER_SOURCE = "weatherSource";
    public static final String PREF_WEATHER_REFRESH_INTERVAL = "weatherRefreshInterval";
    public static final String PREF_WEATHER_TEMP_UNIT = "weatherTempUnit";
    public static final String PREF_WEATHER_LOCATION = "weatherLocation";
    public static final String PREF_BATTERY_TEMPERATURE = "batteryTemperature";
    public static final String PREF_MAX_DAYS_OF_EVENT = "maxDaysOfEvent";
    public static final String PREF_BATTERY_TEMPERATURE_UNIT = "batteryTempUnit";
    public static final String PREF_TRAFFIC_STATISTICS = "trafficStatistics";
    public static final String PREF_TRAFFIC_STATISTICS_CYCLE = "trafficStatisticsCycle";
    public static final String PREF_TRAFFIC_STATISTICS_FIRST_DAY = "trafficStatisticsFirstDay";
    public static final String PREFS_AUTOFIT_FIELD_PATTERN = "autofit-%d";

    public static final String ACTION_BUTTON_CLICK = "com.realwidget.action.BUTTON_CLICK";
    public static final String ACTION_WIDGET_UPDATE = "com.realwidget.WIDGET_UPDATE";
    public static final String TRAFFIC_STATISTICS_ALERT = "com.realwidget.TRAFFIC_STATISTICS_ALERT";

    public static final int BUTTON_APP = 0;
    public static final int BUTTON_SETTING = 1;
    public static final int BUTTON_PHONE = 2;
    public static final int BUTTON_SMS = 3;
    public static final int BUTTON_GMAIL = 4;
    public static final int BUTTON_ALARM = 5;
    public static final int BUTTON_BETTERY = 6;
    public static final int BUTTON_CALENDAR = 7;
    public static final int BUTTON_DATA = 8;
    public static final int BUTTON_BRIGHTNESS = 9;
    public static final int BUTTON_SYNC = 10;
    public static final int BUTTON_BLUETOOTH = 11;
    public static final int BUTTON_WIFI = 12;
    public static final int BUTTON_SHORTCUT = 13;
    public static final int BUTTON_AUTO_ROTATE = 14;
    public static final int BUTTON_WEATHER = 15;
    public static final int BUTTON_MUSIC = 16;
    public static final int BUTTON_MEMORY = 17;
    public static final int BUTTON_STORAGE = 18;

    public static final int ICON_CUSTOM = -1;
    public static final int ICON_NONE = 0;
    public static final int ICON_PHONE = 1;
    public static final int ICON_SMS = 2;
    public static final int ICON_GMAIL = 3;
    public static final int ICON_CALENDAR = 4;
    public static final int ICON_ALARM = 5;
    public static final int ICON_AUTO_ROTATE = 6;
    public static final int ICON_DATA = 7;
    public static final int ICON_BLUETOOTH = 8;
    public static final int ICON_BRIGHTNESS = 9;
    public static final int ICON_SYNC = 10;
    public static final int ICON_WIFI = 11;

    /**
     * 存储widget配置信息
     */
    public interface TABLE_WIDGET {
        String TABLE_NAME = "widget";

        String COLUMN_WIDGET_ID = "widgetId";
        String COLUMN_BUTTON_ID = "btnId";
        String COLUMN_BUTTON_TYPE = "type";
        String COLUMN_BUTTON_SIZE = "size";
        String COLUMN_BACK_COLOR = "backColor";
        String COLUMN_ICON_COLOR = "iconColor";
        String COLUMN_LABEL_COLOR = "lableColor";
        String COLUMN_LABEL = "label";
        String COLUMN_INTENT = "intent";
        String COLUMN_ICON_FILE = "icoFile";
        String COLUMN_BACK_FILE = "backFile";

        int INDEX_WIDGET_ID = 1;
        int INDEX_BUTTON_ID = 2;
        int INDEX_BUTTON_TYPE = 3;
        int INDEX_BUTTON_SIZE = 4;
        int INDEX_BACK_COLOR = 5;
        int INDEX_ICON_COLOR = 6;
        int INDEX_LABEL_COLOR = 7;
        int INDEX_LABEL = 8;
        int INDEX_INTENT = 9;
        int INDEX_ICON_FILE = 10;
        int INDEX_BACK_FILE = 11;
    }
}
