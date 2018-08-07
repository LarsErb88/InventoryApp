package com.example.android.inventoryapp.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.text.TextUtils;
import android.util.Log;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import static android.content.ContentValues.TAG;

public class InventoryDbHelper extends SQLiteOpenHelper {

    public static final String BLOB = " BLOB ";
    public static final String BLOB_WITH_COMMA = " BLOB, ";
    private final Context context;
    private static final String DATABASE_NAME = "shop.db";
    private static final int DATABASE_VERSION = 1;
    private static final String CREATE_TABLE = "CREATE TABLE ";
    private static final String INTEGER_PRIMARY_KEY_AUTOINCREMENT = " INTEGER PRIMARY KEY AUTOINCREMENT, ";
    private static final String TEXT_NOT_NULL = " TEXT NOT NULL, ";
    private static final String INTEGER_NOT_NULL = " INTEGER NOT NULL, ";
    private static final String TEXT_NOT_NULL_DEFAULT_0 = " TEXT NOT NULL DEFAULT 0, ";

    public InventoryDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        this.context = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

        final String SQL_CREATE_ENTRIES = CREATE_TABLE + InventoryEntry.TABLE_NAME + " ("
                + InventoryEntry._ID + INTEGER_PRIMARY_KEY_AUTOINCREMENT
                + InventoryEntry.COLUMN_PRODUCT_NAME + TEXT_NOT_NULL
                + InventoryEntry.COLUMN_PRICE + INTEGER_NOT_NULL
                + InventoryEntry.COLUMN_IN_STOCK + INTEGER_NOT_NULL
                + InventoryEntry.COLUMN_QUANTITY + TEXT_NOT_NULL_DEFAULT_0
                + InventoryEntry.COLUMN_SUPPLIER_NAME + TEXT_NOT_NULL
                + InventoryEntry.COLUMN_SUPPLIER_PHONE + INTEGER_NOT_NULL
                + InventoryEntry.KEY_IMAGE_GOOD + BLOB_WITH_COMMA
                + InventoryEntry.KEY_IMAGE_BAD + BLOB
                + ");";

        sqLiteDatabase.execSQL(SQL_CREATE_ENTRIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
        Log.e(TAG, context.getString(R.string.update_table_from) + oldVersion + context.getString(R.string.TO) + newVersion);
        try {
            for (int i = oldVersion; i < newVersion; ++i) {
                String migrationName = String.format(context.getString(R.string.from_to_sql), i, (i + 1));
                Log.d(TAG, context.getString(R.string.migration_file) + migrationName);
                readAndExecuteSQLScript(sqLiteDatabase, context, migrationName);
            }
        } catch (Exception exception) {
            Log.e(TAG, context.getString(R.string.exception_running_upgrade_script), exception);
        }
    }

    private void readAndExecuteSQLScript(SQLiteDatabase sqLiteDatabase, Context ctx, String fileName) {
        if (TextUtils.isEmpty(fileName)) {
            Log.d(TAG, context.getString(R.string.SQL_script_empty));
            return;
        }

        Log.d(TAG, context.getString(R.string.Script_found));
        AssetManager assetManager = ctx.getAssets();
        BufferedReader reader = null;

        try {
            InputStream is = assetManager.open(fileName);
            InputStreamReader isr = new InputStreamReader(is);
            reader = new BufferedReader(isr);
            executeSQLScript(sqLiteDatabase, reader);
        } catch (IOException e) {
            Log.e(TAG, context.getString(R.string.IOException), e);
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, context.getString(R.string.IOException), e);
                }
            }
        }
    }

    private void executeSQLScript(SQLiteDatabase sqLiteDatabase, BufferedReader reader) throws IOException {
        String line;
        StringBuilder statement = new StringBuilder();
        while ((line = reader.readLine()) != null) {
            statement.append(line);
            statement.append("\n");
            if (line.endsWith(";")) {
                sqLiteDatabase.execSQL(statement.toString());
                statement = new StringBuilder();
            }
        }
    }


}
