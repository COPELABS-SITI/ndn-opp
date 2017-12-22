package pt.ulusofona.copelabs.ndn.android.umobile.manager.packet;


import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import pt.ulusofona.copelabs.ndn.android.preferences.Configuration;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticFaceManager;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

public class PacketManager implements PacketListener.Manager, WifiP2pListener.WifiP2pConnectionStatus {

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

    private OpportunisticFaceManager mOppFaceManager;
    private boolean mConnectionEstablished = false;
    private PacketListener.Requester mRequester;
    private int mPacketId = 0;
    private Context mContext;

    public PacketManager(Context context, PacketListener.Requester requester, OpportunisticFaceManager oppFaceManager) {
        mContext = context;
        mRequester = requester;
        mOppFaceManager = oppFaceManager;
        WifiP2pListenerManager.registerListener(this);
    }

    @Override
    public void onTransferInterest(String sender, String recipient, byte[] payload, int nonce) {
        Log.i(TAG, "Transferring interest from " + sender + " to " + recipient);
        String pktId = generatePacketId();
        Log.i(TAG, "It's packet id is " + pktId);
        Packet packet = new Packet(pktId, sender, recipient, payload);
        mPendingPackets.put(pktId, packet);
        pushInterestPacket(pktId, nonce);
        sendPacket(packet);
    }

    @Override
    public void onTransferData(String sender, String recipient, byte[] payload, String name) {
        Log.i(TAG, "Transferring interest from " + sender + " to " + recipient);
        String pktId = generatePacketId();
        Log.i(TAG, "It's packet id is " + pktId);
        Packet packet = new Packet(pktId, sender, recipient, payload);
        mPendingPackets.put(pktId, packet);
        pushDataPacket(pktId, name);
        sendPacket(packet);
    }

    @Override
    public void onPacketTransferred(String pktId) {
        Packet packet = mPendingPackets.remove(pktId);
        if(isDataPacket(pktId)) {
            Log.i(TAG, "Data packet with id " + pktId + " was transferred");
            mRequester.onDataPacketTransferred(packet.getRecipient(), removeDataPacket(pktId));
        } else {
            Log.i(TAG, "Interest packet with id " + pktId + " was transferred");
            mRequester.onInterestPacketTransferred(packet.getRecipient(), removeInterestPacket(pktId));
        }
    }

    @Override
    public void onCancelInterest(long faceId, int nonce) {
        String pktId = mPendingInterestIdsFromNonces.get(nonce);
        Log.i(TAG, "Cancelling interest packet id " + pktId);
        Packet packet = mPendingPackets.remove(pktId);
        if(pktId != null && packet != null) {
            mRequester.onCancelPacketSentOverConnectionLess(packet);
            removeInterestPacket(pktId);
        }
    }

    @Override
    public void disable() {
        mConnectionEstablished = false;
        WifiP2pListenerManager.unregisterListener(this);
    }

    @Override
    public void onConnected(Intent intent) {
        Log.i(TAG, "Wi-Fi or Wi-Fi P2P connection detected");
        mConnectionEstablished = true;
    }

    @Override
    public void onDisconnected(Intent intent) {
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
            if(mOppFaceManager.isSocketAvailable(packet.getRecipient())) {
                mRequester.onSendOverConnectionOriented(packet);
            }
        } else {
            mRequester.onSendOverConnectionLess(packet);
        }
    }

    private void backupOption(Packet packet) {
        if(mConnectionEstablished && mOppFaceManager.isSocketAvailable(packet.getRecipient())) {
            mRequester.onSendOverConnectionOriented(packet);
        } else {
            mRequester.onSendOverConnectionLess(packet);
        }
    }

    private boolean isDataPacket(String pktId) {
        return mPendingDataNamesFromIds.get(pktId) != null;
    }

    private void pushDataPacket(String pktId, String name) {
        Log.i(TAG, "Pushing data packet id " + pktId);
        mPendingDataIdsFromNames.put(name, pktId);
        mPendingDataNamesFromIds.put(pktId, name);
    }

    private String removeDataPacket(String pktId) {
        Log.i(TAG, "Removing data packet id " + pktId);
        String name = mPendingDataNamesFromIds.remove(pktId);
        mPendingDataIdsFromNames.remove(name);
        return name;
    }

    private void pushInterestPacket(String pktId, int nonce) {
        Log.i(TAG, "Pushing interest packet id " + pktId);
        mPendingInterestIdsFromNonces.put(nonce, pktId);
        mPendingInterestNoncesFromIds.put(pktId, nonce);
    }

    private int removeInterestPacket(String pktId) {
        Log.i(TAG, "Removing interest packet id " + pktId);
        int nonce = mPendingInterestNoncesFromIds.remove(pktId);
        mPendingInterestIdsFromNonces.remove(nonce);
        return nonce;
    }

}
