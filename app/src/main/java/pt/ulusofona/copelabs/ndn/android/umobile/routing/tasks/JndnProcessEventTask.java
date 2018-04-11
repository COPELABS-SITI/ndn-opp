package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.encoding.EncodingException;

import java.io.IOException;

/**
 * This class is AsyncTask which is checking the faces with the intention of see if there is a new
 * interest or data incoming to the face.
 */
public class JndnProcessEventTask extends AsyncTask<Void, Void, Integer> {

    private static final String TAG = JndnProcessEventTask.class.getSimpleName();

    /** NDN Face object. */
    private Face mFace;

    /** Integer which values determines if an error occur during the process. */
    private int mRetVal = 0;

    /**
     * JndnProcessEventTask constructor.
     *
     * @param face NDN face object.
     */
    public JndnProcessEventTask(Face face) {
        mFace = face;
    }

    @Override
    protected Integer doInBackground(Void... params) {

        //Log.d(TAG, "JndnProcessEvents (doInBackground)");
        //Process events.
        try {
            mFace.processEvents();
        } catch (IOException e) {
            e.printStackTrace();
            //Error occurred.
            mRetVal = -1;
        } catch (EncodingException e) {
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
            //Log.d(TAG, "Register Prefix Task ended (onPostExecute)");
        }
    }
}
