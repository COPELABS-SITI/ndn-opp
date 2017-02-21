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
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.CsEntry;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public class ContentStore extends Fragment implements Refreshable {
	private Table<CsEntry> mContentStore;

	public ContentStore() {
		mContentStore = Table.newInstance(R.string.contentstore, R.layout.item_cell_two);
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View fwdConfig = inflater.inflate(R.layout.fragment_content_store, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.contentstore, mContentStore)
			.commit();

		return fwdConfig;
	}

	@Override
	public int getTitle() {
		return R.string.contentstore;
	}

	@Override
	public void refresh(@NonNull ForwardingDaemon daemon) {
		mContentStore.refresh(daemon.getContentStore());
	}
}