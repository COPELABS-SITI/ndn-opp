package pt.ulusofona.copelabs.ndn.android.ui;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.FibEntry;
import pt.ulusofona.copelabs.ndn.android.SctEntry;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Refreshable;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class ForwarderConfiguration extends Refreshable {
	private Table<Face> mFacetable;
	private Table<FibEntry> mFib;
	private Table<SctEntry> mSct;

	public ForwarderConfiguration(Activity act, Table<Face> ft) {
		mFacetable = ft;

		mFib = new Table<FibEntry>(R.string.fib, new FibEntry.Adapter(act), new Table.EntryProvider<FibEntry>() {
			@Override
			public List<FibEntry> getEntries() {
				return ForwardingDaemon.getFib();
			}
		});

		mSct = new Table<SctEntry>(R.string.sct, new SctEntry.Adapter(act), new Table.EntryProvider<SctEntry>() {
			@Override
			public List<SctEntry> getEntries() {
				return ForwardingDaemon.getStrategies();
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View contentStore = inflater.inflate(R.layout.fragment_forwarder_configuration, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.facetable, mFacetable)
			.replace(R.id.fib, mFib)
			.replace(R.id.sct, mSct)
			.commit();

		return contentStore;
	}

	@Override
	public void clear() {
		mFacetable.clear(); mFib.clear(); mSct.clear();
	}

	@Override
	public void update() {
		mFacetable.update(); mFib.update(); mSct.update();
	}

	@Override
	public void refresh() {
		mFacetable.refresh(); mFib.refresh(); mSct.refresh();
	}
}