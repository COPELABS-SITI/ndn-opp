/**
 *  @version 1.1
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the ContentStore entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 * @author Miguel Tavares (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;

/** The model class used to represent entries from the ContentStore within the Android app.
 *  Given that an entry from CS is a Name + Content, a layout with two cells is used.
 */
public class CsEntry implements Comparable<CsEntry> {

    /** Attribute to store entry's name */
    private String name;

    /** Attribute to store entry's data */
	private String data;

    /** Main constructor.
     * @param name the NDN Name associated to this entry
     * @param data a string encoding the WifiP2pCache packet associated to the Name of this entry
     */
	public CsEntry(String name, String data) {
		this.name = name;
		this.data = data;
	}

    /**
     * Getter for attribute name
     * @return name
     */
	public String getName() {
        return name;
    }

    /**
     * Getter for attribute data
     * @return data
     */
    public String getData() {
        return data;
    }

    /** Comparison of CsEntry based on their Name
     * @param that other CsEntry to compare this CsEntrywith
     * @return lexicographic distance between the two Names (based on String.compareTo)
     */
    @Override
    public int compareTo(@NonNull CsEntry that) {
        return this.name.compareTo(that.name);
    }

    @Override
    public boolean equals(Object obj) {
        CsEntry that = (CsEntry) obj;
        return name.equals(that.name) && data.equals(that.data);
    }
}