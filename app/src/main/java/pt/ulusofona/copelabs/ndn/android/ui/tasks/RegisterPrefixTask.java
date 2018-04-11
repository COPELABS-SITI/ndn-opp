/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Task to register a prefix.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.security.identity1.IdentityManager;
import net.named_data.jndn.security.identity1.MemoryIdentityStorage;
import net.named_data.jndn.security.identity1.MemoryPrivateKeyStorage;
import net.named_data.jndn1.Face;
import net.named_data.jndn1.Name;
import net.named_data.jndn1.OnInterestCallback;
import net.named_data.jndn1.OnRegisterFailed;
import net.named_data.jndn1.OnRegisterSuccess;
import net.named_data.jndn.security1.KeyChain;
import net.named_data.jndn.security1.SecurityException;

import java.io.IOException;

public class RegisterPrefixTask extends AsyncTask<Void, Void, Integer> {
    private static final String TAG = RegisterPrefixTask.class.getSimpleName();

    private Face mFace;
    private String mPrefix;
    private OnRegisterSuccess mOnRegistrationSuccess;
    private OnInterestCallback mOnInterestCallback;

    private int mRetVal = 0;

    public RegisterPrefixTask(Face face, String prefix, OnRegisterSuccess ors, OnInterestCallback oic) {
        mFace = face;
        mPrefix = prefix;
        mOnRegistrationSuccess = ors;
        mOnInterestCallback = oic;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "ServiceRegister Prefix WifiP2pCacheRefresher (doInBackground)");

        // Create keychain
        KeyChain keyChain;
        try {
            keyChain = buildTestKeyChain();
        } catch (SecurityException e) {
            e.printStackTrace();
            mRetVal = -1;
            return mRetVal;
        }

        // ServiceRegister keychain with the face
        keyChain.setFace(mFace);
        try {
            mFace.setCommandSigningInfo(keyChain, keyChain.getDefaultCertificateName());
        } catch (SecurityException e) {
            e.printStackTrace();
            mRetVal = -1;
            return mRetVal;
        }

        registerPrefix();

        return mRetVal;

    }

    @Override
    protected void onPostExecute(final Integer result) {
        if (mRetVal == -1) {
            Log.d(TAG, "Error ServiceRegister Prefix WifiP2pCacheRefresher");
        } else {
            Log.d(TAG, "ServiceRegister Prefix WifiP2pCacheRefresher ended (onPostExecute)");
        }
    }

    private static KeyChain buildTestKeyChain() throws net.named_data.jndn.security1.SecurityException {
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);
        KeyChain keyChain = new KeyChain(identityManager);
        try {
            keyChain.getDefaultCertificateName();
        } catch (net.named_data.jndn.security1.SecurityException e) {
            keyChain.createIdentity(new Name("/test/identity"));
            keyChain.getIdentityManager().setDefaultIdentity(new Name("/test/identity"));
        }
        return keyChain;
    }

    public void registerPrefix(){
        try {
            Log.v(TAG, "ServiceRegister prefix ...");
            mFace.registerPrefix(new Name(mPrefix), mOnInterestCallback, new OnRegisterFailed() {
                @Override
                public void onRegisterFailed(Name prefix) {
                    Log.e(TAG, "Registration failed : " + prefix);
                }
            }, mOnRegistrationSuccess);
            Log.v(TAG, "ServiceRegister prefix issued ...");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
