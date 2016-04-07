package ro.unibuc.fmi.fmi.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by alexandru on 27.03.2016
 */
public class FmiContract {

    public static final String CONTENT_AUTHORITY = "ro.unibuc.fmi.fmi";
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_CATEGORY = "category";
    public static final String PATH_POST = "post";
    public static final String PATH_STRING = "string";
    public static final String PATH_TRANSLATION = "translation";

    public static final class CategoryEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_CATEGORY).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_CATEGORY;

        public static final String TABLE_NAME = "categories";

        public static final String COLUMN_NAME_STRING_KEY = "name_string_key";
    }

    public static final class StringEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_STRING).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STRING;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_STRING;

        public static final String TABLE_NAME = "strings";
    }

    public static final class TranslationEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_TRANSLATION).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSLATION;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_TRANSLATION;

        public static final String TABLE_NAME = "translations";

        public static final String COLUMN_STRING_KEY = "string_key";
        public static final String COLUMN_LOCALE = "locale";
        public static final String COLUMN_VALUE = "value";
    }

    public final static class PostEntry implements BaseColumns {

        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_POST).build();

        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_POST;

        public static final String TABLE_NAME = "posts";

        public static final String COLUMN_TITLE_STRING_KEY = "title_string_key";
        public static final String COLUMN_CONTENT_STRING_KEY = "content_string_key";
        public static final String COLUMN_CATEGORY_KEY = "category_key";
    }
}
