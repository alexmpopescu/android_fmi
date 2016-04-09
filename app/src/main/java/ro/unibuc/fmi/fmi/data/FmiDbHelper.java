package ro.unibuc.fmi.fmi.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import ro.unibuc.fmi.fmi.data.FmiContract.*;

/**
 * Created by alexandru on 27.03.2016
 */
public class FmiDbHelper extends SQLiteOpenHelper {

    private static final int DATABASE_VERSION = 1;
    static final String DATABASE_NAME = "fmi.db";

    public FmiDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_STRINGS = "CREATE TABLE " + StringEntry.TABLE_NAME + " (" +
                StringEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT );";
        final String SQL_CREATE_TRANSLATIONS = "CREATE TABLE " + TranslationEntry.TABLE_NAME + "(" +
                TranslationEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TranslationEntry.COLUMN_STRING_KEY + " INTEGER NOT NULL, " +
                TranslationEntry.COLUMN_LOCALE + " TEXT NOT NULL, " +
                TranslationEntry.COLUMN_VALUE + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + TranslationEntry.COLUMN_STRING_KEY + ") REFERENCES " +
                StringEntry.TABLE_NAME + " (" + StringEntry._ID + "));";
        final String SQL_CREATE_CATEGORIES = "CREATE TABLE " + CategoryEntry.TABLE_NAME + " (" +
                CategoryEntry._ID + " TEXT PRIMARY KEY, " +
                CategoryEntry.COLUMN_NAME_STRING_KEY + " INTEGER NOT NULL, " +
                "FOREIGN KEY ("+CategoryEntry.COLUMN_NAME_STRING_KEY + ") REFERENCES " +
                StringEntry.TABLE_NAME + " (" + StringEntry._ID + "));";
        final String SQL_CREATE_POSTS = " CREATE TABLE " + PostEntry.TABLE_NAME + " (" +
                PostEntry._ID + " TEXT PRIMARY KEY, " +
                PostEntry.COLUMN_TITLE_STRING_KEY + " INTEGER, " +
                PostEntry.COLUMN_CONTENT_STRING_KEY + " INTEGER NOT NULL, " +
                PostEntry.COLUMN_CATEGORY_KEY + " TEXT NOT NULL, " +
                "FOREIGN KEY (" + PostEntry.COLUMN_TITLE_STRING_KEY + ") REFERENCES " +
                StringEntry.TABLE_NAME + " (" + StringEntry._ID + "), " +
                "FOREIGN KEY (" + PostEntry.COLUMN_CONTENT_STRING_KEY + ") REFERENCES " +
                StringEntry.TABLE_NAME + " (" + StringEntry._ID + "), " +
                "FOREIGN KEY (" + PostEntry.COLUMN_CATEGORY_KEY + ") REFERENCES " +
                CategoryEntry.TABLE_NAME + " (" + CategoryEntry._ID + "));";

        db.execSQL(SQL_CREATE_STRINGS);
        db.execSQL(SQL_CREATE_TRANSLATIONS);
        db.execSQL(SQL_CREATE_CATEGORIES);
        db.execSQL(SQL_CREATE_POSTS);

        Log.d(this.getClass().toString(), "Database created");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + PostEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + CategoryEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + TranslationEntry.TABLE_NAME);
        db.execSQL("DROP TABLE IF EXISTS " + StringEntry.TABLE_NAME);
        Log.d(this.getClass().toString(), "Dropped the tables");
        onCreate(db);
    }
}
