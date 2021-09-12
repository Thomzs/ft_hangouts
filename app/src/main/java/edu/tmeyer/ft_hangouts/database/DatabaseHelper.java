package edu.tmeyer.ft_hangouts.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "SQLite";

    //Database info
    private static final int    DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "FT_HANGOUTS";
    private static final String TABLE_NAME = "CONTACT";

    //Fields
    private static final String COLUMN_CONTACT_ID = "Id";
    private static final String COLUMN_CONTACT_FIRST_NAME = "FirstName";
    private static final String COLUMN_CONTACT_LAST_NAME = "LastName";
    private static final String COLUMN_CONTACT_PHONE = "Phone";
    private static final String COLUMN_CONTACT_PICTURE = "Picture";
    private static final String COLUMN_CONTACT_NOTE = "Note";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }


    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        Log.i(TAG, this.getClass().getName() + ".onCreate");

        String query = "CREATE TABLE " + TABLE_NAME + "(" +
                COLUMN_CONTACT_ID + " INTEGER PRIMARY KEY, " +
                COLUMN_CONTACT_FIRST_NAME + " TEXT, " +
                COLUMN_CONTACT_LAST_NAME + " TEXT, " +
                COLUMN_CONTACT_PHONE + " TEXT, " +
                COLUMN_CONTACT_PICTURE + " BLOB, " +
                COLUMN_CONTACT_NOTE + " TEXT" +
                ")";
        sqLiteDatabase.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        Log.i(TAG, this.getClass().getName() + ".onUpdate");

        String query = "DROP TABLE IF EXISTS " + TABLE_NAME;
        sqLiteDatabase.execSQL(query);
        onCreate(sqLiteDatabase);
    }

    public void addContact(Contact contact) {
        Log.i(TAG, this.getClass().getName() + ".addContact");

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTACT_FIRST_NAME, contact.getFirstName());
        values.put(COLUMN_CONTACT_LAST_NAME, contact.getLastName());
        values.put(COLUMN_CONTACT_PHONE, contact.getPhone());
        values.put(COLUMN_CONTACT_PICTURE, contact.getPicture());
        values.put(COLUMN_CONTACT_NOTE, contact.getNote());

        db.insert(TABLE_NAME, null, values);
        db.close();
    }

    public void updateContact(Contact contact) {
        Log.i(TAG, this.getClass().getName() + ".UpdateContact");

        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CONTACT_FIRST_NAME, contact.getFirstName());
        values.put(COLUMN_CONTACT_LAST_NAME, contact.getLastName());
        values.put(COLUMN_CONTACT_PHONE, contact.getPhone());
        values.put(COLUMN_CONTACT_PICTURE, contact.getPicture());
        values.put(COLUMN_CONTACT_NOTE, contact.getNote());

        db.update(TABLE_NAME, values, "id = ?", new String[]{String.valueOf(contact.getId())});
        db.close();
    }

    public void deleteContact(Contact contact) {
        Log.i(TAG, this.getClass().getName() + ".DeleteContact: id:" + contact.getId());

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_NAME, "id = ?", new String[]{String.valueOf(contact.getId())});
        db.close();
    }

    public List<Contact> getAllContacts() {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_NAME;
        Cursor cursor = db.rawQuery(query, null);
        List<Contact> contactList = new ArrayList<>();

        while (cursor.moveToNext()) {
            Contact contact = new Contact();
            contact.setId(cursor.getInt(0));
            contact.setFirstName(cursor.getString(1));
            contact.setLastName(cursor.getString(2));
            contact.setPhone(cursor.getString(3));
            contact.setPicture(cursor.getBlob(4));
            contact.setNote(cursor.getString(5));
            contactList.add(contact);
        }
        cursor.close();
        db.close();
        return contactList;
    }

    public Contact getContact(int id) {
        Contact contact = null;
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(
                TABLE_NAME,
                new String[]{
                        COLUMN_CONTACT_ID,
                        COLUMN_CONTACT_FIRST_NAME,
                        COLUMN_CONTACT_LAST_NAME,
                        COLUMN_CONTACT_PHONE,
                        COLUMN_CONTACT_PICTURE,
                        COLUMN_CONTACT_NOTE
                },
                COLUMN_CONTACT_ID + "= ?",
                new String[]{String.valueOf(id)},
                null,
                null,
                null,
                null
        );

        if (cursor != null) {
            cursor.moveToFirst();
            contact = new Contact();
            contact.setId(cursor.getInt(0));
            contact.setFirstName(cursor.getString(1));
            contact.setLastName(cursor.getString(2));
            contact.setPhone(cursor.getString(3));
            contact.setPicture(cursor.getBlob(4));
            contact.setNote(cursor.getString(5));
            cursor.close();
        }
        db.close();
        //Return null if no contact was found
        return contact;
    }

    public int getCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_NAME, null);
        int count;

        cursor.moveToFirst();
        count = cursor.getInt(0);
        cursor.close();
        db.close();
        return count;
    }
}
