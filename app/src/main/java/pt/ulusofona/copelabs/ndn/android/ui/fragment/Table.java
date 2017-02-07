package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import android.widget.TextView;
import android.widget.ListView;
import android.widget.ArrayAdapter;

import java.util.List;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public class Table<E> extends Refreshable {
	public interface EntryProvider<E> {
		List<E> getEntries(ForwardingDaemon fd);
	}

	private View table;

	private int mResIdTitle;
	private ArrayAdapter<E> mAdapter;
	private EntryProvider<E> mProvider;

	public Table(int rid, ArrayAdapter<E> aa, EntryProvider<E> ep) {
		mResIdTitle = rid;
		mAdapter = aa;
		mProvider = ep;
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        super.onCreateView(inflater, parent, savedInstanceState);
		table = inflater.inflate(R.layout.fragment_table, parent, false);

		((TextView) table.findViewById(R.id.title)).setText(mResIdTitle);
		((ListView) table.findViewById(R.id.contents)).setAdapter(mAdapter);

		return table;
	}

	public void clear() { mAdapter.clear(); }
	public void refresh(ForwardingDaemon fd) {
        clear();
        if(fd != null)
            mAdapter.addAll(mProvider.getEntries(fd));
    }
}