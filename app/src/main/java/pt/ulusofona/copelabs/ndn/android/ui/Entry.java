/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The Interface for all Entries on which a Table can be defined.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui;

import android.view.LayoutInflater;
import android.view.View;

public interface Entry {
    View getView(LayoutInflater infl);
    void setViewContents(View entry);
}
