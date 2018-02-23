/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class is a model to store NSD data
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models;


import java.io.Serializable;

public class NsdInfo extends HostInfo implements Serializable {

    /** This variable holds a device uuid */
    private String mUuid;

    public NsdInfo(String uuid, String ipAddress, int port) {
        super(ipAddress, port);
        mUuid = uuid;
    }

    /**
     * This method is a getter for mUuid attribue
     * @return mUuid
     */
    public String getUuid() {
        return mUuid;
    }

    @Override
    public String toString() {
        return "uuid: " + mUuid + " " + super.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof NsdInfo)) return false;

        NsdInfo nsdInfo = (NsdInfo) o;

        return mUuid != null ? mUuid.equals(nsdInfo.mUuid) : nsdInfo.mUuid == null;
    }

    @Override
    public int hashCode() {
        return mUuid != null ? mUuid.hashCode() : 0;
    }

}
