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
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public class FaceTable extends Fragment implements Refreshable {
	private Table<Face> mFacetable;

    public FaceTable() {
        mFacetable = Table.newInstance(R.string.facetable, R.layout.item_face);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View fwdConfig = inflater.inflate(R.layout.fragment_ndn_table, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.table, mFacetable)
			.commit();

		return fwdConfig;
	}

    @Override
    public int getTitle() {
        return R.string.facetable;
    }

    @Override
	public void refresh(@NonNull ForwardingDaemon daemon) {
		mFacetable.refresh(daemon.getFaceTable());
	}

	@Override
	public void clear() {
		mFacetable.clear();
	}
}