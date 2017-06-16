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
import pt.ulusofona.copelabs.ndn.android.models.PitEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public class PendingInterestTable extends Fragment implements Refreshable {
	private Table<PitEntry> mPit;

    public PendingInterestTable() {
        mPit = Table.newInstance(R.string.pit, R.layout.item_pit_entry);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View overview = inflater.inflate(R.layout.fragment_ndn_table, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.table, mPit)
			.commit();

		return overview;
	}

    @Override
    public int getTitle() {
        return R.string.overview;
    }

    @Override
	public void refresh(@NonNull ForwardingDaemon daemon) {
		mPit.refresh(daemon.getPendingInterestTable());
	}

	@Override
	public void clear() {
		mPit.clear();
	}
}