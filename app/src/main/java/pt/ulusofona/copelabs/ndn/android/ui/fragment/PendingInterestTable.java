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
import pt.ulusofona.copelabs.ndn.android.models.PitEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.databinding.FragmentTableBinding;
import pt.ulusofona.copelabs.ndn.databinding.ItemPitEntryBinding;

/** Fragment used to display the PendingInterestTable of the running daemon. */
public class PendingInterestTable extends Fragment implements Refreshable {
    private FragmentTableBinding mTableBinding;
    private List<PitEntry> mPitEntries = new ArrayList<>();
	private PitEntryAdapter mPitEntriesAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPitEntriesAdapter = new PitEntryAdapter(context, R.layout.item_pit_entry);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTableBinding = FragmentTableBinding.inflate(getActivity().getLayoutInflater());
        mTableBinding.title.setText(R.string.pit);
        mPitEntriesAdapter = new PendingInterestTable.PitEntryAdapter(getContext(), R.layout.item_face);
        mTableBinding.contents.setAdapter(mPitEntriesAdapter);
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
        List<PitEntry> newTable = daemon.getPendingInterestTable();
        if(!mPitEntries.equals(newTable)) {
            mPitEntries.clear();
            mPitEntries.addAll(newTable);
            mPitEntriesAdapter.clear();
            mPitEntriesAdapter.addAll(newTable);
        }
	}

	/** Clear the contents of the enclosed table */
	@Override
	public void clear() {
		mPitEntriesAdapter.clear();
	}

	private class PitEntryAdapter extends ArrayAdapter<PitEntry> {
        private LayoutInflater mInflater;
        PitEntryAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
            mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            ItemPitEntryBinding ipeb = ItemPitEntryBinding.inflate(mInflater, parent, false);
            ipeb.setEntry(mPitEntries.get(position));
            return ipeb.getRoot();
        }
    }
}