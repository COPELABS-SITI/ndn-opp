/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-13
 * This interface sets all the methods used by NeighborTableManager.
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Neighbor;


public interface NeighborTableManager {

    void start();
    void stop();
    Neighbor getNeighbor(String neighborUuid) throws NeighborNotFoundException;
}
