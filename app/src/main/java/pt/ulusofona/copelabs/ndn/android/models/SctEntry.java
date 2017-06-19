/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Strategy Choice Table entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

/** The model class used to represent entries from the StrategyChoiceTable within the Android app.
 *  A SctEntry associates a forwarding strategy (identified by a string) to a certain Name prefix.
 */
public class SctEntry implements Table.Entry, Comparable<SctEntry> {
    private String prefix;
	private String strategy;

    /**
     * Main constructor.
     * @param prefix the Name prefix associated with this entry
     * @param strategy the name of the strategy associated with this entry
     */
	public SctEntry(String prefix, String strategy) {
		this.prefix = prefix;
		this.strategy = strategy;
	}

    /** Constructs the View to use to display an instance of SctEntry.
     * @param inflater the system inflater to used for turning the layout file into objects.
     * @return the View to be used for displaying an instance of SctEntry.
     */
    @Override
	public View getView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.item_cell_two, null, false);
	}

    /** Initialize the fields of a View with the values stored in this SctEntry.
     * @param entry the View to use for displaying this SctEntry.
     */
    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.left)).setText(this.prefix);
        ((TextView) entry.findViewById(R.id.right)).setText(this.strategy);
    }

    /** Comparison of SctEntries based on their Name prefix
     * @param that other entry to compare this with
     * @return lexicographic distance between the two prefix Names (based on String.compareTo)
     */
    @Override
    public int compareTo(@NonNull SctEntry that) {
        return this.prefix.compareTo(that.prefix);
    }
}