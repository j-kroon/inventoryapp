package com.example.android.inventoryapp;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.ItemContract;

import static android.R.attr.id;

/**
 * Created by Joseph Kroon on 4/2/2017.
 */

public class ItemCursorAdapter extends CursorAdapter {

    private static final String LOG_TAG = ItemCursorAdapter.class.getSimpleName();

    public ItemCursorAdapter(Context context, Cursor c) {
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, final Cursor cursor) {
        //find the views
        TextView nameTextView = (TextView) view.findViewById(R.id.name);
        TextView priceTextView = (TextView) view.findViewById(R.id.price);
        final TextView quantityTextView = (TextView) view.findViewById(R.id.quantity);

        //get the columns
        int nameColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
        int priceColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
        int quantityColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);

        //read the columns
        String itemName = cursor.getString(nameColumnIndex);
        String itemPrice = cursor.getString(priceColumnIndex);
        final String itemQuantity = cursor.getString(quantityColumnIndex);

        //update the views
        nameTextView.setText(itemName);
        priceTextView.setText(itemPrice);
        quantityTextView.setText(itemQuantity);

        // get the button and cursor position. Set up click listener
        Button saleButton = (Button)view.findViewById(R.id.sale);
        final int position = cursor.getPosition();
        saleButton.setOnClickListener(new View.OnClickListener() {

            @TargetApi(19)
            @Override
            public void onClick(View v) {

                //move the cursor to position of clicked button
                cursor.moveToPosition(position);

                //convert text quantity to int quantity for manipulation
                int itemQuantityInt = Integer.parseInt(itemQuantity);

                if (itemQuantityInt != 0) {
                    itemQuantityInt -= 1;
                    //convert int quantity to text quantity for updating view and database
                    String newQuant = Integer.toString(itemQuantityInt);

                    //get the id for the specific item, for appending to larger URI
                    int itemIdColumnIndex = cursor.getColumnIndex(ItemContract.ItemEntry._ID);
                    final long itemId = cursor.getLong(itemIdColumnIndex);

                    // Get the current item URI, append item id, and update database
                    Uri mCurrentItemUri;
                    mCurrentItemUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI,  itemId);

                    ContentValues values = new ContentValues();
                    values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, newQuant);

                    ContentResolver cr = v.getContext().getContentResolver();
                    Log.v(LOG_TAG, "@@@@@@@@@@@ mCurrentItemUri is: " + mCurrentItemUri);
                    cr.update(mCurrentItemUri, values, null, null);

                    //set the new value to the text view.
                    quantityTextView.setText(newQuant);
                }
            }
        });

    }
}
