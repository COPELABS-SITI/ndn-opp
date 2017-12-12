package pt.ulusofona.copelabs.ndn.android.umobile.manager;


import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import pt.ulusofona.copelabs.ndn.android.preferences.Configuration;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

public class PacketManager implements PacketListener.Manager, WifiP2pListener.ConnectionStatus {

    public static final String PACKET_KEY_PREFIX = "PKT:";
    private static final String TAG = PacketManager.class.getSimpleName();
    private static final int MAX_PAYLOAD_SIZE_CL = 80;

    // Maps Packet ID -> Name (Data)
    private Map<String, Packet> mPendingPackets = new HashMap<>();
    // Maps Nonce -> Packet ID
    private Map<Integer, String> mPendingInterestIdsFromNonces = new HashMap<>();
    // Maps Packet ID -> Nonce
    private Map<String, Integer> mPendingInterestNoncesFromIds = new HashMap<>();
    // Maps Name -> Packet ID (Data)
    private Map<String, String> mPendingDataIdsFromNames = new HashMap<>();
    // Maps Packet ID -> Name (Data)
    private Map<String, String> mPendingDataNamesFromIds = new HashMap<>();

    private PacketListener.Requester mRequester;
    private boolean mConnectionEstablished;
    private int mPacketId = 0;
    private Context mContext;

    public PacketManager(Context context, PacketListener.Requester requester) {
        mContext = context;
        mRequester = requester;
        WifiP2pListenerManager.registerListener(this);
    }

    @Override
    public void onTransferInterest(String sender, String recipient, byte[] payload, int nonce) {
        String pktId = generatePacketId();
        Packet packet = new Packet(pktId, sender, recipient, payload);
        mPendingPackets.put(pktId, packet);
        pushInterestPacket(pktId, nonce);
        sendPacket(packet);
    }

    @Override
    public void onTransferData(String sender, String recipient, byte[] payload, String name) {
        String pktId = generatePacketId();
        Packet packet = new Packet(pktId, sender, recipient, payload);
        mPendingPackets.put(pktId, packet);
        pushDataPacket(pktId, name);
        sendPacket(packet);
    }

    @Override
    public void onPacketTransferred(String pktId) {
        Packet packet = mPendingPackets.remove(pktId);
        if(isDataPacket(pktId)) {
            mRequester.onDataPacketTransferred(packet.getRecipient(), removeDataPacket(pktId));
        } else {
            mRequester.onInterestPacketTransferred(packet.getRecipient(), removeInterestPacket(pktId));
        }
    }

    @Override
    public void onCancelInterest(long faceId, int nonce) {

    }

    @Override
    public void onConnected() {
        Log.i(TAG, "Wi-Fi or Wi-Fi P2P connection detected");
        mConnectionEstablished = true;

    }

    @Override
    public void onDisconnected() {
        Log.i(TAG, "Wi-Fi or Wi-Fi P2P connection dropped");
        mConnectionEstablished = false;
    }

    private synchronized String generatePacketId() {
        return PACKET_KEY_PREFIX + (mPacketId++);
    }

    private void sendPacket(Packet packet) {
        if(Configuration.isBackupOptionEnabled(mContext)) {
            backupOption(packet);
        } else {
            payloadSizeOption(packet);
        }
    }

    private void payloadSizeOption(Packet packet) {
        if(packet.getPayloadSize() > MAX_PAYLOAD_SIZE_CL) {
            mRequester.onSendOverConnectionOriented(packet);
        } else {
            mRequester.onSendOverConnectionLess(packet);
        }
    }

    private void backupOption(Packet packet) {
        if(mConnectionEstablished) {
            mRequester.onSendOverConnectionOriented(packet);
        } else {
            mRequester.onSendOverConnectionLess(packet);
        }
    }

    private boolean isDataPacket(String pktId) {
        return mPendingDataNamesFromIds.get(pktId) != null;
    }

    private void pushDataPacket(String pktId, String name) {
        mPendingDataIdsFromNames.put(name, pktId);
        mPendingDataNamesFromIds.put(pktId, name);
    }

    private String removeDataPacket(String pktId) {
        String name = mPendingDataNamesFromIds.remove(pktId);
        mPendingDataIdsFromNames.remove(name);
        return name;
    }

    private void pushInterestPacket(String pktId, int nonce) {
        mPendingInterestIdsFromNonces.put(nonce, pktId);
        mPendingInterestNoncesFromIds.put(pktId, nonce);
    }

    private int removeInterestPacket(String pktId) {
        int nonce = mPendingInterestNoncesFromIds.remove(pktId);
        mPendingInterestIdsFromNonces.remove(nonce);
        return nonce;
    }

}
