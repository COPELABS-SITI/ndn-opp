/** @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Face entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.databinding.BaseObservable;
import android.databinding.Bindable;
import android.support.annotation.NonNull;
import android.util.SparseArray;

import java.util.Locale;

/** The model class used to represent Faces within the Android app.
 * A face has 7 important properties in NDN; Face ID, Local URI, Remote URI, FaceScope, FacePersistency, Link Type and FaceState
 * Beside those, our OppFaces include a packet queue so we also include the number of pending packets.
 * @version 1.0
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
public class Face implements Comparable<Face> {
	private long faceId;
	private String remoteUri;
    private int scope;
    private int persistency;
    private int linkType;
    private int state;
	private int queueSize;

	public long getFaceId() {
		return faceId;
	}
	public String getRemoteUri() {
		return remoteUri;
	}

	public String remoteUri() {
		if (remoteUri.startsWith("opp://"))
			return remoteUri + (queueSize > 0 ? " [" + queueSize + "]" : "");
		else
			return remoteUri + (queueSize > 0 ? " [" + queueSize + "]" : "");
	}

	public String faceId() { return String.format(Locale.getDefault(), "%03d", faceId); }
	public String scope() { return FaceScope.get(scope); }
	public String persistency() { return FacePersistency.get(persistency); }
	public String linkType() { return FaceLinkType.get(linkType); }
	public String state() { return FaceState.get(state); }

	/** Main constructor. Refer to NFD Developer's Guide Section 2. Face System (p. 9) for details about the meaning of the fields
	 * @param faceId the Face ID
	 * @param remoteUri the RemoteURI of the Face
	 * @param scope the FaceScope of the Face
	 * @param persistency the FacePersistency of the Face
	 * @param linkType the Link Type of the Face
	 * @param state the Status of the Face
	 * @param queueSize the number of packets pending in the Face's queue
	 */
	public Face(long faceId, String remoteUri, int scope, int persistency, int linkType, int state, int queueSize) {
		this.faceId = faceId;
		this.remoteUri = remoteUri;
		this.scope = scope;
		this.persistency = persistency;
		this.linkType = linkType;
		this.state = state;
        this.queueSize = queueSize;
	}

	/* Static names to be used for pretty-printing the raw data obtained from the daemon.
	 * Based on https://redmine.named-data.net/projects/nfd/wiki/FaceMgmt#Static-Face-Attributes */
	private static SparseArray<String> FaceScope = new SparseArray<>();
	private static SparseArray<String> FacePersistency = new SparseArray<>();
	private static SparseArray<String> FaceLinkType = new SparseArray<>();
	private static SparseArray<String> FaceState = new SparseArray<>();

	static {
		FaceScope.put(0, "NL"); // Non-local
		FaceScope.put(1, "Lo"); // Local
		FaceScope.put(255, "--"); // None

		FacePersistency.put(0, "Ps"); // Persistent
		FacePersistency.put(1, "OD"); // On-demand
		FacePersistency.put(2, "Pm"); // Permanent
		FacePersistency.put(255, "--"); // None

		FaceLinkType.put(0, "PP"); // Point-to-point
		FaceLinkType.put(1, "MA"); // Multi-access

		FaceState.put(0, "--"); // NONE
		FaceState.put(1, "Up"); // UP
		FaceState.put(2, "Dn"); // DOWN
		FaceState.put(3, "Cg"); // CLOSING
		FaceState.put(4, "Fd"); // FAILED
		FaceState.put(5, "Cd"); // CLOSED
	}

	/** Comparison of Faces based on their ID
	 * @param that other Face to compare this Face with
	 * @return ID of this Face minus ID of that Face
	 */
	@Override
	public int compareTo(@NonNull Face that) {
		return (int) (this.faceId - that.faceId);
	}

	@Override
	public boolean equals(Object obj) {
		Face that = (Face) obj;
		return this.faceId == that.faceId &&
			   this.remoteUri.equals(that.remoteUri) &&
				this.scope == that.scope &&
				this.persistency == that.persistency &&
				this.linkType == that.linkType &&
				this.state == that.state &&
				this.queueSize == that.queueSize;

	}
}