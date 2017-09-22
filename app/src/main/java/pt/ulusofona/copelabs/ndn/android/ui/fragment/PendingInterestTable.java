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
import android.widget.ListView;
import android.widget.TextView;

import java.util.Locale;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.models.PitEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;

/** Fragment used to display the PendingInterestTable of the running daemon. */
public class PendingInterestTable extends Fragment implements Refreshable {
	private PitEntryAdapter mPitEntriesAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mPitEntriesAdapter = new PitEntryAdapter(context, R.layout.item_pit_entry);
    }

    /** Fragment lifecycle method. See https://developer.android.com/guide/components/fragments.html
	 * @param inflater Android-provided layout inflater
	 * @param parent parent View within the hierarchy
	 * @param savedInstanceState previously saved state of the View instance
	 * @return the View to be used
	 */
    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View pitView = inflater.inflate(R.layout.fragment_table, parent, false);

		((TextView) pitView.findViewById(R.id.title)).setText(R.string.pit);
		((ListView) pitView.findViewById(R.id.contents)).setAdapter(mPitEntriesAdapter);

		return pitView;
	}

	/** Obtain the title to be displayed for this table
	 * @return the title to be displayed
	 */
    @Override
    public int getTitle() {
        return R.string.pit;
    }

	/** Performs a refresh of the contents of the enclosed table
	 * @param daemon Binder to the ForwardingDaemon used to retrieve the new entries to update this View with
	 */
	@Override
	public void refresh(@NonNull OpportunisticDaemon.Binder daemon) {
        mPitEntriesAdapter.clear();
		mPitEntriesAdapter.addAll(daemon.getPendingInterestTable());
	}

	/** Clear the contents of the enclosed table */
	@Override
	public void clear() {
		mPitEntriesAdapter.clear();
	}

	private class PitEntryAdapter extends ArrayAdapter<PitEntry> {
        PitEntryAdapter(@NonNull Context context, @LayoutRes int resource) {
            super(context, resource);
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_pit_entry, parent, false);


            PitEntry entry = getItem(position);
            RecordAdapter inRecordsAdapter = new RecordAdapter(getContext(), R.layout.item_pit_entry_in_record);
            RecordAdapter outRecordsAdapter = new RecordAdapter(getContext(), R.layout.item_pit_entry_out_record);
            if (entry != null) {
                inRecordsAdapter.clear();
                inRecordsAdapter.addAll(entry.getInRecords());
                outRecordsAdapter.clear();
                outRecordsAdapter.addAll(entry.getOutRecords());

                ((TextView) convertView.findViewById(R.id.text_name)).setText(entry.getName());
                ((ListView) convertView.findViewById(R.id.inRecords)).setAdapter(inRecordsAdapter);
                ((ListView) convertView.findViewById(R.id.outRecords)).setAdapter(outRecordsAdapter);
            }

            return convertView;
        }
    }

    private class RecordAdapter extends ArrayAdapter<PitEntry.FaceRecord> {
        private int resourceId;

        RecordAdapter(Context context, int resourceId) {
            super(context, resourceId);
            this.resourceId = resourceId;
        }

        @NonNull
        @Override
        public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
            if (convertView == null)
                convertView = LayoutInflater.from(getContext()).inflate(resourceId, parent, false);

            PitEntry.FaceRecord record = getItem(position);
            if (record != null) {
                ((TextView) convertView.findViewById(R.id.faceId)).setText(String.format(Locale.getDefault(), "%03d", record.getFaceId()));
                ((TextView) convertView.findViewById(R.id.lastNonce)).setText(String.format(Locale.getDefault(), "%d", record.getNonce()));
            }

            return convertView;
        }
    }
}