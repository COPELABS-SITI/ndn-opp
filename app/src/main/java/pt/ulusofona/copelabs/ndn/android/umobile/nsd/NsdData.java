package pt.ulusofona.copelabs.ndn.android.umobile.nsd;


class NsdData {

    private int mPort;
    private String mUuid, mIpAddress;

    NsdData(String uuid, String ipAddress, int port) {
        mIpAddress = ipAddress;
        mUuid = uuid;
        mPort = port;
    }

    String getUuid() {
        return mUuid;
    }

    String getIpAddress() {
        return mIpAddress;
    }

    int getPort() {
        return mPort;
    }

}
