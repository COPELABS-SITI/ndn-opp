/*
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-mm-dd
 *
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
import pt.ulusofona.copelabs.ndn.android.models.FibEntry;
import pt.ulusofona.copelabs.ndn.android.models.SctEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;

/** Fragment used to display the Forwarder configuration (FIB + SCT) of the running daemon. */
public class ForwarderConfiguration extends Fragment implements Refreshable {
	private Table<FibEntry> mFib = Table.newInstance(R.string.fib, R.layout.item_cell_two);
	private Table<SctEntry> mSct = Table.newInstance(R.string.sct, R.layout.item_cell_two);

	/** Fragment lifecycle method. See https://developer.android.com/guide/components/fragments.html
	 * @param inflater Android-provided layout inflater
	 * @param parent parent View within the hierarchy
	 * @param savedInstanceState previously saved state of the View instance
	 * @return the View to be used
	 */
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View fwdConfig = inflater.inflate(R.layout.fragment_ndn_table2, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.table1, mFib)
			.replace(R.id.table2, mSct)
			.commit();

		return fwdConfig;
	}

	/** Obtain the title to be displayed for these tables
	 * @return the title to be displayed
	 */
    @Override
    public int getTitle() {
        return R.string.forwarderConfiguration;
    }

	/** Performs a refresh of the contents of the enclosed tables
	 * @param daemon Binder to the ForwardingDaemon used to retrieve the new entries to update this View with
	 */
	@Override
	public void refresh(@NonNull OpportunisticDaemon.Binder daemon) {
		mSct.refresh(daemon.getStrategyChoiceTable());
		mFib.refresh(daemon.getForwardingInformationBase());
	}

	/** Clear the contents of the enclosed tables */
	@Override
	public void clear() {
		mSct.clear();
		mFib.clear();
	}
}