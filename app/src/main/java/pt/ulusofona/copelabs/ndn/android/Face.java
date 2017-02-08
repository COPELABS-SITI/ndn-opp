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

public class Face implements Entry {
    public static final Bundle TABLE_ARGUMENTS = new Bundle();
    static {
        TABLE_ARGUMENTS.putInt(Table.TITLE, R.string.facetable);
        TABLE_ARGUMENTS.putInt(Table.DEFAULT_VIEW, R.layout.item_face_detailed);
        TABLE_ARGUMENTS.putInt(Table.VIEW_TYPE_COUNT, 2);
    }

    private static final int DETAILED = 0;
    private static final int CONDENSED = 1;

	private long id;
    private String localURI;
    private String remoteURI;
    private int scope;
    private int persistency;
    private int linkType;
    private int state;
    private long expiresIn;

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
		Scope.put(0, "Non-local");
		Scope.put(1, "Local");
		Scope.put(255, "None");

		Persistency.put(0, "Persistent");
		Persistency.put(1, "On-demand");
		Persistency.put(2, "Permanent");
		Persistency.put(255, "None");

		LinkType.put(0, "Point-to-point");
		LinkType.put(1, "Multi-access");

		State.put(0, "-"); // NONE
		State.put(1, "U"); // UP
		State.put(2, "D"); // DOWN
		State.put(3, "c"); // CLOSING
		State.put(4, "X"); // FAILED
		State.put(5, "C"); // CLOSED
	}

    private static void setTextView(View face, int rid, String content) {
        ((TextView) face.findViewById(rid)).setText(content);
    }

    private boolean useCondensedView() {
        return id <= 255 || remoteURI.startsWith("wfd://");
    }

    @Override
    public int getItemViewType() {
        int type;
        if(useCondensedView()) type = CONDENSED;
        else type = DETAILED;
        return type;
    }

    @Override
    public View getView(LayoutInflater inflater) {
        View entry;

        if(useCondensedView())
            entry = inflater.inflate(R.layout.item_face_condensed, null, false);
        else
            entry = inflater.inflate(R.layout.item_face_detailed, null, false);

        return entry;
	}

    @Override
    public void setViewContents(View entry) {
        if(!useCondensedView()) {
            setTextView(entry, R.id.remoteUri, this.remoteURI);
            setTextView(entry, R.id.scope, Scope.get(this.scope));
            setTextView(entry, R.id.persistency, Persistency.get(this.persistency));
            setTextView(entry, R.id.linkType, LinkType.get(this.linkType));
            setTextView(entry, R.id.expiresIn, Long.toString(this.expiresIn));
        }

        // Fields common to both entry_face and entry_face_short
        setTextView(entry, R.id.faceId, String.format(Locale.getDefault(), "%03d", this.id));
        setTextView(entry, R.id.state, State.get(this.state));
        setTextView(entry, R.id.localUri, this.localURI);
    }
}