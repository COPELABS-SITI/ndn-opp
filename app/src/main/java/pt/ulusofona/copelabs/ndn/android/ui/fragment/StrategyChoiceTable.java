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
import pt.ulusofona.copelabs.ndn.android.models.SctEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public class StrategyChoiceTable extends Fragment implements Refreshable {
	private Table<SctEntry> mSct;

    public StrategyChoiceTable() {
        mSct = Table.newInstance(R.string.sct, R.layout.item_cell_two);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View fwdConfig = inflater.inflate(R.layout.fragment_ndn_table, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.table, mSct)
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
	}

	@Override
	public void clear() {
		mSct.clear();
	}
}