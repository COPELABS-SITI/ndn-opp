package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;

/**
 * Created by miguel on 13-03-2018.
 */


public class RoutingEntry {

    private static final long DEFAULT_ORIGIN = 128L;
    private static final long DEFAULT_FLAG = 1L;
    private String mPrefix;
    private long mFace;
    private long mOrigin;
    private long mCost;
    private long mFlag;


    public RoutingEntry(String prefix, long face, long cost) {
        mPrefix = prefix;
        mFace = face;
        mCost = cost;
        mOrigin = DEFAULT_ORIGIN;
        mFlag = DEFAULT_FLAG;
    }

    public String getPrefix() {
        return mPrefix;
    }

    public void setPrefix(String mPrefix) {
        this.mPrefix = mPrefix;
    }

    public long getFace() {
        return mFace;
    }

    public void setFace(long mFace) {
        this.mFace = mFace;
    }

    public long getOrigin() {
        return mOrigin;
    }

    public void setOrigin(long mOrigin) {
        this.mOrigin = mOrigin;
    }

    public long getCost() {
        return mCost;
    }

    public void setCost(long mCost) {
        this.mCost = mCost;
    }

    public long getFlag() {
        return mFlag;
    }

    public void setFlag(long mFlag) {
        this.mFlag = mFlag;
    }
}
