package pt.ulusofona.copelabs.ndn.android.umobile.manager.packet;


import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

public interface PacketListener {

    interface Manager {
        void onTransferInterest(String sender, String recipient, byte[] payload, int nonce);
        void onTransferData(String sender, String recipient, byte[] payload, String name);
        void onPacketTransferred(String pktId);
        void onCancelInterest(long faceId, int nonce);
    }

    interface Requester {
        void onInterestPacketTransferred(String recipient, int nonce);
        void onDataPacketTransferred(String recipient, String name);
        void onSendOverConnectionOriented(Packet packet);
        void onSendOverConnectionLess(Packet packet);
        void onSendOverWifi(Packet packet);
    }
}
