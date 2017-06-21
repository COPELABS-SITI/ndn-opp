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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.Collections;
import java.util.List;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.models.Name;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

/** Fragment used to display the Name Tree of the running daemon. */
public class NameTree extends Fragment implements Refreshable {
	private Table<Name> mNameTree;

    public NameTree() {
		mNameTree = Table.newInstance(R.string.nametree, R.layout.item_name);
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View fwdConfig = inflater.inflate(R.layout.fragment_ndn_table, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.table, mNameTree)
			.commit();

		return fwdConfig;
	}

    @Override
    public int getTitle() {
        return R.string.nametree;
    }

    @Override
	public void refresh(@NonNull ForwardingDaemon daemon) {
        List<Name> names = daemon.getNameTree();
        Collections.sort(names);
		mNameTree.refresh(names);
	}

	@Override
	public void clear() {
		mNameTree.clear();
	}
}