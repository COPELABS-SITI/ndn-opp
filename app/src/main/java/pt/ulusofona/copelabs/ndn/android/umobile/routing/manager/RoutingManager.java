/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This interface sets all the methods used by RoutingManager
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;



import android.content.Context;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;


public interface RoutingManager {

    void start(OpportunisticDaemon.Binder binder, Context context);
    void stop();
}
