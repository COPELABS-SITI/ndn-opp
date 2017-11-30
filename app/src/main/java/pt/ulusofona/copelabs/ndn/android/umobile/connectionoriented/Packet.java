package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented;


import java.io.Serializable;

public class Packet implements Serializable {

    private String mId;
    private String mSender;
    private byte [] mPayload;

    public Packet(String id, String sender, byte[] payload) {
        mId = id;
        mSender = sender;
        mPayload = payload;
    }

    public String getId() {
        return mId;
    }

    public String getSender() {
        return mSender;
    }

    public byte[] getPayLoad() {
        return mPayload;
    }

}
