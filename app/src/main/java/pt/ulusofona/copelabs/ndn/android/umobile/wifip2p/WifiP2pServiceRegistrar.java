package pt.ulusofona.copelabs.ndn.android.umobile.wifip2p;

import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pStateTracker;

public class WifiP2pServiceRegistrar implements Observer {
    private static final String TAG = WifiP2pServiceRegistrar.class.getSimpleName();

    private WifiP2pManager mWifiP2pMgr;
    private WifiP2pManager.Channel mWifiP2pChn;

    private WifiP2pDnsSdServiceInfo mDescriptor;

    private WifiP2pStateTracker mStateTracker = new WifiP2pStateTracker();

    private boolean mEnabled = false;
    private boolean mRegistered = false;

    public synchronized void enable(Context context, WifiP2pManager wifiP2pMgr, WifiP2pManager.Channel wifiP2pChn, String uuid) {
        if(!mEnabled) {
            Log.w(TAG, "Enabling.");
            mWifiP2pMgr = wifiP2pMgr;
            mWifiP2pChn = wifiP2pChn;
            mDescriptor = WifiP2pDnsSdServiceInfo.newInstance(uuid, WifiP2pService.SVC_INSTANCE_TYPE, null);

            mStateTracker.addObserver(this);
            mStateTracker.enable(context);

            mEnabled = true;
        } else
            Log.w(TAG, "Attempt to enable a second time.");
    }

    public synchronized void disable() {
        if(mEnabled) {
            Log.w(TAG, "Disabling.");
            unregister();

            mStateTracker.disable();
            mStateTracker.deleteObserver(this);

            mWifiP2pMgr = null;
            mWifiP2pChn = null;
            mDescriptor = null;
            mEnabled = false;
        } else
            Log.w(TAG, "Attempt to disable a second time.");
    }

    private synchronized void register() {
        if(mEnabled && !mRegistered) {
            mRegistered = true;
            mWifiP2pMgr.addLocalService(mWifiP2pChn, mDescriptor, afterLocalServiceAdded);
        } else
            Log.w(TAG, "Attempt to register a second time. mEnabled=" + mEnabled + ", mRegistered=" + mRegistered);
    }

    private synchronized void unregister() {
        if(mEnabled && mRegistered) {
            mWifiP2pMgr.clearServiceRequests(mWifiP2pChn, afterLocalServiceCleared);
            mRegistered = false;
        } else
            Log.w(TAG, "Attempt to unregister a second time. mEnabled=" + mEnabled + ", mRegistered=" + mRegistered);
    }

    private ActionListener afterLocalServiceAdded = new ActionListener() {
        @Override public void onSuccess() {Log.d(TAG, "Local Service Registered.");}

        @Override public void onFailure(int error) {
            Log.d(TAG, "Failed to register (" + error + ")");
            mRegistered = false;
        }
    };

    private ActionListener afterLocalServiceCleared = new ActionListener() {
        @Override public void onSuccess() {Log.d(TAG, "Clear SUCCESS");}
        @Override public void onFailure(int e) {Log.d(TAG, "Clear FAILED (" + e + ")");}
    };

    @Override
    public void update(Observable observable, Object obj) {
        if(observable instanceof WifiP2pStateTracker) {
            boolean enabled = (boolean) obj;
            if(enabled) register();
            else unregister();
        }
    }
}
