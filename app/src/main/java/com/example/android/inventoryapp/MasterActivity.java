package com.example.android.inventoryapp;

import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.android.inventoryapp.data.ItemContract;

public class MasterActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //adaptor for the ListView
    ItemCursorAdapter mCursorAdapter;

    //data loader identifier
    private static final int ITEM_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_master);

        //Hookup fab to add screen
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MasterActivity.this, DetailActivity.class);
                startActivity(intent);
            }
        });
        //find the list view
        ListView itemListView = (ListView) findViewById(R.id.list);

        //set the empty view
        View emptyView = findViewById(R.id.empty_view);
        itemListView.setEmptyView(emptyView);

        //set the adapter on the list view
        mCursorAdapter = new ItemCursorAdapter(this, null);
        itemListView.setAdapter(mCursorAdapter);

        //item click listener to access detail view
        itemListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                Intent intent = new Intent(MasterActivity.this, DetailActivity.class);
                //URI of item that was tapped
                Uri currentUri = ContentUris.withAppendedId(ItemContract.ItemEntry.CONTENT_URI, id);
                //pass the URI as data through the intent
                intent.setData(currentUri);
                //start the Detail activity
                startActivity(intent);
            }
        });
        //start the loader
        getLoaderManager().initLoader(ITEM_LOADER, null, this);
    }

    public CursorLoader onCreateLoader(int id, Bundle args) {
        //define a projection that contains all columns

        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY };

        //Executes Providers query on a background thread
        return new CursorLoader(this,
                ItemContract.ItemEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mCursorAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mCursorAdapter.swapCursor(null);
    }
}