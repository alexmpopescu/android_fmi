package ro.unibuc.fmi.fmi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.widget.TextView;

import ro.unibuc.fmi.fmi.data.FmiContract;

public class PostViewActivity extends AppCompatActivity {

    public static final String POST_ID = "post_id";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_view);

        Intent intent = getIntent();
        if (intent.hasExtra(POST_ID)) {
            Log.d(getClass().getSimpleName(), "Get content for category " +
                    intent.getStringExtra(POST_ID));

            SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
            Cursor postCursor = this.getContentResolver().query
                    (FmiContract.PostEntry.CONTENT_URI.buildUpon().appendPath("translation").
                                    appendPath(intent.getStringExtra(POST_ID)).build(),
                            null,
                            FmiContract.TranslationEntry.COLUMN_LOCALE + " = ?",
                            new String[]{
                                    sharedPref.getString(this.getString(R.string.pref_language_key), "ro")
                            },
                            null);
            if (postCursor != null) {
                if (postCursor.moveToFirst()) {
                    String content = postCursor.getString(postCursor.getColumnIndex(
                            FmiContract.PostEntry.COLUMN_CONTENT_STRING_KEY));
                    ((TextView) findViewById(R.id.post_content)).setText(Html.fromHtml(content));
                }
                else
                    Log.d(getClass().getSimpleName(), "No records");
                postCursor.close();
            }
        }
    }
}
