package ro.unibuc.fmi.fmi.data;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.Nullable;

import ro.unibuc.fmi.fmi.data.FmiContract.*;

/**
 * Created by alexandru on 27.03.2016
 */
public class FmiProvider extends ContentProvider {

    private static final UriMatcher uriMatcher = buildUriMatcher();
    private FmiDbHelper fmiDbHelper;

    static final int CATEGORY = 100;
    static final int CATEGORY_WITH_POSTS = 101;
    static final int POST = 200;
    static final int TRANSLATION = 300;
    static final int STRING = 400;

    static UriMatcher buildUriMatcher() {
        final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
        final String authority = FmiContract.CONTENT_AUTHORITY;

        matcher.addURI(authority, FmiContract.PATH_CATEGORY, CATEGORY);
        matcher.addURI(authority, FmiContract.PATH_CATEGORY + "/*", CATEGORY_WITH_POSTS);
        matcher.addURI(authority, FmiContract.PATH_POST + "/*", POST);

        return matcher;
    }

    @Override
    public boolean onCreate() {
        fmiDbHelper = new FmiDbHelper(getContext());
        return true;
    }

    @Nullable
    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {

        Cursor cursor;
        switch (uriMatcher.match(uri)) {
            case CATEGORY:
                cursor = getCategories(selectionArgs);
                break;
            case CATEGORY_WITH_POSTS:
                cursor = getCategoryWithPosts(uri, selectionArgs);
                break;
            case POST:
                cursor = getPost(uri, selectionArgs);
                break;
            default:
                throw new UnsupportedOperationException("Unknown uri " + uri);
        }
        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    private Cursor getPost(Uri uri, String[] selectionArgs) {
        String postId = uri.getPathSegments().get(1);
        String sql = "SELECT p1." + PostEntry._ID + ", t1." + TranslationEntry.COLUMN_VALUE +
                " AS " + PostEntry.COLUMN_TITLE_STRING_KEY + ", (SELECT t2." +
                TranslationEntry.COLUMN_VALUE + " FROM " + PostEntry.TABLE_NAME + " p2 JOIN " +
                TranslationEntry.TABLE_NAME + " t2 ON p2." + PostEntry.COLUMN_CONTENT_STRING_KEY +
                " = t2." + TranslationEntry.COLUMN_STRING_KEY + " WHERE p2." + PostEntry._ID +
                " = p1." + PostEntry._ID + " AND t1." + TranslationEntry.COLUMN_LOCALE + " = t2." +
                TranslationEntry.COLUMN_LOCALE + " LIMIT 1) AS " + PostEntry.COLUMN_CONTENT_STRING_KEY +
                " FROM " + PostEntry.TABLE_NAME + " p1 JOIN " + TranslationEntry.TABLE_NAME +
                " t1 ON p1." + PostEntry.COLUMN_TITLE_STRING_KEY + " = t1." +
                TranslationEntry.COLUMN_STRING_KEY + " WHERE p1." + PostEntry._ID + " = ? AND t1." +
                TranslationEntry.COLUMN_LOCALE + " = ?";

        return fmiDbHelper.getReadableDatabase().rawQuery(sql,
                new String[] {postId, selectionArgs[0]});
    }

    private Cursor getCategoryWithPosts(Uri uri, String[] selectionArgs) {
        String categoryId = uri.getPathSegments().get(1);
        String sql = "SELECT p1." + PostEntry._ID + ", t1." + TranslationEntry.COLUMN_VALUE +
                " AS " + PostEntry.COLUMN_TITLE_STRING_KEY + " FROM " + PostEntry.TABLE_NAME +
                " p1 JOIN " + CategoryEntry.TABLE_NAME + " c1 ON p1." + PostEntry.COLUMN_CATEGORY_KEY +
                " = c1." + CategoryEntry._ID + " JOIN " + TranslationEntry.TABLE_NAME + " t1 ON p1." +
                PostEntry.COLUMN_TITLE_STRING_KEY + " = t1." + TranslationEntry.COLUMN_STRING_KEY +
                " WHERE c1." + CategoryEntry._ID + " = ? AND t1." + TranslationEntry.COLUMN_LOCALE + " = ?";

        return fmiDbHelper.getReadableDatabase().rawQuery(sql,
                new String[] {categoryId, selectionArgs[0]});
    }

    private Cursor getCategories(String[] selectionArgs) {

        String sql = "SELECT c1." + CategoryEntry._ID + ", t1." + TranslationEntry.COLUMN_VALUE +
                " AS " + CategoryEntry.COLUMN_NAME_STRING_KEY + " FROM " + CategoryEntry.TABLE_NAME +
                " c1 JOIN " + TranslationEntry.TABLE_NAME + " t1 ON c1." +
                CategoryEntry.COLUMN_NAME_STRING_KEY + " = t1." + TranslationEntry.COLUMN_STRING_KEY +
                " WHERE t1." + TranslationEntry.COLUMN_LOCALE + " = ?" ;

        return fmiDbHelper.getReadableDatabase().rawQuery(sql, selectionArgs);
    }

    @Nullable
    @Override
    public String getType(Uri uri) {

        final int match = uriMatcher.match(uri);

        switch (match) {
            case CATEGORY:
            case CATEGORY_WITH_POSTS:
                return  CategoryEntry.CONTENT_TYPE;
            case POST:
                return PostEntry.CONTENT_ITEM_TYPE;
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
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }
}
