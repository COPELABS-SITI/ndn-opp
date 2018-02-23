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
import pt.ulusofona.copelabs.ndn.android.models.Name;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.databinding.FragmentTableBinding;
import pt.ulusofona.copelabs.ndn.databinding.ItemNameBinding;

/** Fragment used to display the Name Tree of the running daemon. */
public class NameTree extends Fragment implements Refreshable {
	FragmentTableBinding mTableBinding;
	List<Name> mNames = new ArrayList<>();
	NameTreeAdapter mAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTableBinding = FragmentTableBinding.inflate(getActivity().getLayoutInflater());
		mTableBinding.title.setText(R.string.nametree);
		mAdapter = new NameTreeAdapter(getContext(), R.layout.item_name);
		mTableBinding.contents.setAdapter(mAdapter);

	}

	/** Fragment lifecycle method. See https://developer.android.com/guide/components/fragments.html
	 * @param inflater Android-provided layout inflater
	 * @param parent parent View within the hierarchy
	 * @param savedInstanceState previously saved state of the View instance
	 * @return the View to be used
	 */
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		return mTableBinding.getRoot();
	}

	/** Performs a refresh of the contents of the enclosed table
	 * @param daemon Binder to the ForwardingDaemon used to retrieve the new entries to update this View with
	 */
	@Override
	public void refresh(@NonNull OpportunisticDaemon.Binder daemon) {
		List<Name> newTable = daemon.getNameTree();
		if(!mNames.equals(newTable)) {
			mNames.clear();
			mNames.addAll(newTable);
			mAdapter.clear();
			mAdapter.addAll(mNames);
		}
	}

	/** Clear the contents of the enclosed table */
	@Override
	public void clear() {
		mNames.clear();
	}

	private class NameTreeAdapter extends ArrayAdapter<Name> {
		private LayoutInflater mInflater;

		public NameTreeAdapter(@NonNull Context context, @LayoutRes int resource) {
			super(context, resource);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
			ItemNameBinding inb = ItemNameBinding.inflate(mInflater, parent, false);
			inb.setName(mNames.get(position));
			return inb.getRoot();
		}
	}
}