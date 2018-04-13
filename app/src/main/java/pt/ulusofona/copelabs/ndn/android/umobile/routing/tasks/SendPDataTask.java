package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import net.named_data.jndn1.Data;
import net.named_data.jndn1.Face;

import java.io.IOException;
/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class is an AsyncTask used to send Pdata in the face.
 *
 * @author Omar Aponte (COPELABS/ULHT)
 */

public class SendPDataTask extends AsyncTask<Void, Void, Integer> {

    /**
     * Variable used for sebug.
     */
    private static final String TAG = SendPDataTask.class.getSimpleName();
    /**
     * face used to send data.
     */
    private Face mFace;
    /**
     * Data to be sent.
     */
    private Data mData;
    /**
     * This variable change to -1 if the task fails.
     */
    private int mRetVal = 0;

    /**
     * Constructor of the SendPDataTask
     * @param face Face used to sen data.
     * @param data Data to be sent.
     */
    public SendPDataTask(Face face, Data data) {
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
            mRetVal=-1;
        }

        return mRetVal;

    }

    @Override
    protected void onPostExecute(Integer result) {
        if (mRetVal == -1) {
            Log.d(TAG, "Error sending pdata");
        } else {
            Log.d(TAG, "Pdata sent");
        }
    }
}
