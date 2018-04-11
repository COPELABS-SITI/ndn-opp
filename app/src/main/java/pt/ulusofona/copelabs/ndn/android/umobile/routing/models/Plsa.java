package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;

import java.io.Serializable;
import java.util.List;

/**
 * Created by copelabs on 14/03/2018.
 */

public class Plsa implements Serializable {

    /** Used for debug.*/
    private String TAG = Plsa.class.getSimpleName();

    /** Name of prefix registered by external applications. */
    private String mName;

    /** Cost of the neighbor. */
    private long mCost;

    /** Neighbor identifier. */
    private String mNeighbor;

    private List mList;

    /**
     * Constructor of the Plsa class.
     * @param name Name of prefix registered by external applications.
     * @param cost Cost of the neighbor.
     * @param neighbor Neighbor identifier.
     */
    public Plsa(String name, long cost, String neighbor) {
        mName = name;
        mCost = cost;
        mNeighbor = neighbor;
    }

    /**
     * Get name registered.
     * @return String with the name registered.
     */
    public String getName(){
        return mName;
    }

    /**
     * Get cost of the neighbor.
     * @return Long with the cost of neighbor.
     */
    public long getCost(){
        return mCost;
    }

    /**
     * Get neighbor identifier.
     * @return String with the identifier of the neighbor.
     */
    public String getNeighbor(){
        return mNeighbor;
    }
}
