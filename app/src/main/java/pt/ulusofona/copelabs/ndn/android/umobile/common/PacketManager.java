package pt.ulusofona.copelabs.ndn.android.umobile.common;


import android.content.Context;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;

public interface PacketManager {

    interface Manager {
        void enable(Context context, PacketManager.Requester requester, OpportunisticFaceManager oppFaceManager);
        void disable();
        void onTransferInterest(String sender, String recipient, byte[] payload, int nonce);
        void onTransferData(String sender, String recipient, byte[] payload, String name);
        void onPacketTransferred(String pktId);
        void onCancelInterest(long faceId, int nonce);
    }

    interface Requester {
        void onInterestPacketTransferred(String recipient, int nonce);
        void onDataPacketTransferred(String recipient, String name);
        void onCancelPacketSentOverConnectionLess(Packet packet);
        void onSendOverConnectionOriented(Packet packet);
        void onSendOverConnectionLess(Packet packet);
    }
}
