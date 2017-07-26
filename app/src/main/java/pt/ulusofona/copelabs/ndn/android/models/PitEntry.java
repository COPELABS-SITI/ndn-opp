/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Pending Interest Table entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;

import java.util.HashSet;
import java.util.Set;

/** The model class used to represent entries from the PendingInterestTable within the Android app.
 *  A PitEntry associates two lists to keep track which Interests arrived on which Faces (Incoming Faces)
 *  and down which Faces they were sent (Outgoing Faces).
 */
public class PitEntry implements Comparable<PitEntry> {
    private String name;
	// Associates a FaceId with the last Nonce received on that Face
    private Set<FaceRecord> mInRecords = new HashSet<>();
	// Associates a FaceId with the last Nonce sent out on that Face
    private Set<FaceRecord> mOutRecords = new HashSet<>();

    public PitEntry(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

	/** Add a new IN-Record for this PitEntry which records the information on the last Interest packet received on a Face
	 * @param faceId ID of the Incoming Face
     * @param nonce the nonce of the last Interest packet received on the Face
	 */
	public void addInRecord(long faceId, int nonce) {
        mInRecords.add(new FaceRecord(faceId, nonce));
	}

    /** Obtain the IN-Records of this PitEntry.
     * @return the set of all IN-Records of this PitEntry.
     */
	public Set<FaceRecord> getInRecords() {
        return mInRecords;
    }

	/** Add a new OUT-Record to this PitEntry which records the information on the last Interest packet sent on a Face
	 * @param faceId ID of the Outgoing Face
     * @param nonce the nonce of the last Interest packet sent down the Face
	 */
	public void addOutRecord(long faceId, int nonce) {
        mOutRecords.add(new FaceRecord(faceId, nonce));
	}

    /** Obtain the OUT-Records of this PitEntry.
     * @return the set of all OUT-Records of this PitEntry.
     */
	public Set<FaceRecord> getOutRecords() {
        return mOutRecords;
    }

	/** Comparison of PitEntries based on their Interest Name
	 * @param that : other entry to compare this with
	 * @return lexicographic distance between the two Names (based on String.compareTo)
	 */
	@Override
    public int compareTo(@NonNull PitEntry that) {
        return this.name.compareTo(that.name);
    }

	public class FaceRecord {
		private long faceId;
		private int nonce;

		FaceRecord(long faceId, int nonce) {
			this.faceId = faceId;
			this.nonce = nonce;
		}

		public long getFaceId() {
            return faceId;
        }

        public int getNonce() {
            return nonce;
        }
    }
}