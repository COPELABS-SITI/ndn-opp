package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;

/**
 * Created by miguel on 13-03-2018.
 */

public class RibEntry {

    private String mPrefix;
    private long mFaceId;
    private long mOrigin;
    private long mCost;
    private long mFlag;

    public RibEntry(String prefix, long faceId, long origin, long cost) {
        mPrefix = prefix;
        mFaceId = faceId;
        mOrigin = origin;
        mCost = cost;
        mFlag = 1L;
    }



}
