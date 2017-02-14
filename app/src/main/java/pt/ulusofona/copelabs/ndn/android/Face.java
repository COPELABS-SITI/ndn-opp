/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Face entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android;

import android.os.Bundle;
import android.util.SparseArray;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import java.util.Locale;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Entry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class Face implements Entry, Comparable<Face> {
    public static final Bundle TABLE_ARGUMENTS = new Bundle();
    static {
        TABLE_ARGUMENTS.putInt(Table.TITLE, R.string.facetable);
        TABLE_ARGUMENTS.putInt(Table.DEFAULT_VIEW, R.layout.item_face);
    }

	private long id;
    private String localURI;
    private String remoteURI;
    private int scope;
    private int persistency;
    private int linkType;
    private int state;

	public long getId() {
		return id;
	}

	public Face(long fId, String lu, String ru, int sc, int p, int lt, int st) {
		id = fId;
		localURI = lu;
		remoteURI = ru;
		scope = sc;
		persistency = p;
		linkType = lt;
		state = st;
	}

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

    private static void setTextView(View face, int rid, String content) {
        ((TextView) face.findViewById(rid)).setText(content);
    }

    @Override
    public View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_face, null, false);
	}

    @Override
    public void setViewContents(View entry) {
        setTextView(entry, R.id.faceId, String.format(Locale.getDefault(), "%03d", this.id));
        setTextView(entry, R.id.state, State.get(this.state));
        setTextView(entry, R.id.remoteUri, this.remoteURI);
        setTextView(entry, R.id.scope, Scope.get(this.scope));
        setTextView(entry, R.id.persistency, Persistency.get(this.persistency));
        setTextView(entry, R.id.linkType, LinkType.get(this.linkType));
    }

	@Override
	public int compareTo(Face that) {
		return (int) (this.id - that.id);
	}
}