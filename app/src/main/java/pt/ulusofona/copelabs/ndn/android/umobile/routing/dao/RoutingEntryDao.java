/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-20
 * This interface contains all methods related with RoutingEntry table
 * that are used to communicate with db
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.RoutingEntry;

public interface RoutingEntryDao {

    List<RoutingEntry> getAllEntries();
    void createRoutingEntry(RoutingEntry routingEntry);
    void updateRoutingEntry(RoutingEntry routingEntry);
    void deleteRoutingEntry(RoutingEntry routingEntry);
    boolean isRoutingEntryExists(RoutingEntry routingEntry) throws NeighborNotFoundException;
    RoutingEntry getRoutingEntry(String prefix, long faceid) throws NeighborNotFoundException;

}
