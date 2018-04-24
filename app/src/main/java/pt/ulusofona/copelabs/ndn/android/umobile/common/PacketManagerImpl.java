/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-12-22
 * This class manages the packet flow over the application.
 * It decides if the packet is transferred over connection less or
 * connection oriented.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.common;


import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulusofona.copelabs.ndn.android.preferences.Configuration;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

public class PacketManagerImpl implements Runnable, PacketManager.Manager, WifiP2pListener.WifiP2pConnectionStatus {

    /** This variable is used as a label for packets */
    public static final String PACKET_KEY_PREFIX = "PKT:";

    /** This variable is used to debug PacketManagerImpl class */
    private static final String TAG = PacketManagerImpl.class.getSimpleName();

    /** This variable is used to delay the pending packets sending in order to create its sockets */
    private static final int WAIT_TIME_FOR_SOCKETS_CREATION = 5000;

    /** This variable stores the max number of bytes allowed to send over connection less */
    private static final int MAX_PAYLOAD_CL = 200;

    /** This variable stores the max number of bytes allowed to send over connection less by KITKAT */
    private static final int MAX_PAYLOAD_CL_KITKAT = 80;

    private static ArrayList<PacketManager.Listener> sListeners = new ArrayList<>();

    /** Maps Packet ID -> Name (WifiP2pCache) */
    private ConcurrentHashMap<String, Packet> mPendingPackets = new ConcurrentHashMap<>();

    /** Maps Nonce -> Packet ID */
    private ConcurrentHashMap<Integer, String> mPendingInterestIdsFromNonces = new ConcurrentHashMap<>();

    /** Maps Packet ID -> Nonce */
    private ConcurrentHashMap<String, Integer> mPendingInterestNoncesFromIds = new ConcurrentHashMap<>();

    /** Maps Name -> Packet ID (WifiP2pCache) */
    private ConcurrentHashMap<String, String> mPendingDataIdsFromNames = new ConcurrentHashMap<>();

    /** Maps Packet ID -> Name (WifiP2pCache) */
    private ConcurrentHashMap<String, String> mPendingDataNamesFromIds = new ConcurrentHashMap<>();

    /** This object is need to check if there is a connection oriented connection for an uuid */
    private OpportunisticFaceManager mOppFaceManager;

    /** This variable holds the Wi-Fi P2P connection state */
    private boolean mConnectionEstablished = false;

    /** This interface is used to communicate with the binder */
    private PacketManager.Requester mRequester;

    private Handler mHandler = new Handler();

    /** This variable is used to generate packet ids */
    private int mPacketId = 0;

    /** This object holds the application context */
    private Context mContext;

    /** This variable holds the state of this class, if is running or not */
    private boolean mEnable;



    /**
     * This method registers a listener
     * @param listener listener to be registered
     */
    public static void registerListener(PacketManager.Listener listener) {
        sListeners.add(listener);
    }

    /**
     * This method unregisters a listener
     * @param listener listener to be unregistered
     */
    public static void unregisterListener(PacketManager.Listener listener) {
        sListeners.remove(listener);
    }

    /**
     * This method notifies its listeners about an interest that was transferred
     * @param name interest name
     */
    private static void notifyInterestTransferred(String sender, String name){
        Log.i(TAG, "notifyInterestTransferred");
        for(PacketManager.Listener listener : sListeners) {
            listener.onInterestTransferred(sender, name);
        }
    }

    /**
     * This method notifies its listeners about a data packet that was transferred
     * @param name data name
     */
    private static void notifyDataReceived(String sender, String name){
        Log.i(TAG, "notifyDataReceived");
        for(PacketManager.Listener listener : sListeners) {
            listener.onDataReceived(sender, name);
        }
    }

    /**
     * This method starts the packet manager
     * @param context application context
     * @param requester packet requester
     * @param oppFaceManager opp face manager
     */
    @Override
    public synchronized void enable(Context context, PacketManager.Requester requester, OpportunisticFaceManager oppFaceManager) {
        if(!mEnable) {
            mContext = context;
            mRequester = requester;
            mOppFaceManager = oppFaceManager;
            WifiP2pListenerManager.registerListener(this);
            Log.i(TAG, "Max connection-less packet size supported by this api is " + getMaxPayloadByAndroidApi() + " bytes");
            mEnable = true;
        }
    }

    /**
     * This method stops the packet manager
     */
    @Override
    public synchronized void disable() {
        if(mEnable) {
            WifiP2pListenerManager.unregisterListener(this);
            mConnectionEstablished = false;
            mEnable = false;
        }
    }

    /**
     * This method is invoked when an interest packet is going to be transferred
     * @param sender sender uuid
     * @param recipient recipient uuid
     * @param payload payload to send
     * @param nonce interest's nonce
     */
    @Override
    public synchronized void onTransferInterest(String sender, String recipient, int nonce, String name, byte[] payload) {
        Log.i(TAG, "Transferring interest from " + sender + " to " + recipient);
        String pktId = generatePacketId();
        Log.i(TAG, "It's packet id is " + pktId);
        Packet packet = new Packet(pktId, sender, recipient, payload);
        mPendingPackets.put(pktId, packet);
        pushInterestPacket(pktId, nonce);
        notifyInterestTransferred(recipient, packet.getName());
        sendPacket(packet);
    }

    /**
     * This method is invoked when a data packet is going to be transferred
     * @param sender sender uuid
     * @param recipient recipient uuid
     * @param payload payload to send
     * @param name data's name
     */
    @Override
    public synchronized void onTransferData(String sender, String recipient, String name, byte[] payload) {
        Log.i(TAG, "Transferring interest from " + sender + " to " + recipient);
        String pktId = generatePacketId();
        Log.i(TAG, "It's packet id is " + pktId);
        Packet packet = new Packet(pktId, sender, recipient, payload);
        mPendingPackets.put(pktId, packet);
        pushDataPacket(pktId, name);
        sendPacket(packet);
    }

    /**
     * This method is invoked when the packet was already transferred
     * @param pktId id of transferred packet
     */
    @Override
    public synchronized void onPacketTransferred(String pktId) {
        if(mPendingPackets.containsKey(pktId)) {
            Packet packet = mPendingPackets.remove(pktId);
            if (isDataPacket(pktId)) {
                Log.i(TAG, "Data packet with id " + pktId + " was transferred");
                mRequester.onDataPacketTransferred(packet.getRecipient(), removeDataPacket(pktId));
            } else {
                Log.i(TAG, "Interest packet with id " + pktId + " was transferred");
                mRequester.onInterestPacketTransferred(packet.getRecipient(), removeInterestPacket(pktId));
            }
        }
    }

    @Override
    public void onPacketReceived(String sender, byte[] payload) {
        notifyDataReceived(sender, Packet.getName(payload));
    }

    /**
     * This method is used to cancel an interest
     * @param faceId interest's face
     * @param nonce interest's nonce
     */
    @Override
    public synchronized void onCancelInterest(long faceId, int nonce) {
        /*
        String pktId = mPendingInterestIdsFromNonces.get(nonce);
        if(pktId != null) {
            Log.i(TAG, "Cancelling interest packet id " + pktId);
            removeInterestPacket(pktId);
            Packet packet = mPendingPackets.get(pktId);
            sendPacket(packet);
        }
        */
    }

    /**
     * This method is invoked when a Wi-Fi P2P connection established
     * @param intent intent
     */
    @Override
    public void onConnected(Intent intent) {
        Log.i(TAG, "Wi-Fi or Wi-Fi P2P connection detected");
        mConnectionEstablished = true;
        mHandler.postDelayed(this, WAIT_TIME_FOR_SOCKETS_CREATION);
    }

    @Override
    public void run() {
        sendPendingPackets();
    }

    private void sendPendingPackets() {
        for (Map.Entry packetEntry : mPendingPackets.entrySet()) {
            sendPacket(mPendingPackets.get(packetEntry.getKey()));
        }
    }

    /**
     * This method is invoked when the Wi-Fi P2P connection goes down
     * @param intent intent
     */
    @Override
    public void onDisconnected(Intent intent) {
        Log.i(TAG, "Wi-Fi or Wi-Fi P2P connection dropped");
        mConnectionEstablished = false;
    }

    /**
     * This method is used to generate a packet id
     * @return packet id
     */
    private synchronized String generatePacketId() {
        return PACKET_KEY_PREFIX + (mPacketId++);
    }

    /**
     * This method is used to send the packets and decides where the packet will be sent
     * @param packet packet to send
     */
    private void sendPacket(Packet packet) {
        if(Configuration.isBackupOptionEnabled(mContext)) {
            backupOption(packet);
        } else {
            packetSizeOption(packet);
        }
    }

    /**
     * This method uses the size of packet approach.
     * if the packet has more than MAX_PAYLOAD_SIZE_CL (80) bytes, it will be
     * sent over connection oriented. Otherwise, it will be sent over connection less
     * @param packet packet to be sent
     */
    private void packetSizeOption(Packet packet) {
        Log.i(TAG, "Using packet size option");
        Log.i(TAG, "Sending packet with id " + packet.getId() + " and size " + packet.getPayloadSize());
        if(packet.getPayloadSize() > getMaxPayloadByAndroidApi()) {
            if(mOppFaceManager.isSocketAvailable(packet.getRecipient())) {
                mRequester.onSendOverConnectionOriented(packet);
                Log.i(TAG, "Packet with id " + packet.getId() + " sent over connection oriented");
            }
        } else if(packet.getPayloadSize() <= getMaxPayloadByAndroidApi()) {
            mRequester.onSendOverConnectionLess(packet);
            Log.i(TAG, "Packet with id " + packet.getId() + " sent over connection less");
        } else {
            Log.e(TAG, "Packet not sent with id " + packet.getId() + " and size " + packet.getPayloadSize());
        }
    }

    /**
     * This method uses the backup approach
     * If there is a connection oriented way to send the data, NDN-OPP will use it.
     * Otherwise, the packet will be send using connection less
     * @param packet packet to be sent
     */
    private void backupOption(Packet packet) {
        if(packet != null) {
            Log.i(TAG, "Using backup option");
            Log.i(TAG, "Sending packet with id " + packet.getId() + " and size " + packet.getPayloadSize());
            if (mConnectionEstablished && mOppFaceManager.isSocketAvailable(packet.getRecipient())) {
                mRequester.onSendOverConnectionOriented(packet);
                Log.i(TAG, "Packet with id " + packet.getId() + " sent over connection oriented");
            } else if (packet.getPayloadSize() < getMaxPayloadByAndroidApi()) {
                mRequester.onSendOverConnectionLess(packet);
                Log.i(TAG, "Packet with id " + packet.getId() + " sent over connection less");
            } else {
                Log.e(TAG, "Packet not sent with id " + packet.getId() + " and size " + packet.getPayloadSize());
            }
        }
    }

    private int getMaxPayloadByAndroidApi() {
        return android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP ? MAX_PAYLOAD_CL : MAX_PAYLOAD_CL_KITKAT;
    }

    /**
     * This method returns true if the packet id passed as a parameter
     * belongs to a data packet. Returns false if not.
     * @param pktId packet id
     * @return
     */
    private boolean isDataPacket(String pktId) {
        return mPendingDataNamesFromIds.get(pktId) != null;
    }

    /**
     * This method associates the name to it's packet id and vice versa
     * @param pktId packet id
     * @param name data's name
     */
    private synchronized void pushDataPacket(String pktId, String name) {
        Log.i(TAG, "Pushing data packet id " + pktId);
        mPendingDataIdsFromNames.put(name, pktId);
        mPendingDataNamesFromIds.put(pktId, name);
    }

    /**
     * This method disassociates the name to it's packet id and vice versa.
     * @param pktId packet id
     * @return returns the name that was associated to this packet id
     */
    private synchronized String removeDataPacket(String pktId) {
        Log.i(TAG, "Removing data packet id " + pktId);
        String name = mPendingDataNamesFromIds.remove(pktId);
        mPendingDataIdsFromNames.remove(name);
        return name;
    }

    /**
     * This method associates the nonce to it's packet id and vice versa
     * @param pktId packet id
     * @param nonce interest's nonce
     */
    private synchronized void pushInterestPacket(String pktId, int nonce) {
        Log.i(TAG, "Pushing interest packet id " + pktId);
        mPendingInterestIdsFromNonces.put(nonce, pktId);
        mPendingInterestNoncesFromIds.put(pktId, nonce);
    }

    /**
     * This method disassociates the name to it's packet id and vice versa.
     * @param pktId packet id
     * @return returns the nonce that was associated to this packet id
     */
    private synchronized int removeInterestPacket(String pktId) {
        Log.i(TAG, "Removing interest packet id " + pktId);
        int nonce = mPendingInterestNoncesFromIds.remove(pktId);
        mPendingInterestIdsFromNonces.remove(nonce);
        return nonce;
    }

}
