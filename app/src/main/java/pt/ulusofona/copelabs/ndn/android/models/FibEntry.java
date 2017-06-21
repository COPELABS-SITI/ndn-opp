/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Forwarding Information Base entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;
import android.util.LongSparseArray;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

/** The model class used to represent entries from the ForwardingInformationBase within the Android app.
 *  A FibEntry associates a list of pairs (FaceID, Cost) to a Name prefix.
 */
public class FibEntry implements Table.Entry, Comparable<FibEntry> {
	private String prefix;
	private LongSparseArray<Integer> faceIds;

    /** Main constructor.
     * @param prefix Name prefix associated to this FibEntry.
     */
	public FibEntry(String prefix) {
		this.prefix = prefix;
		this.faceIds = new LongSparseArray<>();
	}

    /** Associate a pair (Face, Cost) to this FibEntry. Updates the Cost of the Face if it is
     * already associated.
     * @param faceId the ID of the Face to associated to this entry
     * @param cost the cost to associate the Face identified by faceId
     */
	public void addNextHop(long faceId, int cost) {
		faceIds.put(faceId, cost);
	}

    /** Constructs the View to use to display an instance of FibEntry.
     * @param inflater the system inflater to used for turning the layout file into objects.
     * @return the View to be used for displaying an instance of FibEntry.
     */
    @Override
	public View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_cell_two, null, false);
    }

    /** Initialize the fields of a View with the values stored in this FibEntry.
     * @param entry the View to use for displaying this FibEntry.
     */
    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.left)).setText(prefix);

        StringBuilder nhString = new StringBuilder();
        for(int k = 0; k < faceIds.size(); k++) {
            long key = faceIds.keyAt(k);
            nhString.append(Long.toString(key))
                    .append(":")
                    .append(faceIds.get(key));
            if(k < faceIds.size() - 1)
                nhString.append(",");
        }

        ((TextView) entry.findViewById(R.id.right)).setText(nhString.toString());
    }

    /** Comparison of FibEntries based on their Name prefix
     * @param that other entry to compare this with
     * @return lexicographic distance between the two Name prefixes (based on String.compareTo)
     */
    @Override
    public int compareTo(@NonNull FibEntry that) {
        return this.prefix.compareTo(that.prefix);
    }
}