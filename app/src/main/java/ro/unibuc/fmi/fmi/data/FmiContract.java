package ro.unibuc.fmi.fmi.data;

import android.provider.BaseColumns;

/**
 * Created by alexandru on 27.03.2016
 */
public class FmiContract {
    public static final class CategoryEntry implements BaseColumns {
        public static final String TABLE_NAME = "categories";

        public static final String COLUMN_NAME_STRING_KEY = "name_string_key";
    }

    public static final class StringEntry implements BaseColumns {
        public static final String TABLE_NAME = "strings";
    }

    public static final class TranslationEntry implements BaseColumns {
        public static final String TABLE_NAME = "translations";

        public static final String COLUMN_STRING_KEY = "string_key";
        public static final String COLUMN_LOCALE = "locale";
        public static final String COLUMN_VALUE = "value";
    }

    public final static class PostEntry implements BaseColumns {
        public static final String TABLE_NAME = "posts";

        public static final String COLUMN_TITLE_STRING_KEY = "title_string_key";
        public static final String COLUMN_CONTENT_STRING_KEY = "content_string_key";
    }
}
