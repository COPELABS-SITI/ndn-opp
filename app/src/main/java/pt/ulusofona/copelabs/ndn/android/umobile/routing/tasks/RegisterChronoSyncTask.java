package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.sync.ChronoSync2013;

import java.io.IOException;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.utilities.Utilities;

/**
 * This class is a AsyncTack which is used to register a prefix into the Face.
 *
 * @author Omar Aponte (COPELABS/ULHT)
 * @version 1.0
 *          COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 02/14/18
 */
public class RegisterChronoSyncTask extends AsyncTask<Void, Void, Integer> {

    public interface ChronoInterface{
        void onChronoSyncRegistered(ChronoSync2013 chronoSync2013);
    }
    /** Variable used for debug. */
    private static final String TAG = RegisterChronoSyncTask.class.getSimpleName();
    /** NDN face object. */
    private Face mFace;

    /** Integer which values determines if an error occur during the process. */
    private int mRetVal = 0;

    /** Context of the application. */
    private Context mContext;

    /** Dialog used to notify users about that a background process is being performed. */
    private ProgressDialog dialog;

    private Name mApplicationDataPrefix;

    private Name mApplicationBroadcastPrefix;

    private ChronoInterface mInterface;

    private double mSyncLifeTime = 60 * 1000;

    private ChronoSync2013 mChronoSync;

    private long mSessionNo = 0L;

    private ChronoSync2013.OnReceivedSyncState mOnReceivedSyncState;

    private ChronoSync2013.OnInitialized mOnInitialized;

    private KeyChain mKeyChain;

    private OnRegisterFailed mOnRegisterFailed;
    /**
     * RegisterPrefixTask constructor.
     */
    public RegisterChronoSyncTask(Face face, String applicationDataPrefix, String applicationBroadcastPrefix, ChronoSync2013.OnInitialized onInitialized, ChronoSync2013.OnReceivedSyncState onReceivedSyncState, OnRegisterFailed onRegisterFailed, ChronoInterface chronoInterface, Context context) {
        mFace = face;
        mContext=context;
        mApplicationDataPrefix = new Name(applicationDataPrefix);
        mApplicationBroadcastPrefix = new Name(applicationBroadcastPrefix);
        mOnInitialized = onInitialized;
        mOnReceivedSyncState = onReceivedSyncState;
        mOnRegisterFailed = onRegisterFailed;
        mInterface=chronoInterface;
    }


    /**
     * Performs the registration of the prefixes.
     * @param params
     * @return Integer value, where 0 means that the operation was succeed, otherwise the value will be -1;
     */
    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "Register ChronoSync: ApplicationDataPrefix: " + mApplicationDataPrefix.toString() + " ApplicationBroadCastPrefix: " + mApplicationBroadcastPrefix.toString());

        try {

            mKeyChain = Utilities.buildTestKeyChain();

            mChronoSync = new ChronoSync2013(mOnReceivedSyncState,
                    mOnInitialized,
                    mApplicationDataPrefix,
                    mApplicationBroadcastPrefix,
                    mSessionNo,
                    mFace,
                    mKeyChain,
                    mKeyChain.getDefaultCertificateName(),
                    mSyncLifeTime,
                    mOnRegisterFailed);

        } catch (IOException | net.named_data.jndn.security.SecurityException e) {
            e.printStackTrace();
        }

        return mRetVal;

    }

    /**
     * After execute the task.
     *
     * @param result Integer which value indicates if the task was done successfully.
     */
    @Override
    protected void onPostExecute(final Integer result) {
        if (mRetVal == -1) {
            Log.d(TAG, "Error Register Prefix Task");
        } else {
            mInterface.onChronoSyncRegistered(mChronoSync);
            Log.d(TAG, "Register Prefix Task ended (onPostExecute)");
        }
    }

}
