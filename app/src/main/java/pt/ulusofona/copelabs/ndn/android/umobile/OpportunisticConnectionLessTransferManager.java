/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-08-25
 * This class implements the connection-less transfers using the Wi-Fi P2P Service Discovery
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.net.wifi.p2p.nsd.WifiP2pServiceInfo;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;
import java.util.regex.Pattern;

import pt.ulusofona.copelabs.ndn.android.Identity;
import pt.ulusofona.copelabs.ndn.android.OperationResult;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListener;
import pt.ulusofona.copelabs.ndn.android.wifi.p2p.WifiP2pListenerManager;

/** Manages the transfer of small payload packets through the TXT record of Wifi P2P Services.
 *  note that per http://www.drjukka.com/blog/wordpress/?p=127 (), this is not a sensible transfer
 *  mecanisms for passing packets of important size. Android implementations vary and a maximum of
 *  ±900 bytes seems to be the current limit. This does not consider if older versions are used which
 *  will be incapable of seeing the payloads.
 *
 *  Also, as per https://tools.ietf.org/html/rfc6763 (2013)
 *
 *     The total size of a typical DNS-SD TXT record is intended to be small
 *     -- 200 bytes or less.
 *
 *     In cases where more data is justified (e.g. LPR printing [BJP]),
 *     keeping the total size under 400 bytes should allow it to fit in a
 *     single 512-byte DNS message [RFC 1035].
 *
 *     In extreme cases where even this is not enough, keeping the size of
 *     the TXT record under 1300 bytes should allow it to fit in a single
 *     1500-byte Ethernet packet.
 *
 *     Using TXT records larger than 1300 bytes is NOT RECOMMENDED at this
 *     time.
 */
public class OpportunisticConnectionLessTransferManager implements Observer, WifiP2pManager.ChannelListener,
        WifiP2pListener.TxtRecordAvailable {
    public static final String TAG = OpportunisticConnectionLessTransferManager.class.getSimpleName();

    // Key used in the TXT-Record to store packets. Packet ID is appended after it.
    private static final String PACKET_KEY_PREFIX = "PKT:";
    private static int CURRENT_PACKET_ID = 0;

    // Key used in the TXT-Record to store the list of acknowledgments. The value is a comma-separated
    // string with all the Packet IDs that are being acknowledged.
    private static final String ACKNOWLEDGMENTS_KEY = "ACKs";
    private static final String ACKNOWLEDGMENT_ID_SEPARATOR = ",";

    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;
    private WifiP2pServiceInfo mDescriptor;

    // The Context to which this Manager is attached.
    private Observer mObservingContext;
    private String mAssignedUuid;

    private Set<String> mKnownPeers = new HashSet<>();

    // Associates the UUID of a recipient with the packets pending for it (PKT:ID, PAYLOAD)
    private Map<String, Map<String, String>> mPendingPackets = new HashMap<>();
    // Associates the UUID of a recipient with the descriptor containing the packets pending for it.
    private Map<String, WifiP2pDnsSdServiceInfo> mRegisteredDescriptors = new HashMap<>();

    // Associates the UUID of a packet sender with the acknowledgements addressed to that sender
    private Map<String, Set<String>> mPendingAcknowledgements = new HashMap<>();

    public void enable(Context context) {
        mContext = context;
        // Store the reference to the context observing us for calling back when packets are received or transfers acknowledged.
        mObservingContext = (Observer) context;

        // Retrieve the UUID this application should use.
        mAssignedUuid = Identity.getUuid();

        // Retrieve the instance of the WiFi P2P Manager, the Channel and create the DNS-SD Service request.


        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(context, context.getMainLooper(), this);
        //mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, this);

        mDescriptor = Identity.getDescriptor();

        WifiP2pListenerManager.registerListener(this);

        // Startup the Broadcast Receiver to react to changes in the state of WiFi P2P.
        mContext.registerReceiver(mIntentReceiver, new IntentFilter(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION));
    }

    public void disable() {
        // Cleanup upon detachment.
        mContext.unregisterReceiver(mIntentReceiver);
        WifiP2pListenerManager.unregisterListener(this);
        mWifiP2pManager.setDnsSdResponseListeners(mWifiP2pChannel, null, null);
    }

    /** Generate the next packet ID
     * @return a unique identifier for the next packet to be sent.
     */
    private String generatePacketId() {
        String msgId = Integer.toString(CURRENT_PACKET_ID);
        CURRENT_PACKET_ID += 1;
        return PACKET_KEY_PREFIX + msgId;
    }

    private static String digest(byte[] payload) {
        String sha1 = "FAIL";
        try {
            byte[] sha1sum = MessageDigest.getInstance("SHA-1").digest(payload);
            Formatter fmt = new Formatter();
            for(byte b : sha1sum)
                fmt.format("%02x", b);
            sha1 = fmt.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        return sha1;
    }

    /** Request the sending of a packet to an intended recipient.
     * @param recipient the intended recipient of the packet to be sent (UUID)
     * @param payload the bytes of the packet to be sent
     * @return the Packet ID assigned to this transfer by the manager. Used when the acknowledgement arrives.
     */
    public String sendPacket(String recipient, byte[] payload) {
        Log.i(TAG, "sendPacket [sha1sum=" + payload.length + "] <" + digest(payload) + ">");
        String pktId = generatePacketId();

        // Retrieve the packets pending for the recipient.
        Map<String, String> pendingPacketsForRecipient = mPendingPackets.get(recipient);
        if(pendingPacketsForRecipient == null)
            pendingPacketsForRecipient = new HashMap<>();

        // Base64 encoding of the String that guarantees Android works. NO_PADDING because if there are trailing ==, Android
        // does not report the TXT-Record ...
        String packet = Base64.encodeToString(payload, Base64.NO_PADDING);
        // Add the new packet to those intended for the recipient.
        pendingPacketsForRecipient.put(pktId, packet);
        mPendingPackets.put(recipient, pendingPacketsForRecipient);

        // Perform an update to the descriptor for that recipient.
        updateRegisteredDescriptor(recipient);

        return pktId;
    }

    /** Cancel a sending of a packet to an intended recipient.
     * @param recipient the intended recipient of the packet to be sent. Base64-encoded UUID.
     * @param pktId the ID of the packet to be cancelled
     */
    public void cancelPacket(String recipient, String pktId) {
        // Retrieve its pending packets
        Map<String, String> pendingPackets = mPendingPackets.get(recipient);
        Log.d(TAG, "Cancelling packet : " + pktId + " for <" + recipient + ">");

        // Lookup the packet, remove it from the pending ones for the recipient and update the descriptor.
        if(pendingPackets != null) {
            Log.d(TAG, "Pending packets : " + pendingPackets.toString());
            if(pendingPackets.containsKey(pktId)) {
                pendingPackets.remove(pktId);
                mPendingPackets.put(recipient, pendingPackets);
                updateRegisteredDescriptor(recipient);
            }
        }
    }

    /** Update the packet which encodes the list of acknowledgments for a given sender. When the list of acknowledgements
     * changes due to the removal of a packet from a remote TXT-Record, we remove our acknowledgement for it. As a consequence,
     * we must an update packet in our TXT-Record for that remote with the new list of acknowledgements.
     * @param sender the sender for whom our list of acknowledgements has changed
     */
    private void updatePendingAcknowledgements(String sender) {
        String acknowledgementList = "";
        List<String> pendingAcknowledgementsForSender = new ArrayList<>();
        if(mPendingAcknowledgements.containsKey(sender))
            pendingAcknowledgementsForSender.addAll(mPendingAcknowledgements.get(sender));

        int numberOfAcks = pendingAcknowledgementsForSender.size();

        Map<String, String> pendingPacketsForRemote = mPendingPackets.get(sender);
        if (pendingPacketsForRemote == null)
            pendingPacketsForRemote = new HashMap<>();

        if (numberOfAcks > 0) {
            for (int a = 0; a < numberOfAcks; a++) {
                acknowledgementList += pendingAcknowledgementsForSender.get(a);
                if (a < numberOfAcks - 1)
                    acknowledgementList += ACKNOWLEDGMENT_ID_SEPARATOR;
            }
            pendingPacketsForRemote.put(ACKNOWLEDGMENTS_KEY, acknowledgementList);
        } else {
            pendingPacketsForRemote.remove(ACKNOWLEDGMENTS_KEY);
        }

        mPendingPackets.put(sender, pendingPacketsForRemote);
    }

    /** Performs the registration of a Transfer Service Instance containing a TXT Records with all the pending packets.
     * @param recipient the intended recipient of this transfer (UUID)
     * @param pendingPacketsForRecipient the packets to be sent (PKT:ID, <payload>)
     */
    private void registerTransferDescriptor(final String recipient, Map<String, String> pendingPacketsForRecipient) {
        final WifiP2pDnsSdServiceInfo descriptor = Identity.getTransferDescriptorWithTxtRecord(recipient, pendingPacketsForRecipient);
        Log.v(TAG, "Register Descriptor : " + recipient + " = " + pendingPacketsForRecipient.toString());
        mWifiP2pManager.addLocalService(mWifiP2pChannel, descriptor, new WifiP2pManager.ActionListener() {
            @Override public void onSuccess() {
                Log.v(TAG, "Local Service added");
                mRegisteredDescriptors.put(recipient, descriptor);
            }
            @Override public void onFailure(int i) {}
        });
    }

    /** Updates the registered TXT Record for an intended recipient. Called when some changes happen in the list of packets (cancellation or acknowledgement)
     * @param recipient the intended recipient of this transfer (UUID).
     */
    private void updateRegisteredDescriptor(final String recipient) {
        final Map<String, String> pendingPacketsForRecipient = mPendingPackets.get(recipient);
        if(pendingPacketsForRecipient != null) {
            Log.d(TAG, "Updating Registered Descriptor :" + recipient + " = " + pendingPacketsForRecipient.toString());

            WifiP2pDnsSdServiceInfo descriptor = mRegisteredDescriptors.get(recipient);
            if (descriptor != null) {
                mWifiP2pManager.removeLocalService(mWifiP2pChannel, descriptor, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        mRegisteredDescriptors.remove(recipient);
                        registerTransferDescriptor(recipient, pendingPacketsForRecipient);
                    }

                    @Override
                    public void onFailure(int i) {

                    }
                });
            } else
                registerTransferDescriptor(recipient, pendingPacketsForRecipient);
        }
    }

    /** Specific listener for TXT Records. Invoked by Android when a new TXT-Record is found through a Service Discovery.
     */

    @Override
    public void onTxtRecordAvailable(String fulldomain, Map<String, String> txt, WifiP2pDevice srcDevice) {

            /* Take the fulldomain field and split it. The convention used by this implementation is that it contains three components;
               - [0] UUID of the sender (a.k.a. remote UUID)
               - [1] Type of the service (i.e. NDN-Opp)
               - [2] UUID of the intended recipient (a.k.a. local UUID)
             */

        Log.v(TAG, "Received TXT Record : " + fulldomain + " " + txt.toString());

            /* Android encodes the information describing the service as _<instanceName>._<serviceType>._local.
               We first split based on the '.' to separate the components into separate Strings. */
        String[] components = fulldomain.split(Pattern.quote("."));
        // Only consider TXT Records for fulldomain made up of at least three components.
        if (components.length >= 3) {
            final String remoteUuid = components[0];
            String serviceType = components[1];
            String localUuid = components[2];
                /* We only consider TXT Record if-and-only-if
                 * - It does not come from us (yes, Android notifies you of your own services)
                 * - Our assigned UUID matches that of the intended recipient (i.e. this TXT Record is intended for us)
                 * - The service type is the one we're interested in (i.e. NDN-Opp) */

            if (mKnownPeers.contains(remoteUuid) && !mAssignedUuid.equals(remoteUuid) && mAssignedUuid.equals(localUuid) && Identity.SVC_TRANSFER_TYPE.equals(serviceType)) {
                Log.i(TAG, "Received from <" + fulldomain + "> : " + txt.toString());

                // Perform an update to the acknowledgements we advertise to the remote UUID.
                Set<String> pendingAcknowledgements = mPendingAcknowledgements.get(remoteUuid);
                if(pendingAcknowledgements == null)
                    pendingAcknowledgements = new HashSet<>();

                    /* The fact that a packet ID we are acknowledging to the remote UUID is not in this TXT-Record
                     * means that the remote has received our ACK and removed the packet from the Record. As a consequence,
                     * we can remove all the acknowledgements whose packet ID do not appear in the TXT-Record and only keep the ones
                     * that do appear in it. */
                Set<String> packetKeys = txt.keySet();
                boolean acknowledgmentChanges = pendingAcknowledgements.retainAll(packetKeys);

                // Retrieve the pending packets for the remote UUID
                Map<String, String> pendingPackets = mPendingPackets.get(remoteUuid);
                boolean packetsRemoved = false;

                    /* For all the packet identifiers registered in the TXT-Record, we process it based on whether
                     * it is a packet identifier (i.e. PKT:n, for some n). */
                for (String pktKey : packetKeys) {
                        /* In the case the packet key starts with "PKT:", this is a packet. In response, we add an acknowledgement
                         * for it addressed to the remote and notify the context observing us that a packet was received from the remote
                         * along with its payload. Only do so if it is the first time we see this packet (i.e. we haven't acknowledged
                         * it before) */
                    if (pktKey.startsWith(PACKET_KEY_PREFIX)) {
                        // Only consider this packet if we haven't seen it before.
                        if(!pendingAcknowledgements.contains(pktKey)) {
                            acknowledgmentChanges |= pendingAcknowledgements.add(pktKey);
                            final byte[] payload = Base64.decode(txt.get(pktKey), Base64.NO_PADDING);
                            Log.i(TAG, "receivedPacket [" + payload.length + "] <" + digest(payload) + ">");
                            mObservingContext.onPacketReceived(remoteUuid, payload);
                        }
                        /* In the case the packet key equals "ACKs", this is a list of acknowledgements for some of the packets
                         * that we sent to the remote. In response, we remove all the packets that are acknowledged and notify
                         * the context observing us of the success of those transfers. */
                    } else if (pktKey.equals(ACKNOWLEDGMENTS_KEY)) {
                        Log.d(TAG, "c : " + txt.get(ACKNOWLEDGMENTS_KEY));
                        if(pendingPackets != null) {
                            // The list of ACKs is a comma-separated list of packet IDs.
                            String ackIdentifiers[] = txt.get(pktKey).split(Pattern.quote(ACKNOWLEDGMENT_ID_SEPARATOR));
                            Log.d(TAG, "Acknowedgement list : " + Arrays.toString(ackIdentifiers));
                            Log.d(TAG, "Pending packets    : " + pendingPackets);
                                /* We go through the list of ACK IDs and for each, we remove the pending packet and notify
                                 * the parent Activity that is was successfully transferred. */
                            for (final String ackId : ackIdentifiers) {
                                if(pendingPackets.containsKey(ackId)) {
                                    pendingPackets.remove(ackId);
                                    packetsRemoved |= true;
                                    mObservingContext.onPacketTransferred(remoteUuid, ackId);
                                }
                            }
                        } else
                            Log.d(TAG, "No pending packets found");
                    }
                }

                // If packets were removed from the pendingPackets list, we must update it in the Map
                if(packetsRemoved)
                    mPendingPackets.put(remoteUuid, pendingPackets);

                    /* If there were any changes to the list of acknowledgements, we perform an update */
                if (acknowledgmentChanges) {
                    mPendingAcknowledgements.put(remoteUuid, pendingAcknowledgements);
                    updatePendingAcknowledgements(remoteUuid);
                }

                // If there are any changes at all, we must update the registered Transfer Service Instance with a new TXT-Record
                if (acknowledgmentChanges || packetsRemoved)
                    updateRegisteredDescriptor(remoteUuid);
            }
        }
    }

    @Override public void onChannelDisconnected() {
        Log.e(TAG, "onChannelDisconnected");
    }

    private final BroadcastReceiver mIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {
                int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
                if(state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                    Log.e(TAG, "LINHA 406 BROADCAST RECEIVER");
                    mWifiP2pManager.addLocalService(mWifiP2pChannel, mDescriptor, new OperationResult(TAG, "Local Service addition"));
                } else {
                    mWifiP2pManager.clearServiceRequests(mWifiP2pChannel, new OperationResult(TAG, "Clear Service Requests"));
                    mWifiP2pManager.clearLocalServices(mWifiP2pChannel, new OperationResult(TAG, "Clear Local Services"));
                }
            }
        }
    };

    /** Used by the Opportunistic Peer Tracker to notify of changes in the list of Peers.
     * @param observable
     * @param obj
     */
    @Override
    public void update(Observable observable, Object obj) {
        if (observable instanceof OpportunisticPeerTracker) {
            Map<String, OpportunisticPeer> peers = (Map<String, OpportunisticPeer>) obj;
            /* If the peer is unknown (i.e. its UUID is not in the list of UMobile peers,
             * we request the creation of a Face for it. Then, if the Face has an ID referenced
             * in the existing Opportunistic faces, bring it up. */
            if(peers != null)
                mKnownPeers.addAll(peers.keySet());
        }
    }


    /** Implemented by the Context object so that the Manager can notify of Transfer successes and received packets.
     */
    public interface Observer {
        void onPacketTransferred(String recipient, String pktId);
        void onPacketReceived(String sender, byte[] payload);
    }
}