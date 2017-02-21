package pt.ulusofona.copelabs.ndn.android;

import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import java.util.Locale;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class UmobileService implements Table.Entry {
    public enum Status {
        AVAILABLE("Av"),
        UNAVAILABLE("Un");
		private String symbol;
		Status(String s) { symbol = s; }
		public String getSymbol() { return symbol; }
    }

	public Status status;
    public String name;
	public String host;
	public int port;

    public UmobileService(Status s, String n, String h, int p) {
		status = s;
        name = n;
		host = h;
		port = p;
	}

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        UmobileService that = (UmobileService) other;
        return this.status == that.status
                && this.name.equals(that.name)
                && this.host.equals(that.host)
                && this.port == that.port;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    @Override
	public View getView(LayoutInflater inflater) {
        View entry = inflater.inflate(R.layout.item_service, null, false);
        setViewContents(entry);
        return entry;
    }

    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.status)).setText(this.status.getSymbol());
        ((TextView) entry.findViewById(R.id.host)).setText(this.host);
        ((TextView) entry.findViewById(R.id.port)).setText(String.format(Locale.getDefault(), "%d", this.port));
        ((TextView) entry.findViewById(R.id.name)).setText(this.name);
    }
}