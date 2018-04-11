package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;

/**
 * Created by copelabs on 09/04/2018.
 */

public interface Lsdb {
    void insertPlsa(Plsa plsa);
    void deletePlsa(Plsa plsa);
    void updatePlsa(Plsa plsa);
    boolean existsName(String name, String neighbor);
    Plsa getPlsa(String name, String neighbor) throws NeighborNotFoundException;
}
