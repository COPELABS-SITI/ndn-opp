/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The NsdService encapsulates the information required to establish a connection to that Service
 * running on some remote device.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.net.nsd.NsdServiceInfo;

/** The model class used to represent NsdServices discovered within the group to which this device is connected.
 *  A NsdService associates a UUID with a status along with an IP and a port number. The status reflect whether
 *  the service is currently reachable or not at the associated IP and port.
 */
public class NsdService {
    private static final String UNKNOWN_HOST = "0.0.0.0";
    private static final int UNKNOWN_PORT = 0;

    public static final String SERVICE_TYPE = "_nsdtracker._tcp";

    /**
     * Enumeration of possible statuses.
     */
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

    /** Default constructor.
     * @param uuid UUID of the device advertising the NsdService.
     */
    public NsdService(String uuid) {
        this(Status.UNAVAILABLE, uuid, UNKNOWN_HOST, UNKNOWN_PORT);
    }

    /** Obtain the status of this NsdService
     * @return the status currently known for this NsdService
     */
    public Status getStatus() {return currently;}

    /** Obtain the UUID of the device from which this NsdService is advertised
     * @return the UUID of the advertising device of this NsdService
     */
    public String getUuid() {return uuid;}

    /** Obtain the IP address of the device within the Wi-Fi Direct Group that is advertising this NsdService.
     * @return the IP address of the advertising device
     */
    public String getHost() {return host;}

    /** Obtain the port number under which the NDN-Opp daemon can be reached
     * @return the port number at the adversting device
     */
    public int getPort() {return port;}

    /** Updates the IP and port associated to this NsdService upon resolution by the Android platform
     * @param descriptor information encoding, among other things, the IP and port number.
     */
    public void resolved(NsdServiceInfo descriptor) {
        currently = Status.AVAILABLE;
        // Check this is an IPv4 ...
        host = descriptor.getHost().getHostAddress();
        port = descriptor.getPort();
    }

    /** Updates the NsdService to the unavailable state
     */
    public void markAsUnavailable() {
        currently = Status.UNAVAILABLE;
        host = UNKNOWN_HOST;
        port = UNKNOWN_PORT;
    }

    /** Create a pretty-print String of this NsdService.
     * @return String representing the values of this NsdService.
     */
    @Override
    public String toString() {
        return "NetworkService{" +
                "currently=" + currently +
                ", uuid='" + uuid + '\'' +
                ", host='" + host + '\'' +
                ", port=" + port +
                '}';
    }

    /** Equality test of two NsdServices
     * @param other NsdService against which this NsdService is to be compared with
     * @return true if-and-only-if status, UUID, IP and port of the two NsdServices are identical.
     */
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

    /** Hashcode operation.
     * @return Hash code of the NsdService. Based on String.hashCode() of the UUID.
     */
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}