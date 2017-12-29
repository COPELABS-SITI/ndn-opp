package pt.ulusofona.copelabs.ndn.android.umobile.manager.host;

public interface HostManagerListener {

    interface Receiver extends HostManagerListener {
        void onReceiveIpPacket(HostPacket packet);
    }

    interface Sender extends HostManagerListener {
        void onReceiveIpAddress(String ipAddress);
    }

}
