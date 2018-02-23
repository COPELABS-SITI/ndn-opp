/**
 * @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Pending Interest Table entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 * @author Miguel Tavares (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;

import java.util.HashMap;
import java.util.Map;

/** The model class used to represent entries from the PendingInterestTable within the Android app.
 *  A PitEntry associates two lists to keep track which Interests arrived on which Faces (Incoming Faces)
 *  and down which Faces they were sent (Outgoing Faces).
 */
public class PitEntry implements Comparable<PitEntry> {

	/** Associates a name to the PitEntry */
    private String mName;

	/** Associates a FaceId with the last Nonce received on that Face */
    private Map<Long, Integer> mInRecords = new HashMap<>();

	/** Associates a FaceId with the last Nonce sent out on that Face */
    private Map<Long, Integer> mOutRecords = new HashMap<>();

	/** Main constructor. Refer to NFD Developer's Guide Section 3.4. Pending Interest Table (p. 23) for details about the meaning of the fields
	 * @param name NDN mName associated to this PitEntry.
	 */
    public PitEntry(String name) {
        mName = name;
    }

	/** Retrieve the NDN Name of this entry.
	 * @return NDN Name
	 */
	public String getName() {
        return mName;
    }

	/** Add a new IN-Record for this PitEntry which records the information on the last Interest packet received on a Face.
	 * NOTE: This has no effect on the PIT of the running Daemon, it only updates this object.
	 * @param faceId ID of the Incoming Face
     * @param nonce the nonce of the last Interest packet received on the Face
	 */
	public void addInRecord(long faceId, int nonce) {
        mInRecords.put(faceId, nonce);
	}

    /** Obtain the IN-Records of this PitEntry.
     * @return the set of all IN-Records of this PitEntry.
     */
	public String getInRecords() {
		StringBuilder sb = new StringBuilder();
		for(Long faceId : mInRecords.keySet())
			sb.append(faceId + "=" + mInRecords.get(faceId) + " ");
		return sb.toString();
    }

	/** Add a new OUT-Record to this PitEntry which records the information on the last Interest packet sent on a Face.
	 * NOTE: This has no effect on the PIT of the running Daemon, it only updates this object.
	 * @param faceId ID of the Outgoing Face
     * @param nonce the nonce of the last Interest packet sent down the Face
	 */
	public void addOutRecord(long faceId, int nonce) {
        mOutRecords.put(faceId, nonce);
	}

    /** Obtain the OUT-Records of this PitEntry.
     * @return the set of all OUT-Records of this PitEntry.
     */
	public String getOutRecords() {
		StringBuilder sb = new StringBuilder();
		for(Long faceId : mOutRecords.keySet())
			sb.append(faceId + "=" + mOutRecords.get(faceId) + " ");
		return sb.toString();
    }

	/** Comparison of PitEntries based on their Interest Name
	 * @param that : other entry to compare this with
	 * @return lexicographic distance between the two Names (based on String.compareTo)
	 */
	@Override
    public int compareTo(@NonNull PitEntry that) {
        return this.mName.compareTo(that.mName);
    }

	@Override
	public boolean equals(Object obj) {
		PitEntry that = (PitEntry) obj;
		return mName.equals(that.mName) && mInRecords.equals(that.mInRecords) && mOutRecords.equals(that.mOutRecords);
	}
}