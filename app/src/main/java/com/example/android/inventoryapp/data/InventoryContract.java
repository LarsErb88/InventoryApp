package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;


public final class InventoryContract {

    private InventoryContract() {
    }

    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";
    private static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
    public static final String PATH_PRODUCTSS = "inventory";

    public static abstract class InventoryEntry implements BaseColumns {
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_PRODUCTSS);
        public static final String TABLE_NAME = "inventory";
        public static final String COLUMN_PRODUCT_NAME = "ProductName";
        public static final String COLUMN_IN_STOCK = "inStock";
        public static final String COLUMN_PRICE = "price";
        public static final String COLUMN_QUANTITY = "quantity";
        public static final String COLUMN_SUPPLIER_NAME = "supplier";
        public static final String COLUMN_SUPPLIER_PHONE = "supplierPhone";
        public static final String KEY_IMAGE_GOOD = "goodImage";
        public static final String KEY_IMAGE_BAD = "badImage";

        public static final int IN_STOCK_NO = 0;
        public static final int IN_STOCK_YES = 1;

        public static boolean isValidInStock(int inStock) {
            return inStock == IN_STOCK_NO || inStock == IN_STOCK_YES;
        }

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTSS;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_PRODUCTSS;

    }
}
