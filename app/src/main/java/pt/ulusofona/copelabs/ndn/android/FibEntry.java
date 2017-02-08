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

    @Override
	public View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_cell_two, null, false);
    }

    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.left)).setText(prefix);
        ((TextView) entry.findViewById(R.id.right)).setText(nextHops.toString());
    }

    @Override
    public int compareTo(FibEntry that) {
        return this.prefix.compareTo(that.prefix);
    }
}