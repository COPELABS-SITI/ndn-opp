package pt.ulusofona.copelabs.ndn.android;

import android.net.wifi.p2p.WifiP2pManager;
import android.util.Log;

public class OperationResult implements WifiP2pManager.ActionListener {
    private String mTag;
    private String mOperation;

    public OperationResult(String tag, String operation) {
        mTag = tag;
        mOperation = operation;
    }

    @Override
    public void onSuccess() {
        Log.v(mTag, mOperation + " : SUCCESS");
    }

    @Override
    public void onFailure(int i) {
        Log.v(mTag, mOperation + " : FAILED (" + i + ")");
    }
}
