/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * This class is in charge of managing a Table Fragment (TextView + ListView)
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;

import java.util.List;

import pt.ulusofona.copelabs.ndn.R;

public class Table<E extends Table.Entry> extends Fragment {
    public interface Entry {
        View getView(LayoutInflater infl);
        void setViewContents(View entry);
    }

    private static final String TAG = Table.class.getSimpleName();

    public static final String TITLE = "ResourceIdTitle";
    public static final String DEFAULT_VIEW = "ResourceIdViews";

    private int mResIdTitle;
    private int mResIdViews; // Default view to use.
    private EntryAdapter<E> mAdapter;

    public static Table newInstance(int titleId, int viewId) {
        Table fragment = new Table();
        Bundle args = new Bundle();
        args.putInt(TITLE, titleId);
        args.putInt(DEFAULT_VIEW, viewId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle args = getArguments();
        if(args != null) {
            mResIdTitle = args.getInt(TITLE);
            mResIdViews = args.getInt(DEFAULT_VIEW);
            mAdapter = new EntryAdapter<>(getContext(), mResIdViews);
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
		View table = inflater.inflate(R.layout.fragment_table, parent, false);

		((TextView) table.findViewById(R.id.title)).setText(mResIdTitle);
		((ListView) table.findViewById(R.id.contents)).setAdapter(mAdapter);

		return table;
	}

	public void clear() { if(mAdapter != null) mAdapter.clear(); }
	public void refresh(List<E> entries) { clear(); if (mAdapter != null) mAdapter.addAll(entries); }

    private class EntryAdapter<E extends Entry> extends ArrayAdapter<E> {
        private LayoutInflater mInflater;

        EntryAdapter(Context context, int resourceId) {
            super(context, resourceId);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override @NonNull
        public View getView(int i, View convertView, @NonNull ViewGroup viewGroup) {
            View entry;
            Entry data = getItem(i);

            if(convertView != null) entry = convertView;
            else entry = data.getView(mInflater);

            data.setViewContents(entry);

            return entry;
        }
    }
}