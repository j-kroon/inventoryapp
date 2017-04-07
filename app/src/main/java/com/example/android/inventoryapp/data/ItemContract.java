package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

import static android.R.attr.path;

/**
 * Created by Joseph Kroon on 4/3/2017.
 */

//API Contract for inventory app

public class ItemContract {

    //Empty constructor to prevent accidental instantiation
    private ItemContract () {}

    //Content authority for the content provider
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    //URI base
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    // Items path.
    public static final String PATH_ITEMS = "items";

    //Inner Class for defining values for the items database table
    public static final class ItemEntry implements BaseColumns {

        //URI to access item data in ItemProvider
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_ITEMS);

        //MIME type for a list of items
        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        //MIME type for a list of items
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_ITEMS;

        //table name for items
        public final static String TABLE_NAME = "items";

        //Setup the columns for the database.
        public final static String _ID = BaseColumns._ID; //Type:INTEGER
        public final static String COLUMN_ITEM_NAME = "name"; //Type:TEXT
        public final static String COLUMN_ITEM_PRICE = "price"; //Type:FLOAT
        public final static String COLUMN_ITEM_QUANTITY = "quantity"; //Type:INTEGER
        public final static String COLUMN_ITEM_SUPPLIER = "supplier"; //Type:TEXT
        public final static String COLUMN_ITEM_EMAIL = "email"; //Type:TEXT
        public final static String COLUMN_ITEM_IMAGE = "image"; //Type: Text

    }



}