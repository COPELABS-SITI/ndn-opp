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
 *  A FibEntry associates a list of pairs (FaceID, Cost) to a Name mPrefix.
 */
public class FibEntry implements Table.Entry, Comparable<FibEntry> {
    // The Prefix associated to this entry
	private String mPrefix;
    // The list of FaceIds with their corresponding Cost. mFaceIds.get(faceId) gives the <cost> for <faceId>
	private LongSparseArray<Integer> mFaceIds;

    /** Main constructor. Refer to NFD Developer's Guide Section 3. Forwarding Information Base (p. 19) for details about the meaning of the fields
     * @param prefix NDN Name mPrefix associated to this FibEntry.
     */
	public FibEntry(String prefix) {
		this.mPrefix = prefix;
		this.mFaceIds = new LongSparseArray<>();
	}

    /** Associate a pair (Face, Cost) to this FibEntry. Updates the Cost of the Face if it is
     * already associated. Note: This has no effect on the FIB of the Daemon; this only updates this object.
     * @param faceId the ID of the Face to associated to this entry
     * @param cost the cost to associate the Face identified by faceId
     */
	public void addNextHop(long faceId, int cost) {
		mFaceIds.put(faceId, cost);
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
        ((TextView) entry.findViewById(R.id.left)).setText(mPrefix);

        StringBuilder nhString = new StringBuilder();
        for(int k = 0; k < mFaceIds.size(); k++) {
            long key = mFaceIds.keyAt(k);
            nhString.append(Long.toString(key))
                    .append(":")
                    .append(mFaceIds.get(key));
            if(k < mFaceIds.size() - 1)
                nhString.append(",");
        }

        ((TextView) entry.findViewById(R.id.right)).setText(nhString.toString());
    }

    /** Comparison of FibEntries based on their Name mPrefix
     * @param that other entry to compare this with
     * @return lexicographic distance between the two Name prefixes (based on String.compareTo)
     */
    @Override
    public int compareTo(@NonNull FibEntry that) {
        return this.mPrefix.compareTo(that.mPrefix);
    }
}