package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Neighbor;

/**
 * Created by miguel on 13-03-2018.
 */

public interface NeighborTableManager {

    void start();
    void stop();
    Neighbor getNeighbor(String neighborUuid) throws NeighborNotFoundException;
}
