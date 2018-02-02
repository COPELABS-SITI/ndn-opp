package pt.ulusofona.copelabs.ndn.android.ui.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.encoding.EncodingException;

import java.io.IOException;

public class JndnProcessEventTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = JndnProcessEventTask.class.getSimpleName();

    private Face mFace;

    private int mRetVal = 0;

    public JndnProcessEventTask(Face face) {
        mFace = face;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        //Log.d(TAG, "JndnProcessEvents (doInBackground)");

        try {
            mFace.processEvents();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (EncodingException e) {
            e.printStackTrace();
        }

        return mRetVal;
    }

    @Override
    protected void onPostExecute(final Integer result) {
        if (mRetVal == -1) {
            Log.e(TAG, "Error ServiceRegister Prefix Task");
        } else {
            //Log.d(TAG, "ServiceRegister Prefix Task ended (onPostExecute)");
        }
    }
}
