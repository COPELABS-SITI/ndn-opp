/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-12-22
 * This model was created to encapsulates the information
 * related with packets that are transferred.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;


import java.io.Serializable;

public class Packet implements Serializable {

    /** This variable is used to identify uniquely the packet */
    private String mId;

    /** This variable is used to holds the data to be transferred */
    private byte [] mPayload;

    /** These variables are used to hold the sender and the recipient of the packet */
    private String mSender, mRecipient;

    public Packet(String id, String sender, String recipient, byte[] payload) {
        mId = id;
        mSender = sender;
        mPayload = payload;
        mRecipient = recipient;
    }

    /**
     * This method is a getter of the attribute mId
     * @return mId
     */
    public String getId() {
        return mId;
    }

    /**
     * This method is a getter of the attribute mPayload
     * @return mPayload
     */
    public byte[] getPayload() {
        return mPayload;
    }

    /**
     * This method is returns the payload size
     * @return payload size
     */
    public int getPayloadSize() {
        return mPayload.length;
    }

    /**
     * This method is a getter of the attribute mSender
     * @return mSender
     */
    public String getSender() {
        return mSender;
    }

    /**
     * This method is a getter of the attribute mRecipient
     * @return mRecipient
     */
    public String getRecipient() {
        return mRecipient;
    }

}
