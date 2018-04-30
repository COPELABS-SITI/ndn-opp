package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import android.os.Handler;
import android.util.Log;

import net.named_data.jndn.util1.Blob;
import net.named_data.jndn1.Data;
import net.named_data.jndn1.Face;
import net.named_data.jndn1.Name;
import net.named_data.jndn1.OnPushedDataCallback;
import net.named_data.jndn1.OnRegisterFailed;
import net.named_data.jndn1.OnRegisterSuccess;

import org.apache.commons.lang3.SerializationUtils;

import pt.ulusofona.copelabs.ndn.android.ui.tasks.JndnProcessEventTask;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks.RegisterPrefixForPushedDataTask;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks.SendPDataTask;

/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class implements the methods to send and receive pdata.
 *
 * @author Omar Aponte (COPELABS/ULHT)
 */
public class PDataManager implements OnRegisterFailed, OnPushedDataCallback,OnRegisterSuccess {
    /**
     * Variable used for debug.
     */
    private static final String TAG = PDataManager.class.getSimpleName();

    /**
     * Variable used to determine the time to check new events in the face.
     */
    private static int PROCESS_INTERVAL = 1000;
    /**
     * Prefix used to specify the prefix.
     */
    private static final String PREFIX = "/ndn/opp";
    /**
     * Name used for dabber.
     */
    private static final String DABBER_PREFIX = PREFIX + "/dabber";

    /**
     * Identifier of each pData.
     */
    private int mPdatanumber=0;

    /**
     * Identifier of the actual user.
     */
    private String mUuid;

    public interface PDataManagerInterface {
        void pDataInComing(Plsa plsa);
    }

    /**
     * interface used to communicate new pDatas.
     */
    private PDataManagerInterface mInterface;

    /**
     * Face used to send and receive pDatas.
     */
    private Face mFace = new Face("127.0.0.1");

    /**
     * Constructor of PDataManager.
     * @param PDataManagerInterface Interface used to receive new pData.
     */
    public PDataManager(PDataManagerInterface PDataManagerInterface, String UUID){
        mUuid=UUID;
        mInterface= PDataManagerInterface;
        registerPrefix();
    }

    /**
     * Method used to register a prefix used to receive pData.
     */
    private void registerPrefix(){
        new RegisterPrefixForPushedDataTask(mFace, DABBER_PREFIX, this, this, this).execute();
        mHandler.postDelayed(mJndnProcessor, PROCESS_INTERVAL);
    }

    /**
     * Handler used to control the time to check new evente in the face. This is used in order to know
     * when a new pData arrives to NDN.
     */
    private Handler mHandler = new Handler();
    private Runnable mJndnProcessor = new Runnable() {
        @Override
        public void run() {
            new JndnProcessEventTask(mFace).execute();
            mHandler.postDelayed(mJndnProcessor, PROCESS_INTERVAL);
        }
    };

    /**
     * Method used to send pData.
     * @param plsa PLsa to be sent.
     */
    public void sendPushData(Plsa plsa){
        Name dName = new Name(DABBER_PREFIX + "/" +mUuid+"/"+ mPdatanumber );
        Data data = new Data(dName);
        data.setPushed(true);
        Blob blob = new Blob(SerializationUtils.serialize(plsa));
        data.setContent(blob);
        mPdatanumber++;
        new SendPDataTask(mFace,data).execute();
    }

    /**
     * Method used to receive the pDatas from Jndnd.
     * @param data Pdata packet.
     */
    @Override
    public void onPushedData(Data data) {
        Log.v(TAG, "Push Data Received : " + data.getName().toString());
        Plsa plsa = SerializationUtils.deserialize(data.getContent().getImmutableArray());
        mInterface.pDataInComing(plsa);
    }

    /**
     * this method is called when the prefix was registered with success.
     * @param name Name of the prefix registered.
     * @param l id of the prefix registered.
     */
    @Override
    public void onRegisterSuccess(Name name, long l) {
        Log.v(TAG, "Registration Success : " + name.toString());
        Log.d(TAG,"Register ok");
    }

    /**
     * This method is called when the registration of a prefix failed.
     * @param name name of the prefix which registration was failed.
     */
    @Override
    public void onRegisterFailed(Name name) {
        Log.v(TAG, "Registration Failed : " + name.toString());
        Log.d(TAG,"Starting a new registration task");
        new RegisterPrefixForPushedDataTask(mFace, name.toString(), this, this, this).execute();
    }
}
