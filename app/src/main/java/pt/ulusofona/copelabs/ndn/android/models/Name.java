/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Name entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;

import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

/** The model class used to represent entries from the Name tree within the Android app.
 */
public class Name implements Table.Entry, Comparable<Name> {
	private String name;

	/** Main constructor.
	 * @param name Name associated to this entry.
	 */
	public Name(String name) {
		this.name = name;
	}

	/** Comparison of Name
	 * @param that other entry to compare this with
	 * @return lexicographic distance between the two Names (based on String.compareTo)
	 */
	@Override
	public int compareTo(@NonNull Name that) {
		return this.name.compareTo(that.name);
	}

	/** Constructs the View to use to display an instance of Name
	 * @param inflater the system inflater to used for turning the layout file into objects.
	 * @return the View to be used for displaying an instance of Name.
	 */
    @Override
	public View getView(LayoutInflater inflater) {
		return inflater.inflate(R.layout.item_name, null, false);
	}

	/** Initialize the fields of a View with the values stored in this Name entry.
	 * @param entry the View to use for displaying this Name.
	 */
	@Override
	public void setViewContents(View entry) {
		((TextView) entry.findViewById(R.id.name)).setText(this.name);
	}
}
