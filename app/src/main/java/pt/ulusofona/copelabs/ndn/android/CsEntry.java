package pt.ulusofona.copelabs.ndn.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Entry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class CsEntry implements Entry {
    public static final Bundle TABLE_ARGUMENTS = new Bundle();
    static {
        TABLE_ARGUMENTS.putInt(Table.TITLE, R.string.contentstore);
        TABLE_ARGUMENTS.putInt(Table.DEFAULT_VIEW, R.layout.item_cell_two);
        TABLE_ARGUMENTS.putInt(Table.VIEW_TYPE_COUNT, 1);
    }

    public String name;
	public String data;

	public CsEntry(String n, String d) {
		name = n;
		data = d;
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
        ((TextView) entry.findViewById(R.id.left)).setText(name);
        ((TextView) entry.findViewById(R.id.right)).setText(data);
    }
}