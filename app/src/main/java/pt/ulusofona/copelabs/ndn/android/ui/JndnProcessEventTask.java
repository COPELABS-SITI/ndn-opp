package pt.ulusofona.copelabs.ndn.android.ui;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnRegisterSuccess;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;

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
        Log.d(TAG, "JndnProcessEvents (doInBackground)");

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
            Log.d(TAG, "Error Register Prefix Task");
        } else {
            Log.d(TAG, "Register Prefix Task ended (onPostExecute)");
        }
    }
}
