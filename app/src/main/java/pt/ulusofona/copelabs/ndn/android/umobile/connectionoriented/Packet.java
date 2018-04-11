/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-12-22
 * This model was created to encapsulates the information
 * related with packets that are transferred.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;


import net.named_data.jndn1.Data;
import net.named_data.jndn1.Interest;
import net.named_data.jndn.encoding1.EncodingException;
import net.named_data.jndn.encoding1.TlvWireFormat;
import net.named_data.jndn.encoding.tlv1.Tlv;
import net.named_data.jndn.encoding.tlv1.TlvDecoder;

import java.io.Serializable;
import java.nio.ByteBuffer;

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

    public static String getName(byte[] payload) {
        return new Packet(null, null, null, payload).getName();
    }

    private String unmarshalPacket() {
        ByteBuffer byteBuffer = ByteBuffer.wrap(mPayload);
        TlvDecoder tlvDecoder = new TlvDecoder(byteBuffer);
        try {
            if(tlvDecoder.peekType(Tlv.Interest, byteBuffer.remaining())) {
                Interest interest = new Interest();
                interest.wireDecode(byteBuffer, TlvWireFormat.get());
                interest.getInterestLifetimeMilliseconds();
                return interest.getName().toString();
            } else {
                Data data = new Data();
                data.wireDecode(byteBuffer, TlvWireFormat.get());
                return data.getName().toString();
            }
        } catch (EncodingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * This method is a getter of the attribute mId
     * @return mId
     */
    public String getId() {
        return mId;
    }

    /**
     * This method is a getter of the attribute mName
     * @return mName
     */
    public String getName() {
        return unmarshalPacket();
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
