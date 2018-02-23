package pt.ulusofona.copelabs.ndn.android.umobile.common;

import java.util.concurrent.ConcurrentHashMap;

import pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.channels.OpportunisticChannelOut;

/**
 * Created by miguel on 23-02-2018.
 */

public class CommunicationManager {

    // Associates a OpportunisticChannel to a UUID
    private ConcurrentHashMap<String, OpportunisticChannelOut> mOppOutChannels = new ConcurrentHashMap<>();

}
