package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import java.util.List;

import pt.ulusofona.copelabs.ndn.R;

public class Table<E> extends Refreshable {
	public interface EntryProvider<E> {
		List<E> getEntries();
	}

	private View table;

	private ArrayAdapter<E> mAdapter;
	private EntryProvider<E> mProvider;
	private int mResIdTitle;

	public Table(int rid, ArrayAdapter<E> a, EntryProvider<E> p) {
		mResIdTitle = rid;
		mAdapter = a;
		mProvider = p;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		table = inflater.inflate(R.layout.fragment_table, parent, false);

		((TextView) table.findViewById(R.id.title)).setText(mResIdTitle);
		((ListView) table.findViewById(R.id.contents)).setAdapter(mAdapter);

		return table;
	}

	public void setTitle(String t) {
		((TextView) table.findViewById(R.id.title)).setText(t);
	}

	@Override
	public void clear() {
		mAdapter.clear();
	}

	@Override
	public void update() {
		List<E> entries = mProvider.getEntries();
		if(entries != null)
			mAdapter.addAll(entries);
	}

	@Override
	public void refresh() {
		clear();
		update();
	}
}