/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * The ServiceTracker encapsulates the logic of discovering UMobile peers on the same network. It is
 * mostly intended for demo and testing purposes.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.service;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;
import java.util.Observable;

import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.UmobileService.Status;

class ServiceTracker extends Observable {
    private static final String TAG = ServiceTracker.class.getSimpleName();

    private static final String SVC_INSTANCE_TYPE = "_ndn._tcp";
    private static final int SVC_INSTANCE_PORT = 6442;

    private Context mContext;

    private WifiStateTracker mWifiTracker;
    private IntentFilter mWifiIntents;

    private NsdManager mNsdManager;

    private boolean mEnabled;
    private boolean mRegistered;
    private boolean mDiscovering;

    private NsdServiceInfo mDescriptor;
    private String mAssignedServiceName = null;
    Map<String, UmobileService> mServices;

    ServiceTracker(Context context, String uuid) {
        mContext = context;

        mDescriptor = new NsdServiceInfo();
        mDescriptor.setServiceName(uuid);
        mDescriptor.setServiceType(SVC_INSTANCE_TYPE);
        mDescriptor.setPort(SVC_INSTANCE_PORT);

        mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

        mEnabled = false;
        mRegistered = false;
        mDiscovering = false;

        mServices = new HashMap<>();

        mWifiIntents = new IntentFilter();
        mWifiIntents.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);

        mWifiTracker = new WifiStateTracker();
    }

    public void enable() {
        if(!mEnabled) {
            mContext.registerReceiver(mWifiTracker, mWifiIntents);
            mEnabled = true;
        }
    }

    public void disable() {
        if(mEnabled) {
            mContext.unregisterReceiver(mWifiTracker);
            mWifiTracker.disable();
            mEnabled = false;
        }
    }

    private NsdManager.RegistrationListener mRegistrationListener = new NsdManager.RegistrationListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
            Log.d(TAG, "Registration succeeded : " + nsdServiceInfo.getServiceName());
            mAssignedServiceName = nsdServiceInfo.getServiceName();
            mRegistered = true;
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
            Log.d(TAG, "Unregistration succeeded : " + nsdServiceInfo.getServiceName());
            mRegistered = false;
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int error) {
            Log.d(TAG, "Registration failed (" + error + ")");
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int error) {
            Log.d(TAG, "Unregistration failed (" + error + ")");
        }
    };

    private NsdManager.DiscoveryListener mDiscoveryListener = new NsdManager.DiscoveryListener() {
        private static final String UNKNOWN_HOST = "0.0.0.0";
        private static final int UNKNOWN_PORT = 0;

        @Override
        public void onStartDiscoveryFailed(String s, int error) {
            Log.d(TAG, "Start err" + error);
        }

        @Override
        public void onStopDiscoveryFailed(String s, int error) {
            Log.d(TAG, "Stop err" + error);
        }

        @Override
        public void onDiscoveryStarted(String regType) {
            Log.d(TAG, "Started : " + regType);
            mDiscovering = true;
        }

        @Override
        public void onDiscoveryStopped(String regType) {
            Log.d(TAG, "Stopped : " + regType);
            mDiscovering = false;
        }

        @Override
        public void onServiceFound(NsdServiceInfo nsdServiceInfo) {
            String serviceName = nsdServiceInfo.getServiceName();
            Log.d(TAG, "UmobileService FOUND : <" + serviceName + ">");
            if(!serviceName.equals(mAssignedServiceName)) {
                mServices.put(serviceName, new UmobileService(Status.AVAILABLE, serviceName, UNKNOWN_HOST, UNKNOWN_PORT));
                setChanged(); notifyObservers();
                mNsdManager.resolveService(nsdServiceInfo, mResolveListener);
            }
        }

        @Override
        public void onServiceLost(NsdServiceInfo nsdServiceInfo) {
            String serviceName = nsdServiceInfo.getServiceName();
            Log.d(TAG, "UmobileService LOST : <" + serviceName + "> ");
            if(!serviceName.equals(mAssignedServiceName)) {
                if(mServices.containsKey(serviceName)) {
                    UmobileService svc = mServices.get(serviceName);
                    svc.status = Status.UNAVAILABLE;
                    svc.host = UNKNOWN_HOST;
                    svc.port = UNKNOWN_PORT;
                    setChanged(); notifyObservers();
                }
            }
        }
    };

    private NsdManager.ResolveListener mResolveListener = new NsdManager.ResolveListener() {
        @Override
        public void onServiceResolved(NsdServiceInfo nsdServiceInfo) {
            String name = nsdServiceInfo.getServiceName();
            UmobileService svc = mServices.get(name);
            svc.host = nsdServiceInfo.getHost().getHostAddress();
            svc.port = nsdServiceInfo.getPort();
            setChanged(); notifyObservers();
        }

        @Override
        public void onResolveFailed(NsdServiceInfo nsdServiceInfo, int error) {
            Log.d(TAG, "Resolution err" + error + " : " + nsdServiceInfo.getServiceName());
        }
    };

    private class WifiStateTracker extends BroadcastReceiver {
        private boolean isUp = false;

        private void enable() {
            if(!mRegistered)
                mNsdManager.registerService(mDescriptor, NsdManager.PROTOCOL_DNS_SD, mRegistrationListener);

            if(!mDiscovering)
                mNsdManager.discoverServices(SVC_INSTANCE_TYPE, NsdManager.PROTOCOL_DNS_SD, mDiscoveryListener);
        }

        public void disable() {
            if(mRegistered) mNsdManager.unregisterService(mRegistrationListener);
            if(mDiscovering) mNsdManager.stopServiceDiscovery(mDiscoveryListener);
            mEnabled = false;

            for(UmobileService svc : mServices.values()) svc.status = Status.UNAVAILABLE;
            setChanged(); notifyObservers();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(action)) {
                NetworkInfo netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
                isUp = netInfo.isConnected();
                if (isUp) enable();
                else disable();
            }
        }
    }
}
