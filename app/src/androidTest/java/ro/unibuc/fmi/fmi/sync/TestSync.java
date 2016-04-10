package ro.unibuc.fmi.fmi.sync;

import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.test.AndroidTestCase;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

import ro.unibuc.fmi.fmi.data.FmiContract.*;

/**
 * Created by alexandru on 10.04.2016
 */
public class TestSync extends AndroidTestCase {
    private static final String CATEGORIES_TEST_JSON =
            "[{\"_id\":\"tc5rmYDRpjMWFi5Xb\",\"title\":{\"ro\":\"Noutati\", \"en\":\"News\"}}," +
                    "{\"_id\":\"dfXDNY9Xg9pXBRKpE\",\"title\":{\"ro\":\"Conferinte & Seminarii\", \"en\":\"Conferences & Seminars\"}}," +
                    "{\"_id\":\"ttNvCpssh9tTYZyqq\",\"title\":{\"ro\":\"Cercetare / Concursuri\", \"en\":\"Research / Contests\"}}]";


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();

        mContext.getContentResolver().delete(TranslationEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(StringEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(CategoryEntry.CONTENT_URI, null, null);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mContext.getContentResolver().delete(TranslationEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(StringEntry.CONTENT_URI, null, null);
        mContext.getContentResolver().delete(CategoryEntry.CONTENT_URI, null, null);
    }

    public void testParseCategories() {
        FmiSyncAdapter syncAdapter = new FmiSyncAdapter(mContext, false);

        ContentValues stringContentValues = new ContentValues();
        stringContentValues.put(StringEntry._ID, (Integer) null);
        Uri newStringUri = mContext.getContentResolver().insert(StringEntry.CONTENT_URI, stringContentValues);
        int newStringKey = Integer.parseInt(newStringUri.getPathSegments().get(1));

        ContentValues translationContentValues = new ContentValues();
        translationContentValues.put(TranslationEntry.COLUMN_STRING_KEY, newStringKey);
        translationContentValues.put(TranslationEntry.COLUMN_LOCALE, "ro");
        translationContentValues.put(TranslationEntry.COLUMN_VALUE, "Noutati");
        mContext.getContentResolver().insert(TranslationEntry.CONTENT_URI, translationContentValues);

        ContentValues categoryContentValues = new ContentValues();
        categoryContentValues.put(CategoryEntry._ID, "tc5rmYDRpjMWFi5Xb");
        categoryContentValues.put(CategoryEntry.COLUMN_NAME_STRING_KEY, newStringKey);
        mContext.getContentResolver().insert(CategoryEntry.CONTENT_URI, categoryContentValues);

        try {
            Method method = FmiSyncAdapter.class.getDeclaredMethod("parseCategories", String.class);

            method.setAccessible(true);
            method.invoke(syncAdapter, CATEGORIES_TEST_JSON);

            Cursor englishCategories = mContext.getContentResolver().query(CategoryEntry.CONTENT_URI.buildUpon().appendPath("translation").build(),
                    new String[] {
                            CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID,
                            TranslationEntry.COLUMN_LOCALE,
                            TranslationEntry.COLUMN_VALUE,
                    },
                    TranslationEntry.COLUMN_LOCALE + " = ?",
                    new String[] {"en"},
                    CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID + " ASC");

            Cursor romanianCategories = mContext.getContentResolver().query(CategoryEntry.CONTENT_URI.buildUpon().appendPath("translation").build(),
                    new String[] {
                            CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID,
                            TranslationEntry.COLUMN_LOCALE,
                            TranslationEntry.COLUMN_VALUE,
                    },
                    TranslationEntry.COLUMN_LOCALE + " = ?",
                    new String[] {"ro"},
                    CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID + " ASC");

            assertTrue(englishCategories.moveToFirst());
            assertTrue(romanianCategories.moveToFirst());
            assertEquals(3, englishCategories.getCount());
            assertEquals(3, romanianCategories.getCount());

            assertEquals("dfXDNY9Xg9pXBRKpE", englishCategories.getString(englishCategories.getColumnIndex(CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID)));
            assertEquals("dfXDNY9Xg9pXBRKpE", romanianCategories.getString(romanianCategories.getColumnIndex(CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID)));
            assertEquals("Conferences & Seminars", englishCategories.getString(englishCategories.getColumnIndex(TranslationEntry.COLUMN_VALUE)));
            assertEquals("Conferinte & Seminarii", romanianCategories.getString(romanianCategories.getColumnIndex(TranslationEntry.COLUMN_VALUE)));
            assertTrue(englishCategories.moveToNext());
            assertTrue(romanianCategories.moveToNext());
            assertEquals("tc5rmYDRpjMWFi5Xb", englishCategories.getString(englishCategories.getColumnIndex(CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID)));
            assertEquals("tc5rmYDRpjMWFi5Xb", romanianCategories.getString(romanianCategories.getColumnIndex(CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID)));
            assertEquals("News", englishCategories.getString(englishCategories.getColumnIndex(TranslationEntry.COLUMN_VALUE)));
            assertEquals("Noutati", romanianCategories.getString(romanianCategories.getColumnIndex(TranslationEntry.COLUMN_VALUE)));
            assertTrue(englishCategories.moveToNext());
            assertTrue(romanianCategories.moveToNext());
            assertEquals("ttNvCpssh9tTYZyqq", englishCategories.getString(englishCategories.getColumnIndex(CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID)));
            assertEquals("ttNvCpssh9tTYZyqq", romanianCategories.getString(romanianCategories.getColumnIndex(CategoryEntry.TABLE_NAME + "." + CategoryEntry._ID)));
            assertEquals("Research / Contests", englishCategories.getString(englishCategories.getColumnIndex(TranslationEntry.COLUMN_VALUE)));
            assertEquals("Cercetare / Concursuri", romanianCategories.getString(romanianCategories.getColumnIndex(TranslationEntry.COLUMN_VALUE)));

            englishCategories.close();
            romanianCategories.close();

        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            fail("Method not found");
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            fail("Invocation target exception: " + e.getCause().getMessage());
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            fail("Illegal access");
        }
    }
}
