/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class implements the methods needed to communicate
 * with contextual manager.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.senception.contextualmanager.aidl.CManagerInterface;

import java.util.List;
import java.util.Map;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.ContextualManagerNotConnectedException;


public class AidlManagerImpl implements AidlManager.Manager, ServiceConnection {

    /** This variable is used to debug AidlManagerImpl class */
    private static final String TAG = AidlManagerImpl.class.getSimpleName();

    /** This variable holds the contextual manager package name in order to establish the communication */
    private static final String CM_PKG_NAME = "com.senception.contextualmanager";

    /** This object is used to communicate with contextual manager */
    private CManagerInterface mRemoteContextualManager;

    /** This listener is used as a callback in order to notify occurred events */
    private AidlManager.Listener mListener;

    /** This variable holds the application context */
    private Context mContext;

    /** This variable holds the contextual manager connection status */
    private boolean mBound;


    /**
     * This method is the constructor of AidlManagerImpl class
     * @param context Application context
     * @param listener callback listener
     */
    AidlManagerImpl(Context context, AidlManager.Listener listener) {
        mContext = context;
        mListener = listener;
    }

    /**
     * This method binds the communications with contextual manager
     */
    @Override
    public synchronized void start() {
        if(!mBound) {
            Intent intent = new Intent().setPackage(CM_PKG_NAME);
            mBound = mContext.bindService(intent, this, Context.BIND_AUTO_CREATE);
        }
    }

    /**
     * This method unbinds the communications with contextual manager
     */
    @Override
    public synchronized void stop() {
        if(isBound()) {
            mContext.unbindService(this);
            mBound = false;
        }
    }

    /**
     * This method checks if the connection with contextual manager is bound
     * @return if bound returns true, if not returns false
     */
    @Override
    public boolean isBound() {
        return mBound;
    }

    /**
     * This method returns the device's availability
     * @return device's availability
     * @throws RemoteException
     * @throws ContextualManagerNotConnectedException
     */
    @Override
    public Map getAvailability(List<String> cmIdentifiers) throws RemoteException, ContextualManagerNotConnectedException {
        if(isBound()) {
            return mRemoteContextualManager.getAvailability(cmIdentifiers);
        }
        throw new ContextualManagerNotConnectedException();
    }

    /**
     * This method returns the device's centrality
     * @return device's centrality
     * @throws RemoteException
     * @throws ContextualManagerNotConnectedException
     */
    @Override
    public Map getCentrality(List<String> cmIdentifiers) throws RemoteException, ContextualManagerNotConnectedException {
        if(isBound()) {
            return mRemoteContextualManager.getCentrality(cmIdentifiers);
        }
        throw new ContextualManagerNotConnectedException();
    }

    /**
     * This method returns the device's similarity
     * @return device's similarity
     * @throws RemoteException
     * @throws ContextualManagerNotConnectedException
     */
    @Override
    public Map getSimilarity(List<String> cmIdentifiers) throws RemoteException, ContextualManagerNotConnectedException {
        if(isBound()) {
            return mRemoteContextualManager.getSimilarity(cmIdentifiers);
        }
        throw new ContextualManagerNotConnectedException();
    }

    /**
     * This method is invoked once the contextual manager connects
     * @param componentName
     * @param iBinder
     */
    @Override
    public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
        mRemoteContextualManager = CManagerInterface.Stub.asInterface(iBinder);
        mListener.onContextualManagerConnected();
        Log.i(TAG, "Connected to Contextual Manager");
    }

    /**
     * This method is invoked once the contextual manager disconnects
     * @param componentName
     */
    @Override
    public void onServiceDisconnected(ComponentName componentName) {
        mRemoteContextualManager = null;
        mListener.onContextualManagerDisconnected();
        Log.i(TAG, "Disconnected from Contextual Manager");
    }

}
