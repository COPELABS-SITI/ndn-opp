package pt.ulusofona.copelabs.ndn.android.service;

import android.app.Service;
import android.content.Intent;
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
import java.util.Collections;
import java.util.List;

//TODO: All the 'static' methods are from NFD-Android. Clean it up.
public class ForwardingDaemon extends Service {
    private static final String TAG = ForwardingDaemon.class.getSimpleName();

	public static final String STARTED = "pt.ulusofona.copelabs.ndn.android.service.STARTED";
	public static final String STOPPED = "pt.ulusofona.copelabs.ndn.android.service.STOPPED";

	private enum State { STARTED , STOPPED }

	private static long startTime;

    // Routing
    private static Routing mRouting;
    private static ContextualManager mContextualMgr;

    private static State current = State.STOPPED;
	private static synchronized State getAndSetState(State nextState) {
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
	public int onStartCommand(Intent intent, int flags, int startId) {
		if(State.STOPPED == getAndSetState(State.STARTED)) {
            mRouting = new Routing();
            mContextualMgr = new ContextualManager(this, mRouting);
            mContextualMgr.register(this);

            jniInitialize(getFilesDir().getAbsolutePath(), getConfiguration());
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
		return null;
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
		}
	}

	public static String getVersion() {
		return jniGetVersion();
	}

	public static long getUptime() {
		if(current == State.STARTED)
			return System.currentTimeMillis() - startTime;
		else
			return 0L;
	}

	public static List<Face> getFaceTable() {
		return jniGetFaceTable();
	}

	public static void createFace(String faceUri, int persistency, boolean localFields) {
		jniCreateFace(faceUri, persistency, localFields);
	}

	public static List<Name> getNameTree() {
		List<Name> nametree = jniGetNameTree();
		Collections.sort(nametree);
		return nametree;
	}

    public static List<Peer> getPeers() {
        List<Peer> peers = null;
        if(mRouting != null)
            peers = mRouting.getPeers();
        else
            peers = new ArrayList<>();
        return peers;
    }

	public static List<FibEntry> getFib() {
		return jniGetForwardingInformationBase();
	}

	public static List<PitEntry> getPit() {
		return jniGetPendingInterestTable();
	}

	public static List<CsEntry> getContentStore() {
		return jniGetContentStore();
	}

	public static List<SctEntry> getStrategies() {
		return jniGetStrategyChoiceTable();
	}

	// JNI-related declarations.
	static {
		System.loadLibrary("nfd-wrapped");
	}

	// Service related functions.
	private native static void jniInitialize(String homepath, String config);
    private native static void jniCleanUp();
    private native static void jniStart();
	private native static void jniStop();

	// General information about the running daemon.
	private native static String jniGetVersion();

	// Manipulation of NameTree.
	private native static List<Name> jniGetNameTree();

	// Manipulation of FaceTable.
	private native static List<Face> jniGetFaceTable();
	private native static void jniCreateFace(String faceUri, int persistency, boolean localFields);
	private native static void jniDestroyFace(long faceId);

	// Manipulation of the Forwarding Information Base (FIB).
	private native static List<FibEntry> jniGetForwardingInformationBase();

	// Manipulation of the Pending Interest Table (PIT).
	private native static List<PitEntry> jniGetPendingInterestTable();

	// Manipulation of the Content Store (CS).
	private native static List<CsEntry> jniGetContentStore();

	// Manipulation of the Strategy Choice Table (SCT).
	private native static List<SctEntry> jniGetStrategyChoiceTable();
}