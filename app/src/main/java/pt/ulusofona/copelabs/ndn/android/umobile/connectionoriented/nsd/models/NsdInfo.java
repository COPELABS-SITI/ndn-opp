package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models;


import java.io.Serializable;

public class NsdInfo extends HostInfo implements Serializable {

    private String mUuid;

    public NsdInfo(String uuid, String ipAddress, int port) {
        super(ipAddress, port);
        mUuid = uuid;
    }

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
