/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The NsdService encapsulates the information required to establish a connection to that Service
 * running on some remote device.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android;

import android.net.nsd.NsdServiceInfo;

public class NsdService {
    private static final String UNKNOWN_HOST = "0.0.0.0";
    private static final int UNKNOWN_PORT = 0;

    public static final String SERVICE_TYPE = "_nsdtracker._tcp";

    public enum Status {
        AVAILABLE("Av"),
        UNAVAILABLE("Un");
		private String symbol;
		Status(String s) { symbol = s; }
		public String toString() { return symbol; }
    }

    private Status currently;
    private String uuid;
    private String host;
	private int port;

    private NsdService(Status st, String u, String h, int p) {
        currently = st;
        uuid = u;
        host = h;
        port = p;
    }

    public NsdService(String u) {
        this(Status.UNAVAILABLE, u, UNKNOWN_HOST, UNKNOWN_PORT);
    }

    public Status getStatus() {return currently;}
    public String getUuid() {return uuid;}
    public String getHost() {return host;}
    public int getPort() {return port;}

    public void resolved(NsdServiceInfo descriptor) {
        currently = Status.AVAILABLE;
        // Check this is an IPv4 ...
        host = descriptor.getHost().getHostAddress();
        port = descriptor.getPort();
    }

    public void markAsUnavailable() {
        currently = Status.UNAVAILABLE;
        host = UNKNOWN_HOST;
        port = UNKNOWN_PORT;
    }

    @Override
    public String toString() {
        return "NetworkService{" +
                "currently=" + currently +
                ", uuid='" + uuid + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        NsdService that = (NsdService) other;
        return this.currently == that.currently
                && this.uuid.equals(that.uuid)
                && this.host.equals(that.host)
                && this.port == that.port;
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}