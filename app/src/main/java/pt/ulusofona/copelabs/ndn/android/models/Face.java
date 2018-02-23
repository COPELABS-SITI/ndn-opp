/** @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Face entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.Locale;

/** The model class used to represent Faces within the Android app.
 * A face has 7 important properties in NDN; Face ID, Local URI, Remote URI, sFaceScope, sFacePersistency, Link Type and sFaceState
 * Beside those, our OppFaces include a packet queue so we also include the number of pending packets.
 * @version 1.0
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
public class Face implements Comparable<Face> {

	/* Static names to be used for pretty-printing the raw data obtained from the daemon.
	 * Based on https://redmine.named-data.net/projects/nfd/wiki/FaceMgmt#Static-Face-Attributes */
	private static SparseArray<String> sFaceScope = new SparseArray<>();
	private static SparseArray<String> sFacePersistency = new SparseArray<>();
	private static SparseArray<String> sFaceLinkType = new SparseArray<>();
	private static SparseArray<String> sFaceState = new SparseArray<>();

	/** Attribute to store face's id */
	private long mFaceId;

	/** Attribute to store face's remote uri */
	private String mRemoteUri;

	/** Attribute to store face's mScope */
    private int mScope;

    /** Attribute to store face's persistency */
    private int mPersistency;

    /** Attribute to store face's link type */
    private int mLinkType;

    /** Attribute to store face's state */
    private int mState;

    /** Attribute to store face's queue size */
	private int mQueueSize;

	/** Main constructor. Refer to NFD Developer's Guide Section 2.
	 *  Face System (p. 9) for details about the meaning of the fields
	 * @param faceId the Face ID
	 * @param remoteUri the RemoteURI of the Face
	 * @param scope the sFaceScope of the Face
	 * @param persistency the sFacePersistency of the Face
	 * @param linkType the Link Type of the Face
	 * @param state the Status of the Face
	 * @param queueSize the number of packets pending in the Face's queue
	 */
	public Face(long faceId, String remoteUri, int scope, int persistency, int linkType, int state, int queueSize) {
		this.mFaceId = faceId;
		this.mRemoteUri = remoteUri;
		this.mScope = scope;
		this.mPersistency = persistency;
		this.mLinkType = linkType;
		this.mState = state;
        this.mQueueSize = queueSize;
	}

	static {
		sFaceScope.put(0, "NL"); // Non-local
		sFaceScope.put(1, "Lo"); // Local
		sFaceScope.put(255, "--"); // None

		sFacePersistency.put(0, "Ps"); // Persistent
		sFacePersistency.put(1, "OD"); // On-demand
		sFacePersistency.put(2, "Pm"); // Permanent
		sFacePersistency.put(255, "--"); // None

		sFaceLinkType.put(0, "PP"); // Point-to-point
		sFaceLinkType.put(1, "MA"); // Multi-access

		sFaceState.put(0, "--"); // NONE
		sFaceState.put(1, "Up"); // UP
		sFaceState.put(2, "Dn"); // DOWN
		sFaceState.put(3, "Cg"); // CLOSING
		sFaceState.put(4, "Fd"); // FAILED
		sFaceState.put(5, "Cd"); // CLOSED
	}

	public long getFaceId() {
		return mFaceId;
	}

	public String getRemoteUri() {
		return mRemoteUri;
	}

	public String remoteUri() {
		if (mRemoteUri.startsWith("opp://"))
			return mRemoteUri + (mQueueSize > 0 ? " [" + mQueueSize + "]" : "");
		else
			return mRemoteUri + (mQueueSize > 0 ? " [" + mQueueSize + "]" : "");
	}

	public String faceId() { return String.format(Locale.getDefault(), "%03d", mFaceId); }
	public String scope() { return sFaceScope.get(mScope); }
	public String persistency() { return sFacePersistency.get(mPersistency); }
	public String linkType() { return sFaceLinkType.get(mLinkType); }
	public String state() { return sFaceState.get(mState); }
	public boolean isFaceUp() {
		return mState == 1;
	}

	/** Comparison of Faces based on their ID
	 * @param that other Face to compare this Face with
	 * @return ID of this Face minus ID of that Face
	 */
	@Override
	public int compareTo(@NonNull Face that) {
		return (int) (this.mFaceId - that.mFaceId);
	}

	@Override
	public boolean equals(Object obj) {
		Face that = (Face) obj;
		return this.mFaceId == that.mFaceId &&
			   this.mRemoteUri.equals(that.mRemoteUri) &&
				this.mScope == that.mScope &&
				this.mPersistency == that.mPersistency &&
				this.mLinkType == that.mLinkType &&
				this.mState == that.mState &&
				this.mQueueSize == that.mQueueSize;

	}
}