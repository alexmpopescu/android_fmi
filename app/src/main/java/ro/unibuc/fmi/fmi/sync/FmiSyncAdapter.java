package ro.unibuc.fmi.fmi.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.Vector;

import ro.unibuc.fmi.fmi.BuildConfig;
import ro.unibuc.fmi.fmi.R;
import ro.unibuc.fmi.fmi.data.FmiContract;

/**
 * Created by alexandru on 09.04.2016
 */
public class FmiSyncAdapter extends AbstractThreadedSyncAdapter {

    private static final int SYNC_INTERVAL = 60 * 60 * 3;
    private static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

    public FmiSyncAdapter(Context context, boolean autoInitialize) {
        super(context, autoInitialize);
    }

    @Override
    public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
        Log.d(this.getClass().getSimpleName(), "Start syncing");
        try {
            parseCategories(performHttpRequest(new URL("http://" + BuildConfig.FMI_SERVER_ADDR + "/api/categories")));
            // TODO: download and parse posts
            Log.d(this.getClass().getSimpleName(), "Sync successful");
        } catch (MalformedURLException e) {
            e.printStackTrace();
            Log.e(this.getClass().getSimpleName(), "Sync error");
        } catch (Exception e) {
            Log.e(this.getClass().getSimpleName(), "An error occurred");
        }
    }

    private void parseCategories(String categoriesJsonStr) {
        try {
            JSONArray baseArray = new JSONArray(categoriesJsonStr);
            Vector<ContentValues> translationContentValuesVector = new Vector<>();
            Vector<ContentValues> categoryContentValuesVector = new Vector<>();
            for (int i = 0; i < baseArray.length(); i++)
            {
                JSONObject baseElement = baseArray.getJSONObject(i);
                JSONObject title = baseElement.getJSONObject("title");
                String _id = baseElement.getString("_id");
                int stringKey;

                Cursor categoryCursor = getContext().getContentResolver().query(
                        FmiContract.CategoryEntry.CONTENT_URI,
                        new String[]{FmiContract.CategoryEntry.COLUMN_NAME_STRING_KEY},
                        FmiContract.CategoryEntry._ID + " = ?",
                        new String[]{_id},
                        null);

                if (categoryCursor.moveToFirst()) {
                    stringKey = categoryCursor.getInt(categoryCursor.getColumnIndex(
                            FmiContract.CategoryEntry.COLUMN_NAME_STRING_KEY));
                } else {
                    /* A small hack:
                     * this ContentValues is required to provide a null when inserting into
                     * strings table as this table has only one auto-incremented column, _id */
                    ContentValues stringContentValues = new ContentValues();
                    stringContentValues.put(FmiContract.StringEntry._ID, (Integer)null);
                    Uri newString = getContext().getContentResolver().insert(
                            FmiContract.StringEntry.CONTENT_URI, stringContentValues);

                    stringKey = Integer.parseInt(newString.getPathSegments().get(1));
                    ContentValues newCategoryValues = new ContentValues();
                    newCategoryValues.put(FmiContract.CategoryEntry.COLUMN_NAME_STRING_KEY, stringKey);
                    newCategoryValues.put(FmiContract.CategoryEntry._ID, _id);
                    categoryContentValuesVector.add(newCategoryValues);
                }

                categoryCursor.close();

                Iterator<String> keys = title.keys();

                while (keys.hasNext()) {
                    String key = keys.next();
                    Log.d(this.getClass().toString(), "adding locale " + key);
                    ContentValues contentValues = new ContentValues();
                    contentValues.put(FmiContract.TranslationEntry.COLUMN_STRING_KEY, stringKey);
                    contentValues.put(FmiContract.TranslationEntry.COLUMN_LOCALE,
                            key);
                    contentValues.put(FmiContract.TranslationEntry.COLUMN_VALUE,
                            title.getString(key));
                    translationContentValuesVector.add(contentValues);
                }
            }

            ContentValues[] translationContentValuesArray = new ContentValues[translationContentValuesVector.size()];
            translationContentValuesVector.toArray(translationContentValuesArray);
            getContext().getContentResolver().bulkInsert(FmiContract.TranslationEntry.CONTENT_URI,
                    translationContentValuesArray);

            ContentValues[] categoryContentValuesArray = new ContentValues[categoryContentValuesVector.size()];
            categoryContentValuesVector.toArray(categoryContentValuesArray);
            getContext().getContentResolver().bulkInsert(FmiContract.CategoryEntry.CONTENT_URI,
                    categoryContentValuesArray);

        } catch (JSONException e) {
            e.printStackTrace();
        }

    }

    // TODO: add method for parsing posts

    private String performHttpRequest(URL url) throws ConnectException {

        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;

        String returnJsonStr = null;

        try {
            //URL url = new URL("http://"+host+"/api/categories");

            // Create the request to OpenWeatherMap, and open the connection
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.connect();

            // Read the input stream into a String
            InputStream inputStream = urlConnection.getInputStream();
            StringBuffer buffer = new StringBuffer();
            if (inputStream == null) {
                // Nothing to do.
                return null;
            }
            reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                // But it does make debugging a *lot* easier if you print out the completed
                // buffer for debugging.
                buffer.append(line + "\n");
            }

            if (buffer.length() == 0) {
                // Stream was empty.  No point in parsing.
                return null;
            }
            returnJsonStr = buffer.toString();
        } catch (IOException e) {
            Log.e(this.getClass().getName(), "Error ", e);
            return null;
        } finally{
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
            if (reader != null) {
                try {
                    reader.close();
                } catch (final IOException e) {
                    Log.e(this.getClass().getName(), "Error closing stream", e);
                }
            }
        }

        return returnJsonStr;
    }

    public static Account getSyncAccount(Context context) {
        AccountManager accountManager =
                (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);
        Account newAccount = new Account(context.getString(R.string.app_name),
                context.getString(R.string.account_type));

        if (null == accountManager.getPassword(newAccount)) {
            if (!accountManager.addAccountExplicitly(newAccount, "", null))
                return null;
            onAccountCreated(newAccount, context);
        }
        return newAccount;
    }

    private static void onAccountCreated(Account newAccount, Context context) {
        FmiSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
        ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
        syncImmediately(context);
    }

    public static void syncImmediately(Context context) {
        Bundle bundle = new Bundle();
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
        bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
        ContentResolver.requestSync(getSyncAccount(context),
                context.getString(R.string.content_authority), bundle);
    }

    private static void configurePeriodicSync(Context context, int syncInterval, int syncFlextime) {
        Account account = getSyncAccount(context);
        String authority = context.getString(R.string.content_authority);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            SyncRequest request = new SyncRequest.Builder().
                    syncPeriodic(syncInterval, syncFlextime).
                    setSyncAdapter(account, authority).
                    setExtras(new Bundle()).build();
            ContentResolver.requestSync(request);
        } else {
            ContentResolver.addPeriodicSync(account,
                    authority, new Bundle(), syncInterval);
        }
    }

    public static void initializeSyncAdapter(Context context) {
        getSyncAccount(context);
    }
}
