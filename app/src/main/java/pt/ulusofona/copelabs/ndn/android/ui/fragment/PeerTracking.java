/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * This class manages the Fragment which displays the PeerTracking and controls Group Formation.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.NsdService;
import pt.ulusofona.copelabs.ndn.android.ui.adapter.NsdServiceAdapter;
import pt.ulusofona.copelabs.ndn.android.ui.adapter.WifiP2pPeerAdapter;
import pt.ulusofona.copelabs.ndn.android.umobile.Utilities;
import pt.ulusofona.copelabs.ndn.android.umobile.nsd.NsdServiceTracker;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.WifiP2pConnectivityManager;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.WifiP2pPeer;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.WifiP2pPeerTracker;

public class PeerTracking extends Fragment implements Observer {
    private static final String TAG = PeerTracking.class.getSimpleName();

    private String mAssignedUuid;

    private NsdServiceTracker mServiceTracker = NsdServiceTracker.getInstance();
    private WifiP2pPeerTracker mWifiP2pPeerTracker = WifiP2pPeerTracker.getInstance();
    private WifiP2pConnectivityManager mWifiP2pConnectivityManager = new WifiP2pConnectivityManager();

    private Map<String, WifiP2pPeer> mPeers = new HashMap<>();
    private Map<String, NsdService> mServices = new HashMap<>();

    private WifiP2pPeerAdapter mWifiP2pPeerAdapter;
    private NsdServiceAdapter mNsdServiceAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        WifiP2pManager mWifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
        WifiP2pManager.Channel mWifiP2pChannel = mWifiP2pManager.initialize(context, Looper.getMainLooper(), null);

        mAssignedUuid = Utilities.obtainUuid(context);

        mWifiP2pPeerAdapter = new WifiP2pPeerAdapter(context);
        mNsdServiceAdapter = new NsdServiceAdapter(context);

        mPeers.putAll(mWifiP2pPeerTracker.getPeers());
        mServices.putAll(mServiceTracker.getServices());

        mWifiP2pConnectivityManager.enable(context, mWifiP2pManager, mWifiP2pChannel, mAssignedUuid);
    }

    @Override
    public void onDetach() {
        mWifiP2pConnectivityManager.disable();
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mServiceTracker.addObserver(this);
        mWifiP2pPeerTracker.addObserver(this);
    }

    @Override
    public void onPause() {
        mServiceTracker.deleteObserver(this);
        mWifiP2pPeerTracker.deleteObserver(this);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewWifiP2pTracking = inflater.inflate(R.layout.fragment_nsd_over_wifip2p_tracking, container, false);

        Button btn_groupFormation = (Button) viewWifiP2pTracking.findViewById(R.id.button_group_formation);
        btn_groupFormation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWifiP2pConnectivityManager.join(mWifiP2pPeerTracker.getPeers());
            }
        });

        TextView uuid = (TextView) viewWifiP2pTracking.findViewById(R.id.text_nsd_uuid);
        uuid.setText(mAssignedUuid);

        ListView listViewWifiP2pPeers = (ListView) viewWifiP2pTracking.findViewById(R.id.list_wifiP2pPeers);
        listViewWifiP2pPeers.setAdapter(mWifiP2pPeerAdapter);
        mWifiP2pPeerAdapter.addAll(mPeers.values());

        ListView listViewServices = (ListView) viewWifiP2pTracking.findViewById(R.id.list_nsd_services);
        listViewServices.setAdapter(mNsdServiceAdapter);
        mNsdServiceAdapter.addAll(mServices.values());

        return viewWifiP2pTracking;
    }

    private Runnable mPeerUpdater = new Runnable() {
        @Override
        public void run() {
            mWifiP2pPeerAdapter.clear();
            mWifiP2pPeerAdapter.addAll(mPeers.values());
        }
    };

    private Runnable mServiceUpdater = new Runnable() {
        @Override
        public void run() {
            mNsdServiceAdapter.clear();
            mNsdServiceAdapter.addAll(mServices.values());
        }
    };

    @Override
    public void update(Observable observable, Object obj) {
        FragmentActivity act = getActivity();

        if(observable instanceof WifiP2pPeerTracker) {
            mPeers.clear();
            mPeers.putAll(mWifiP2pPeerTracker.getPeers());

            if(act != null)
                act.runOnUiThread(mPeerUpdater);
        } else if (observable instanceof NsdServiceTracker) {
            if(obj != null) {
                NsdService svc = (NsdService) obj;
                mServices.put(svc.getUuid(), svc);
            } else {
                mServices.clear();
                mServices.putAll(mServiceTracker.getServices());
            }

            if(act != null)
                act.runOnUiThread(mServiceUpdater);
        }
    }
}