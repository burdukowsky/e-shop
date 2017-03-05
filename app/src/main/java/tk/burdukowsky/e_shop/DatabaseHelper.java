package tk.burdukowsky.e_shop;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Android Studio
 * User: STANISLAV
 * Date: 01 Март 2017 23:38
 */

class DatabaseHelper extends SQLiteOpenHelper {

    // константы
    private final static int DB_VERSION = 1;
    private final static String DB_NAME = "products.db";
    private final static String TABLE_NAME_PRODUCTS = "products";
    private final static String COLUMN_NAME_ID = "_id";
    private final static String COLUMN_NAME_NAME = "name";
    private final static String COLUMN_NAME_COST = "cost";

    // конструктор
    DatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createTableProducts = "CREATE TABLE " + TABLE_NAME_PRODUCTS + " (" +
                COLUMN_NAME_ID + " INTEGER PRIMARY KEY," +
                COLUMN_NAME_NAME + " TEXT," +
                COLUMN_NAME_COST + " INTEGER" +
                ")";
        db.execSQL(createTableProducts);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    // добавление нескольких товаров
    boolean insertIntoProducts(Map<Integer, Product> items) {
        SQLiteDatabase db = this.getWritableDatabase();
        boolean result;
        db.beginTransaction();
        try {
            deleteAllProducts(db);
            for (Map.Entry<Integer, Product> entry : items.entrySet()) {
                insertIntoProducts(entry.getValue(), db);
            }
            db.setTransactionSuccessful();
            result = true;
        } catch (MyDatabaseException e) {
            Log.e("MyDatabaseException", e.getMessage(), e);
            //e.printStackTrace();
            result = false;
        } finally {
            db.endTransaction();
        }
        return result;
    }

    // добавление одного товара
    private void insertIntoProducts(Product item, SQLiteDatabase db) throws MyDatabaseException {
        if (db.isReadOnly()) {
            throw new MyDatabaseException("Database read only!");
        }
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME_ID, item.getId());
        values.put(COLUMN_NAME_NAME, item.getName());
        values.put(COLUMN_NAME_COST, item.getCost());

        if (db.insert(TABLE_NAME_PRODUCTS, null, values) == -1) {
            throw new MyDatabaseException("Insert Error");
        }
    }

    // очистка таблицы товаров
    private void deleteAllProducts(SQLiteDatabase db) throws MyDatabaseException {
        if (db.isReadOnly()) {
            throw new MyDatabaseException("Database read only!");
        }
        db.execSQL("DELETE FROM " + TABLE_NAME_PRODUCTS);
    }

    // получение всех товаров
    Map<Integer, Product> getProducts() {
        Map<Integer, Product> items = new HashMap<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String sortOrder = COLUMN_NAME_ID;
        Cursor c = db.query(TABLE_NAME_PRODUCTS, null, null, null, null, null, sortOrder);
        while (c.moveToNext()) {
            Integer id = c.getInt(c.getColumnIndex(COLUMN_NAME_ID));
            String name = c.getString(c.getColumnIndex(COLUMN_NAME_NAME));
            Integer cost = c.getInt(c.getColumnIndex(COLUMN_NAME_COST));
            items.put(id, new Product(id, name, cost));
        }
        c.close();
        return items;
    }

    // собственное исключение, чтобы определить успех/провал транзакции
    // в методе boolean insertIntoProducts(Map<Integer, Product> items)
    private class MyDatabaseException extends Exception {
        private String message;

        MyDatabaseException(String message) {
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }
    }
}
