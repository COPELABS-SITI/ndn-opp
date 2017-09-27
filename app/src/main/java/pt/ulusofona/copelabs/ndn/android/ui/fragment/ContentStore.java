/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * This class manages the Fragment which displays the FaceTable, FIB & Strategy Choice Table of
 * the ForwardingDaemon.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.models.CsEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.databinding.FragmentTableBinding;
import pt.ulusofona.copelabs.ndn.databinding.ItemContentStoreEntryBinding;

/** Fragment used to display the ContentStore of the running daemon. */
public class ContentStore extends Fragment implements Refreshable {

	private FragmentTableBinding mBindingTable;
	private List<CsEntry> mContents = new ArrayList<>();
	private ContentStoreAdapter mAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mBindingTable = FragmentTableBinding.inflate(getActivity().getLayoutInflater());
		mBindingTable.title.setText(R.string.contentstore);
		mAdapter = new ContentStoreAdapter(getContext(), R.layout.item_face);
		mBindingTable.contents.setAdapter(mAdapter);
	}

	/** Fragment lifecycle method. See https://developer.android.com/guide/components/fragments.html */
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		return mBindingTable.getRoot();
	}

	/** Performs a refresh of the contents of the enclosed table
	 * @param daemon Binder to the ForwardingDaemon used to retrieve the new entries to update this View with
	 */
	@Override
	public void refresh(@NonNull OpportunisticDaemon.Binder daemon) {
		List<CsEntry> newTable = daemon.getContentStore();
		if(!mContents.equals(newTable)) {
			mContents.clear();
			mContents.addAll(newTable);
			mAdapter.clear();
			mAdapter.addAll();
		}
	}

	/** Clear the contents of the enclosed table */
	@Override public void clear() {
		mContents.clear();
	}

	private class ContentStoreAdapter extends ArrayAdapter<CsEntry> {
		private LayoutInflater mInflater;

		ContentStoreAdapter(@NonNull Context context, @LayoutRes int resource) {
			super(context, resource);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
			ItemContentStoreEntryBinding ifb = ItemContentStoreEntryBinding.inflate(mInflater, parent, false);
			ifb.setEntry(mContents.get(position));
			return ifb.getRoot();
		}
	}
}