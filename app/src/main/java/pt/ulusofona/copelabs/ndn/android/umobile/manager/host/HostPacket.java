package pt.ulusofona.copelabs.ndn.android.umobile.manager.host;


import java.io.Serializable;

public class HostPacket implements Serializable {

    private String mIpAddress;
    private String mUuid;

    public HostPacket(String ipAddress, String uuid) {
        mIpAddress = ipAddress;
        mUuid = uuid;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public String getUuid() {
        return mUuid;
    }

    public boolean isHostValid() {
        return mIpAddress != null && mUuid != null;
    }
}
