package com.realwidget.widget.toggle;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.Browser;
import com.realwidget.R;

public class BookmarkUtils {
    public static final String _ID = "_id";
    public static final String URL = "url";
    public static final String TITLE = "title";
    public static final String FAVICON = "favicon";
    public static final String THUMBNAIL = "thumbnail";

    private static final String[] PROJECTION = new String[]{_ID, TITLE, URL, FAVICON, THUMBNAIL};

    public static Intent createShortcutIntent(String url) {
        Intent shortcutIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        long urlHash = url.hashCode();
        long uniqueId = (urlHash << 32) | shortcutIntent.hashCode();
        shortcutIntent.putExtra(Browser.EXTRA_APPLICATION_ID, Long.toString(uniqueId));
        return shortcutIntent;
    }

    public static Bookmark builderBookmark(Context context, Uri data) {
        Bookmark bookmark = new Bookmark();
        ContentResolver resolver = context.getContentResolver();

        //BOOKMARKS_URI
        Cursor cursor = resolver.query(Uri.parse("content://browser/bookmarks"), PROJECTION,
                //BookmarkColumns.URL, // 1
                1 + "='" + data + "'", null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                bookmark.mDisplayName = cursor.getString(cursor.getColumnIndex(TITLE));
                byte[] img = cursor.getBlob(cursor.getColumnIndex(THUMBNAIL));

                if (img != null) {
                    bookmark.mBitmapData = BitmapFactory.decodeByteArray(img, 0, img.length);
                } else {
                    bookmark.mBitmapData = BitmapFactory.decodeResource(context.getResources(),
                            R.drawable.browser_thumbnail);
                }
            }
            while (cursor.moveToNext());
        }

        cursor.close();
        return bookmark;
    }

    public static class Bookmark {
        public String mDisplayName;
        public Bitmap mBitmapData;
        public long mPhotoId;
    }
}
