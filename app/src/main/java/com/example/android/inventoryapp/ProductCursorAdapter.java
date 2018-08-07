package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract;

public class ProductCursorAdapter extends CursorAdapter {

    public ProductCursorAdapter(Context context, Cursor c) {
        super(context, c, 0 /* flags */);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {

        TextView nameTextView = view.findViewById(R.id.name);
        TextView priceTextView = view.findViewById(R.id.price);
        TextView inStockTextView = view.findViewById(R.id.inStock);
        TextView quantityTextView = view.findViewById(R.id.quantity);
        ImageView goodBadImage = view.findViewById(R.id.goodOrBad);

        priceTextView.setText(R.string.price);
        inStockTextView.setText(R.string.inStockText);
        quantityTextView.setText(R.string.quantityText);

        int nameColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRODUCT_NAME);
        int priceColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_PRICE);
        int inStockColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_IN_STOCK);
        int quantityColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_QUANTITY);
        final int IdColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry._ID);
        final int imageGoodColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.KEY_IMAGE_GOOD);
        final int imageBadColumnIndex = cursor.getColumnIndex(InventoryContract.InventoryEntry.KEY_IMAGE_BAD);

        String productName = cursor.getString(nameColumnIndex);
        String productPrice = cursor.getString(priceColumnIndex);
        int productInStock = cursor.getInt(inStockColumnIndex);
        final int productQuantity = cursor.getInt(quantityColumnIndex);
        final int ID = cursor.getInt(IdColumnIndex);
        byte[] goodBytes = cursor.getBlob(imageGoodColumnIndex);
        byte[] badBytes = cursor.getBlob(imageBadColumnIndex);
        Bitmap goodBadBitmap;

        if (productQuantity > 0) {
            goodBadBitmap = getImage(goodBytes);
            goodBadImage.setImageBitmap(goodBadBitmap);
        } else {
            goodBadBitmap = getImage(badBytes);
            goodBadImage.setImageBitmap(goodBadBitmap);
        }

        nameTextView.setText(productName);
        priceTextView.append(productPrice);
        if(productInStock == InventoryContract.InventoryEntry.IN_STOCK_YES) {
            inStockTextView.append(context.getString(R.string.yes));
        } else {
            inStockTextView.append(context.getString(R.string.no));
        }
        quantityTextView.append(String.valueOf(productQuantity));

        Button saleButton = view.findViewById(R.id.sale);
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newQuantity = productQuantity;
                if (newQuantity > 0) {
                    newQuantity--;
                    ContentValues contentValues = new ContentValues();
                    if (newQuantity == 0) {
                        contentValues.put(InventoryContract.InventoryEntry.COLUMN_IN_STOCK, InventoryContract.InventoryEntry.IN_STOCK_NO);
                    }
                    contentValues.put(InventoryContract.InventoryEntry.COLUMN_QUANTITY, newQuantity);
                    String selection = InventoryContract.InventoryEntry._ID + "=?";
                    String[] selectionsArgs = {String.valueOf(ID)};

                    int success = context.getContentResolver().update(InventoryContract.InventoryEntry.CONTENT_URI, contentValues, selection, selectionsArgs);
                    if (success == 0) {
                        Toast.makeText(context, R.string.Invalid, Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, R.string.Done, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(context, R.string.sale_not_in_stock, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public Bitmap getImage(byte[] image) {
        return BitmapFactory.decodeByteArray(image, 0, image.length);
    }
}