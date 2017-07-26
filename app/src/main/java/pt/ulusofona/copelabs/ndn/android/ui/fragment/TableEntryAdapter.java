/*
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017/7/26
 *
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

public class TableEntryAdapter<E extends Table.Entry> extends ArrayAdapter<E> {
    private LayoutInflater mInflater;

    public TableEntryAdapter(Context context, int resourceId) {
        super(context, resourceId);
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    @NonNull
    public View getView(int i, View convertView, @NonNull ViewGroup viewGroup) {
        View entry;
        Table.Entry data = getItem(i);

        if (convertView != null) entry = convertView;
        else entry = data.getView(mInflater);

        data.setViewContents(entry);

        return entry;
    }
}
