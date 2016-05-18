package ro.unibuc.fmi.fmi;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import ro.unibuc.fmi.fmi.data.FmiContract;

/**
 * Created by alexandru on 18.05.2016
 */
public class NewsAdapter extends CursorAdapter {

    public static class ViewHolder {
        TextView title;

        public ViewHolder(View view) {
            this.title = (TextView) view.findViewById(R.id.post_title);
        }
    }

    public NewsAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View view = LayoutInflater.from(context).inflate(R.layout.fragment_post, parent, false);
        ViewHolder viewHolder = new ViewHolder(view);
        view.setTag(viewHolder);
        return view;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        ViewHolder viewHolder = (ViewHolder) view.getTag();

        viewHolder.title.setText(cursor.getString(cursor.getColumnIndex(FmiContract.TranslationEntry.COLUMN_VALUE)));
    }
}
