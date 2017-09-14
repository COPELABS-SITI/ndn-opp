/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This class provides management of WiFi P2P Group Formation.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile;

import android.content.Context;

/** Manager for WifiP2p connectivity to take care of everything related to forming groups, connecting
 *  devices together. */
public class OpportunisticConnectivityManager {
    private static final String TAG = OpportunisticConnectivityManager.class.getSimpleName();
    //private static final long SERVICE_DISCOVERY_INTERVAL = 2500; // Milliseconds between re-issuing a request to discover services.

    private Context mContext;

    public void enable(Context context) {
        mContext = context;

    }

    public void disable() {

    }
}