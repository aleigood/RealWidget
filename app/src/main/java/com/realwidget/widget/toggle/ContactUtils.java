package com.realwidget.widget.toggle;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Photo;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import com.realwidget.R;

public class ContactUtils {

    private static final String[] CONTACT_COLUMNS = {Contacts.DISPLAY_NAME, Contacts.PHOTO_ID,};

    private static final int CONTACT_DISPLAY_NAME_COLUMN_INDEX = 0;
    private static final int CONTACT_PHOTO_ID_COLUMN_INDEX = 1;

    private static final String[] PHOTO_COLUMNS = {Photo.PHOTO,};

    private static final int PHOTO_PHOTO_COLUMN_INDEX = 0;

    private static final String PHOTO_SELECTION = Photo._ID + "=?";

    protected Uri mUri;
    protected String mDisplayName;
    protected byte[] mBitmapData;
    protected long mPhotoId;

    public static Contact builderContact(Context context, Uri data) {
        Contact contact = new Contact();

        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = resolver.query(data, CONTACT_COLUMNS, null, null, null);

        if (cursor != null) {
            try {
                if (cursor.moveToFirst()) {
                    contact.mDisplayName = cursor.getString(CONTACT_DISPLAY_NAME_COLUMN_INDEX);
                    contact.mPhotoId = cursor.getLong(CONTACT_PHOTO_ID_COLUMN_INDEX);
                }
            } finally {
                cursor.close();
            }
        }

        if (contact.mPhotoId != 0) {
            Cursor photoCursor = resolver.query(Data.CONTENT_URI, PHOTO_COLUMNS, PHOTO_SELECTION,
                    new String[]{String.valueOf(contact.mPhotoId)}, null);
            if (photoCursor != null) {
                try {
                    if (photoCursor.moveToFirst()) {
                        byte[] img = photoCursor.getBlob(PHOTO_PHOTO_COLUMN_INDEX);

                        if (img != null) {
                            contact.mBitmapData = BitmapFactory.decodeByteArray(img, 0, img.length);
                        }
                    }
                } finally {
                    photoCursor.close();
                }
            }
        } else {
            contact.mBitmapData = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_contact_picture);
        }

        return contact;
    }

    public static class Contact {
        public String mDisplayName;
        public Bitmap mBitmapData;
        public long mPhotoId;
    }
}
