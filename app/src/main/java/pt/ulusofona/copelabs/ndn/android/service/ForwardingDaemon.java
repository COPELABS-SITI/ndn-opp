package pt.ulusofona.copelabs.ndn.android.service;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.CsEntry;
import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.FibEntry;
import pt.ulusofona.copelabs.ndn.android.Name;
import pt.ulusofona.copelabs.ndn.android.Peer;
import pt.ulusofona.copelabs.ndn.android.PitEntry;
import pt.ulusofona.copelabs.ndn.android.SctEntry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class ForwardingDaemon extends Service {
    private static final String TAG = ForwardingDaemon.class.getSimpleName();

	public static final String STARTED = "pt.ulusofona.copelabs.ndn.android.service.STARTED";
	public static final String STOPPED = "pt.ulusofona.copelabs.ndn.android.service.STOPPED";

	private enum State { STARTED , STOPPED }

    public class DaemonBinder extends Binder {
        public ForwardingDaemon getService() {
            return ForwardingDaemon.this;
        }
    }
    private final IBinder local = new DaemonBinder();

    // Start time
	private long startTime;

    // Routing & Contextual Manager
    private Routing mRouting;
    private ContextualManager mContextualMgr;

    private State current = State.STOPPED;
	private synchronized State getAndSetState(State nextState) {
		State oldValue = current;
		current = nextState;
		return oldValue;
	}

    private String getConfiguration() {
        StringBuilder cfgBuilder = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(getResources().openRawResource(R.raw.nfd_conf)));
            try {
                while (br.ready()) cfgBuilder.append(br.readLine() + "\n");
            } finally { br.close(); }
        } catch (IOException e) {
            Log.d(TAG, "I/O error while reading configuration : " + e.getMessage());
        }
        Log.d(TAG, "Read configuration : " + cfgBuilder.length());
        return cfgBuilder.toString();
    }

    @Override
    public void onCreate() {
        super.onCreate();
        mRouting = new Routing();
        mContextualMgr = new ContextualManager(this, mRouting);
        mContextualMgr.register(this);
        jniInitialize(getFilesDir().getAbsolutePath(), getConfiguration());
    }

    @Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(State.STOPPED == getAndSetState(State.STARTED)) {
            jniStart();
			startTime = System.currentTimeMillis();
			// TODO: Reload NFD and NRD in memory structures (if any)
            Log.d(TAG, STARTED);
			sendBroadcast(new Intent(STARTED));
		}
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent in) {
		return local;
	}

	@Override
	public void onDestroy() {
		if(State.STARTED == getAndSetState(State.STOPPED)) {
			// TODO: Save NFD and NRD in memory data structures.
			jniStop();
            jniCleanUp();
            mContextualMgr.unregister(this);
			sendBroadcast(new Intent(STOPPED));
			stopSelf();
            super.onDestroy();
		}
	}

    // Uptime of the Forwarding Daemon in milliseconds.
	public long getUptime() {
		if(current == State.STARTED)
			return System.currentTimeMillis() - startTime;
		else
			return 0L;
	}

    public List<Peer> getPeers() {
        List<Peer> peers;
        if(mRouting != null)
            peers = mRouting.getPeers();
        else
            peers = new ArrayList<>();
        return peers;
    }

	// JNI-related declarations.
	static {
		System.loadLibrary("nfd-wrapped");
	}

	// Service related functions.
	private native void jniInitialize(String homepath, String config);
    private native void jniCleanUp();
    private native void jniStart();
	private native void jniStop();

	// General information about the running daemon.
	public native String getVersion();

	// Manipulation of NameTree.
	public native List<Name> getNameTree();

	// Manipulation of FaceTable.
	public native List<Face> getFaceTable();
	public native void createFace(String faceUri, int persistency, boolean localFields);
	public native void destroyFace(long faceId);

	// Manipulation of the Forwarding Information Base (FIB).
	public native List<FibEntry> getForwardingInformationBase();

	// Manipulation of the Pending Interest Table (PIT).
	public native List<PitEntry> getPendingInterestTable();

	// Manipulation of the Content Store (CS).
	public native List<CsEntry> getContentStore();

	// Manipulation of the Strategy Choice Table (SCT).
	public native List<SctEntry> getStrategyChoiceTable();
}