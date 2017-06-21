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
import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.models.FibEntry;
import pt.ulusofona.copelabs.ndn.android.models.SctEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

/** Fragment used to display the Forwarder configuration (FIB + SCT) of the running daemon. */
public class ForwarderConfiguration extends Fragment implements Refreshable {
	private Table<FibEntry> mFib;
	private Table<SctEntry> mSct;

    public ForwarderConfiguration() {
        mFib = Table.newInstance(R.string.fib, R.layout.item_cell_two);
		mSct = Table.newInstance(R.string.sct, R.layout.item_cell_two);
    }

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

    @Override
    public int getTitle() {
        return R.string.forwarderConfiguration;
    }

    @Override
	public void refresh(@NonNull ForwardingDaemon daemon) {
		mSct.refresh(daemon.getStrategyChoiceTable());
		mFib.refresh(daemon.getForwardingInformationBase());
	}

	@Override
	public void clear() {
		mSct.clear();
		mFib.clear();
	}
}