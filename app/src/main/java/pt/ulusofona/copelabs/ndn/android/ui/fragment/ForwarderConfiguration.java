/*
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-mm-dd
 *
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
import pt.ulusofona.copelabs.ndn.android.models.FibEntry;
import pt.ulusofona.copelabs.ndn.android.models.SctEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.databinding.FragmentTwoTablesBinding;
import pt.ulusofona.copelabs.ndn.databinding.ItemFibEntryBinding;
import pt.ulusofona.copelabs.ndn.databinding.ItemSctEntryBinding;

/** Fragment used to display the Forwarder configuration (FIB + SCT) of the running daemon. */
public class ForwarderConfiguration extends Fragment implements Refreshable {
	private FragmentTwoTablesBinding mTableBinding;

	private List<FibEntry> mFib = new ArrayList<>();
	private FibEntryAdapter mFibEntryAdapter;

	private List<SctEntry> mSct = new ArrayList<>();
	private SctEntryAdapter mSctEntryAdapter;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mTableBinding = FragmentTwoTablesBinding.inflate(getActivity().getLayoutInflater());
		mTableBinding.title1.setText(R.string.fib);
		mTableBinding.title2.setText(R.string.sct);
		mFibEntryAdapter = new FibEntryAdapter(getContext(), R.layout.item_fib_entry);
		mSctEntryAdapter = new SctEntryAdapter(getContext(), R.layout.item_sct_entry);
		mTableBinding.contents1.setAdapter(mFibEntryAdapter);
		mTableBinding.contents2.setAdapter(mSctEntryAdapter);
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

	/** Performs a refresh of the contents of the enclosed tables
	 * @param daemon Binder to the ForwardingDaemon used to retrieve the new entries to update this View with
	 */
	@Override
	public void refresh(@NonNull OpportunisticDaemon.Binder daemon) {
		List<FibEntry> newFib = daemon.getForwardingInformationBase();
		if(!mFib.equals(newFib)) {
			mFib.clear();
			mFib.addAll(newFib);
			mFibEntryAdapter.clear();
			mFibEntryAdapter.addAll(newFib);
		}

		List<SctEntry> newSct = daemon.getStrategyChoiceTable();
		if(!mSct.equals(newSct)) {
			mSct.clear();
			mSct.addAll(newSct);
			mSctEntryAdapter.clear();
			mSctEntryAdapter.addAll(newSct);
		}
	}

	/** Clear the contents of the enclosed tables */
	@Override
	public void clear() {
		mSct.clear();
		mFib.clear();
	}

	private class FibEntryAdapter extends ArrayAdapter<FibEntry> {
		private LayoutInflater mInflater;

		public FibEntryAdapter(@NonNull Context context, @LayoutRes int resource) {
			super(context, resource);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
			ItemFibEntryBinding ifeb = ItemFibEntryBinding.inflate(mInflater);
			ifeb.setEntry(mFib.get(position));
			return ifeb.getRoot();
		}
	}

	private class SctEntryAdapter extends ArrayAdapter<SctEntry> {
		private LayoutInflater mInflater;

		public SctEntryAdapter(@NonNull Context context, @LayoutRes int resource) {
			super(context, resource);
			mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		}

		@NonNull
		@Override
		public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
			ItemSctEntryBinding iseb = ItemSctEntryBinding.inflate(mInflater);
			iseb.setEntry(mSct.get(position));
			return iseb.getRoot();
		}
	}
}