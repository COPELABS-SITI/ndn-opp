/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-12-22
 * This interface implements the methods that could be use to manage
 * WifiFaceManagerImpl objects.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.multihoming;


import android.content.Context;

import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;

public interface WifiFaceManager {

    void enable(Context context, OpportunisticDaemon.Binder binder);
    void disable();
}
