package pt.ulusofona.copelabs.ndn.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Entry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class SctEntry implements Entry, Comparable<SctEntry> {
    public static final Bundle TABLE_ARGUMENTS = new Bundle();
    static {
        TABLE_ARGUMENTS.putInt(Table.TITLE, R.string.sct);
        TABLE_ARGUMENTS.putInt(Table.DEFAULT_VIEW, R.layout.item_cell_two);
    }

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
    public int compareTo(SctEntry that) {
        return this.prefix.compareTo(that.prefix);
    }
}