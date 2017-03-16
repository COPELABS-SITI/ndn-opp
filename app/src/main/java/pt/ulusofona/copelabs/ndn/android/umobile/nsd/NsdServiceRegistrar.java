/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * //TODO: Description.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import java.util.Observable;
import java.util.Observer;

import pt.ulusofona.copelabs.ndn.android.NsdService;
import pt.ulusofona.copelabs.ndn.android.umobile.tracker.WifiP2pConnectivityTracker;

public class NsdServiceRegistrar implements Observer {
    private static final String TAG = NsdServiceRegistrar.class.getSimpleName();

    private NsdManager mNsdManager;

    private boolean mEnabled = false;
    private boolean mRegistered = false;

    private NsdServiceInfo mDescriptor;
    private String mAssignedUuid;

    private final WifiP2pConnectivityTracker mConnectivityTracker;

    public NsdServiceRegistrar() {
        mConnectivityTracker = new WifiP2pConnectivityTracker();
    }

    public synchronized void enable(Context context, String uuid, int port) {
        if(!mEnabled) {
            Log.v(TAG, "Enabling");
            mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

            mAssignedUuid = uuid;

            mDescriptor = new NsdServiceInfo();
            mDescriptor.setServiceName(mAssignedUuid);
            mDescriptor.setServiceType(NsdService.SERVICE_TYPE);
            mDescriptor.setPort(port);

            mConnectivityTracker.addObserver(this);
            mConnectivityTracker.enable(context);

            mEnabled = true;
        } else
            Log.w(TAG, "Attempt to enable a second time.");
    }

    public synchronized void disable() {
        if(mEnabled) {
            Log.v(TAG, "Disabling");
            unregister();

            mConnectivityTracker.deleteObserver(this);
            mConnectivityTracker.disable();

            mEnabled = false;
        } else
            Log.w(TAG, "Attempt to disable a second time.");
    }

    private synchronized void register() {
        if (mEnabled && !mRegistered) {
            Log.d(TAG, "Registering " + mDescriptor);
            mNsdManager.registerService(mDescriptor, NsdManager.PROTOCOL_DNS_SD, mListener);
        }
    }

    private synchronized void unregister() {
        if (mEnabled && mRegistered) {
            mNsdManager.unregisterService(mListener);
            mRegistered = false;
        }
    }

    @Override
    public void update(Observable observable, Object obj) {
        if (observable instanceof WifiP2pConnectivityTracker) {
            boolean isConnected = (boolean) obj;
            Log.d(TAG, "Connection change : " + (isConnected ? "CONNECTED" : "DISCONNECTED"));

            if(isConnected) register();
            else unregister();
        }
    }

    private NsdManager.RegistrationListener mListener = new NsdManager.RegistrationListener() {
        @Override
        public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
            Log.d(TAG, "Registration succeeded : " + nsdServiceInfo);

            // @TODO: deal with the case where my UUID is already used ...
            if(!mAssignedUuid.equals(nsdServiceInfo.getServiceName()))
                Log.w(TAG, "UUID not available for Service registration.");

            mRegistered = true;
        }

        @Override
        public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
            Log.v(TAG, "Unregistration succeeded : " + nsdServiceInfo);
        }

        @Override
        public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int error) {
            Log.e(TAG, "Registration failed (" + error + ")");
            mRegistered = false;
        }

        @Override
        public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int error) {
            Log.v(TAG, "Unregistration failed (" + error + ")");
        }
    };
}
