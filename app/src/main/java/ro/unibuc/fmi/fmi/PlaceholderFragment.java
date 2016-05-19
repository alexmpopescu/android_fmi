package ro.unibuc.fmi.fmi;

import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;

import ro.unibuc.fmi.fmi.data.FmiContract;
import ro.unibuc.fmi.fmi.sync.FmiSyncAdapter;

/**
 * A placeholder fragment containing a simple view.
 */
public class PlaceholderFragment extends Fragment implements android.support.v4.app.LoaderManager.LoaderCallbacks<Cursor> {
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    private static final String ARG_CATEGORY_ID = "category_id";
    private NewsAdapter mNewsAdapter;
    private String categoryId;
    private SwipeRefreshLayout swipeRefreshLayout;

    public static int hash(String s) {
        int h = 0;
        for (int i = 0; i < s.length(); i++) {
            h = 2 * h + s.charAt(i);
        }
        return h;
    }

    public PlaceholderFragment() {
    }

    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static PlaceholderFragment newInstance(String categoryId) {
        PlaceholderFragment fragment = new PlaceholderFragment();
        Bundle args = new Bundle();
        args.putString(ARG_CATEGORY_ID, categoryId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        swipeRefreshLayout = (SwipeRefreshLayout) rootView.findViewById(R.id.activity_main_swipe_refresh_layout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                FmiSyncAdapter.syncImmediately(getContext());
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        swipeRefreshLayout.setRefreshing(false);
                    }
                }, 3000);
            }
        });;
        GridView gridView = (GridView) rootView.findViewById(R.id.news_grid);
        mNewsAdapter = new NewsAdapter(getActivity(), null, 0);
        gridView.setAdapter(mNewsAdapter);
        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor categoryCursor = (Cursor)parent.getItemAtPosition(position);
                String post_id = categoryCursor.getString(categoryCursor.getColumnIndex(
                        FmiContract.PostEntry.TABLE_NAME + "." +
                                FmiContract.PostEntry._ID));

                Intent intent = new Intent(getActivity(), PostViewActivity.class);
                intent.putExtra(PostViewActivity.POST_ID, post_id);
                startActivity(intent);
            }
        });
        categoryId = getArguments().getString(ARG_CATEGORY_ID);
        Log.d(getClass().getSimpleName(), "CreateView fot category " + categoryId);
        getLoaderManager().initLoader(hash(getArguments().getString(ARG_CATEGORY_ID)), null, this);
        return rootView;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());

        return new CursorLoader(getActivity(),
                FmiContract.PostEntry.CONTENT_URI.buildUpon().appendPath("translation").build(),
                new String[] {
                        FmiContract.PostEntry.TABLE_NAME + "." + FmiContract.PostEntry._ID,
                        FmiContract.TranslationEntry.COLUMN_VALUE
                },
                FmiContract.TranslationEntry.COLUMN_LOCALE + " = ?" + " AND "+
                FmiContract.CategoryEntry.TABLE_NAME + "." + FmiContract.CategoryEntry._ID + " = ?",
                new String[] {
                        sharedPref.getString(getActivity().getString(R.string.pref_language_key), "ro"),
                        categoryId
                },
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mNewsAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mNewsAdapter.swapCursor(null);
    }
}
