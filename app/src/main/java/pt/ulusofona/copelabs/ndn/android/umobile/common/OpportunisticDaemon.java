/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The main wrapper in Java of the NDN Forwarding Daemon for use on the Android platform.
 * This class embodies the implementation of the NDN-Opp framework.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.common;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.Base64;
import android.util.Log;
import android.util.LongSparseArray;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.models.CsEntry;
import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.models.FibEntry;
import pt.ulusofona.copelabs.ndn.android.models.Name;
import pt.ulusofona.copelabs.ndn.android.models.PitEntry;
import pt.ulusofona.copelabs.ndn.android.models.SctEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionless.Identity;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionless.OpportunisticConnectionLessTransferManager;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.OpportunisticChannelIn;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.OpportunisticConnectivityManager;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.Packet;
import pt.ulusofona.copelabs.ndn.android.umobile.manager.packet.PacketListener;
import pt.ulusofona.copelabs.ndn.android.umobile.manager.packet.PacketManager;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceDiscoverer;
import pt.ulusofona.copelabs.ndn.android.utilities.Utilities;
import pt.ulusofona.copelabs.ndn.android.wifi.Wifi;

/** JNI wrapper around the NDN Opportunistic Daemon (NOD), which is the modified version of NFD to include the Opportunistic Faces,
 * and PUSH communications. This class provides an interface entirely through JNI to manage the configuration of the running
 * NOD including face creation, destruction and addition of routes to the RIB.
 */
public class OpportunisticDaemon extends Service implements PacketObserver, PacketListener.Requester {
    private static final String TAG = OpportunisticDaemon.class.getSimpleName();

	public static final String STARTED = "pt.ulusofona.copelabs.ndn.android.service.STARTED";
	public static final String STOPPING = "pt.ulusofona.copelabs.ndn.android.service.STOPPING";

    public static final IntentFilter mIntents = new IntentFilter();
    static {
        mIntents.addAction(STARTED);
        mIntents.addAction(STOPPING);
    }

    private enum State { STARTED , STOPPED }
    private NsdServiceDiscoverer mServiceTracker = NsdServiceDiscoverer.getInstance();

    public class Binder extends android.os.Binder {
        public long getUptime() { return (current == State.STARTED) ? System.currentTimeMillis() - startTime : 0L; }
        public String getUmobileUuid() { return (mAssignedUuid != null) ? mAssignedUuid : getString(R.string.notAvailable); }
        public String getVersion() { return jniGetVersion(); }
        public boolean isFaceUp(long faceId) { return jniIsFaceUp(faceId); }
        public long getFaceId(String uri) { return jniGetFaceId(uri); }
        public List<Name> getNameTree() { return jniGetNameTree(); }
        public List<Face> getFaceTable() { return jniGetFaceTable(); }
        public void createFace(String faceUri, int persistency, boolean localFields) { jniCreateFace(faceUri, persistency, localFields); }
        public void destroyFace(long faceId) { jniDestroyFace(faceId); }
        public void bringUpFace(long faceId) { jniBringUpFace(faceId); }
        public void bringDownFace(long faceId) { jniBringDownFace(faceId); }
        public void passData(long faceId, String name) { jniPushData(faceId, name); }
        public void passInterests(long faceId, String name) { jniPassInterests(faceId, name); }
        public List<FibEntry> getForwardingInformationBase() { return jniGetForwardingInformationBase(); }
        public void addRoute(String prefix, long faceId, long origin, long cost, long flags) { jniAddRoute(prefix, faceId, origin, cost, flags);}
        public List<PitEntry> getPendingInterestTable() { return jniGetPendingInterestTable(); }
        public List<CsEntry> getContentStore() { return jniGetContentStore(); }
        public List<SctEntry> getStrategyChoiceTable() { return jniGetStrategyChoiceTable(); }
    }

    private final Binder local = new Binder();

    // Start time
	private long startTime;

    // FaceTable
    private LongSparseArray<Face> mFacetable = new LongSparseArray<>();

    // Assigned UUID
    private String mAssignedUuid;
    // String containing the configuration stored in /res/raw/nfd_config
    private String mConfiguration;

    private OpportunisticPeerTracker mPeerTracker = new OpportunisticPeerTracker();
    private OpportunisticFaceManager mOppFaceManager = new OpportunisticFaceManager();

    private OpportunisticChannelIn mOppChannelIn = new OpportunisticChannelIn();
    private OpportunisticConnectivityManager mConnectivityManager = new OpportunisticConnectivityManager();
    private OpportunisticConnectionLessTransferManager mConnectionLessManager = new OpportunisticConnectionLessTransferManager();

    private Wifi mWifi;

    private PacketListener.Manager mPacketManager;



    // Custom lock to regulate the transitions between STARTED and STOPPED.
    // @TODO: Replace this with a standard lock.

    private State current = State.STOPPED;
	private synchronized State getAndSetState(State nextState) {
		State oldValue = current;
		current = nextState;
		return oldValue;
	}

    /** Initializes the ForwardingDaemon; retrieve the UUID, initialize the Routing Engine, construct the configuration string.
     *  Service lifecycle method. See https://developer.android.com/guide/components/services.html */
    @Override
    public void onCreate() {
        super.onCreate();

        // Retrieve the contents of the configuration file to build a String out of it.
        StringBuilder configuration = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.nfd_conf)));
            try {
                while (br.ready()) configuration.append(br.readLine()).append("\n");
            } finally { br.close(); }
        } catch (IOException e) {
            Log.d(TAG, "I/O error while reading configuration : " + e.getMessage());
        }
        Log.d(TAG, "Read configuration : " + configuration.length());

        mConfiguration = configuration.toString();



    }

    /** Starts the NDN Opportunistic Daemon and enables all the software components of NDN-Opp. Service lifecycle method.
     *  See https://developer.android.com/guide/components/services.html */
    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(State.STOPPED == getAndSetState(State.STARTED)) {
            // Retrieve the UUID
            mAssignedUuid = Identity.getUuid();
            jniStart(getFilesDir().getAbsolutePath(), mConfiguration);
            Utilities.turnOnWifi(this);

			startTime = System.currentTimeMillis();

            mPeerTracker.addObserver(mOppFaceManager);
            mPeerTracker.addObserver(mConnectionLessManager);

            mPeerTracker.enable(this);
            mOppFaceManager.enable(local, this);
            mConnectionLessManager.enable(this);
            mOppChannelIn.enable(local, this);

            mConnectivityManager.enable(this);
            mWifi = new Wifi(this);
            mWifi.start();

            mServiceTracker.addObserver(mOppFaceManager);
            mServiceTracker.enable(this, mAssignedUuid);

            mPacketManager = new PacketManager(this, this, mOppFaceManager);

            //new WifiFaceManager(this, local);

            Log.d(TAG, STARTED);
            sendBroadcast(new Intent(STARTED));

		}
		return START_NOT_STICKY;
	}

    /** Service lifecycle method. See https://developer.android.com/guide/components/services.html */
	@Override
    public IBinder onBind(Intent in) {
		return local;
	}

    /** Stops the NDN Opportunistic Daemon and disables all the software components of NDN-Opp. Service lifecycle method.
     * See https://developer.android.com/guide/components/services.html */
	@Override
	public void onDestroy() {
		if(State.STARTED == getAndSetState(State.STOPPED)) {

			jniStop();

            mOppChannelIn.disable();
            mConnectivityManager.disable();
            mConnectionLessManager.disable();
            mOppFaceManager.disable();
            mPeerTracker.disable();
            mPacketManager.disable();
            mWifi.close();

			sendBroadcast(new Intent(STOPPING));
			stopSelf();
            super.onDestroy();
		}
	}


    /** Retrieve a given Face
     * @param faceId the FaceId for which the Face should be returned
     * @return the Face or null if no Face has that faceId
     */
    public Face getFace(long faceId) {
        return mFacetable.get(faceId);
    }

    // Called by the C++ daemon when it adds a Face to its FaceTable.
    private void afterFaceAdded(Face face) {
        long faceId = face.getFaceId();
        mFacetable.put(faceId, face);
        mOppFaceManager.afterFaceAdded(face);
    }

    // Called by the C++ daemon when it adds a Face to its FaceTable.
    private void beforeFaceRemoved(Face face) {
        long faceId = face.getFaceId();
        mFacetable.remove(faceId);
    }

    // Called from JNI
    private void transferData(long faceId, String name, byte[] payload) {
        Log.d(TAG, "Transfer Data : " + faceId + " " + name + " length (" + payload.length + ") [" + Base64.encodeToString(payload, Base64.NO_PADDING) + "]");
        String recipient = mOppFaceManager.getUuid(faceId);
        mPacketManager.onTransferData(mAssignedUuid, recipient, payload, name);
    }

    // Called from JNI
    private void transferInterest(long faceId, int nonce, byte[] payload) {
        Log.d(TAG, "Transfer Interest : " + faceId + " " + nonce + " length (" + ((payload != null) ? payload.length : "NULL") + ")");
        String recipient = mOppFaceManager.getUuid(faceId);
        mPacketManager.onTransferInterest(mAssignedUuid, recipient, payload, nonce);
    }

    // Called from JNI
    private void cancelInterest(long faceId, int nonce) {
        Log.d(TAG, "Cancel Interest : " + faceId + " " + nonce);
        mPacketManager.onCancelInterest(faceId, nonce);
    }

    @Override
    public void onPacketTransferred(String recipient, String pktId) {
        Log.i(TAG, "Packet transferred from : " + recipient + " with id : [" + pktId + "]");
        Long faceId = mOppFaceManager.getFaceId(recipient);
        if(faceId != null) {
            mPacketManager.onPacketTransferred(pktId);
        }
    }

    @Override
    public void onInterestPacketTransferred(String recipient, int nonce) {
        jniOnInterestTransferred(mOppFaceManager.getFaceId(recipient), nonce);
    }

    @Override
    public void onDataPacketTransferred(String recipient, String name) {
        jniOnDataTransferred(mOppFaceManager.getFaceId(recipient), name);
    }

    @Override
    public void onCancelPacketSentOverConnectionLess(Packet packet) {
        mConnectionLessManager.cancelPacket(packet.getRecipient(), packet.getId());
    }

    @Override
    public void onSendOverConnectionOriented(Packet packet) {
        mOppFaceManager.sendPacket(packet);
    }

    @Override
    public void onSendOverConnectionLess(Packet packet) {
        mConnectionLessManager.sendPacket(packet);
    }

    @Override
    public void onPacketReceived(String sender, byte[] payload) {
        Log.i(TAG, "Packet received from : " + sender + " with length : [" + payload.length + "]");
        Long faceId = mOppFaceManager.getFaceId(sender);
        if(faceId != null)
            jniReceiveOnFace(faceId, payload.length, payload);
    }

    static {
        System.loadLibrary("gnustl_shared");
        System.loadLibrary("crystax");
        System.loadLibrary("nfd-wrapped");
    }

	// UmobileService related functions.
    private native void jniStart(String homepath, String config);
	private native void jniStop();

    private native long jniGetFaceId(String uri);
    private native boolean jniIsFaceUp(long faceId);

    /** [JNI] Retrieve the version of the underlying NDN Opportunistic Daemon.
     * @return build version of the NOD used.
     */
    private native void jniCreateFace(long faceId);

    /** [JNI] Retrieve the version of the underlying NDN Opportunistic Daemon.
     * @return build version of the NOD used.
     */
	private native String jniGetVersion();

    /** [JNI] Retrieve the Name Tree of the running NDN Opportunistic Daemon.
     * @return list of all Names in the NameTree of the running NOD.
     */
    private native List<Name> jniGetNameTree();

    /** [JNI] Retrieve the FaceTable of the running NDN Opportunistic Daemon.
     * @return list of all Faces currently registered in the running NOD.
     */
    private native List<Face> jniGetFaceTable();

    /** [JNI] Request the creation of a Face by the running NDN Opportunistic Daemon (see https://redmine.named-data.net/projects/nfd/wiki/FaceMgmt)
     * @param faceUri Face URI to be used as the main parameter for the creation request
     * @param persistency persistency setting to be used
     * @param localFields whether local fields are enabled on this face
     */
    private native void jniCreateFace(String faceUri, int persistency, boolean localFields);

    /** [JNI] Set the status of an Opportunistic Face to UP and attach an OpportunisticChannel to it
     * @param id the FaceId of the Face to bring up
     */
    private native void jniBringUpFace(long id);

    /** [JNI] Set the status of an Opportunistic Face to DOWN and detach its OpportunisticChannel
     * @param id the FaceId of the Face to bring down
     */
    private native void jniBringDownFace(long id);

    /** [JNI] Send all Pushed-Data present in the ContentStore through a Face
     * @param id the FaceId of the Face to which data should be pushed
     */
    private native void jniPushData(long id, String name);

    private native void jniPassInterests(long id, String name);

    /** [JNI] Used by the OpportunisticConnectivityManager to notify its encapsulating Face of the result of the
     * transmission of an Interest.
     * @param faceId the FaceId of the Face to notify
     * @param nonce the Nonce of the Interest concerned
     */
    private native void jniOnInterestTransferred(long faceId, int nonce);

    /** [JNI] Used by the OpportunisticConnectivityManager to notify its encapsulating Face of the result of the
     * transmission of a Data.
     * @param faceId the FaceId of the Face to notify
     * @param name the Name of the Data concerned
     */
    private native void jniOnDataTransferred(long faceId, String name);

    /** [JNI] Used by the OpportunisticChannel to notify its encapsulating Face that a packet has been received
     * @param id the FaceId of the Face to notify
     * @param receivedBytes the number of bytes received
     * @param buffer the buffer storing the received bytes
     */
    native void jniReceiveOnFace(long id, int receivedBytes, byte[] buffer);

    /** [JNI] Close a Face
     * @param faceId the FaceId of the Face to close
     */
    private native void jniDestroyFace(long faceId);

    /** [JNI] Retrieve the ForwardingInformationBase of the running NDN Opportunistic Daemon.
     * @return the list of all FIB entries of the running NOD
     */
    private native List<FibEntry> jniGetForwardingInformationBase();

    /** [JNI] Request the addition of a new route into the RoutingInformationBase (see https://redmine.named-data.net/projects/nfd/wiki/RibMgmt)
     * @param prefix the Name prefix to which the route is associated
     * @param faceId the FaceId to be included as one of the next-hops
     * @param origin the origin of this new route (e.g. app, static, nlsr)
     * @param cost the cost associated to the FaceId
     * @param flags the route inheritance flags
     */
    private native void jniAddRoute(String prefix, long faceId, long origin, long cost, long flags);

    /** [JNI] Retrieve the PendingInterestTable of the running NDN Opportunistic Daemon.
     * @return the list of all PIT entries of the running NOD
     */
    private native List<PitEntry> jniGetPendingInterestTable();

    /** [JNI] Retrieve the ContentStore of the running NDN Opportunistic Daemon.
     * @return the list of all CS entries of the running NOD
     */
    private native List<CsEntry> jniGetContentStore();

    /** [JNI] Retrieve the StrategyChoiceTable of the running NDN Opportunistic Daemon.
     * @return the list of all SCT entries of the running NOD
     */
    private native List<SctEntry> jniGetStrategyChoiceTable();
}