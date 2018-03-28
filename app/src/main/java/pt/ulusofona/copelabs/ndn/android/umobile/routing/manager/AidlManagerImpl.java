package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.senception.contextualmanager.aidl.CManagerInterface;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.ContextualManagerNotConnectedException;

/**
 * Created by miguel on 07-03-2018.
 */

public class AidlManagerImpl implements AidlManager.Manager, ServiceConnection {

    private static final String TAG = AidlManagerImpl.class.getSimpleName();
    private static final String CM_PKG_NAME = "com.senception.contextualmanager";
    private CManagerInterface mRemoteContextualManager;
    private AidlManager.Listener mListener;
    private Context mContext;
    private boolean mBound;


    AidlManagerImpl(Context context, AidlManager.Listener listener) {
        mContext = context;
        mListener = listener;
    }

    @Override
    public synchronized void start() {
        if(!mBound) {
            Intent intent = new Intent().setPackage(CM_PKG_NAME);
            mBound = mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
    }

    @Override
    public synchronized void stop() {
        if(isBound()) {
            mContext.unbindService(this);
            mBound = false;
        }
    }

    @Override
    public boolean isBound() {
        return mBound;
    }

    @Override
    public int getAvailability() throws RemoteException, ContextualManagerNotConnectedException {
        if(isBound()) {
            return mRemoteContextualManager.getAvailability();
        }
        throw new ContextualManagerNotConnectedException();
    }

    @Override
    public int[] getCentrality() throws RemoteException, ContextualManagerNotConnectedException {
        if(isBound()) {
            return mRemoteContextualManager.getCentrality();
        }
        throw new ContextualManagerNotConnectedException();
    }

    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mRemoteContextualManager = CManagerInterface.Stub.asInterface(iBinder);
        mListener.onContextualManagerConnected();
        Log.i(TAG, "Connected to Contextual Manager");
    }

    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mRemoteContextualManager = null;
        mListener.onContextualManagerDisconnected();
        Log.i(TAG, "Disconnected from Contextual Manager");
    }

}
