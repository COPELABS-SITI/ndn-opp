/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Name entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */package pt.ulusofona.copelabs.ndn.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Entry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class Name implements Entry, Comparable<Name> {
    public static final Bundle TABLE_ARGUMENTS = new Bundle();
    static {
        TABLE_ARGUMENTS.putInt(Table.TITLE, R.string.nametree);
        TABLE_ARGUMENTS.putInt(Table.DEFAULT_VIEW, R.layout.item_name);
    }

	private String name;

	public Name(String n) {
		name = n;
	}

	@Override
	public int compareTo(Name that) {
		return this.name.compareTo(that.name);
	}

    @Override
	public View getView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.item_name, null, false);
	}

	@Override
	public void setViewContents(View entry) {
		((TextView) entry.findViewById(R.id.name)).setText(this.name);
	}
}
