/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * This class manages the Fragment which displays the PeerTracking and controls Group Formation.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterSuccess;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.OperationResult;
import pt.ulusofona.copelabs.ndn.android.ui.ExpressInterestTask;
import pt.ulusofona.copelabs.ndn.android.ui.JndnProcessEventTask;
import pt.ulusofona.copelabs.ndn.android.ui.RegisterPrefixTask;
import pt.ulusofona.copelabs.ndn.android.ui.adapter.OpportunisticPeerAdapter;
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.OpportunisticPeer;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.OpportunisticPeerTracker;

/** Interface to the Peer Tracking functionality of NDN-Opp. This Fragment is responsible for integrating
 * the functionalities of the NsdServiceTracker, the WifiP2pPeerTracker and the WifiP2pConnectivityManager.
 *
 * The interactions between these three components is as follows;
 *
 * - The Peer Tracker provides the up-to-date list of Wi-Fi P2P devices running NDN-Opp that were encountered
 * - The Connectivity Manager is used to take care of the formation of a Wi-Fi Direct Group (whether to form a new one or join an existing one)
 * - The NSD Service Tracker is used to know which NDN-Opp daemon can be reached within the Group to which the current device is connected (if it is)
 */
public class OpportunisticPeerTracking extends Fragment implements Observer, View.OnClickListener, OnInterestCallback, OnRegisterSuccess, OnData {
    private static final String TAG = OpportunisticPeerTracking.class.getSimpleName();
    private static final String PREFIX = "/ndn/opp";

    private static int PROCESS_INTERVAL = 1000;

    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    // Used for feedback to the user that a peerDiscovery is in progress
    private ProgressBar mDiscoveryInProgress;

    private OpportunisticPeerTracker mPeerTracker = new OpportunisticPeerTracker();

    private Map<String, OpportunisticPeer> mPeers = new HashMap<>();

    private OpportunisticPeerAdapter mPeerAdapter;

    private Face mFace;
    private Button mSendInterest;
    private int mInterestSequence = 0;
    private TextView mInterestName;

    /** Fragment lifecycle method. See https://developer.android.com/guide/components/fragments.html
     * @param context Android-provided Application context
     */
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        Log.v(TAG, "onAttach");
        mContext = context;
        mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(context, Looper.getMainLooper(), null);

        mPeerAdapter = new OpportunisticPeerAdapter(mContext);

        IntentFilter mIntentFilter = new IntentFilter();
        mIntentFilter.addAction(OpportunisticDaemon.STARTED);
        mIntentFilter.addAction(OpportunisticDaemon.STOPPING);
        mIntentFilter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        mContext.registerReceiver(mBr, mIntentFilter);

        mPeerTracker.addObserver(this);
    }

    /** Fragment lifecycle method (see https://developer.android.com/guide/components/fragments.html) */
    @Override
    public void onDetach() {
        mPeerTracker.deleteObserver(this);
        mContext.unregisterReceiver(mBr);
        super.onDetach();
    }

    /** Fragment lifecycle method (see https://developer.android.com/guide/components/fragments.html) */
    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_start_peer_discovery:
                mWifiP2pManager.discoverPeers(mWifiP2pChannel, new OperationResult(TAG, "Peer Discovery"));
                break;
            case R.id.btn_send_interest:
                Name name = new Name(PREFIX + "/" + mInterestName.getText().toString() + "/" + mInterestSequence);
                mInterestSequence += 1;
                Toast.makeText(mContext, "Sending Interest : " + name.toString(), Toast.LENGTH_SHORT).show();
                new ExpressInterestTask(mFace, name, this).execute();
                break;
        }
    }

    /** Fragment lifecycle method. See https://developer.android.com/guide/components/fragments.html */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        Log.v(TAG, "onCreateView");
        View viewWifiP2pTracking = inflater.inflate(R.layout.fragment_opp_peer_tracking, container, false);

        viewWifiP2pTracking.findViewById(R.id.btn_start_peer_discovery).setOnClickListener(this);
        mDiscoveryInProgress = (ProgressBar) viewWifiP2pTracking.findViewById(R.id.discoveryInProgress);

        mSendInterest = (Button) viewWifiP2pTracking.findViewById(R.id.btn_send_interest);
        mSendInterest.setOnClickListener(this);
        mInterestName = (TextView) viewWifiP2pTracking.findViewById(R.id.interestName);

        ListView listViewWifiP2pPeers = (ListView) viewWifiP2pTracking.findViewById(R.id.list_wifiP2pPeers);
        listViewWifiP2pPeers.setAdapter(mPeerAdapter);

        return viewWifiP2pTracking;
    }

    @Override
    public void onDestroy() {
        mHandler.removeCallbacks(mJndnProcessor);
        super.onDestroy();
    }

    private Runnable mPeerUpdater = new Runnable() {
        @Override
        public void run() {
            mPeerAdapter.clear();
            mPeerAdapter.addAll(mPeers.values());
        }
    };

    @Override
    public void update(Observable observable, Object obj) {
        FragmentActivity act = getActivity();

        /* When the PeerTracker notifies of some changes to its list, retrieve the new list of Peers
           and use it to update the UI accordingly. */
        mPeers.clear();
        mPeers.putAll(mPeerTracker.getPeers());

        if(act != null)
            act.runOnUiThread(mPeerUpdater);
    }

    // Used to toggle the visibility of the ProgressBar based on whether peer discovery is running
    private BroadcastReceiver mBr = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.v(TAG, "Received Intent : " + action);
            if(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {
                int extra = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, -1);
                if(extra == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED)
                    mDiscoveryInProgress.setVisibility(View.VISIBLE);
                else
                    mDiscoveryInProgress.setVisibility(View.INVISIBLE);
            } else if (OpportunisticDaemon.STARTED.equals(action)) {
                mPeerTracker.enable(context);
                mFace = new Face("127.0.0.1", 6363);
                RegisterPrefixTask rpt = new RegisterPrefixTask(mFace, PREFIX, OpportunisticPeerTracking.this, OpportunisticPeerTracking.this);
                rpt.execute();
                mSendInterest.setEnabled(true);
                mHandler.postDelayed(mJndnProcessor, PROCESS_INTERVAL);
            } else if (OpportunisticDaemon.STOPPING.equals(action)){
                mPeerTracker.disable();
                mHandler.removeCallbacks(mJndnProcessor);
                mFace = null;
                mSendInterest.setEnabled(false);
            }
        }
    };

    @Override
    public void onInterest(Name prefix, Interest interest, Face face, long interestFilterId, InterestFilter filter) {
        Log.v(TAG, "Received Interest : " + prefix.toString());
    }

    @Override
    public void onRegisterSuccess(Name prefix, long registeredPrefixId) {
        Log.v(TAG, "Registration Success : " + prefix.toString());
    }

    @Override
    public void onData(Interest interest, Data data) {
        Log.v(TAG, "Received Data : " + data.getName().toString());
    }

    private Handler mHandler = new Handler();
    private Runnable mJndnProcessor = new Runnable() {
        @Override
        public void run() {
            new JndnProcessEventTask(mFace).execute();
            mHandler.postDelayed(mJndnProcessor, PROCESS_INTERVAL);
        }
    };
}