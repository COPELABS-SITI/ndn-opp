/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the ContentStore entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

/** The model class used to represent entries from the ContentStore within the Android app.
 *  Given that an entry from CS is a Name + Content, the item_cell_two layout is used.
 */
public class CsEntry implements Table.Entry, Comparable<CsEntry> {
    private String name;
	private String data;

    /**
     * Main constructor.
     * @param name the Name of this CsEntry
     * @param data a string representation of the Data packet associated to this' Name
     */
	public CsEntry(String name, String data) {
		this.name = name;
		this.data = data;
	}

    /** Constructs the View to use to display an instance of CsEntry.
     * @param inflater the system inflater to used for turning the layout file into objects.
     * @return the View to be used for displaying an instance of CsEntry.
     */
    @Override
	public View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_cell_two, null, false);
    }

    /** Initialize the fields of a View with the values stored in this CsEntry.
     * @param entry the View to use for displaying this CsEntry.
     */
    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.left)).setText(name);
        ((TextView) entry.findViewById(R.id.right)).setText(data);
    }

    /** Comparison of CsEntry based on their Name
     * @param that other CsEntry to compare this CsEntrywith
     * @return lexicographic distance between the two Names (based on String.compareTo)
     */
    @Override
    public int compareTo(@NonNull CsEntry that) {
        return this.name.compareTo(that.name);
    }
}