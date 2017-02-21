/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * This class manages the Fragment which displays the FaceTable, FIB & Strategy Choice Table of
 * the ForwardingDaemon.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.FibEntry;
import pt.ulusofona.copelabs.ndn.android.SctEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public class ForwarderConfiguration extends Fragment implements Refreshable {
	private Table<Face> mFacetable;
	private Table<FibEntry> mFib;
	private Table<SctEntry> mSct;

    public ForwarderConfiguration() {
        mFacetable = Table.newInstance(R.string.facetable, R.layout.item_face);
        mFib = Table.newInstance(R.string.fib, R.layout.item_cell_two);
        mSct = Table.newInstance(R.string.sct, R.layout.item_cell_two);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View fwdConfig = inflater.inflate(R.layout.fragment_forwarder_configuration, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.facetable, mFacetable)
			.replace(R.id.fib, mFib)
			.replace(R.id.sct, mSct)
			.commit();

		return fwdConfig;
	}

    @Override
    public int getTitle() {
        return R.string.forwarderConfiguration;
    }

    @Override
	public void refresh(@NonNull ForwardingDaemon daemon) {
		mFacetable.refresh(daemon.getFaceTable());
		mFib.refresh(daemon.getForwardingInformationBase());
		mSct.refresh(daemon.getStrategyChoiceTable());
	}
}