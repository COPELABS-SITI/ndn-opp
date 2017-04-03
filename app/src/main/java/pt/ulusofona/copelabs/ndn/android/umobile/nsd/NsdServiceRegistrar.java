/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * The NsdServiceRegistrar takes care of registering and unregistering a Service
 * for advertisement on an IP network.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.nsd;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

import pt.ulusofona.copelabs.ndn.android.NsdService;

public class NsdServiceRegistrar {
    private static final String TAG = NsdServiceRegistrar.class.getSimpleName();

    private NsdManager mNsdManager;

    private boolean mRegistered = false;

    private String mAssignedUuid;

    public synchronized void register(Context context, String uuid, int port) {
        if(!mRegistered) {
            Log.v(TAG, "Enabling");
            mNsdManager = (NsdManager) context.getSystemService(Context.NSD_SERVICE);

            mAssignedUuid = uuid;

            NsdServiceInfo mDescriptor = new NsdServiceInfo();
            mDescriptor.setServiceName(mAssignedUuid);
            mDescriptor.setServiceType(NsdService.SERVICE_TYPE);
            mDescriptor.setPort(port);

            Log.d(TAG, "Registering " + mDescriptor);
            mNsdManager.registerService(mDescriptor, NsdManager.PROTOCOL_DNS_SD, mListener);

            mRegistered = true;
        } else
            Log.w(TAG, "Attempt to register a second time.");
    }

    public synchronized void unregister() {
        if(mRegistered) {
            Log.v(TAG, "Unregistering");
            mNsdManager.unregisterService(mListener);
            mRegistered = false;
        } else
            Log.w(TAG, "Attempt to unregister a second time.");
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
