package ro.unibuc.fmi.fmi.data;

import android.annotation.TargetApi;
import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;
import android.support.annotation.Nullable;
import android.util.Log;

import ro.unibuc.fmi.fmi.data.FmiContract.*;

/**
 * Created by alexandru on 27.03.2016
 */
public class FmiProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private FmiDbHelper fmiDbHelper;

    static final int CATEGORY = 100;
    static final int CATEGORY_WITH_TRANSLATION = 101;
    static final int POSTS_WITH_TRANSLATION_BY_CATEGORY = 202;
    static final int POST = 200;
    static final int POST_WITH_TRANSLATION = 201;
    static final int TRANSLATION = 300;
    static final int STRING = 400;

    private static final SQLiteQueryBuilder sCategoryWithPostsQueryBuilder;
    private static final SQLiteQueryBuilder sCategoryQueryBuilder;


    static {
        sCategoryWithPostsQueryBuilder = new SQLiteQueryBuilder();
        sCategoryQueryBuilder = new SQLiteQueryBuilder();

        sCategoryWithPostsQueryBuilder.setTables(
                PostEntry.TABLE_NAME + " JOIN " +
                        CategoryEntry.TABLE_NAME +
                        " ON " + PostEntry.TABLE_NAME +
                        "." + PostEntry.COLUMN_CATEGORY_KEY +
                        " = " + CategoryEntry.TABLE_NAME +
                        "." + CategoryEntry._ID + " JOIN " +
                        TranslationEntry.TABLE_NAME +
                        " ON " + PostEntry.TABLE_NAME +
                        "." + PostEntry.COLUMN_TITLE_STRING_KEY +
                        " = " + TranslationEntry.TABLE_NAME +
                        "." + TranslationEntry.COLUMN_STRING_KEY
        );

        sCategoryQueryBuilder.setTables(
                CategoryEntry.TABLE_NAME + " JOIN " +
                        TranslationEntry.TABLE_NAME +
                        " ON " + CategoryEntry.TABLE_NAME +
                        "." + CategoryEntry.COLUMN_NAME_STRING_KEY +
                        " = " + TranslationEntry.TABLE_NAME +
                        "." + TranslationEntry.COLUMN_STRING_KEY
        );
    }

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FmiContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FmiContract.PATH_CATEGORY, CATEGORY);
        matcher.addURI(authority, FmiContract.PATH_POST, POST);
        matcher.addURI(authority, FmiContract.PATH_STRING, STRING);
        matcher.addURI(authority, FmiContract.PATH_TRANSLATION, TRANSLATION);

        matcher.addURI(authority, FmiContract.PATH_CATEGORY + "/translation", CATEGORY_WITH_TRANSLATION);
        matcher.addURI(authority, FmiContract.PATH_POST + "/translation", POSTS_WITH_TRANSLATION_BY_CATEGORY);
        matcher.addURI(authority, FmiContract.PATH_POST + "/translation/*", POST_WITH_TRANSLATION);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        fmiDbHelper = new FmiDbHelper(getContext());
        Log.d("FmiProvider", "Provider created");
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor retCursor;
        switch (uriMatcher.match(uri)) {
            case CATEGORY:
                retCursor = fmiDbHelper.getReadableDatabase().query(
                        CategoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case POST:
                retCursor = fmiDbHelper.getReadableDatabase().query(
                        PostEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case TRANSLATION:
                retCursor = fmiDbHelper.getReadableDatabase().query(
                        TranslationEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case STRING:
                retCursor = fmiDbHelper.getReadableDatabase().query(
                        StringEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case CATEGORY_WITH_TRANSLATION:
                retCursor = sCategoryQueryBuilder.query(
                        fmiDbHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );
                break;
            case POSTS_WITH_TRANSLATION_BY_CATEGORY:
                retCursor = sCategoryWithPostsQueryBuilder.query(
                        fmiDbHelper.getReadableDatabase(),
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;
            case POST_WITH_TRANSLATION:
                retCursor = getPost(selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        retCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return retCursor;
    }

    private Cursor getPost(String selection, String[] selectionArgs) {
        String sql = "SELECT p1." + PostEntry._ID + ", t1." + TranslationEntry.COLUMN_VALUE +
                " AS " + PostEntry.COLUMN_TITLE_STRING_KEY + ", (SELECT t2." +
                TranslationEntry.COLUMN_VALUE + " FROM " + PostEntry.TABLE_NAME + " p2 JOIN " +
                TranslationEntry.TABLE_NAME + " t2 ON p2." + PostEntry.COLUMN_CONTENT_STRING_KEY +
                " = t2." + TranslationEntry.COLUMN_STRING_KEY + " WHERE p2." + PostEntry._ID +
                " = p1." + PostEntry._ID + " AND t1." + TranslationEntry.COLUMN_LOCALE + " = t2." +
                TranslationEntry.COLUMN_LOCALE + " LIMIT 1) AS " + PostEntry.COLUMN_CONTENT_STRING_KEY +
                " FROM " + PostEntry.TABLE_NAME + " p1 JOIN " + TranslationEntry.TABLE_NAME +
                " t1 ON p1." + PostEntry.COLUMN_TITLE_STRING_KEY + " = t1." +
                TranslationEntry.COLUMN_STRING_KEY + " WHERE " + selection;

        return fmiDbHelper.getReadableDatabase().rawQuery(sql,
                selectionArgs);
    }

    private Cursor getCategoryWithPosts(String selection, String[] selectionArgs, String sortOrder) {
        String sql = "SELECT p1." + PostEntry._ID + ", t1." + TranslationEntry.COLUMN_VALUE +
                " AS " + PostEntry.COLUMN_TITLE_STRING_KEY + " FROM " + PostEntry.TABLE_NAME +
                " p1 JOIN " + CategoryEntry.TABLE_NAME + " c1 ON p1." + PostEntry.COLUMN_CATEGORY_KEY +
                " = c1." + CategoryEntry._ID + " JOIN " + TranslationEntry.TABLE_NAME + " t1 ON p1." +
                PostEntry.COLUMN_TITLE_STRING_KEY + " = t1." + TranslationEntry.COLUMN_STRING_KEY +
                " WHERE " + selection + " ORDER BY " + sortOrder;

        return fmiDbHelper.getReadableDatabase().rawQuery(sql,
                selectionArgs);
    }

    private Cursor getCategories(String locale) {

        String sql = "SELECT c1." + CategoryEntry._ID + ", t1." + TranslationEntry.COLUMN_VALUE +
                " AS " + CategoryEntry.COLUMN_NAME_STRING_KEY + " FROM " + CategoryEntry.TABLE_NAME +
                " c1 JOIN " + TranslationEntry.TABLE_NAME + " t1 ON c1." +
                CategoryEntry.COLUMN_NAME_STRING_KEY + " = t1." + TranslationEntry.COLUMN_STRING_KEY +
                " WHERE t1." + TranslationEntry.COLUMN_LOCALE + " = ?";

        return fmiDbHelper.getReadableDatabase().rawQuery(sql, new String[]{locale});
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = uriMatcher.match(uri);

        switch (match) {
            case CATEGORY:
            case CATEGORY_WITH_TRANSLATION:
                return CategoryEntry.CONTENT_TYPE;
            case POST:
            case POSTS_WITH_TRANSLATION_BY_CATEGORY:
                return PostEntry.CONTENT_TYPE;
            case POST_WITH_TRANSLATION:
                return PostEntry.CONTENT_ITEM_TYPE;
            case STRING:
                return StringEntry.CONTENT_TYPE;
            case TRANSLATION:
                return TranslationEntry.CONTENT_TYPE;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
    }

    @Nullable
    @Override
    public Uri insert(Uri uri, ContentValues values) {
        final SQLiteDatabase db = fmiDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        Uri returnUri;
        long _id;

        switch (match) {
            case STRING:
                _id = db.insert(StringEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = StringEntry.buildStringUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case TRANSLATION:
                _id = db.insert(TranslationEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = TranslationEntry.buildTranslationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case CATEGORY:
                _id = db.insert(CategoryEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = CategoryEntry.buildTranslationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            case POST:
                _id = db.insert(PostEntry.TABLE_NAME, null, values);
                if (_id > 0)
                    returnUri = PostEntry.buildTranslationUri(_id);
                else
                    throw new android.database.SQLException("Failed to insert row into " + uri);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        getContext().getContentResolver().notifyChange(uri, null);
        return returnUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = fmiDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int deletedRows;

        if (null == selection) selection = "1";
        switch (match) {
            case STRING:
                deletedRows = db.delete(StringEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case TRANSLATION:
                deletedRows = db.delete(TranslationEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case CATEGORY:
                deletedRows = db.delete(CategoryEntry.TABLE_NAME, selection, selectionArgs);
                break;
            case POST:
                deletedRows = db.delete(PostEntry.TABLE_NAME, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (deletedRows > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return deletedRows;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        final SQLiteDatabase db = fmiDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int updatedRows;

        switch (match) {
            case STRING:
                updatedRows = db.update(StringEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case TRANSLATION:
                updatedRows = db.update(TranslationEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case CATEGORY:
                updatedRows = db.update(CategoryEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            case POST:
                updatedRows = db.update(PostEntry.TABLE_NAME, values, selection, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri: " + uri);
        }

        if (updatedRows > 0)
            getContext().getContentResolver().notifyChange(uri, null);
        return updatedRows;
    }

    @Override
    public int bulkInsert(Uri uri, ContentValues[] values) {
        final SQLiteDatabase db = fmiDbHelper.getWritableDatabase();
        final int match = uriMatcher.match(uri);
        int inserts;

        switch (match) {
            case TRANSLATION:
                db.beginTransaction();
                inserts = 0;
                int lastStringKey = -1;
                long currentInsert;
                Cursor translationCursor = null;
                String log = "bulkInsert " + match;
                try {
                    for (ContentValues value : values) {

                        Log.d(log, "Considering content value with string " +
                                value.getAsInteger(TranslationEntry.COLUMN_STRING_KEY) +
                                " locale " + value.getAsString(TranslationEntry.COLUMN_LOCALE) +
                                " value " + value.getAsString(TranslationEntry.COLUMN_VALUE));

                        if (!value.containsKey(TranslationEntry.COLUMN_STRING_KEY) ||
                                !value.containsKey(TranslationEntry.COLUMN_LOCALE) ||
                                !value.containsKey(TranslationEntry.COLUMN_VALUE))
                            continue;

                        if (value.getAsInteger(TranslationEntry.COLUMN_STRING_KEY) != lastStringKey) {

                            Log.d(log, "New string key, get all translations");

                            lastStringKey = value.getAsInteger(TranslationEntry.COLUMN_STRING_KEY);

                            if (translationCursor != null)
                                translationCursor.close();

                            translationCursor = db.query(
                                    TranslationEntry.TABLE_NAME,
                                    new String[]{
                                            TranslationEntry._ID,
                                            TranslationEntry.COLUMN_LOCALE,
                                            TranslationEntry.COLUMN_VALUE
                                    },
                                    TranslationEntry.COLUMN_STRING_KEY + " = ?",
                                    new String[]{value.getAsInteger(TranslationEntry.COLUMN_STRING_KEY).toString()},
                                    null,
                                    null,
                                    null);
                        }

                        if (translationCursor == null)
                            throw new UnknownError("translationCursor is null");

                        if (translationCursor.moveToFirst()) {
                            Log.d(log, "Translations found");
                            boolean foundLocale = false;
                            do {
                                if (translationCursor.getString(translationCursor.getColumnIndex(TranslationEntry.COLUMN_LOCALE)).equals(value.getAsString(TranslationEntry.COLUMN_LOCALE))) {
                                    foundLocale = true;
                                    break;
                                }
                            }
                            while (translationCursor.moveToNext());


                            if (foundLocale) {
                                Log.d(log, "Locale found, updating");
                                if (!translationCursor.getString(translationCursor.getColumnIndex(TranslationEntry.COLUMN_VALUE)).equals(value.getAsString(TranslationEntry.COLUMN_VALUE)))
                                    db.update(TranslationEntry.TABLE_NAME,
                                            value,
                                            TranslationEntry._ID + " = ?",
                                            new String[]{translationCursor.getString(translationCursor.getColumnIndex(TranslationEntry._ID))});
                                // prevent insert statement below
                                continue;
                            }
                            Log.d(log, "Locale not found");
                        }

                        Log.d(log, "No translations found, inserting");

                        // if there is no translation for this string or the locale was not found
                        currentInsert = db.insert(TranslationEntry.TABLE_NAME, null, value);
                        Log.d(log, "Inserted record with id " + currentInsert);

                        if (currentInsert > 0)
                            inserts++;
                    }

                    if (translationCursor != null)
                        translationCursor.close();

                    db.setTransactionSuccessful();
                } finally {
                    db.endTransaction();
                }
                return inserts;
            default:
                return super.bulkInsert(uri, values);
        }
    }

    @Override
    @TargetApi(11)
    public void shutdown() {
        fmiDbHelper.close();
        super.shutdown();
    }
}
