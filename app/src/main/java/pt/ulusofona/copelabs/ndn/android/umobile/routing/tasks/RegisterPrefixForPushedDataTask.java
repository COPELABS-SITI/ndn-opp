package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

import android.os.AsyncTask;
import android.util.Log;

import net.named_data.jndn.security.identity1.IdentityManager;
import net.named_data.jndn.security.identity1.MemoryIdentityStorage;
import net.named_data.jndn.security.identity1.MemoryPrivateKeyStorage;
import net.named_data.jndn.security1.KeyChain;
import net.named_data.jndn.security1.SecurityException;
import net.named_data.jndn1.Face;
import net.named_data.jndn1.Name;
import net.named_data.jndn1.OnPushedDataCallback;
import net.named_data.jndn1.OnRegisterFailed;
import net.named_data.jndn1.OnRegisterSuccess;

import java.io.IOException;

/**
 * This class is a AsyncTack which is used to register a prefix into the Face.
 * This prefix is going to be used to exchange Pdata.
 * @author Omar Aponte (COPELABS/ULHT)
 * @version 1.0
 *          COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 02/14/18
 */
public class RegisterPrefixForPushedDataTask extends AsyncTask<Void, Void, Integer> {
    /**
     * Used for debug.
     */
    private static final String TAG = RegisterPrefixForPushedDataTask.class.getSimpleName();

    /**
     * Face use to register the prefix.
     */
    private Face mFace;
    /**
     * Prefix to be registered.
     */
    private String mPrefix;
    /**
     * Interface used to receive pData.
     */
    private OnPushedDataCallback mOnPushedData;
    /**
     * Interface used to know when the registration was success.
     */
    private OnRegisterSuccess mOnRegistrationSuccess;
    /**
     * Interface used to know when the registration failed.
     */
    private OnRegisterFailed mOnRegistrationFailed;


    private int mRetVal = 0;

    /**
     * This is the constructor of the RegisterPrefixForPushedDataTask class.
     * @param face face used to register the prefix.
     * @param prefix prefix to be registered.
     * @param opdc pushdata interface.
     * @param ors registration success interface.
     * @param orf registration fail interface.
     */
    public RegisterPrefixForPushedDataTask(Face face, String prefix, OnPushedDataCallback opdc, OnRegisterSuccess ors, OnRegisterFailed orf) {
        mFace = face;
        mPrefix = prefix;
        mOnPushedData = opdc;
        mOnRegistrationSuccess = ors;
        mOnRegistrationFailed=orf;
    }

    @Override
    protected Integer doInBackground(Void... params) {
        Log.d(TAG, "Register Prefix Task (doInBackground)");

        // Create keychain
        KeyChain keyChain;
        try {
            keyChain = buildTestKeyChain();
        } catch (SecurityException e) {
            e.printStackTrace();
            mRetVal = -1;
            return mRetVal;
        }

        // Register keychain with the face
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
            Log.d(TAG, "Error Register Prefix Task");

        } else {
            Log.d(TAG, "Register Prefix Task ended (onPostExecute)");
        }
    }

    /**
     * This method crates a keyChain
     * @return keyChain created.
     * @throws SecurityException
     */
    private static KeyChain buildTestKeyChain() throws SecurityException {
        MemoryIdentityStorage identityStorage = new MemoryIdentityStorage();
        MemoryPrivateKeyStorage privateKeyStorage = new MemoryPrivateKeyStorage();
        IdentityManager identityManager = new IdentityManager(identityStorage, privateKeyStorage);
        KeyChain keyChain = new KeyChain(identityManager);

        try {
            keyChain.getDefaultCertificateName();
        } catch (SecurityException e) {
            e.printStackTrace();
        }

        keyChain.createIdentity(new Name("/test/identity"));
            keyChain.getIdentityManager().setDefaultIdentity(new Name("/test/identity"));

        return keyChain;
    }

    /**
     * This method executes the registration of the prefix.
     */
    public void registerPrefix(){
        try {
            Log.v(TAG, "Register prefix ...");
            mFace.registerPrefix(new Name(mPrefix), mOnPushedData, mOnRegistrationSuccess, mOnRegistrationFailed);
            Log.v(TAG, "Register prefix issued ...");
        } catch (SecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
