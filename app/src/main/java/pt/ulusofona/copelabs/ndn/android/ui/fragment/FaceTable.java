/*
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-mm-dd
 * Fragment for displaying the FaceTable
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
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;

/** Fragment used to display the FaceTable of the running daemon. */
public class FaceTable extends Fragment implements Refreshable {
	private Table<Face> mFacetable = Table.newInstance(R.string.facetable, R.layout.item_face);

	/** Fragment lifecycle method. See https://developer.android.com/guide/components/fragments.html
	 * @param inflater Android-provided layout inflater
	 * @param parent parent View within the hierarchy
	 * @param savedInstanceState previously saved state of the View instance
	 * @return the View to be used
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View fwdConfig = inflater.inflate(R.layout.fragment_ndn_table, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.table, mFacetable)
			.commit();

		return fwdConfig;
	}

	/** Obtain the title to be displayed for this table
	 * @return the title to be displayed
	 */
	@Override
    public int getTitle() {
        return R.string.facetable;
    }

	/** Performs a refresh of the contents of the enclosed table
	 * @param daemon Binder to the ForwardingDaemon used to retrieve the new entries to update this View with
	 */
	@Override
	public void refresh(@NonNull OpportunisticDaemon.Binder daemon) {
		mFacetable.refresh(daemon.getFaceTable());
	}

	/** Clear the contents of the enclosed table */
	@Override
	public void clear() {
		mFacetable.clear();
	}
}