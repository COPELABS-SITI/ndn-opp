package pt.ulusofona.copelabs.ndn.android.umobile.routing.models;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;

/**
 * Created by miguel on 21-03-2018.
 */

public class NeighborTable {

    private List<Neighbor> mNeighbors = new ArrayList<>();

    public void addNeighborIfDoesntExist(Neighbor neighbor) {
        if(!mNeighbors.contains(neighbor)) {
            mNeighbors.add(neighbor);
        }
    }

    public Neighbor getNeighbor(String neighborUuid) throws NeighborNotFoundException {
        for(Neighbor neighbor : mNeighbors) {
            if(neighbor.getUuid().equals(neighborUuid)) {
                return neighbor;
            }
        }
        throw new NeighborNotFoundException();
    }

    public List<Neighbor> getNeighbors() {
        return mNeighbors;
    }

    public void clear() {
        mNeighbors.clear();
    }

}
