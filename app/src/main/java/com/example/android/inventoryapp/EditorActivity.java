package com.example.android.inventoryapp;

import android.app.AlertDialog;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.text.InputType;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mPriceEditText;
    private ToggleButton mInStockToggleButton;
    private EditText mQuantityEditText;
    private EditText mSupNameEditText;
    private EditText mSupPhoneEditText;
    private LinearLayout quantityButtonsLayout;
    private int mInStock = 0;
    private final static int EXISTING_PRODUCT_LOADER = 0;
    private static Uri mCurrentProductUri;
    private boolean mProductHasChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        Intent intent = getIntent();
        mCurrentProductUri = intent.getData();

        mNameEditText = findViewById(R.id.edit_product_name);
        mPriceEditText = findViewById(R.id.edit_product_price);
        mInStockToggleButton = findViewById(R.id.ToggleButton_inStock);
        mQuantityEditText = findViewById(R.id.edit_product_quantity);
        mSupNameEditText = findViewById(R.id.edit_supplier_name);
        mSupPhoneEditText = findViewById(R.id.edit_supplier_phone);
        quantityButtonsLayout = findViewById(R.id.quantityButtons);
        quantityButtonsLayout.setVisibility(View.INVISIBLE);
        mQuantityEditText.setVisibility(View.INVISIBLE);
        mPriceEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mQuantityEditText.setInputType(InputType.TYPE_CLASS_NUMBER);
        mSupPhoneEditText.setInputType(InputType.TYPE_CLASS_PHONE);

        mNameEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mInStockToggleButton.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mSupNameEditText.setOnTouchListener(mTouchListener);
        mSupPhoneEditText.setOnTouchListener(mTouchListener);

        Button quantityDecrease = findViewById(R.id.decreaseQuantity);
        Button quantityIncrease = findViewById(R.id.increaseQuantity);
        Button callButton = findViewById(R.id.call);
        callButton.setVisibility(View.VISIBLE);

        if (mCurrentProductUri == null) {
            setTitle(getString(R.string.editor_activity_title_new_product));
            callButton.setVisibility(View.INVISIBLE);
            invalidateOptionsMenu();
        } else {
            setTitle(getString(R.string.editor_activity_title_edit_product));
            callButton.setVisibility(View.VISIBLE);
            getLoaderManager().initLoader(EXISTING_PRODUCT_LOADER, null, this);
        }

        setupToggleButton();

        quantityDecrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
                    int Quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                    if (Quantity > 0) {
                        Quantity--;
                        if (Quantity == 0) {
                            mQuantityEditText.setText(Integer.toString(Quantity));
                            mInStockToggleButton.setChecked(false);
                        } else {
                            mQuantityEditText.setText(Integer.toString(Quantity));
                        }
                    } else {
                        Toast.makeText(getApplicationContext(), R.string.sale_not_in_stock, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.first_enter_the_quantity, Toast.LENGTH_SHORT).show();
                }
            }
        });

        quantityIncrease.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
                    int Quantity = Integer.parseInt(mQuantityEditText.getText().toString().trim());
                    if (Quantity >= 0) {
                        Quantity++;
                        mQuantityEditText.setText(Integer.toString(Quantity));
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.first_enter_the_quantity, Toast.LENGTH_SHORT).show();
                }
            }
        });

        callButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!TextUtils.isEmpty(mSupPhoneEditText.getText().toString().trim())) {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse(getString(R.string.dial_tel) + mSupPhoneEditText.getText().toString().trim()));
                    if (callIntent.resolveActivity(getPackageManager()) != null) {
                        startActivity(callIntent);
                    }
                } else {
                    Toast.makeText(getApplicationContext(), R.string.no_phone_number_found, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mProductHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(
            DialogInterface.OnClickListener discardButtonClickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    @Override
    public void onBackPressed() {
        if (!mProductHasChanged) {
            super.onBackPressed();
            return;
        }
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                };
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void setupToggleButton() {
        mInStockToggleButton.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (mInStockToggleButton.isChecked()) {
                    mInStock = InventoryEntry.IN_STOCK_YES;
                    mQuantityEditText.setVisibility(View.VISIBLE);
                    quantityButtonsLayout.setVisibility(View.VISIBLE);
                    if (mQuantityEditText.getText().toString().equals("0") || TextUtils.isEmpty(mQuantityEditText.getText().toString().trim())) {
                        mQuantityEditText.setText("1");
                    }
                } else {
                    mInStock = InventoryEntry.IN_STOCK_NO;
                    mQuantityEditText.setText("0");
                    mQuantityEditText.setVisibility(View.INVISIBLE);
                    quantityButtonsLayout.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    private void saveProduct() {
        String nameString = mNameEditText.getText().toString().trim();
        String Price = mPriceEditText.getText().toString().trim();
        String Quantity = "0";
        if (mQuantityEditText.getVisibility() == View.VISIBLE) {
            if (TextUtils.isEmpty(mQuantityEditText.getText().toString().trim()) || mQuantityEditText.getText().toString().trim().equals("0")) {
                mInStock = InventoryEntry.IN_STOCK_NO;
            } else {
                Quantity = mQuantityEditText.getText().toString().trim();
            }
        }
        String SupNameString = mSupNameEditText.getText().toString().trim();
        String SupPhone = mSupPhoneEditText.getText().toString().trim();

        if (mCurrentProductUri == null &&
                (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(Price) || TextUtils.isEmpty(Integer.toString(mInStock))
                || TextUtils.isEmpty(Quantity) || TextUtils.isEmpty(SupNameString) || TextUtils.isEmpty(SupPhone))) {
            Toast.makeText(this, R.string.required_elements,
                    Toast.LENGTH_SHORT).show();
            return;
        } else if (mCurrentProductUri != null &&
                (TextUtils.isEmpty(nameString) || TextUtils.isEmpty(Price) || TextUtils.isEmpty(Integer.toString(mInStock))
                        || TextUtils.isEmpty(Quantity) || TextUtils.isEmpty(SupNameString) || TextUtils.isEmpty(SupPhone))) {
            Toast.makeText(this, R.string.no_update_required_elements,
                    Toast.LENGTH_SHORT).show();
            return;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, nameString);
        values.put(InventoryEntry.COLUMN_PRICE, Price);
        values.put(InventoryEntry.COLUMN_IN_STOCK, mInStock);
        values.put(InventoryEntry.COLUMN_QUANTITY, Quantity);
        values.put(InventoryEntry.COLUMN_SUPPLIER_NAME, SupNameString);
        values.put(InventoryEntry.COLUMN_SUPPLIER_PHONE, SupPhone);

        if (mCurrentProductUri == null) {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);
            if (newUri == null) {
                Toast.makeText(this, R.string.editor_insert_product_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_insert_product_successful,
                        Toast.LENGTH_SHORT).show();
            }

        } else {
            int rowsAffected = getContentResolver().update(mCurrentProductUri, values, null, null);

            if (rowsAffected == 0) {
                Toast.makeText(this, R.string.editor_update_product_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_update_product_successful,
                        Toast.LENGTH_SHORT).show();
            }
        }
finish();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_save:
                saveProduct();
                return true;
            case R.id.action_delete:
                showDeleteConfirmationDialog();
                return true;
            case android.R.id.home:
                if (!mProductHasChanged) {
                    NavUtils.navigateUpFromSameTask(EditorActivity.this);
                    return true;
                }
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void deleteProduct() {
        if (mCurrentProductUri != null) {
            int rowsDeleted = getContentResolver().delete(mCurrentProductUri, null, null);
            if (rowsDeleted == 0) {
                Toast.makeText(this, R.string.editor_delete_product_failed,
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.editor_delete_product_successful,
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        if (mCurrentProductUri == null) {
            return null;
        }
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRICE,
                InventoryEntry.COLUMN_QUANTITY,
                InventoryEntry.COLUMN_IN_STOCK,
                InventoryEntry.COLUMN_SUPPLIER_NAME,
                InventoryEntry.COLUMN_SUPPLIER_PHONE};

        return new CursorLoader(this,
                mCurrentProductUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
            int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
            int inStockColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IN_STOCK);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
            int supNameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_NAME);
            int supPhoneColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_SUPPLIER_PHONE);

            String productName = cursor.getString(nameColumnIndex);
            int productPrice = cursor.getInt(priceColumnIndex);
            int productInStock = cursor.getInt(inStockColumnIndex);
            int productQuantity = cursor.getInt(quantityColumnIndex);
            String productSupName = cursor.getString(supNameColumnIndex);
            String productSupPhone = cursor.getString(supPhoneColumnIndex);

            mNameEditText.setText(productName);
            mPriceEditText.setText(Integer.toString(productPrice));
            mQuantityEditText.setText(Integer.toString(productQuantity));
            mSupNameEditText.setText(productSupName);
            mSupPhoneEditText.setText(productSupPhone);

            switch (productInStock) {
                case InventoryEntry.IN_STOCK_NO:
                    mInStockToggleButton.setChecked(false);
                    break;
                case InventoryEntry.IN_STOCK_YES:
                    mInStockToggleButton.setChecked(true);
                    break;
                default:
                    mInStockToggleButton.setChecked(false);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNameEditText.setText("");
        mPriceEditText.setText("");
        mQuantityEditText.setText("");
        mInStockToggleButton.setChecked(false);
        mSupNameEditText.setText("");
        mSupPhoneEditText.setText("");
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        if (mCurrentProductUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    private void showDeleteConfirmationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                deleteProduct();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
