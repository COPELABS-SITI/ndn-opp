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
	private Table<Name> mNameTree = Table.newInstance(R.string.nametree, R.layout.item_name);

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
			.replace(R.id.table, mNameTree)
			.commit();

		return fwdConfig;
	}

	/** Obtain the title to be displayed for this table
	 * @return the title to be displayed
	 */
    @Override
    public int getTitle() {
        return R.string.nametree;
    }

	/** Performs a refresh of the contents of the enclosed table
	 * @param daemon Binder to the ForwardingDaemon used to retrieve the new entries to update this View with
	 */
	@Override
	public void refresh(@NonNull ForwardingDaemon.DaemonBinder daemon) {
        List<Name> names = daemon.getNameTree();
        Collections.sort(names);
		mNameTree.refresh(names);
	}

	/** Clear the contents of the enclosed table */
	@Override
	public void clear() {
		mNameTree.clear();
	}
}