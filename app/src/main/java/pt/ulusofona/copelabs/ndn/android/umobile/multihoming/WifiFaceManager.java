package pt.ulusofona.copelabs.ndn.android.umobile.multihoming;


import android.content.Context;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;

public interface WifiFaceManager {

    void enable(Context context, OpportunisticDaemon.Binder binder);
    void disable();
}
