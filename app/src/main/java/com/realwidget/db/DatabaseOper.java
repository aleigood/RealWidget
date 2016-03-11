package com.realwidget.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.realwidget.Constants;

import java.util.List;

public class DatabaseOper {
    private SQLiteDatabase mWritableDatabase;
    private SQLiteDatabase mReadableDatabase;

    public DatabaseOper(Context context) {
        SQLiteOpenHelper openHelper = new DatabaseHelper(context);
        mWritableDatabase = openHelper.getWritableDatabase();
        mReadableDatabase = openHelper.getReadableDatabase();
    }

    public void insertButtons(int widgetId, List<Button> btns) {
        String insertWidgetStr = "insert into " + Constants.TABLE_WIDGET.TABLE_NAME + "("
                + Constants.TABLE_WIDGET.COLUMN_WIDGET_ID + "," + Constants.TABLE_WIDGET.COLUMN_BUTTON_ID + ","
                + Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE + "," + Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE + ","
                + Constants.TABLE_WIDGET.COLUMN_BACK_COLOR + "," + Constants.TABLE_WIDGET.COLUMN_ICON_COLOR + ","
                + Constants.TABLE_WIDGET.COLUMN_LABEL_COLOR + "," + Constants.TABLE_WIDGET.COLUMN_LABEL + ","
                + Constants.TABLE_WIDGET.COLUMN_INTENT + "," + Constants.TABLE_WIDGET.COLUMN_ICON_FILE + ","
                + Constants.TABLE_WIDGET.COLUMN_BACK_FILE + ") values (?,?,?,?,?,?,?,?,?,?,?);";

        for (int i = 0; i < btns.size(); i++) {
            Button btn = btns.get(i);
            mWritableDatabase.execSQL(insertWidgetStr, new Object[]{widgetId, btn.btnId, btn.type, btn.size,
                    btn.backColor, btn.iconColor, btn.labelColor, btn.label, btn.intent, btn.iconFile, btn.backFile});
        }
    }

    public Cursor queryWidgetById(int widgetId) {
        return mReadableDatabase.rawQuery("SELECT * FROM " + Constants.TABLE_WIDGET.TABLE_NAME + " WHERE "
                + Constants.TABLE_WIDGET.COLUMN_WIDGET_ID + "=" + widgetId + " order by "
                + Constants.TABLE_WIDGET.COLUMN_BUTTON_ID, null);
    }

    public Cursor queryWidgetByType(int widgetId, int type) {
        return mReadableDatabase.rawQuery("SELECT * FROM " + Constants.TABLE_WIDGET.TABLE_NAME + " WHERE "
                + Constants.TABLE_WIDGET.COLUMN_WIDGET_ID + "=" + widgetId + " and "
                + Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE + "=" + type, null);
    }

    public void deleteWidgetById(int widgetId) {
        mWritableDatabase.execSQL("DELETE FROM " + Constants.TABLE_WIDGET.TABLE_NAME + " WHERE "
                + Constants.TABLE_WIDGET.COLUMN_WIDGET_ID + "=" + widgetId);
    }

    public void close() {
        mWritableDatabase.close();
        mReadableDatabase.close();
    }

    private static class DatabaseHelper extends SQLiteOpenHelper {
        private static final String DATABASE_NAME = "realwidget.db";
        private static final int DATABASE_VERSION = 1;

        public DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {
            db.execSQL("CREATE TABLE " + Constants.TABLE_WIDGET.TABLE_NAME
                    + " (_id integer primary key autoincrement, " + Constants.TABLE_WIDGET.COLUMN_WIDGET_ID
                    + " INTEGER, " + Constants.TABLE_WIDGET.COLUMN_BUTTON_ID + " INTEGER, "
                    + Constants.TABLE_WIDGET.COLUMN_BUTTON_TYPE + " INTEGER, "
                    + Constants.TABLE_WIDGET.COLUMN_BUTTON_SIZE + " INTEGER, "
                    + Constants.TABLE_WIDGET.COLUMN_BACK_COLOR + " INTEGER, "
                    + Constants.TABLE_WIDGET.COLUMN_ICON_COLOR + " INTEGER, "
                    + Constants.TABLE_WIDGET.COLUMN_LABEL_COLOR + " INTEGER, " + Constants.TABLE_WIDGET.COLUMN_LABEL
                    + " TEXT," + Constants.TABLE_WIDGET.COLUMN_INTENT + " TEXT,"
                    + Constants.TABLE_WIDGET.COLUMN_ICON_FILE + " TEXT," + Constants.TABLE_WIDGET.COLUMN_BACK_FILE
                    + " TEXT);");
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int currentVersion) {
            db.execSQL("DROP TABLE IF EXISTS " + Constants.TABLE_WIDGET.TABLE_NAME);
            onCreate(db);
        }
    }

}
