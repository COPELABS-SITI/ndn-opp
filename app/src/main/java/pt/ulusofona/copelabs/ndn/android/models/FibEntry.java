/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Forwarding Information Base entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/** The model class used to represent entries from the ForwardingInformationBase within the Android app.
 *  A FibEntry associates a list of pairs (FaceID, Cost) to a Name mPrefix.
 */
public class FibEntry implements Comparable<FibEntry> {
    // The Prefix associated to this entry
	private String mPrefix;
    // The list of FaceIds with their corresponding Cost. mFaceIds.get(faceId) gives the <cost> for <faceId>
	private Map<Long, Integer> mFaceIds;

    /** Main constructor. Refer to NFD Developer's Guide Section 3. Forwarding Information Base (p. 19) for details about the meaning of the fields
     * @param prefix NDN Name mPrefix associated to this FibEntry.
     */
	public FibEntry(String prefix) {
		this.mPrefix = prefix;
		this.mFaceIds = new HashMap<>();
	}

    /** Associate a pair (Face, Cost) to this FibEntry. Updates the Cost of the Face if it is
     * already associated. Note: This has no effect on the FIB of the Daemon; this only updates this object.
     * @param faceId the ID of the Face to associated to this entry
     * @param cost the cost to associate the Face identified by faceId
     */
	public void addNextHop(long faceId, int cost) {
		mFaceIds.put(faceId, cost);
	}

	public String getPrefix() {
        return mPrefix;
    }

    public String getNextHops() {
        StringBuilder builder = new StringBuilder();
        for(long key : mFaceIds.keySet()) {
            builder.append(" ").append(Long.toString(key)).append("=").append(mFaceIds.get(key));
        }
        return builder.toString();
    }

    /** Comparison of FibEntries based on their Name mPrefix
     * @param that other entry to compare this with
     * @return lexicographic distance between the two Name prefixes (based on String.compareTo)
     */
    @Override
    public int compareTo(@NonNull FibEntry that) {
        return this.mPrefix.compareTo(that.mPrefix);
    }

    @Override
    public boolean equals(Object obj) {
        FibEntry that = (FibEntry) obj;
        return mPrefix.equals(that.mPrefix) && mFaceIds.equals(that.mFaceIds);
    }
}