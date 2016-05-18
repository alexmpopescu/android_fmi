package ro.unibuc.fmi.fmi;

import android.app.LoaderManager;
import android.content.Context;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.design.widget.TabLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import ro.unibuc.fmi.fmi.data.FmiContract;
import ro.unibuc.fmi.fmi.sync.FmiSyncAdapter;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    static final int CATEGORY_LOADER = 1;
    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;
    private TabLayout tabLayout;
    private SwipeRefreshLayout swipeRefreshLayout;

    static Loader<Cursor> createCursor(Context context) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(context);

        return new CursorLoader(context,
                FmiContract.CategoryEntry.CONTENT_URI.buildUpon().appendPath("translation").build(),
                new String[] {
                        FmiContract.CategoryEntry.TABLE_NAME + "." + FmiContract.CategoryEntry._ID,
                        FmiContract.TranslationEntry.COLUMN_VALUE,
                },
                FmiContract.TranslationEntry.COLUMN_LOCALE + " = ?",
                new String[] {
                        sharedPref.getString(context.getString(R.string.pref_language_key), "ro")
                },
                null);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        swipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.activity_main_swipe_refresh_layout);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        mSectionsPagerAdapter = new SectionsPagerAdapter(this, getSupportFragmentManager(), null);

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        tabLayout = (TabLayout) findViewById(R.id.sliding_tabs);
        tabLayout.setupWithViewPager(mViewPager);

        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FmiSyncAdapter.syncImmediately(getBaseContext());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });

        FmiSyncAdapter.initializeSyncAdapter(this);

        getLoaderManager().initLoader(CATEGORY_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        return createCursor(this);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mSectionsPagerAdapter.swapCursor(data);
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mSectionsPagerAdapter.swapCursor(null);
    }

    public class SectionsPagerAdapter extends CursorFragmentPagerAdapter {

        public SectionsPagerAdapter(Context context, FragmentManager fm, Cursor cursor) {
            super(context, fm, cursor);
        }

        @Override
        protected CharSequence getPageTitle(Context mContext, Cursor mCursor) {
            String categoryTitle = mCursor.getString(mCursor.getColumnIndex(FmiContract.TranslationEntry.COLUMN_VALUE));
            Log.d(this.getClass().getSimpleName(), "Returning title for category " + categoryTitle);
            return categoryTitle;
        }

        @Override
        public Fragment getItem(Context context, Cursor cursor) {
            String categoryId = cursor.getString(cursor.getColumnIndex(FmiContract.CategoryEntry.TABLE_NAME + "." + FmiContract.CategoryEntry._ID));
            Log.d(this.getClass().getSimpleName(), "Returning fragment for category "+categoryId);
            return PlaceholderFragment.newInstance(categoryId);
        }
    }
}


