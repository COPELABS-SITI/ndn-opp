/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The NsdData encapsulates the information required to establish a connection to that Service
 * running on some remote device.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.net.nsd.NsdServiceInfo;
import android.os.Handler;
import android.util.Log;


/** The class used to represent NsdServices discovered within the Wi-Fi Direct Group to which this device is connected.
 *  A NsdData associates a UUID with a status along with an IP and a port number. The status reflect whether
 *  the service is currently reachable or not at the associated IP and port.
 */
public class NsdService implements Runnable {

    public static final int DEFAULT_PORT = 16363;
    public static final String SERVICE_TYPE = "_ndnopp._tcp.";
    private static final String TAG = NsdService.class.getSimpleName();
    private static final int LOST_TIME_INTERVAL = 40 * 1000;
    private static final String UNKNOWN_HOST = "0.0.0.0";
    private static final int UNKNOWN_PORT = 0;
    //private Handler mHandler = new Handler();

    /** Enumeration of possible statuses. */
    public enum Status {
        AVAILABLE("Av"),
        UNAVAILABLE("Un");
		private String symbol;
		Status(String s) { symbol = s; }
		public String toString() { return symbol; }
    }

    private Status currently;
    private String uuid;
    public String host;
    public int port;

    /** Default constructor.
     * @param uuid UUID of the device advertising the NsdData.
     */
    public NsdService(String uuid) {
        this.currently = Status.UNAVAILABLE;
        this.uuid = uuid;
        this.host = UNKNOWN_HOST;
        this.port = UNKNOWN_PORT;
    }

    public NsdService(String uuid, String host) {
        this.uuid = uuid;
        this.host = host;
        this.port = DEFAULT_PORT;
        this.currently = Status.AVAILABLE;
    }

    public NsdService(String uuid, String host, int port) {
        this.uuid = uuid;
        this.host = host;
        this.port = port;
        this.currently = Status.AVAILABLE;
        //mHandler.postDelayed(this, LOST_TIME_INTERVAL);
    }

    public void refresh() {
        //mHandler.removeCallbacks(this);
        //mHandler.postDelayed(this, LOST_TIME_INTERVAL);
    }

    public static NsdService convert(NsdServiceInfo descriptor) {
        String[] data = descriptor.getServiceName().split("_");
        return new NsdService(data[0], data[1], DEFAULT_PORT);
    }

    public static String convertUuidFromServiceName(String serviceName) {
        return serviceName.split("_")[0];
    }

    public Status getStatus() {return currently;}
    public String getUuid() {return uuid;}
    public String getHost() {return host;}
    public int getPort() {return port;}
    public boolean isHostValid() { return !UNKNOWN_HOST.equals(host); }
    public boolean isHostValid(String host) { return !UNKNOWN_HOST.equals(host); }

    /** Updates the IP and port associated to this NsdData upon resolution by the Android platform
     * @param descriptor information encoding, among other things, the IP and port number.
     */
    public void resolved(NsdServiceInfo descriptor) {
        currently = Status.AVAILABLE;
        refresh();
        if(isHostValid(descriptor.getHost().getHostAddress())) {
            // Use the broken host resolution implementation on older APIs
            host = descriptor.getHost().getHostAddress();
            Log.v(TAG, "Resolved " + this.uuid + "@" + this.host);
            port = descriptor.getPort();
        }
    }

    /** Updates the NsdData to the unavailable state */
    public void markAsUnavailable() {
        Log.e(TAG, uuid + " expired. Was marked as unavailable");
        currently = Status.UNAVAILABLE;
        host = UNKNOWN_HOST;
        port = UNKNOWN_PORT;
    }

    public void destroy() {
        //mHandler.removeCallbacks(this);
        //mListener = null;
    }

    @Override
    public void run() {
        markAsUnavailable();
        /*
        if(mListener != null)
            mListener.refresh(this);
            */
    }

    /** Create a pretty-print String of this NsdData.
     * @return String representing the values of this NsdData.
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
     * @param other NsdData against which this NsdData is to be compared with
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
     * @return Hash code of the NsdData. Based on String.hashCode() of the UUID.
     */
    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}