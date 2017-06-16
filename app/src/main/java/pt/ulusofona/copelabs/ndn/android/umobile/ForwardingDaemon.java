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
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pConnectivityTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pDiscoveryTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pStateTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.WifiP2pPeerTracker;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ForwardingDaemon extends Service {
    private static final String TAG = ForwardingDaemon.class.getSimpleName();

	public static final String STARTED = "pt.ulusofona.copelabs.ndn.android.service.STARTED";
	public static final String STOPPING = "pt.ulusofona.copelabs.ndn.android.service.STOPPING";

    private enum State { STARTED , STOPPED }

    public class DaemonBinder extends Binder {
        public ForwardingDaemon getService() {
            return ForwardingDaemon.this;
        }
    }
    private final IBinder local = new DaemonBinder();

    // Start time
	private long startTime;

    // FaceTable
    private LongSparseArray<Face> mFacetable = new LongSparseArray<>();

    // Assigned UUID
    private String mAssignedUuid;
    // Routing & Contextual Manager
    private Routing mRouting;
    // Configuration
    private String mConfiguration;

    private WifiP2pPeerTracker mPeerTracker = WifiP2pPeerTracker.getInstance();
    private NsdServiceTracker mServiceTracker = NsdServiceTracker.getInstance();

    private WifiP2pStateTracker mStateTracker = WifiP2pStateTracker.getInstance();
    private WifiP2pDiscoveryTracker mDiscoveryTracker = WifiP2pDiscoveryTracker.getInstance();
    private WifiP2pConnectivityTracker mConnectivityTracker = WifiP2pConnectivityTracker.getInstance();

    // Replace this logic with a lock.
    private State current = State.STOPPED;
	private synchronized State getAndSetState(State nextState) {
		State oldValue = current;
		current = nextState;
		return oldValue;
	}

    @Override
    public void onCreate() {
        super.onCreate();

        mRouting = new Routing(this);
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

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(State.STOPPED == getAndSetState(State.STARTED)) {
            jniInitialize(getFilesDir().getAbsolutePath(), mConfiguration);
            jniStart();
			startTime = System.currentTimeMillis();

            WifiP2pManager wifiP2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
            WifiP2pManager.Channel wifiP2pChannel = wifiP2pManager.initialize(this, getMainLooper(), null);

            mPeerTracker.enable(this, wifiP2pManager, wifiP2pChannel, mAssignedUuid);

            mServiceTracker.addObserver(mRouting);
            mServiceTracker.enable(this, mAssignedUuid);

            mStateTracker.enable(this);
            mDiscoveryTracker.enable(this);

            mConnectivityTracker.addObserver(mRouting);
            mConnectivityTracker.enable(this);

            Log.d(TAG, STARTED);
            sendBroadcast(new Intent(STARTED));
		}
		return START_NOT_STICKY;
	}

	@Override
    public IBinder onBind(Intent in) {
		return local;
	}

	@Override
	public void onDestroy() {
		if(State.STARTED == getAndSetState(State.STOPPED)) {
			jniStop();
            jniCleanUp();

            mPeerTracker.disable();

            mStateTracker.disable(this);
            mDiscoveryTracker.disable(this);
            mConnectivityTracker.disable(this);
            mConnectivityTracker.deleteObserver(mRouting);

            mServiceTracker.disable();
            mServiceTracker.deleteObserver(mRouting);

			sendBroadcast(new Intent(STOPPING));
			stopSelf();
            super.onDestroy();
		}
	}

    public Face getFace(long faceId) {
        return mFacetable.get(faceId);
    }

    // Called by the C++ daemon when it adds a Face to its FaceTable.
    private void addFace(Face face) {
        long faceId = face.getId();
        mFacetable.put(faceId, face);
        mRouting.afterFaceAdd(face);
    }

    // Uptime of the Forwarding Daemon in milliseconds.
	public long getUptime() {
		return (current == State.STARTED) ? System.currentTimeMillis() - startTime : 0L;
	}

    // UMobile UUID used by the ContextualManager.
    public String getUmobileUuid() {
        return (current == State.STARTED) ? mAssignedUuid : getString(R.string.notAvailable);
    }

    // Currently known UMobile Service Devices.
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
	private native void jniInitialize(String homepath, String config);
    private native void jniCleanUp();
    private native void jniStart();
	private native void jniStop();

	public native String getVersion();
    public native List<Name> getNameTree();
    public native List<Face> getFaceTable();
    public native void createFace(String faceUri, int persistency, boolean localFields);
    public native void bringUpFace(long id, OpportunisticChannel oc);
    public native void bringDownFace(long id);
    public native void sendComplete(long id, boolean result);
    public native void receiveOnFace(long id, int receivedBytes, byte[] buffer);
    public native void destroyFace(long faceId);
	public native List<FibEntry> getForwardingInformationBase();
    public native void addRoute(String prefix, long faceId, long origin, long cost, long flags);
	public native List<PitEntry> getPendingInterestTable();
	public native List<CsEntry> getContentStore();
	public native List<SctEntry> getStrategyChoiceTable();
}