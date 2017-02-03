package pt.ulusofona.copelabs.ndn.android.ui;

import java.util.List;

import android.app.Activity;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.Peer;
import pt.ulusofona.copelabs.ndn.android.PitEntry;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;
import pt.ulusofona.copelabs.ndn.android.service.Routing;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Refreshable;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Status;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class Overview extends Refreshable {
	private Status mStatus;
    private Table<Peer> mPeerList;
	private Table<Face> mFacetable;
	private Table<PitEntry> mPit;

	public Overview(Activity act, Table<Peer> pl, Table<Face> ft) {
        mStatus = new Status();

        mPeerList = pl;
		mFacetable = ft;
		mPit = new Table<>(R.string.pit, new PitEntry.Adapter(act), new Table.EntryProvider<PitEntry>() {
			@Override
			public List<PitEntry> getEntries() {
				return ForwardingDaemon.getPit();
			}
		});
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View contentStore = inflater.inflate(R.layout.fragment_overview, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.status, mStatus)
            .replace(R.id.peerlist, mPeerList)
			.replace(R.id.facetable, mFacetable)
			.replace(R.id.pit, mPit)
			.commit();

		return contentStore;
	}

	@Override
	public void clear() {
		mStatus.clear(); mPeerList.clear(); mFacetable.clear(); mPit.clear();
	}

	@Override
	public void update() {
		mStatus.update(); mPeerList.update(); mFacetable.update(); mPit.update();
	}

	@Override
	public void refresh() {
		mStatus.refresh(); mPeerList.refresh(); mFacetable.refresh(); mPit.refresh();
	}
}