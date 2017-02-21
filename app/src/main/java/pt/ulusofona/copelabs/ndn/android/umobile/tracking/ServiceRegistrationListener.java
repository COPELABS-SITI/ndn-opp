/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-21
 * The RegistrationListener used by the ServiceTracker.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.util.Log;

class ServiceRegistrationListener implements NsdManager.RegistrationListener {
    private static final String TAG = ServiceRegistrationListener.class.getSimpleName();

    private ServiceTracker mTracker;

    ServiceRegistrationListener(ServiceTracker t) {
        mTracker = t;
    }

    @Override
    public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
        String assignedName = nsdServiceInfo.getServiceName();
        Log.d(TAG, "Registration succeeded : " + assignedName);

        if(!mTracker.mAssignedUuid.equals(assignedName))
            //TODO: deal with the case where my UUID is already used ...
            Log.w(TAG, "UUID not available for Service registration.");

        mTracker.mRegistered = true;
    }

    @Override
    public void onServiceUnregistered(NsdServiceInfo nsdServiceInfo) {
        Log.d(TAG, "Unregistration succeeded : " + nsdServiceInfo.getServiceName());
        mTracker.mRegistered = false;
    }

    @Override
    public void onRegistrationFailed(NsdServiceInfo nsdServiceInfo, int error) {
        Log.d(TAG, "Registration failed (" + error + ")");
    }

    @Override
    public void onUnregistrationFailed(NsdServiceInfo nsdServiceInfo, int error) {
        Log.d(TAG, "Unregistration failed (" + error + ")");
    }
}
