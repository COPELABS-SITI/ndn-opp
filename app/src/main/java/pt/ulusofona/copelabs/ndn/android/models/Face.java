/** @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Face entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;
import android.util.SparseArray;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import java.util.Locale;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

/** The model class used to represent Faces within the Android app.
 * A face has 7 important properties in NDN; Face ID, Local URI, Remote URI, Scope, Persistency, Link Type and State
 * Beside those, our OppFaces include a packet queue so we also include the number of pending packets.
 * @version 1.0
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
public class Face implements Table.Entry, Comparable<Face> {
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
	public String getRemoteUri() { return remoteUri; }

	/** Main constructor.
	 * @param faceId the Face ID
	 * @param remoteUri the RemoteURI of the Face
	 * @param scope the Scope of the Face
	 * @param persistency the Persistency of the Face
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
	private static SparseArray<String> Scope = new SparseArray<>();
	private static SparseArray<String> Persistency = new SparseArray<>();
	private static SparseArray<String> LinkType = new SparseArray<>();
	private static SparseArray<String> State = new SparseArray<>();

	static {
		Scope.put(0, "NL"); // Non-local
		Scope.put(1, "Lo"); // Local
		Scope.put(255, "--"); // None

		Persistency.put(0, "Ps"); // Persistent
		Persistency.put(1, "OD"); // On-demand
		Persistency.put(2, "Pm"); // Permanent
		Persistency.put(255, "--"); // None

		LinkType.put(0, "PP"); // Point-to-point
		LinkType.put(1, "MA"); // Multi-access

		State.put(0, "--"); // NONE
		State.put(1, "Up"); // UP
		State.put(2, "Dn"); // DOWN
		State.put(3, "Cg"); // CLOSING
		State.put(4, "Fd"); // FAILED
		State.put(5, "Cd"); // CLOSED
	}

	/** Constructs the View to use to display an instance of Face.
	 * @param inflater the system inflater to used for turning the layout file into objects.
	 * @return the View to be used for displaying an instance of Face.
	 */
    @Override
    public View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_face, null, false);
	}

	/** Initialize the fields of a View with the values stored in this Face.
	 * @param entry the View to use for displaying this Face.
	 */
    @Override
    public void setViewContents(View entry) {
		((TextView) entry.findViewById(R.id.faceId)).setText(String.format(Locale.getDefault(), "%03d", faceId));
		((TextView) entry.findViewById(R.id.state)).setText(State.get(state));

		// Append the queue size to the RemoteURI in the case of an Opportunistic Face.
		if(this.remoteUri.startsWith("opp://"))
			((TextView) entry.findViewById(R.id.remoteUri)).setText(remoteUri + (queueSize > 0 ? " [" + queueSize + "]" : ""));
		else
			((TextView) entry.findViewById(R.id.remoteUri)).setText(remoteUri);

		((TextView) entry.findViewById(R.id.scope)).setText(Scope.get(scope));
		((TextView) entry.findViewById(R.id.persistency)).setText(Persistency.get(persistency));
		((TextView) entry.findViewById(R.id.linkType)).setText(LinkType.get(linkType));
    }

	/** Comparison of Faces based on their ID
	 * @param that other Face to compare this Face with
	 * @return ID of this Face minus ID of that Face
	 */
	@Override
	public int compareTo(@NonNull Face that) {
		return (int) (this.faceId - that.faceId);
	}
}