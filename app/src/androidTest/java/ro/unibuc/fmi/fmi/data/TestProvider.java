package ro.unibuc.fmi.fmi.data;

import android.content.ContentValues;
import android.database.Cursor;
import android.test.AndroidTestCase;

import java.util.Vector;

/**
 * Created by alexandru on 09.04.2016
 */
public class TestProvider extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext.getContentResolver().delete(FmiContract.TranslationEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(FmiContract.StringEntry.CONTENT_URI, null, null);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        mContext.getContentResolver().delete(FmiContract.TranslationEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(FmiContract.StringEntry.CONTENT_URI, null, null);
    }

    public void testTranslationBulkInsert() {

        ContentValues values_string = new ContentValues();
        values_string.put(FmiContract.StringEntry._ID, 1);
        mContext.getContentResolver().insert(FmiContract.StringEntry.CONTENT_URI, values_string);

        ContentValues values1 = new ContentValues();
        values1.put(FmiContract.TranslationEntry.COLUMN_STRING_KEY, 1);
        values1.put(FmiContract.TranslationEntry.COLUMN_LOCALE, "en");
        values1.put(FmiContract.TranslationEntry.COLUMN_VALUE, "Old stuff");
        mContext.getContentResolver().insert(FmiContract.TranslationEntry.CONTENT_URI, values1);

        Vector<ContentValues> cvVector = new Vector<>();
        ContentValues values2 = new ContentValues();
        values2.put(FmiContract.TranslationEntry.COLUMN_STRING_KEY, 1);
        values2.put(FmiContract.TranslationEntry.COLUMN_LOCALE, "en");
        values2.put(FmiContract.TranslationEntry.COLUMN_VALUE, "New stuff");
        ContentValues values3 = new ContentValues();
        values3.put(FmiContract.TranslationEntry.COLUMN_STRING_KEY, 1);
        values3.put(FmiContract.TranslationEntry.COLUMN_LOCALE, "ro");
        values3.put(FmiContract.TranslationEntry.COLUMN_VALUE, "Same stuff, different language");
        cvVector.add(values2);
        cvVector.add(values3);
        ContentValues[] cvArray = new ContentValues[cvVector.size()];
        cvVector.toArray(cvArray);

        mContext.getContentResolver().bulkInsert(FmiContract.TranslationEntry.CONTENT_URI, cvArray);

        Cursor testCursor = mContext.getContentResolver().query(FmiContract.TranslationEntry.CONTENT_URI,
                new String[]{
                        FmiContract.TranslationEntry.COLUMN_STRING_KEY,
                        FmiContract.TranslationEntry.COLUMN_LOCALE,
                        FmiContract.TranslationEntry.COLUMN_VALUE},
                null, null, FmiContract.TranslationEntry.COLUMN_LOCALE + " ASC");

        assertTrue("No data in the table", testCursor.moveToFirst());

        assertEquals(2, testCursor.getCount());

        assertEquals("Wrong locale,", "en", testCursor.getString(testCursor.getColumnIndex(FmiContract.TranslationEntry.COLUMN_LOCALE)));

        assertEquals("Existent value not replaced,", "New stuff", testCursor.getString(testCursor.getColumnIndex(FmiContract.TranslationEntry.COLUMN_VALUE)));

        testCursor.moveToNext();

        assertEquals("Wrong locale,", "ro", testCursor.getString(testCursor.getColumnIndex(FmiContract.TranslationEntry.COLUMN_LOCALE)));

        assertEquals("Same stuff, different language", testCursor.getString(testCursor.getColumnIndex(FmiContract.TranslationEntry.COLUMN_VALUE)));

        testCursor.close();
    }
}
