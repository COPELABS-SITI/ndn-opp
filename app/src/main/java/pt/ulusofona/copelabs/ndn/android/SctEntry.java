package pt.ulusofona.copelabs.ndn.android;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.R;

public class SctEntry {
	public String prefix;
	public String strategy;

	public SctEntry(String p, String s) {
		prefix = p;
		strategy = s;
	}

	public static class Adapter extends ArrayAdapter<SctEntry> {
		LayoutInflater inflater;

		public Adapter(Activity act) {
			super(act, R.layout.item_cell_two, new ArrayList<SctEntry>());
			inflater = act.getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View entry;

			if(convertView != null) entry = convertView;
			else entry = inflater.inflate(R.layout.item_cell_two, parent, false);

			SctEntry current = getItem(position);

			((TextView) entry.findViewById(R.id.left)).setText(current.prefix);
			((TextView) entry.findViewById(R.id.right)).setText(current.strategy);

			return entry;
		}
	}
}