/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * This interface is the definition of the methods that a Refreshable fragments must implement.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.support.annotation.NonNull;

import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public interface Refreshable {
    int getTitle();
    void refresh(@NonNull ForwardingDaemon daemon);
}
