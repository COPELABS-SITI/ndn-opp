/**
 * @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-04-02
 * This interface sets all the methods used by TManager
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;


public interface TManager {

    interface Manager {
        void start();
        void stop();
        void notify(String sender, String name, double t);
    }

    interface Listener {
        void onReceiveT(String sender, String name, double t);
    }
}
