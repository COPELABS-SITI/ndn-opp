/**
 * @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Name entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;

/** The model class used to represent entries from the Name tree within the Android app.
 * Uses a single cell layout for displaying.
 */
public class Name implements Comparable<Name> {

	/** Attribute mName */
	private String mName;

	/** Main constructor.
	 * @param name Name associated to this entry.
	 */
	public Name(String name) {
		this.mName = name;
	}

	/**
	 * Getter for attribute name
	 * @return name
	 */
	public String getName() {
		return mName;
	}

	/** Comparison of Name
	 * @param that other entry to compare this with
	 * @return lexicographic distance between the two Names (based on String.compareTo)
	 */
	@Override
	public int compareTo(@NonNull Name that) {
		return this.mName.compareTo(that.mName);
	}

	@Override
	public boolean equals(Object obj) {
		Name that = (Name) obj;
		return mName.equals(that.mName);
	}

}
