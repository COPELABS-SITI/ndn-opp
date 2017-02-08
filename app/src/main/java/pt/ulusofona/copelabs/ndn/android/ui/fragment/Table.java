package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.Context;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.ListView;

import java.util.List;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Entry;

public class Table<E extends Entry> extends Fragment {
    private static final String TAG = Table.class.getSimpleName();

    public static final String TITLE = "ResourceIdTitle";
    public static final String DEFAULT_VIEW = "ResourceIdViews";

    private int mResIdTitle;
    private int mResIdViews; // Default view to use.
    private EntryAdapter<E> mAdapter;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            Bundle args = getArguments();
            if(args != null) {
                mResIdTitle = args.getInt(TITLE);
                mResIdViews = args.getInt(DEFAULT_VIEW);
                mAdapter = new EntryAdapter<>(getContext(), mResIdViews);
            } else
                Log.d(TAG, "Empty argument bundle passed.");
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

        @Override
        public View getView(int i, View convertView, ViewGroup viewGroup) {
            View entry;
            Entry data = getItem(i);

            if(convertView != null) entry = convertView;
            else entry = data.getView(mInflater);

            data.setViewContents(entry);

            return entry;
        }
    }
}