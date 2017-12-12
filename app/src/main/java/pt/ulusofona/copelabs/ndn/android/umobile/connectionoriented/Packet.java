package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;


import java.io.Serializable;

public class Packet implements Serializable {

    private String mId;
    private byte [] mPayload;
    private String mSender, mRecipient;

    public Packet(String id, String sender, String recipient, byte[] payload) {
        mId = id;
        mSender = sender;
        mPayload = payload;
        mRecipient = recipient;
    }

    public String getId() {
        return mId;
    }

    public byte[] getPayload() {
        return mPayload;
    }

    public int getPayloadSize() {
        return mPayload.length;
    }

    public String getSender() {
        return mSender;
    }

    public String getRecipient() {
        return mRecipient;
    }

}
