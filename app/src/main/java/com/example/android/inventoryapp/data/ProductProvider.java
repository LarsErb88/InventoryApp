package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;

public class ProductProvider extends ContentProvider {

    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);
    private static final int PRODUCTS = 1;
    private static final int PRODUCT_ID = 2;
    private InventoryDbHelper mDbHelper;

    static {

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTSS, PRODUCTS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryContract.PATH_PRODUCTSS + "/#", PRODUCT_ID);
    }

    private static final String LOG_TAG = ProductProvider.class.getSimpleName();

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                        String sortOrder) {
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        Cursor cursor;
        int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return insertProduct(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion is not supported for " + uri);
        }
    }

    private Uri insertProduct(Uri uri, ContentValues contentValues) {
        String name = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Product requires a name");
        }
        Integer inStock = contentValues.getAsInteger(InventoryEntry.COLUMN_IN_STOCK);
        if (inStock == null || !InventoryEntry.isValidInStock(inStock)) {
            throw new IllegalArgumentException("InStock requires valid gender");
        }
        Integer price = contentValues.getAsInteger(InventoryEntry.COLUMN_PRICE);
        if (price != null && price < 0) {
            throw new IllegalArgumentException("Product requires valid price");
        }
        Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
        if (quantity != null && quantity < 0) {
            throw new IllegalArgumentException("Product requires valid quantity");
        }
        String supName = contentValues.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
        if (supName == null) {
            throw new IllegalArgumentException("Product requires a supplier name");
        }
        String supPhone = contentValues.getAsString(InventoryEntry.COLUMN_SUPPLIER_PHONE);
        if (supPhone == null) {
            throw new IllegalArgumentException("Product requires a supplier phone number");
        }

        contentValues.put(InventoryEntry.KEY_IMAGE_GOOD, getBytes(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.good)));
        contentValues.put(InventoryEntry.KEY_IMAGE_BAD, getBytes(BitmapFactory.decodeResource(getContext().getResources(), R.drawable.bad)));
        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        long newRowId = database.insert(InventoryEntry.TABLE_NAME, null, contentValues);

        if (newRowId == -1) {
            Log.e(LOG_TAG, getContext().getString(R.string.failed_insert) + uri);
            return null;
        }

        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(uri, newRowId);
    }

    public byte[] getBytes(Bitmap bitmap) {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 0, stream);
        return stream.toByteArray();
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection,
                      String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return updateProduct(uri, contentValues, selection, selectionArgs);
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                return updateProduct(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateProduct(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        if (contentValues.containsKey(InventoryEntry.COLUMN_PRODUCT_NAME)) {
            String name = contentValues.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);
            if (name == null) {
                throw new IllegalArgumentException("Product requires a name");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_IN_STOCK)) {
            Integer inStock = contentValues.getAsInteger(InventoryEntry.COLUMN_IN_STOCK);
            if (inStock == null || !InventoryEntry.isValidInStock(inStock)) {
                throw new IllegalArgumentException("Product requires valid inStock-Key");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_PRICE)) {
            Integer price = contentValues.getAsInteger(InventoryEntry.COLUMN_PRICE);
            if (price == null) {
                throw new IllegalArgumentException("Product requires valid price");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_QUANTITY)) {
            Integer quantity = contentValues.getAsInteger(InventoryEntry.COLUMN_QUANTITY);
            if (quantity != null && quantity < 0) {
                throw new IllegalArgumentException("Product requires valid quantity");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_SUPPLIER_NAME)) {
            String supName = contentValues.getAsString(InventoryEntry.COLUMN_SUPPLIER_NAME);
            if (supName == null) {
                throw new IllegalArgumentException("Product requires a supplier name");
            }
        }

        if (contentValues.containsKey(InventoryEntry.COLUMN_SUPPLIER_PHONE)) {
            String SupPhone = contentValues.getAsString(InventoryEntry.COLUMN_SUPPLIER_PHONE);
            if (SupPhone == null) {
                throw new IllegalArgumentException("Product requires a supplier phone number");
            }
        }

        if (contentValues.size() == 0) {
            return 0;
        }

        SQLiteDatabase database = mDbHelper.getWritableDatabase();

        int newRowId = database.update(InventoryEntry.TABLE_NAME, contentValues, selection, selectionArgs);
        if (newRowId != 0) {
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return newRowId;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        SQLiteDatabase database = mDbHelper.getWritableDatabase();
        int rowsDeleted;
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case PRODUCT_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                rowsDeleted = database.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if (rowsDeleted != 0) {
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case PRODUCTS:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case PRODUCT_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }
}
