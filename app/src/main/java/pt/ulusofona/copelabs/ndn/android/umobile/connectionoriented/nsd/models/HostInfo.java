package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models;


import java.io.Serializable;

public class HostInfo implements Serializable {

    protected String mIpAddress;
    protected int mPort;

    public HostInfo(String ipAddress, int port) {
        mIpAddress = ipAddress;
        mPort = port;
    }

    public String getIpAddress() {
        return mIpAddress;
    }

    public int getPort() {
        return mPort;
    }

    @Override
    public String toString() {
        return "host: " + mIpAddress + ":" + mPort;
    }

}
