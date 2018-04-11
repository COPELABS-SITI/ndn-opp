package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;

import java.io.IOException;

/**
 * This class is a AsyncTack which is used to send data though the face.
 *
 * @author Omar Aponte (COPELABS/ULHT)
 * @version 1.0
 *          COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 02/14/18
 */
public class SendDataTask extends AsyncTask<Void, Void, Integer> {

    /** String used for debug. */
    private static final String TAG = SendDataTask.class.getSimpleName();

    /** NDN face object. */
    private Face mFace;

    /** NDN data object. */
    private Data mData;

    /** Integer which values determines if an error occur during the process. */
    private int mRetVal = 0;

    /**
     * SendDataTask constructor.
     *
     * @param face              NDN face object.
     * @param data              NDN data object.
     */
    public SendDataTask(Face face, Data data) {
        mFace = face;
        mData = data;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "Responding with Data [" + Base64.encodeToString(mData.getContent().getImmutableArray(), Base64.NO_PADDING) + "]");

        try {
            mFace.putData(mData);
        } catch (IOException e) {
            e.printStackTrace();
            //Error occurred.
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
    protected void onPostExecute(Integer result) {
        if (mRetVal == -1) {
            Log.d(TAG, "Data sent fail");
        } else {
            Log.d(TAG, "Data sent");
        }
    }

}
