package pt.ulusofona.copelabs.ndn.android;

import java.util.ArrayList;

import android.app.Activity;

import android.util.SparseArray;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.ArrayAdapter;

import pt.ulusofona.copelabs.ndn.R;

public class Face {
	private static final int CONDENSED = 0;
	private static final int DETAILED = 1;

	public long id;
	public String localURI;
	public String remoteURI;
	public int scope;
	public int persistency;
	public int linkType;
	public int state;
	public long expiresIn;

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

	public static class Adapter extends ArrayAdapter<Face> {
		LayoutInflater inflater;

		public Adapter(Activity act) {
			super(act, R.layout.item_face, new ArrayList<Face>());
			inflater = act.getLayoutInflater();
		}

		private static void setTextView(View face, int rid, String content) {
			((TextView) face.findViewById(rid)).setText(content);
		}

		@Override
		public int getItemViewType(int position) {
			Face current = getItem(position);
			int type;

			if(current.id <= 255 || current.remoteURI.startsWith("wfd://"))
				type = CONDENSED;
			else
				type = DETAILED;

			return type;
		}

		@Override
		public int getViewTypeCount() {
			return 2;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View face;

			Face current = getItem(position);

			if(convertView != null)
				face = convertView;
			else if(getItemViewType(position) == DETAILED)
                    face = inflater.inflate(R.layout.item_face, parent, false);
				 else
                    face = inflater.inflate(R.layout.item_face_reserved, parent, false);

			// Fields common to both entry_face and entry_face_short
			setTextView(face, R.id.faceId, String.format("%03d", current.id));
			setTextView(face, R.id.state, State.get(current.state));
			setTextView(face, R.id.localUri, current.localURI);

			if(current.id > 255 && !current.remoteURI.startsWith("wfd://")) {
				setTextView(face, R.id.remoteUri, current.remoteURI);
				setTextView(face, R.id.scope, Scope.get(current.scope));
				setTextView(face, R.id.persistency, Persistency.get(current.persistency));
				setTextView(face, R.id.linkType, LinkType.get(current.linkType));
				setTextView(face, R.id.expiresIn, Long.toString(current.expiresIn));
			}

			return face;
		}
	}
}