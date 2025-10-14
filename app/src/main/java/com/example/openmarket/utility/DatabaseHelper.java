package com.example.openmarket.utility;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "open_market.db";
    private static final int DATABASE_VERSION = 1;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE commodities " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT, unit TEXT)");
        db.execSQL("CREATE TABLE price_records " +
                "(id INTEGER PRIMARY KEY AUTOINCREMENT, commodity_id INT, price REAL, date " +
                "TEXT, FOREIGN KEY(commodity_id) REFERENCES commodities(id))");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS price_records");
        db.execSQL("DROP TABLE IF EXISTS commodities");
        onCreate(db);
    }
}
