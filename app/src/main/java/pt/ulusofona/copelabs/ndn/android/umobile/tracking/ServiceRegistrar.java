/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-20
 * //TODO: Description.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.umobile.tracking;

import android.content.Context;
import android.net.nsd.NsdManager;
import android.net.nsd.NsdServiceInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.SocketException;
import java.util.Enumeration;
import java.util.Locale;

import pt.ulusofona.copelabs.ndn.android.umobile.Routing;

class ServiceRegistrar {
    private static final String TAG = ServiceRegistrar.class.getSimpleName();

    private static final int DEFAULT_PORT = 16363;

    private final Routing mRouting;

    private NsdManager mNsdManager;
    private String mAssignedUuid;

    private boolean mRegistered = false;
    private RegistrationListener mListener = new RegistrationListener();

    ServiceRegistrar(String uuid, Routing rt) {
        mAssignedUuid = uuid;
        mRouting = rt;
    }

    public void enable(Context ctxt) {
        if (!mRegistered) {
            try {
                try {
                    Enumeration<NetworkInterface> nis = NetworkInterface.getNetworkInterfaces();
                    while(nis.hasMoreElements())
                        Log.d(TAG, "NetworkInterface: " + nis.nextElement().toString());
                } catch (SocketException e) {
                    e.printStackTrace();
                }

                ServerSocket socket = new ServerSocket(DEFAULT_PORT);
                Log.d(TAG, "Opening socket on " + socket.getInetAddress().getHostAddress() + ":" + socket.getLocalPort());

                mRouting.enable(socket);

                NsdServiceInfo mDescriptor = new NsdServiceInfo();
                mDescriptor.setServiceName(mAssignedUuid);
                mDescriptor.setServiceType(ServiceTracker.SERVICE_TYPE);
                mDescriptor.setHost(socket.getInetAddress());
                mDescriptor.setPort(DEFAULT_PORT);

                mNsdManager = (NsdManager) ctxt.getSystemService(Context.NSD_SERVICE);
                mNsdManager.registerService(mDescriptor, NsdManager.PROTOCOL_DNS_SD, mListener);
            } catch (IOException e) {
                Log.e(TAG, "Failed to create ServerSocket.");
            }
        }
    }

    public void disable() {
        if (mRegistered) {
            mRouting.disable();
            mNsdManager.unregisterService(mListener);
        }
    }

    private class RegistrationListener implements NsdManager.RegistrationListener {
        @Override
        public void onServiceRegistered(NsdServiceInfo nsdServiceInfo) {
            String assignedName = nsdServiceInfo.getServiceName();
            Log.d(TAG, "Registration succeeded : " + assignedName);

            // @TODO: deal with the case where my UUID is already used ...
            if(!mAssignedUuid.equals(assignedName))
                Log.w(TAG, "UUID not available for Service registration.");

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
    }
}
