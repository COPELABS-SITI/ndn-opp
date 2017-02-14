/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Forwarding Information Base entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android;

import android.os.Bundle;
import android.util.LongSparseArray;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Entry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class FibEntry implements Entry, Comparable<FibEntry> {
    public static final Bundle TABLE_ARGUMENTS = new Bundle();
    static {
        TABLE_ARGUMENTS.putInt(Table.TITLE, R.string.fib);
        TABLE_ARGUMENTS.putInt(Table.DEFAULT_VIEW, R.layout.item_cell_two);
    }

	private String prefix;
	private LongSparseArray<Integer> nextHops;

	public FibEntry(String p) {
		prefix = p;
		nextHops = new LongSparseArray<>();
	}

	public void addNextHop(long nh, int cost) {
		nextHops.put(nh, Integer.valueOf(cost));
	}

    private String nhString() {
        String res = "";
        for(int k = 0; k < nextHops.size(); k++) {
            long key = nextHops.keyAt(k);
            res += Long.toString(key) + ":" + nextHops.get(key);
            if(k < nextHops.size() - 1)
                res += ",";
        }
        return res;
    }

    @Override
	public View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_cell_two, null, false);
    }

    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.left)).setText(prefix);
        ((TextView) entry.findViewById(R.id.right)).setText(nhString());
    }

    @Override
    public int compareTo(FibEntry that) {
        return this.prefix.compareTo(that.prefix);
    }
}