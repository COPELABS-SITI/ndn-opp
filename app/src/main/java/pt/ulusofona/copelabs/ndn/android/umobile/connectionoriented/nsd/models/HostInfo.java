/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2018-01-31
 * This class is a model to store data related with hosts
 * @author Miguel Tavares (COPELABS/ULHT)
 */


package pt.ulusofona.copelabs.ndn.android.umobile.connectionoriented.nsd.models;


import java.io.Serializable;

public class HostInfo implements Serializable {

    /** This variable holds the IP address */
    protected String mIpAddress;

    /** This variable holds the port */
    protected int mPort;

    public HostInfo(String ipAddress, int port) {
        mIpAddress = ipAddress;
        mPort = port;
    }

    /**
     * This method is a getter for mIpAddress attribute
     * @return mIpAddress
     */
    public String getIpAddress() {
        return mIpAddress;
    }

    /**
     * This method is a getter for mPort attribute
     * @return mPort
     */
    public int getPort() {
        return mPort;
    }

    @Override
    public String toString() {
        return "host: " + mIpAddress + ":" + mPort;
    }

}
