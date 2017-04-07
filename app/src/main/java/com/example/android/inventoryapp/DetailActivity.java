package com.example.android.inventoryapp;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.ItemContract;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static android.R.attr.data;
import static com.example.android.inventoryapp.R.id.price;
import static com.example.android.inventoryapp.R.id.quantity;

public class DetailActivity extends AppCompatActivity implements
        LoaderManager.LoaderCallbacks<Cursor> {

    //data loader id
    private static final int EXISTING_ITEM_LOADER = 0;

    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int SEND_MAIL_REQUEST = 2;

    private static final String STATE_URI = "STATE_URI";

    //URI for the current item
    private Uri mCurrentItemUri;
    private Uri mCurrentImageUri;

    //Edit fields get:
    private EditText mItemInput;
    private EditText mPriceInput;
    private EditText mSupplierInput;
    private EditText mEmailInput;
    private TextView mQuantityInput;
    private ImageView mImageView;

    private static final String LOG_TAG = DetailActivity.class.getSimpleName();

    //Track item edits
    private boolean mItemEdits = false;
    //If the user touches the screen, then it has been edited:
    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mItemEdits = true;
            return false;
        }
    };



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        //Check intent for new or existing item
        Intent intent = getIntent();
        mCurrentItemUri = intent.getData();

        //if new item
        if (mCurrentItemUri == null){
            //change the activity title
            setTitle("Add a new item");
            //don't need delete for new items
            invalidateOptionsMenu();
        }
        // if existing item
        else {
            //change the activity title
            setTitle("Edit your item");
            //display current values in the editor
            getLoaderManager().initLoader(EXISTING_ITEM_LOADER, null, this);
        }

        //get all the input views
        mItemInput = (EditText) findViewById(R.id.input_item);
        mPriceInput = (EditText) findViewById(R.id.input_price);
        mSupplierInput = (EditText) findViewById(R.id.input_supplier);
        mEmailInput = (EditText) findViewById(R.id.input_email);
        mQuantityInput = (TextView) findViewById(R.id.input_quantity);
        mImageView = (ImageView) findViewById(R.id.imgView);

        //set the ontouchlisteners to determine edits:
        mItemInput.setOnTouchListener(mTouchListener);
        mPriceInput.setOnTouchListener(mTouchListener);
        mSupplierInput.setOnTouchListener(mTouchListener);
        mEmailInput.setOnTouchListener(mTouchListener);
        mQuantityInput.setOnTouchListener(mTouchListener);

    }

    public void loadImage(View view) {
        openImageSelector();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        if (mCurrentImageUri != null)
            outState.putString(STATE_URI, mCurrentImageUri.toString());
    }

    public void openImageSelector() {
        Intent intent;

        if (Build.VERSION.SDK_INT < 19) {
            intent = new Intent(Intent.ACTION_GET_CONTENT);
        } else {
            intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.addCategory(Intent.CATEGORY_OPENABLE);
        }

        intent.setType("image/*");
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @TargetApi(16)
    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        if (savedInstanceState.containsKey(STATE_URI) &&
                !savedInstanceState.getString(STATE_URI).equals("")) {
            mCurrentItemUri = Uri.parse(savedInstanceState.getString(STATE_URI));

            ViewTreeObserver viewTreeObserver = mImageView.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    mImageView.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    mImageView.setImageBitmap(getBitmapFromUri(mCurrentItemUri));
                }
            });
        }
    }

    public Bitmap getBitmapFromUri(Uri uri) {

        if (uri == null || uri.toString().isEmpty())
            return null;

        // Get the dimensions of the View
        int targetW = mImageView.getWidth();
        int targetH = mImageView.getHeight();

        InputStream input = null;
        try {
            input = this.getContentResolver().openInputStream(uri);

            // Get the dimensions of the bitmap
            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();

            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;

            input = this.getContentResolver().openInputStream(uri);
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, bmOptions);
            input.close();
            return bitmap;

        } catch (FileNotFoundException fne) {
            Log.e(LOG_TAG, "Failed to load image.", fne);
            return null;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                input.close();
            } catch (IOException ioe) {

            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent resultData) {
        // The ACTION_OPEN_DOCUMENT intent was sent with the request code READ_REQUEST_CODE.
        // If the request code seen here doesn't match, it's the response to some other intent,
        // and the below code shouldn't run at all.

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.  Pull that uri using "resultData.getData()"

            if (resultData != null) {
                mCurrentItemUri = resultData.getData();
                Log.i(LOG_TAG, "Uri: " + mCurrentItemUri.toString());

                mImageView.setImageBitmap(getBitmapFromUri(mCurrentItemUri));
            }
        } else if (requestCode == SEND_MAIL_REQUEST && resultCode == Activity.RESULT_OK) {

        }
    }


    //received button
    public void received(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set up the input
        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int inputNumber = Integer.parseInt(input.getText().toString());
                displayQuantity(inputNumber);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();
    }

    //This method is called when the sold button is pressed
    public void sold(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        // Set up the input
        final EditText input = new EditText(this);

        input.setInputType(InputType.TYPE_CLASS_NUMBER);
        builder.setView(input);

        // Set up the buttons
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                int inputNumber = Integer.parseInt(input.getText().toString());
                int negInputNumber = inputNumber * (-1);
                displayQuantity(negInputNumber);
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }


     //This method displays the given quantity value on the screen.
    private void displayQuantity(int number) {


        //get the quantity input view
        String currentNumberString = mQuantityInput.getText().toString();

        int currentNumber = Integer.parseInt(currentNumberString);

        int newNumber = currentNumber + number;
        if (newNumber < 0) {
            newNumber = 0;
        }

        TextView quantityTextView = (TextView) findViewById(R.id.input_quantity);
        quantityTextView.setText("" + newNumber);
    }


    //this method is called when the reorder button is pressed
    public void reOrder(View view) {
        String email = mEmailInput.getText().toString();
        String subject = mItemInput.getText().toString();
        String supplier = mSupplierInput.getText().toString();
        String body = "Dear " + supplier + ", \nPlease place an order for the following:\n\nItem: "
                + subject + "\nAmount: ";
        composeEmail(email, subject, body);
    }

    public void composeEmail(String email, String subject, String body) {
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:")); // only email apps should handle this
        intent.putExtra(Intent.EXTRA_EMAIL, new String[] { email });
        intent.putExtra(Intent.EXTRA_SUBJECT, subject);
        intent.putExtra(Intent.EXTRA_TEXT, body);
        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        }
    }
    // This adds menu items to the app bar.
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail, menu);
        return true;
    }

    // hide the delete option if the item is a new one
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        if (mCurrentItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    //Detail actions (Save and Delete)
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        //save, delete, or back
        switch (item.getItemId()) {
            case R.id.action_save:
                saveItem();
                finish();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog(); //confirms delete and then deletes
                return true;
            case R.id.home: // checks for changes
                if (!mItemEdits) { //if there are no changes
                    NavUtils.navigateUpFromSameTask(DetailActivity.this);
                    return true;
                }

                //if there are changes, discard them
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(DetailActivity.this);
                            }
                        };
                    showUnsavedChangesDialog(discardButtonClickListener);
                     return true;
                }
        return super.onOptionsItemSelected(item);
    }


    //Get input and save to database
    private void saveItem() {
        String itemString = mItemInput.getText().toString().trim();
        String priceString = mPriceInput.getText().toString().trim();
        String supplierString = mSupplierInput.getText().toString().trim();
        String emailString = mEmailInput.getText().toString().trim();
        String quantityString = mQuantityInput.getText().toString().trim();
        String imageString = mCurrentItemUri.toString();

        //check if fields have data
        if (mCurrentItemUri == null &&
                TextUtils.isEmpty(itemString) &&
                TextUtils.isEmpty(priceString) &&
                TextUtils.isEmpty(supplierString) &&
                TextUtils.isEmpty(emailString) &&
                TextUtils.isEmpty(quantityString)
                ) {
            return; //if there are any values, don't need to create a new item
        }

        //if there isn't data
        //Prep data for database insertion, create ContentValues object
        ContentValues values = new ContentValues();
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_NAME, itemString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_PRICE, priceString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER, supplierString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_EMAIL, emailString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY, quantityString);
        values.put(ItemContract.ItemEntry.COLUMN_ITEM_IMAGE, imageString);

        //if item is new, insert into the provider and get the new URI
        if (mCurrentItemUri == null) {
            Uri newUri = getContentResolver().insert(ItemContract.ItemEntry.CONTENT_URI, values);
            //show success or failure based on newUri
            if (newUri == null) {
                Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item saved successfully", Toast.LENGTH_SHORT).show();
            }
        }
        //if item exists then update entry
        else {
            int rowsAffected = getContentResolver().update(mCurrentItemUri, values, null, null);
            //show success or failure based on newUri
            if (rowsAffected == 0) {
                Toast.makeText(this, "There was an error", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Item saved successfully",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    //delete warning called when delete is pressed.
    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to delete this item?");
        builder.setPositiveButton("Delete", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete
                deleteItem();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    private void deleteItem() {
        // Only perform the delete if this is an existing item.
        if (mCurrentItemUri != null) {
            // Call the ContentResolver to delete the item at the given content URI.
            int rowsDeleted = getContentResolver().delete(mCurrentItemUri, null, null);

            // Show a toast message depending on whether or not the delete was successful.
            if (rowsDeleted == 0) {
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, "Could not delete.",
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, "Item was deleted",
                        Toast.LENGTH_SHORT).show();
            }
        }
        finish(); // Close the activity
    }

    // Discard changes confirmation
    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Are you sure you want to discard changes?");
        builder.setPositiveButton("Yes", discardButtonClickListener);
        builder.setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) { // if clicked no
                    dialog.dismiss();
                }
            }
        });
        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        //define a projection that contains all columns

        String[] projection = {
                ItemContract.ItemEntry._ID,
                ItemContract.ItemEntry.COLUMN_ITEM_NAME,
                ItemContract.ItemEntry.COLUMN_ITEM_PRICE,
                ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER,
                ItemContract.ItemEntry.COLUMN_ITEM_EMAIL,
                ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY };

        //Executes Providers query on a background thread
        return new CursorLoader(this,
                mCurrentItemUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data == null || data.getCount() < 1) {
            return;
        }

        if (data.moveToFirst()) {
            int itemColumnIndex = data.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_NAME);
            int priceColumnIndex = data.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_PRICE);
            int supplierColumnIndex = data.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_SUPPLIER);
            int emailColumnIndex = data.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_EMAIL);
            int quantityColumnIndex = data.getColumnIndex(ItemContract.ItemEntry.COLUMN_ITEM_QUANTITY);

            // Extract out the value from the Cursor for the given column index
            String item = data.getString(itemColumnIndex);
            float price = data.getFloat(priceColumnIndex);
            String supplier = data.getString(supplierColumnIndex);
            String email = data.getString(emailColumnIndex);
            int quantity = data.getInt(quantityColumnIndex);

            // Update the views on the screen with the values from the database
            mItemInput.setText(item);
            mPriceInput.setText(Float.toString(price));
            mSupplierInput.setText(supplier);
            mEmailInput.setText(email);
            mQuantityInput.setText(Integer.toString(quantity));

        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        // If the loader is invalidated, clear out all the data from the input fields.
        mItemInput.setText("");
        mPriceInput.setText(Float.toString(0));
        mSupplierInput.setText("");
        mEmailInput.setText("");
        mQuantityInput.setText(Integer.toString(0));
    }




}