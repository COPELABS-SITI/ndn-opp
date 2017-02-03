package pt.ulusofona.copelabs.ndn.android;

import android.app.Activity;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

import pt.ulusofona.copelabs.ndn.R;

public class Name implements Comparable<Name> {
	private String name;

	public Name(String n) {
		name = n;
	}

	@Override
	public int compareTo(Name that) {
		return this.name.compareTo(that.name);
	}

	public static class Adapter extends ArrayAdapter<Name> {
		LayoutInflater inflater;

		public Adapter(Activity act) {
			super(act, R.layout.item_name, new ArrayList<Name>());
			inflater = act.getLayoutInflater();
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			View name;

			if(convertView != null) name = convertView;
			else name = inflater.inflate(R.layout.item_name, parent, false);

			Name current = getItem(position);

			((TextView) name.findViewById(R.id.name)).setText(current.name);

			return name;
		}
	}
}
