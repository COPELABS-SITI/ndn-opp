package pt.ulusofona.copelabs.ndn.android;

import android.os.Bundle;
import android.util.LongSparseArray;

import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Entry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class FibEntry implements Entry {
    public static final Bundle TABLE_ARGUMENTS = new Bundle();
    static {
        TABLE_ARGUMENTS.putInt(Table.TITLE, R.string.fib);
        TABLE_ARGUMENTS.putInt(Table.DEFAULT_VIEW, R.layout.item_cell_two);
        TABLE_ARGUMENTS.putInt(Table.VIEW_TYPE_COUNT, 1);
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

	private String nextHopList() {
		String nhl = "";
		for(int nh = 0; nh < nextHops.size(); nh++)
			nhl += nextHops.get(nextHops.keyAt(nh)) + (nh < nextHops.size()-1 ? "," : "");
		return nhl;
	}

    @Override
    public int getItemViewType() {
        return 0;
    }

	@Override
	public View getView(LayoutInflater inflater) {
        return inflater.inflate(R.layout.item_cell_two, null, false);
    }

    @Override
    public void setViewContents(View entry) {
        ((TextView) entry.findViewById(R.id.left)).setText(this.prefix);
        ((TextView) entry.findViewById(R.id.right)).setText(this.nextHopList());
    }
}