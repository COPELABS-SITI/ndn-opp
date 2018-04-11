// IRemoteContextualManager.aidl
package com.senception.contextualmanager.aidl;

// Declare any non-default types here with import statements

interface CManagerInterface {

        Map getAvailability(in List<String> cmIdentifiers);
        Map getCentrality(in List<String> cmIdentifiers);
        Map getSimilarity(in List<String> cmIdentifiers);

}
