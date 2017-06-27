/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The main wrapper in Java of the NDN Forwarding Daemon for use on the Android platform.
 * This class embodies the implementation of the NDN-Opp framework.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;
import android.util.LongSparseArray;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.models.CsEntry;
import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.models.FibEntry;
import pt.ulusofona.copelabs.ndn.android.models.Name;
import pt.ulusofona.copelabs.ndn.android.models.NsdService;
import pt.ulusofona.copelabs.ndn.android.models.PitEntry;
import pt.ulusofona.copelabs.ndn.android.models.SctEntry;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceDiscoverer;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.OpportunisticPeerTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/** JNI wrapper around the NDN Opportunistic Daemon (NOD), which is the modified version of NFD to include the Opportunistic Faces,
 * and PUSH communications. This class provides an interface entirely through JNI to manage the configuration of the running
 * NOD including face creation, destruction and addition of routes to the RIB.
 */
public class OpportunisticDaemon extends Service {
    private static final String TAG = OpportunisticDaemon.class.getSimpleName();

	public static final String STARTED = "pt.ulusofona.copelabs.ndn.android.service.STARTED";
	public static final String STOPPING = "pt.ulusofona.copelabs.ndn.android.service.STOPPING";

    private enum State { STARTED , STOPPED }

    public class NodBinder extends Binder {
        public long getUptime() { return (current == State.STARTED) ? System.currentTimeMillis() - startTime : 0L; }
        public String getUmobileUuid() { return (current == State.STARTED) ? mAssignedUuid : getString(R.string.notAvailable); }
        public String getVersion() { return jniGetVersion(); }
        public List<Name> getNameTree() { return jniGetNameTree(); }
        public List<Face> getFaceTable() { return jniGetFaceTable(); }
        public void createFace(String faceUri, int persistency, boolean localFields) { jniCreateFace(faceUri, persistency, localFields);}
        public void bringUpFace(long faceId, OpportunisticChannel oc) { jniBringUpFace(faceId, oc); }
        public void bringDownFace(long faceId) { jniBringDownFace(faceId); }
        public void sendComplete(long faceId, boolean success) { jniSendComplete(faceId, success); }
        public void receiveOnFace(long faceId, int byteCount, byte[] buffer) { jniReceiveOnFace(faceId, byteCount, buffer); }
        public void destroyFace(long faceId) { jniDestroyFace(faceId); }
        public List<FibEntry> getForwardingInformationBase() { return jniGetForwardingInformationBase(); }
        public void addRoute(String prefix, long faceId, long origin, long cost, long flags) { jniAddRoute(prefix, faceId, origin, cost, flags);}
        public List<PitEntry> getPendingInterestTable() { return jniGetPendingInterestTable(); }
        public List<CsEntry> getContentStore() { return jniGetContentStore(); }
        public List<SctEntry> getStrategyChoiceTable() { return jniGetStrategyChoiceTable(); }
    }
    private final NodBinder local = new NodBinder();

    // Start time
	private long startTime;

    // FaceTable
    private LongSparseArray<Face> mFacetable = new LongSparseArray<>();

    // Assigned UUID
    private String mAssignedUuid;
    // Routing & Contextual Manager
    private OpportunisticFaceManager mOppFaceManager;
    // Configuration
    private String mConfiguration;

    private OpportunisticPeerTracker mPeerTracker = OpportunisticPeerTracker.getInstance();
    private NsdServiceDiscoverer mServiceTracker = NsdServiceDiscoverer.getInstance();

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

        // Initialize the Routing Engine
        mOppFaceManager = new OpportunisticFaceManager();
        // Retrieve the UUID
        mAssignedUuid = Utilities.obtainUuid(this);

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
            jniStart(getFilesDir().getAbsolutePath(), mConfiguration);
			startTime = System.currentTimeMillis();

            WifiP2pManager wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            WifiP2pManager.Channel wifiP2pChannel = wifiP2pManager.initialize(this, getMainLooper(), null);

            mPeerTracker.enable(this, wifiP2pManager, wifiP2pChannel, mAssignedUuid);

            mOppFaceManager.enable(this, local);

            mServiceTracker.addObserver(mOppFaceManager);
            mServiceTracker.enable(this, mAssignedUuid);

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

            mPeerTracker.disable();

            mOppFaceManager.disable();

            mServiceTracker.disable();
            mServiceTracker.deleteObserver(mOppFaceManager);

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

    /** Retrieve the list of currently known NDN-Opp service instances. Can be used to identify which services are running
     * in the currently connected Wi-Fi Direct Group (if any).
     * @return the list of all NDN-Opp service instances ever encountered.
     */
    public Collection<NsdService> getUmobileServices() {
        Collection<NsdService> peers;

        if(mServiceTracker != null)
            peers = mServiceTracker.getServices().values();
        else
            peers = new ArrayList<>();

        return peers;
    }

	// JNI-related declarations.
	static {
		System.loadLibrary("nfd-wrapped");
	}

	// UmobileService related functions.
    private native void jniStart(String homepath, String config);
	private native void jniStop();

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
     * @param oc the OpportunisticChannel this Face has to use to transmit packets
     */
    private native void jniBringUpFace(long id, OpportunisticChannel oc);

    /** [JNI] Set the status of an Opportunistic Face to DOWN and detach its OpportunisticChannel
     * @param id the FaceId of the Face to bring down
     */
    private native void jniBringDownFace(long id);

    /** [JNI] Used by the OpportunisticChannel to notify its encapsulating Face of the result of the
     * transmission of the last packet.
     * @param id the FaceId of the Face to notify
     * @param success a boolean value indicated success (true) or failure (false) to transmit the last
     *               packet
     */
    private native void jniSendComplete(long id, boolean success);

    /** [JNI] Used by the OpportunisticChannel to notify its encapsulating Face that a packet has been received
     * @param id the FaceId of the Face to notify
     * @param receivedBytes the number of bytes received
     * @param buffer the buffer storing the received bytes
     */
    private native void jniReceiveOnFace(long id, int receivedBytes, byte[] buffer);

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