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

public class SctEntry implements Table.Entry, Comparable<SctEntry> {
    public String prefix;
	public String strategy;

	public SctEntry(String p, String s) {
		prefix = p;
		strategy = s;
	}

    @Override
	public View getView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.item_cell_two, null, false);
	}

    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.left)).setText(this.prefix);
        ((TextView) entry.findViewById(R.id.right)).setText(this.strategy);
    }

    @Override
    public int compareTo(@NonNull SctEntry that) {
        return this.prefix.compareTo(that.prefix);
    }
}