/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Task to process Jndn events.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.encoding1.EncodingException;
import net.named_data.jndn1.Face;

import java.io.IOException;

public class JndnProcessEventTask extends AsyncTask<Void, Void, Integer> {

    /** This variable is used to debug this class */
    private static final String TAG = JndnProcessEventTask.class.getSimpleName();

    /** This variable is used store the operation's result */
    private int mRetVal = 0;

    /** This object is used to process it's events */
    private Face mFace;

    public JndnProcessEventTask(Face face) {
        mFace = face;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        try {
            mFace.processEvents();
            mRetVal = 0;
        } catch (IOException e) {
            e.printStackTrace();
            mRetVal = -1;
        } catch (EncodingException e) {
            e.printStackTrace();
            mRetVal = -1;
        }
        return mRetVal;
    }

    @Override
    protected void onPostExecute(final Integer result) {
        if (mRetVal == -1) {
            Log.e(TAG, "Error ServiceRegister Prefix WifiP2pCacheRefresher");
        }
    }
}
