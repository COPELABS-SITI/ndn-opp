package pt.ulusofona.copelabs.ndn.android.umobile.common;


public interface PacketObserver {
    void onPacketTransferred(String recipient, String pktId);
    void onPacketReceived(String sender, byte[] payload);
}
