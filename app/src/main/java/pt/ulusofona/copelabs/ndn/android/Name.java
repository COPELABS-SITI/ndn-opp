package pt.ulusofona.copelabs.ndn.android;

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
        TABLE_ARGUMENTS.putInt(Table.VIEW_TYPE_COUNT, 1);
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
    public int getItemViewType() {
        return 0;
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
