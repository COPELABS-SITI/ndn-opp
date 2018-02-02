package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.database;


import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models.NsdInfo;

public class FailureData {

    private ConcurrentHashMap<String, Integer> mFailureData = new ConcurrentHashMap<>();
    private ConcurrentHashMap<String, NsdInfo> mPeerData = new ConcurrentHashMap<>();

    public void add(NsdInfo nsdInfo) {
        mPeerData.put(nsdInfo.getUuid(), nsdInfo);
        mFailureData.put(nsdInfo.getUuid(), 0);
    }

    public void remove(NsdInfo nsdInfo) {
        mPeerData.remove(nsdInfo.getUuid());
        mFailureData.remove(nsdInfo.getUuid());
    }

    public void failed(NsdInfo nsdInfo) {
        if(contains(nsdInfo)) {
            int failures = mFailureData.get(nsdInfo.getUuid());
            mFailureData.put(nsdInfo.getUuid(), ++failures);
        }
    }

    public void reset(NsdInfo nsdInfo) {
        if(contains(nsdInfo)) {
            mFailureData.put(nsdInfo.getUuid(), 0);
        }
    }

    public void clear() {
        mFailureData.clear();
        mPeerData.clear();
    }

    public boolean contains(NsdInfo nsdInfo) {
        return mFailureData.containsKey(nsdInfo.getUuid()) && mPeerData.containsKey(nsdInfo.getUuid());
    }

    public int getFailures(NsdInfo nsdInfo) {
        return mFailureData.get(nsdInfo.getUuid());
    }

    public ArrayList<NsdInfo> getPeers() {
        return new ArrayList<>(mPeerData.values());
    }

}
