/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Implementation of the Strategy Choice Table entry class.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 * @author Miguel Tavares (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.models;

import android.support.annotation.NonNull;

/** The model class used to represent entries from the StrategyChoiceTable within the Android app.
 *  A SctEntry associates a forwarding mStrategy (identified by a string) to a Name mPrefix.
 */
public class SctEntry implements Comparable<SctEntry> {

    /** Associates a prefix to the strategy entry */
    private String mPrefix;

    /** Associates a strategy to the strategy entry */
	private String mStrategy;

    /** Main constructor. Refer to NFD Developer's Guide Section 3.6. Strategy Choice Table (p. 25) for details about the meaning of the fields
     * @param prefix the Name mPrefix associated with this entry
     * @param strategy the name of the mStrategy associated with this entry
     */
	public SctEntry(String prefix, String strategy) {
		mPrefix = prefix;
		mStrategy = strategy;
	}

    /**
     * Getter for prefix attribute
     * @return prefix
     */
	public String getPrefix() {
        return mPrefix;
    }

    /**
     * Getter for strategy attribute
     * @return strategy
     */
    public String getStrategy() {
        return mStrategy;
    }

    /** Comparison of SctEntries based on their Name mPrefix
     * @param that other entry to compare this with
     * @return lexicographic distance between the two mPrefix Names (based on String.compareTo)
     */
    @Override
    public int compareTo(@NonNull SctEntry that) {
        return this.mPrefix.compareTo(that.mPrefix);
    }

    @Override
    public boolean equals(Object obj) {
        SctEntry that = (SctEntry) obj;
        return mPrefix.equals(that.mPrefix) && mStrategy.equals(that.mStrategy);
    }
}