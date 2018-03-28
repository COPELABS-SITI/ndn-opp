package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;

/**
 * Created by miguel on 13-03-2018.
 */

public interface RibUpdater {

    void start();
    void stop();
    void updateRoutingTable(String name, String neighbor, long cost);
}
