package pt.ulusofona.copelabs.ndn.android;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import pt.ulusofona.copelabs.ndn.R;

public class Peer {
	public enum Status {
		CONNECTED("C"),
        AVAILABLE("A"),
		UNAVAILABLE("U");
		private String symbol;
		Status(String s) { symbol = s; }
		String getSymbol() { return symbol; }
	}

	private Status currently;
	private String name;
	private String addr;

	public Peer(Status s, String n, String a) {
		currently = s;
		name = n;
		addr = a;
	}

    public String getName() {
        return name;
    }

    public String getAddr() {
        return addr;
    }

	public void setStatus(Status cs) {
		currently = cs;
	}

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Peer peer = (Peer) o;

        return addr != null ? addr.equals(peer.addr) : peer.addr == null;
    }

    @Override
    public int hashCode() {
        return addr != null ? addr.hashCode() : 0;
    }

    @Override
    public String toString() {
        return name + ":" + addr;
    }

    public static class Adapter extends ArrayAdapter<Peer> {
		LayoutInflater inflater;

		public Adapter(Activity act) {
			super(act, R.layout.item_peer, new ArrayList<Peer>());
			inflater = act.getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View entry;

			if(convertView != null) entry = convertView;
			else entry = inflater.inflate(R.layout.item_peer, parent, false);

			Peer peer = getItem(position);

			((TextView) entry.findViewById(R.id.status)).setText(peer.currently.getSymbol());
			((TextView) entry.findViewById(R.id.name)).setText(peer.name);
			((TextView) entry.findViewById(R.id.addr)).setText(peer.addr);

			return entry;
		}
	}
}