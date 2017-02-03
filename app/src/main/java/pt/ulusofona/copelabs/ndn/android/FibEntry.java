package pt.ulusofona.copelabs.ndn.android;

import android.app.Activity;

import android.util.LongSparseArray;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.R;

public class FibEntry {
	public String prefix;
	public LongSparseArray<Integer> nextHops;

	public FibEntry(String p) {
		prefix = p;
		nextHops = new LongSparseArray<Integer>();
	}

	public void addNextHop(long nh, int cost) {
		nextHops.put(nh, Integer.valueOf(cost));
	}

	public String nextHopList() {
		String nhl = "";
		for(int nh = 0; nh < nextHops.size(); nh++)
			nhl += nextHops.get(nextHops.keyAt(nh)) + (nh < nextHops.size()-1 ? "," : "");
		return nhl;
	}

	public static class Adapter extends ArrayAdapter<FibEntry> {
		LayoutInflater inflater;
	
		public Adapter(Activity act) {
			super(act, R.layout.item_cell_two, new ArrayList<FibEntry>());
			inflater = act.getLayoutInflater();
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View entry;

			if(convertView != null) entry = convertView;
			else entry = inflater.inflate(R.layout.item_cell_two, parent, false);
	
			FibEntry current = getItem(position);
	
			((TextView) entry.findViewById(R.id.left)).setText(current.prefix);
			((TextView) entry.findViewById(R.id.right)).setText(current.nextHopList());

			return entry;
		}
	}
}