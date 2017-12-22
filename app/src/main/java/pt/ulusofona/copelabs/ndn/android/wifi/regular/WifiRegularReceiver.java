package pt.ulusofona.copelabs.ndn.android.wifi.regular;


import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;

public class WifiRegularReceiver extends BroadcastReceiver {


    private static final String TAG = WifiRegularReceiver.class.getSimpleName();

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals(WifiManager.NETWORK_STATE_CHANGED_ACTION)) {
            WifiManager wifiMgr = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            String ssid = wifiMgr.getConnectionInfo().getSSID();
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo != null) {
                NetworkInfo.DetailedState netState = networkInfo.getDetailedState();
                switch (netState) {
                    case CONNECTED:
                        Log.i(TAG, "Connected via Wi-Fi to " + ssid);
                        WifiRegularListenerManager.notifyConnected();
                        break;
                    case DISCONNECTED:
                        Log.i(TAG, "Disconnected from AP");
                        WifiRegularListenerManager.notifyDisconnected();
                        break;

                }
            }

        }
    }
}
