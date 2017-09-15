package pt.ulusofona.copelabs.ndn.android.ui;

import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnRegisterFailed;
import net.named_data.jndn.OnRegisterSuccess;
import net.named_data.jndn.security.KeyChain;
import net.named_data.jndn.security.SecurityException;
import net.named_data.jndn.security.identity.IdentityManager;
import net.named_data.jndn.security.identity.MemoryIdentityStorage;
import net.named_data.jndn.security.identity.MemoryPrivateKeyStorage;

import java.io.IOException;

public class ExpressInterestTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = ExpressInterestTask.class.getSimpleName();

    private Face mFace;
    private Name mPrefix;
    private OnData mOnDataCallback;

    private int mRetVal = 0;

    public ExpressInterestTask(Face face, Name prefix, OnData odc) {
        mFace = face;
        mPrefix = prefix;
        mOnDataCallback = odc;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "Express Interest");

        try {
            mFace.expressInterest(new Interest(mPrefix, 60000), mOnDataCallback);
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
