/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * This class manages the Fragment which displays the Status, FaceTable and PIT of the ForwardingDaemon.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui;

import java.util.List;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.PitEntry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Status;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class Overview extends Fragment {
    private Status mStatus;
	private Table<Face> mFacetable;
	private Table<PitEntry> mPit;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            mStatus = new Status();

            mFacetable = new Table<>();
            mFacetable.setArguments(Face.TABLE_ARGUMENTS);

            mPit = new Table<>();
            mPit.setArguments(PitEntry.TABLE_ARGUMENTS);
        }
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

	public void clear() {
        if(mStatus != null) mStatus.clear();
        if(mFacetable != null) mFacetable.clear();
        if(mPit != null) mPit.clear();
	}

	public void refresh(String version, long uptimeInMilliseconds, List<Face> faces, List<PitEntry> pit) {
        mStatus.refresh(version, uptimeInMilliseconds);
        mFacetable.refresh(faces);
        mPit.refresh(pit);
	}
}