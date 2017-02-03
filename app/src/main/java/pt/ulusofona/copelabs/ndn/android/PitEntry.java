package pt.ulusofona.copelabs.ndn.android;


import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.R;

public class PitEntry {
	public String name;
	public List<Long> inFaces;
	public List<Long> outFaces;

	public PitEntry(String n) {
		name = n;
		inFaces = new ArrayList<Long>();
		outFaces = new ArrayList<Long>();
	}

	public void addInRecord(long fi) {
		inFaces.add(fi);
	}

	public void addOutRecord(long fi) {
		outFaces.add(fi);
	}

	private String pretty(List<Long> faceIds) {
		String str = "";
		for(int id = 0; id < faceIds.size(); id++)
			str += faceIds.get(id) + (id < faceIds.size()-1 ? "," : "");
		return str;
	}

	public String inFaces() {
		return pretty(inFaces);
	}

	public String outFaces() {
		return pretty(outFaces);
	}

	public static class Adapter extends ArrayAdapter<PitEntry> {
		LayoutInflater inflater;

		public Adapter(Activity act) {
			super(act, R.layout.item_pit_entry, new ArrayList<PitEntry>());
			inflater = act.getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View entry;

			if(convertView != null) entry = convertView;
			else entry = inflater.inflate(R.layout.item_pit_entry, parent, false);

			PitEntry current = getItem(position);

			((TextView) entry.findViewById(R.id.name)).setText(current.name);
			((TextView) entry.findViewById(R.id.inFaces)).setText(current.inFaces());
			((TextView) entry.findViewById(R.id.outFaces)).setText(current.outFaces());

			return entry;
		}
	}
}