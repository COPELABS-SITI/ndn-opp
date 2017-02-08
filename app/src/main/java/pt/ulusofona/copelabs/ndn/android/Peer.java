package pt.ulusofona.copelabs.ndn.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Entry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class Peer implements Entry {
    public static final Bundle TABLE_ARGUMENTS = new Bundle();
    static {
        TABLE_ARGUMENTS.putInt(Table.TITLE, R.string.peers);
        TABLE_ARGUMENTS.putInt(Table.DEFAULT_VIEW, R.layout.item_peer);
    }

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

    @Override
	public View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_peer, null, false);
    }

    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.status)).setText(this.currently.getSymbol());
        ((TextView) entry.findViewById(R.id.name)).setText(this.name);
        ((TextView) entry.findViewById(R.id.addr)).setText(this.addr);
    }
}