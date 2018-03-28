package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.RoutingEntry;

/**
 * Created by miguel on 20-03-2018.
 */

public interface RoutingEntryDao {

    List<RoutingEntry> getAllEntries();
    void createRoutingEntry(RoutingEntry routingEntry);
    void updateRoutingEntry(RoutingEntry routingEntry);
    void deleteRoutingEntry(RoutingEntry routingEntry);
    boolean isRoutingEntryExists(RoutingEntry routingEntry);
    RoutingEntry getRoutingEntry(String prefix, long faceid);

}
