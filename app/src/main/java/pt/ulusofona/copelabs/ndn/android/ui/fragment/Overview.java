/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * This class manages the Fragment which displays the Status, FaceTable and PIT of the ForwardingDaemon.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.models.PitEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public class Overview extends Fragment implements Refreshable {
    private Status mStatus;
	private Table<Face> mFacetable;
	private Table<PitEntry> mPit;

    public Overview() {
        mStatus = new Status();
        mFacetable = Table.newInstance(R.string.facetable, R.layout.item_face);
        mPit = Table.newInstance(R.string.pit, R.layout.item_pit_entry);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View overview = inflater.inflate(R.layout.fragment_overview, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.status, mStatus)
			.replace(R.id.facetable, mFacetable)
			.replace(R.id.pit, mPit)
			.commit();

		return overview;
	}

    @Override
    public int getTitle() {
        return R.string.overview;
    }

    @Override
	public void refresh(@NonNull ForwardingDaemon daemon) {
		mStatus.refresh(daemon);
		mFacetable.refresh(daemon.getFaceTable());
		mPit.refresh(daemon.getPendingInterestTable());
	}
}