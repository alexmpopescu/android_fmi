package ro.unibuc.fmi.fmi;

import android.content.Context;
import android.database.Cursor;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.util.Log;
import android.view.ViewGroup;

import java.util.HashMap;

public abstract class CursorFragmentPagerAdapter extends FragmentStatePagerAdapter {

    protected boolean mDataValid;
    protected Cursor mCursor;
    protected Context mContext;
    protected HashMap<String, Integer> mItemPositions;
    protected HashMap<Object, String> mObjectMap;
    protected int mRowIDColumn;
    protected FragmentManager fragmentManager;

    public CursorFragmentPagerAdapter(Context context, FragmentManager fm, Cursor cursor) {
        super(fm);
        fragmentManager = fm;
        init(context, cursor);
    }

    void init(Context context, Cursor c) {
        mObjectMap = new HashMap<Object, String>();
        boolean cursorPresent = c != null;
        mCursor = c;
        mDataValid = cursorPresent;
        mContext = context;
        mRowIDColumn = cursorPresent ? c.getColumnIndexOrThrow("_id") : -1;
    }

    public Cursor getCursor() {
        return mCursor;
    }

    @Override
    public int getItemPosition(Object object) {
        String rowId = mObjectMap.get(object);
        if (rowId != null && mItemPositions != null) {
            return mItemPositions.containsKey(rowId) ? mItemPositions.get(rowId) : POSITION_NONE;
        }
        return POSITION_NONE;
    }

    public void setItemPositions() {
        mItemPositions = null;

        if (mDataValid) {
            int count = mCursor.getCount();
            mItemPositions = new HashMap<>(count);
            mCursor.moveToPosition(-1);
            while (mCursor.moveToNext()) {
                String rowId = mCursor.getString(mRowIDColumn);
                int cursorPos = mCursor.getPosition();
                mItemPositions.put(rowId, cursorPos);
            }
        }
    }

    @Override
    public Fragment getItem(int position) {
        if (mDataValid) {
            mCursor.moveToPosition(position);
            return getItem(mContext, mCursor);
        } else {
            return null;
        }
    }


    @Override
    public CharSequence getPageTitle(int position) {
        if (mDataValid) {
            mCursor.moveToPosition(position);
            return getPageTitle(mContext, mCursor);
        } else {
            return null;
        }
    }

    protected abstract CharSequence getPageTitle(Context mContext, Cursor mCursor);

    @Override
    public void destroyItem(ViewGroup container, int position, Object object) {
        mObjectMap.remove(object);

        super.destroyItem(container, position, object);
    }

    @Override
    public Object instantiateItem(ViewGroup container, int position) {
        if (!mDataValid) {
            throw new IllegalStateException("this should only be called when the cursor is valid");
        }
        if (!mCursor.moveToPosition(position)) {
            throw new IllegalStateException("couldn't move cursor to position " + position);
        }

        String rowId = mCursor.getString(mRowIDColumn);
        Object obj = super.instantiateItem(container, position);
        mObjectMap.put(obj, rowId);

        return obj;
    }

    public abstract Fragment getItem(Context context, Cursor cursor);

    @Override
    public int getCount() {
        if (mDataValid) {
            return mCursor.getCount();
        } else {
            return 0;
        }
    }

    public Cursor swapCursor(Cursor newCursor) {
        if (newCursor == mCursor) {
            return null;
        }
        Cursor oldCursor = mCursor;
        mCursor = newCursor;
        if (newCursor != null) {
            mRowIDColumn = newCursor.getColumnIndexOrThrow("_id");
            mDataValid = true;
        } else {
            mRowIDColumn = -1;
            mDataValid = false;
        }

        setItemPositions();
        if (newCursor != null)
            notifyDataSetChanged();

        return oldCursor;
    }
}