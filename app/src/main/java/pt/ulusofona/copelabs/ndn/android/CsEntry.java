package pt.ulusofona.copelabs.ndn.android;

import java.util.ArrayList;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;

public class CsEntry {
	public String name;
	public String data;

	public CsEntry(String n, String d) {
		name = n;
		data = d;
	}

	public static class Adapter extends ArrayAdapter<CsEntry> {
		LayoutInflater inflater;
	
		public Adapter(Activity act) {
			super(act, R.layout.item_cell_two, new ArrayList<CsEntry>());
			inflater = act.getLayoutInflater();
		}
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View entry;

			if(convertView != null) entry = convertView;
			else entry = inflater.inflate(R.layout.item_cell_two, parent, false);
	
			CsEntry current = getItem(position);
	
			((TextView) entry.findViewById(R.id.left)).setText(current.name);
			((TextView) entry.findViewById(R.id.right)).setText(current.data);

			return entry;
		}
	}
}