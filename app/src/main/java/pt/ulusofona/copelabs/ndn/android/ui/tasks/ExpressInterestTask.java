package pt.ulusofona.copelabs.ndn.android.ui.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.OnData;

import java.io.IOException;

public class ExpressInterestTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = ExpressInterestTask.class.getSimpleName();

    private Face mFace;
    private Interest mInterest;
    private OnData mOnDataCallback;

    private int mRetVal = 0;

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
