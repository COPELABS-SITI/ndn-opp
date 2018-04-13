package pt.ulusofona.copelabs.ndn.android.umobile.routing.dao;

import java.util.List;

import pt.ulusofona.copelabs.ndn.android.umobile.routing.exceptions.NeighborNotFoundException;
import pt.ulusofona.copelabs.ndn.android.umobile.routing.models.Plsa;

/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-03-07
 * This class constains the interface of LsdbDaoImpl class.
 * @author Omar Aponte(COPELABS/ULHT)
 */

public interface LsdbDao {
    void insertPlsa(Plsa plsa);
    void deletePlsa(Plsa plsa);
    void updatePlsa(Plsa plsa);
    boolean existsName(String name, String neighbor);
    List<Plsa> getAllEntries();
    Plsa getPlsa(String name, String neighbor) throws NeighborNotFoundException;
}
