package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnTimeout;

import java.io.IOException;

/**
 * This class is a AsyncTack which is used to express interest into the Face. Also, it implements
 * OnRegisterFailed interface in order to check if the action was done successfully or not.
 *
 * @author Omar Aponte (COPELABS/ULHT)
 * @version 1.0
 *          COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 02/14/18
 */
public class ExpressInterestTask extends AsyncTask<Void, Void, Integer> implements OnRegisterFailed {

    /** String used for debug. */
    private static final String TAG = ExpressInterestTask.class.getSimpleName();

    /** NDN face object. */
    private Face mFace;

    /** NDN interest object. */
    private Interest mInterest;

    /** Long Id of the interest. */
    private long mInterestId;
    /** Interface used to receive data. */
    private OnData mOnDataCallback;

    /** Interface used to receive the timeout of the interest. */
    private OnTimeout mOnTimeOot;
    /** Integer which values determines if an error occur during the process. */
    private int mRetVal = 0;

    /**
     * Constructor of ExpressInterestTask.
     *
     * @param face     NDN face.
     * @param interest NDN interest object.
     * @param odc      Interface implemented.
     */
    public ExpressInterestTask(Face face, Interest interest, OnData odc, OnTimeout onTimeout) {
        mFace = face;
        mInterest = interest;
        mOnDataCallback = odc;
        mOnTimeOot=onTimeout;
        Log.d(TAG, "interest time out: " + interest.getInterestLifetimeMilliseconds());
    }

    /**
     * In this method Expressing Interest is performed.
     *
     * @param params
     * @return Integer value, if is 0 the action was done successfully otherwise will be -1.
     */
    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "Express Interest: " + mInterest.getName().toString());

        //Expressing interest.
        try {
            mInterestId = mFace.expressInterest(mInterest, mOnDataCallback, mOnTimeOot);
        } catch (IOException e) {
            e.printStackTrace();
            //Error occurred
            mRetVal = -1;
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
            Log.d(TAG, "Error Expressing Interest");
        } else {
            Log.d(TAG, "Interest expressed: " + mInterestId);
            //mFace.removePendingInterest(mInterestId);
        }
    }

    /**
     * This function is call if the expressing interest task was not done successfully.
     *
     * @param name name of the prefix which was expressed.
     */
    @Override
    public void onRegisterFailed(Name name) {
        Log.d(TAG, "Registration failed");
    }
}
