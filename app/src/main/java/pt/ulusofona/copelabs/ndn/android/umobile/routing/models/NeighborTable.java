/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-13
 * This class implements a delegation design pattern
 * in order to create an abstraction of NeighborTable
 * @author Miguel Tavares (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;


import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;


public class NeighborTable {

    /** This attribute is used to store neighbors */
    private List<Neighbor> mNeighbors = new ArrayList<>();


    /**
     * This method adds a neighbor if it doesn't exists
     * @param neighbor
     */
    public void addNeighborIfDoesntExist(Neighbor neighbor) {
        if(!mNeighbors.contains(neighbor)) {
            mNeighbors.add(neighbor);
        }
    }

    /**
     * This method returns a neighbor if exists in the neighbor table
     * @param neighborIdentifier neighbor uuid or cmIdentifier to search
     * @return Neighbor found
     * @throws NeighborNotFoundException
     */
    public Neighbor getNeighbor(String neighborIdentifier) throws NeighborNotFoundException {
        for(Neighbor neighbor : mNeighbors) {
            if(neighbor.getUuid().equals(neighborIdentifier) || neighbor.getCmIdentifier().equals(neighborIdentifier)) {
                return neighbor;
            }
        }
        throw new NeighborNotFoundException(neighborIdentifier);
    }

    /**
     * This method returns a list containing all cmIdentifiers
     * @return a list containing all cmIdentifiers
     */
    public List<String> getAllCmIdentifiers() {
        List<String> cmIdentifiers = new ArrayList<>();
        for(Neighbor neighbor : mNeighbors) {
            cmIdentifiers.add(neighbor.getCmIdentifier());
        }
        return cmIdentifiers;
    }

    /**
     * This method return all neighbors in the neighbor table
     * @return all neighbors
     */
    public List<Neighbor> getNeighbors() {
        return mNeighbors;
    }

    /**
     * This method delete all neighbors in the neighbor table
     */
    public void clear() {
        mNeighbors.clear();
    }

}
