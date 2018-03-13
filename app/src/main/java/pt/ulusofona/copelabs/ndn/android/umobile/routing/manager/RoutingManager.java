package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;



import android.content.Context;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;

/**
 * Created by miguel on 07-03-2018.
 */

public interface RoutingManager {

    void start(OpportunisticDaemon.Binder binder, Context context);
    void stop();
}
