/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-13
 * This interface sets all the methods used by RibUpdater
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.tasks;


public interface RibUpdater {

    void start();
    void stop();
    void updateRoutingEntry(String name, String neighbor, long cost);
}
