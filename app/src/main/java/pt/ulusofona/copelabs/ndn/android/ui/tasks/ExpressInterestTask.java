/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Task to express interests.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.OnData;

import java.io.IOException;

public class ExpressInterestTask extends AsyncTask<Void, Void, Integer> {

    /** This variable is used to debug ExpressInterestTask class */
    private static final String TAG = ExpressInterestTask.class.getSimpleName();

    /** This object is used to receive data packets */
    private OnData mOnDataCallback;

    /** This object is used to send the interest */
    private Interest mInterest;

    /** This object is used to define which face will be used to send the interest */
    private Face mFace;

    /** This variable is used to store the result of the process */
    private int mRetVal;

    public ExpressInterestTask(Face face, Interest interest, OnData odc) {
        mFace = face;
        mInterest = interest;
        mOnDataCallback = odc;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "Express Interest");

        try {
            mFace.expressInterest(mInterest, mOnDataCallback);
            mRetVal = 0;
        } catch (IOException e) {
            e.printStackTrace();
            mRetVal = -1;
        }

        return mRetVal;

    }

    @Override
    protected void onPostExecute(final Integer result) {
        if (mRetVal == -1) {
            Log.d(TAG, "Error Expressing Interest");
        } else {
            Log.d(TAG, "Interest expressed");
        }
    }
}
