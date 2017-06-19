/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Pending Interest Table entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

/** The model class used to represent entries from the PendingInterestTable within the Android app.
 *  A PitEntry associates two lists to keep track which Interests arrived on which Faces (Incoming Faces)
 *  and down which Faces they were sent (Outgoing Faces).
 */
public class PitEntry implements Table.Entry, Comparable<PitEntry> {
    private String name;
	private List<Long> inFaces;
	private List<Long> outFaces;

	/** Main constructor
	 * @param name the Name of the Interest associated to this entry
	 */
	public PitEntry(String name) {
		this.name = name;
		inFaces = new ArrayList<>();
		outFaces = new ArrayList<>();
	}

	/** Reference a new Incoming Face for this PitEntry
	 * @param faceId ID of the Incoming Face
	 */
	public void addInRecord(long faceId) {
		inFaces.add(faceId);
	}

	/** Reference a new Outgoing Face for this PitEntry
	 * @param faceId ID of the Outgoing Face
	 */
	public void addOutRecord(long faceId) {
		outFaces.add(faceId);
	}

	/** Constructs the View to use to display an instance of PitEntry.
	 * @param inflater the system inflater to used for turning the layout file into objects.
	 * @return the View to be used for displaying an instance of PitEntry.
	 */
    @Override
	public View getView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.item_pit_entry, null, false);
	}

	/** Initialize the fields of a View with the values stored in this PitEntry.
	 * @param entry : the View to use for displaying this PitEntry.
	 */
	@Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.name)).setText(this.name);
        ((TextView) entry.findViewById(R.id.inFaces)).setText(this.inFaces.toString());
        ((TextView) entry.findViewById(R.id.outFaces)).setText(this.outFaces.toString());
    }

	/** Comparison of PitEntries based on their Interest Name
	 * @param that : other entry to compare this with
	 * @return lexicographic distance between the two Names (based on String.compareTo)
	 */
	@Override
    public int compareTo(@NonNull PitEntry that) {
        return this.name.compareTo(that.name);
    }
}