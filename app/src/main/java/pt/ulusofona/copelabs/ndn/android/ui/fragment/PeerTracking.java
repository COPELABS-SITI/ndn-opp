package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.NsdService;
import pt.ulusofona.copelabs.ndn.android.ui.adapter.NsdServiceAdapter;
import pt.ulusofona.copelabs.ndn.android.ui.adapter.WifiP2pPeerAdapter;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;
import pt.ulusofona.copelabs.ndn.android.umobile.Utilities;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.WifiP2pConnectivityManager;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.WifiP2pPeer;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.WifiP2pPeerTracker;

public class PeerTracking extends Fragment implements Observer, Refreshable {
    private static final String TAG = PeerTracking.class.getSimpleName();

    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mWifiP2pChannel;

    private String mAssignedUuid;

    private WifiP2pPeerTracker mWifiP2pPeerTracker = new WifiP2pPeerTracker();
    private WifiP2pConnectivityManager mWifiP2pConnectivityManager = new WifiP2pConnectivityManager();

    private Map<String, WifiP2pPeer> mPeers = new HashMap<>();
    private Collection<NsdService> mServices = new ArrayList<>();

    private WifiP2pPeerAdapter mWifiP2pPeerAdapter;
    private NsdServiceAdapter mNsdServiceAdapter;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        mContext = context;
        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);
        mWifiP2pChannel = mWifiP2pManager.initialize(mContext, Looper.getMainLooper(), null);

        mAssignedUuid = Utilities.obtainUuid(mContext);

        mWifiP2pConnectivityManager.enable(mContext, mWifiP2pManager, mWifiP2pChannel, mAssignedUuid);
    }

    @Override
    public void onDetach() {
        mWifiP2pConnectivityManager.disable();
        super.onDetach();
    }

    @Override
    public void onResume() {
        super.onResume();
        mWifiP2pPeerTracker.addObserver(this);
    }

    @Override
    public void onPause() {
        mWifiP2pPeerTracker.deleteObserver(this);
        super.onPause();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View viewWifiP2pTracking = inflater.inflate(R.layout.fragment_nsd_over_wifip2p_tracking, container, false);

        Switch swtWifiP2pPeerTracking = (Switch) viewWifiP2pTracking.findViewById(R.id.switch_wifi_p2p_peer_tracking);
        swtWifiP2pPeerTracking.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean isOn) {
                if(isOn) mWifiP2pPeerTracker.enable(mContext, mWifiP2pManager, mWifiP2pChannel, mAssignedUuid);
                else mWifiP2pPeerTracker.disable();
            }
        });

        Button btn_groupFormation = (Button) viewWifiP2pTracking.findViewById(R.id.button_group_formation);
        btn_groupFormation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mWifiP2pConnectivityManager.join(mWifiP2pPeerTracker.getPeers());
            }
        });

        TextView uuid = (TextView) viewWifiP2pTracking.findViewById(R.id.text_nsd_uuid);
        uuid.setText(mAssignedUuid);

        mWifiP2pPeerAdapter = new WifiP2pPeerAdapter(mContext);
        ListView listViewWifiP2pPeers = (ListView) viewWifiP2pTracking.findViewById(R.id.list_wifiP2pPeers);
        listViewWifiP2pPeers.setAdapter(mWifiP2pPeerAdapter);

        mNsdServiceAdapter = new NsdServiceAdapter(mContext);
        ListView listViewServices = (ListView) viewWifiP2pTracking.findViewById(R.id.list_nsd_services);
        listViewServices.setAdapter(mNsdServiceAdapter);

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
            mNsdServiceAdapter.addAll(mServices);
        }
    };

    @Override
    public void update(Observable observable, Object obj) {
        if(observable instanceof WifiP2pPeerTracker) {
            mPeers.clear();
            mPeers.putAll(mWifiP2pPeerTracker.getPeers());

            FragmentActivity act = getActivity();
            if(act != null)
                act.runOnUiThread(mPeerUpdater);
        }
    }

    @Override
    public int getTitle() {
        return R.string.peerTracking;
    }

    @Override
    public void refresh(@NonNull ForwardingDaemon daemon) {
        mServices.clear();
        mServices.addAll(daemon.getUmobileServices());

        FragmentActivity act = getActivity();
        if(act != null)
            act.runOnUiThread(mServiceUpdater);
    }
}