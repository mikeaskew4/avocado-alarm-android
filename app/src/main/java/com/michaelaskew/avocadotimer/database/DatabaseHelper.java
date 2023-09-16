package com.michaelaskew.avocadotimer.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.michaelaskew.avocadotimer.models.Avocado;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "avocado.db";
    private static final int DATABASE_VERSION = 1;

    // Table columns
    public static final String TABLE_NAME = "avocado_table";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_CREATION_TIME = "creation_time";

    // ... add more columns as needed ...

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create table query
        String CREATE_TABLE = "CREATE TABLE " + TABLE_NAME + "("
                + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                + COLUMN_NAME + " TEXT,"
                + COLUMN_IMAGE_URI + " TEXT,"
                + COLUMN_CREATION_TIME + " TEXT" + ")";
        db.execSQL(CREATE_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        onCreate(db);
    }

    public List<Avocado> getAllAvocados() {
        List<Avocado> avocadoList = new ArrayList<>();

        // Select all query
        String selectQuery = "SELECT  * FROM " + TABLE_NAME;

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);

        // Loop through all rows and add to the list
        if (cursor.moveToFirst()) {
            do {
                Avocado avocado = new Avocado();
                avocado.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
                avocado.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
                avocado.setImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URI)));

//                avocado.setCreationTime(cursor.getString(cursor.getColumnIndex(COLUMN_CREATION_TIME)));
                // Add other fields as necessary...

                // Adding avocado to list
                avocadoList.add(avocado);
            } while (cursor.moveToNext());
        }

        // Close the cursor and database connection
        cursor.close();
        db.close();

        // Return the list
        return avocadoList;
    }

    public Avocado getAvocado(int id) {
        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.query(TABLE_NAME, new String[]{COLUMN_ID, COLUMN_NAME, COLUMN_IMAGE_URI}, COLUMN_ID + "=?", new String[]{String.valueOf(id)}, null, null, null);

        if (cursor != null) {
            cursor.moveToFirst();
            Avocado avocado = new Avocado();
            avocado.setId(cursor.getInt(cursor.getColumnIndex(COLUMN_ID)));
            avocado.setName(cursor.getString(cursor.getColumnIndex(COLUMN_NAME)));
            avocado.setImagePath(cursor.getString(cursor.getColumnIndex(COLUMN_IMAGE_URI)));
            // ... (set other attributes)

            cursor.close();
            return avocado;
        } else {
            return null;
        }
    }

    public long insertAvocado(Avocado avocado) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, avocado.getName());
        values.put(COLUMN_IMAGE_URI, avocado.getImagePath());
        values.put(COLUMN_CREATION_TIME, String.valueOf(avocado.getCreationTime()));
        // ... other fields ...

        // Inserting Row
        long id = db.insert(TABLE_NAME, null, values);
        db.close();

        return id;
    }

    public int updateAvocado(Avocado avocado) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, avocado.getName());
        // Add other avocado properties to values as necessary

        // Updating the row with the given avocado ID
        return db.update(TABLE_NAME, values, COLUMN_ID + " = ?", new String[]{String.valueOf(avocado.getId())});
    }

}
