/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Task to respond to an interest.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.tasks;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import net.named_data.jndn1.Data;
import net.named_data.jndn1.Face;

import java.io.IOException;

public class RespondToInterestTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = RespondToInterestTask.class.getSimpleName();

    private Face mFace;
    private Data mData;

    private int mRetVal = 0;

    public RespondToInterestTask(Face face, Data data) {
        mFace = face;
        mData = data;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "Responding with WifiP2pCache [" + Base64.encodeToString(mData.getContent().getImmutableArray(), Base64.NO_PADDING) + "]");

        try {
            mFace.putData(mData);
        } catch (IOException e) {
            e.printStackTrace();
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
