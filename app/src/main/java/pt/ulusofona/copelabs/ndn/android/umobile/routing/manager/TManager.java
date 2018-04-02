package pt.ulusofona.copelabs.ndn.android.umobile.routing.manager;

/**
 * Created by miguel on 28-03-2018.
 */

public interface TManager {

    interface Manager {
        void start();
        void stop();
    }

    interface Listener {
        void onReceiveT(String sender, String name, double t);
    }
}
