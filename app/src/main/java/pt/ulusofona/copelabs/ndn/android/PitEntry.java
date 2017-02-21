/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Pending Interest Table entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */package pt.ulusofona.copelabs.ndn.android;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class PitEntry implements Table.Entry, Comparable<PitEntry> {
    private String name;
	private List<Long> inFaces;
	private List<Long> outFaces;

	public PitEntry(String n) {
		name = n;
		inFaces = new ArrayList<>();
		outFaces = new ArrayList<>();
	}

	public void addInRecord(long fi) {
		inFaces.add(fi);
	}
	public void addOutRecord(long fi) {
		outFaces.add(fi);
	}

    @Override
	public View getView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.item_pit_entry, null, false);
	}

    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.name)).setText(this.name);
        ((TextView) entry.findViewById(R.id.inFaces)).setText(this.inFaces.toString());
        ((TextView) entry.findViewById(R.id.outFaces)).setText(this.outFaces.toString());
    }

    @Override
    public int compareTo(@NonNull PitEntry that) {
        return this.name.compareTo(that.name);
    }
}