/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The WifiP2pService represents a device running a the NDN-Opp service.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

class WifiP2pService {
    static final String SVC_INSTANCE_TYPE = "_wifip2ptracker._tcp";

    private Status currently;
    private String uuid;
    private String addr;

    WifiP2pService(String u, Status s, String a) {
		currently = s;
        uuid = u;
		addr = a;
	}

    public String getUuid() {
        return uuid;
    }
    public Status getStatus() { return currently; }
    String getMacAddress() { return addr; }

    public void setStatus(Status s) {
        currently = s;
    }

    @Override
    public boolean equals(Object other) {
        if (this == other) return true;
        if (other == null || getClass() != other.getClass()) return false;

        WifiP2pService that = (WifiP2pService) other;
        return this.currently == that.currently
                && this.uuid.equals(that.uuid)
                && this.addr.equals(that.addr);
    }

    @Override
    public int hashCode() {
        return uuid.hashCode();
    }
}