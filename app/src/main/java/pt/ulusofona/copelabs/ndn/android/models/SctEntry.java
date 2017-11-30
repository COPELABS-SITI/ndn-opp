/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Strategy Choice Table entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;

/** The model class used to represent entries from the StrategyChoiceTable within the Android app.
 *  A SctEntry associates a forwarding strategy (identified by a string) to a Name prefix.
 */
public class SctEntry implements Comparable<SctEntry> {
    private String prefix;
	private String strategy;

    /** Main constructor. Refer to NFD Developer's Guide Section 3.6. Strategy Choice Table (p. 25) for details about the meaning of the fields
     * @param prefix the Name prefix associated with this entry
     * @param strategy the name of the strategy associated with this entry
     */
	public SctEntry(String prefix, String strategy) {
		this.prefix = prefix;
		this.strategy = strategy;
	}

	public String getPrefix() {
        return prefix;
    }

    public String getStrategy() {
        return strategy;
    }

    /** Comparison of SctEntries based on their Name prefix
     * @param that other entry to compare this with
     * @return lexicographic distance between the two prefix Names (based on String.compareTo)
     */
    @Override
    public int compareTo(@NonNull SctEntry that) {
        return this.prefix.compareTo(that.prefix);
    }

    @Override
    public boolean equals(Object obj) {
        SctEntry that = (SctEntry) obj;
        return prefix.equals(that.prefix) && strategy.equals(that.strategy);
    }
}