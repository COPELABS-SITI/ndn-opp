package pt.ulusofona.copelabs.ndn.android.utilities;

/*
 * jndn-management
 * Copyright (c) 2015-2016, Intel Corporation.
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms and conditions of the GNU Lesser General Public License,
 * version 3, as published by the Free Software Foundation.
 *
 * This program is distributed in the hope it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License for
 * more details.
 */

import com.intel.jndn.management.ManagementException;
import com.intel.jndn.management.enums.LocalControlHeader;
import com.intel.jndn.management.enums.RouteOrigin;
import com.intel.jndn.management.helpers.FetchHelper;
import com.intel.jndn.management.helpers.StatusDatasetHelper;
import com.intel.jndn.management.types.ChannelStatus;
import com.intel.jndn.management.types.FaceStatus;
import com.intel.jndn.management.types.FibEntry;
import com.intel.jndn.management.types.ForwarderStatus;
import com.intel.jndn.management.types.RibEntry;
import com.intel.jndn.management.types.StrategyChoice;
import net.named_data.jndn.ControlParameters;
import net.named_data.jndn.ControlResponse;
import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.ForwardingFlags;
import net.named_data.jndn.Interest;
import net.named_data.jndn.KeyLocator;
import net.named_data.jndn.Name;
import net.named_data.jndn.encoding.EncodingException;
import net.named_data.jndn.security.SecurityException;

import java.io.IOException;
import java.util.List;

/**
 * Helper class for interacting with an NDN forwarder daemon; see
 * <a href="http://redmine.named-data.net/projects/nfd/wiki/Management">NFD Management</a>
 * for explanations of the various protocols used.
 *
 * @author Andrew Brown <andrew.brown@intel.com>
 */
public class Nfdc {
    private static final int OK_STATUS = 200;

    /////////////////////////////////////////////////////////////////////////////

    /**
     * Prevent creation of Nfdc instances.
     */
    private Nfdc() {
    }

    /**
     * Retrieve the status of the given forwarder; calls /localhost/nfd/status/general
     * which requires a local Face (all non-local packets are dropped).
     *
     * @param face only a localhost Face
     * @return the forwarder status object
     * @throws ManagementException if the network request failed or the returned status could not be decoded
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/ForwarderStatus">ForwarderStatus</a>
     */
    public static ForwarderStatus getForwarderStatus(final Face face) throws ManagementException {
        try {
            List<Data> segments = FetchHelper.getSegmentedData(face, new Name("/localhost/nfd/status/general"));
            return new ForwarderStatus(StatusDatasetHelper.combine(segments));
        } catch (IOException | EncodingException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve a list of faces and their status from the given forwarder; calls
     * /localhost/nfd/faces/list which requires a local Face (all non-local
     * packets are dropped).
     *
     * @param face only a localhost Face
     * @return a list of face status objects
     * @throws ManagementException if the network request failed or if the NFD rejected the request
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/FaceMgmt">FaceManagement</a>
     */
    public static List<FaceStatus> getFaceList(final Face face) throws ManagementException {
        try {
            List<Data> segments = FetchHelper.getSegmentedData(face, new Name("/localhost/nfd/faces/list"));
            return StatusDatasetHelper.wireDecode(segments, FaceStatus.class);
        } catch (IOException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve a list of FIB entries and their NextHopRecords from the given
     * forwarder; calls /localhost/nfd/fib/list which requires a local Face (all
     * non-local packets are dropped).
     *
     * @param face only a localhost Face
     * @return a list of FIB entries
     * @throws ManagementException if the network request failed or if the NFD rejected the request
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/FibMgmt#FIB-Dataset">FIB Dataset</a>
     */
    public static List<FibEntry> getFibList(final Face face) throws ManagementException {
        try {
            List<Data> segments = FetchHelper.getSegmentedData(face, new Name("/localhost/nfd/fib/list"));
            return StatusDatasetHelper.wireDecode(segments, FibEntry.class);
        } catch (IOException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve a list of routing entries from the RIB; calls
     * /localhost/nfd/rib/list which requires a local Face (all non-local packets
     * are dropped).
     *
     * @param face only a localhost Face
     * @return a list of RIB entries, i.e. routes
     * @throws ManagementException if the network request failed or if the NFD rejected the request
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/RibMgmt#RIB-Dataset">RIB Dataset</a>
     */
    public static List<RibEntry> getRouteList(final Face face) throws ManagementException {
        try {
            List<Data> segments = FetchHelper.getSegmentedData(face, new Name("/localhost/nfd/rib/list"));
            return StatusDatasetHelper.wireDecode(segments, RibEntry.class);
        } catch (IOException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve the list of strategy choice entries from the NFD; calls
     * /localhost/nfd/rib/list which requires a local Face (all non-local packets
     * are dropped).
     *
     * @param face only a localhost Face
     * @return a list of strategy choice entries, i.e. routes
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/StrategyChoice">StrategyChoice</a>
     */
    public static List<StrategyChoice> getStrategyList(final Face face) throws ManagementException {
        try {
            List<Data> segments = FetchHelper.getSegmentedData(face, new Name("/localhost/nfd/strategy-choice/list"));
            return StatusDatasetHelper.wireDecode(segments, StrategyChoice.class);
        } catch (IOException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Retrieve the list of channel status entries from the NFD; calls
     * /localhost/nfd/faces/channels which requires a local Face (all non-local packets
     * are dropped).
     *
     * @param face only a localhost Face
     * @return a list of channel status entries
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/FaceMgmt#Channel-Dataset">Face Management</a>
     */
    public static List<ChannelStatus> getChannelStatusList(final Face face) throws ManagementException {
        try {
            List<Data> segments = FetchHelper.getSegmentedData(face, new Name("/localhost/nfd/faces/channels"));
            return StatusDatasetHelper.wireDecode(segments, ChannelStatus.class);
        } catch (IOException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }
    /**
     * Retrieve the {@link KeyLocator} for an NFD.
     *
     * @param face only a localhost {@link Face}
     * @return the {@link KeyLocator} of the NFD's key
     * @throws ManagementException if the network request failed, if the NFD rejected the request, or no
     *                             KeyLocator was found
     */
    public static KeyLocator getKeyLocator(final Face face) throws ManagementException {
        try {
            List<Data> segments = FetchHelper.getSegmentedData(face, new Name("/localhost/nfd/status/general"));
            if (segments.isEmpty() || !KeyLocator.canGetFromSignature(segments.get(0).getSignature())) {
                throw new ManagementException("No key locator available.");
            }
            return KeyLocator.getFromSignature(segments.get(0).getSignature());
        } catch (IOException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Create a new face on the given forwarder. Ensure the forwarding face is on
     * the local machine (management requests are to /localhost/...) and that
     * command signing has been set up (e.g. forwarder.setCommandSigningInfo()).
     *
     * @param face only a localhost {@link Face}
     * @param uri  a string like "tcp4://host.name.com" (see nfd-status channels
     *             for more protocol options)
     * @return the newly created face ID
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     */
    public static int createFace(final Face face, final String uri) throws ManagementException {
        Name command = new Name("/localhost/nfd/faces/create");
        ControlParameters parameters = new ControlParameters();
        parameters.setUri(uri);
        command.append(parameters.wireEncode());

        try {
            // send the interest
            ControlResponse response = sendCommand(face, command);

            // return
            return response.getBodyAsControlParameters().getFaceId();
        } catch (IOException | EncodingException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Destroy a face on given forwarder. Ensure the forwarding face is on the
     * local machine (management requests are to /localhost/...) and that command
     * signing has been set up (e.g. forwarder.setCommandSigningInfo()).
     *
     * @param face   only a localhost {@link Face}
     * @param faceId the ID of the face to destroy
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     */
    public static void destroyFace(final Face face, final int faceId) throws ManagementException {
        Name command = new Name("/localhost/nfd/faces/destroy");
        ControlParameters parameters = new ControlParameters();
        parameters.setFaceId(faceId);
        command.append(parameters.wireEncode());

        try {
            sendCommand(face, command);
        } catch (IOException | EncodingException | ManagementException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Enable a local control feature on the given forwarder.
     *
     * @param face   only a localhost {@link Face}
     * @param header the control feature to enable
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/FaceMgmt">Face Management</a>
     */
    public static void enableLocalControlHeader(final Face face, final LocalControlHeader header) throws
            ManagementException {
        // build command name
        Name command = new Name("/localhost/nfd/faces/enable-local-control");
        ControlParameters parameters = new ControlParameters();
        parameters.setLocalControlFeature(header.toInteger());
        command.append(parameters.wireEncode());

        try {
            sendCommand(face, command);
        } catch (IOException | EncodingException | ManagementException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Disable a local control feature on the given forwarder.
     *
     * @param face   only a localhost {@link Face}
     * @param header the control feature to disable
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/FaceMgmt">Face Management</a>
     */
    public static void disableLocalControlHeader(final Face face, final LocalControlHeader header) throws
            ManagementException {
        // build command name
        Name command = new Name("/localhost/nfd/faces/disable-local-control");
        ControlParameters parameters = new ControlParameters();
        parameters.setLocalControlFeature(header.toInteger());
        command.append(parameters.wireEncode());

        try {
            sendCommand(face, command);
        } catch (IOException | EncodingException | ManagementException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Register a route on the forwarder.
     * <p/>
     * Ensure the forwarding face is on the local machine (management requests are to /localhost/...) and that command
     * signing has been set up (e.g. forwarder.setCommandSigningInfo()).
     *
     * @param face              only a localhost {@link Face}
     * @param controlParameters the {@link ControlParameters} command options
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://named-data.net/doc/NFD/current/manpages/nfdc.html">nfdc</a>
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/RibMgmt">RIB Management</a>
     */
    public static void register(final Face face, final ControlParameters controlParameters) throws ManagementException {
        // build command name
        Name command = new Name("/localhost/nfd/rib/register");
        command.append(controlParameters.wireEncode());

        try {
            sendCommand(face, command);
        } catch (IOException | EncodingException | ManagementException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Register a route on a forwarder; this will create a new face on the
     * forwarder towards the face (e.g., self registration).
     *
     * @param face  only a localhost {@link Face}
     * @param route the {@link Name} prefix of the route
     * @param cost  the numeric cost of forwarding along the route
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     */
    public static void register(final Face face, final Name route, final int cost) throws ManagementException {
        ForwardingFlags flags = new ForwardingFlags();
        flags.setCapture(false);
        flags.setChildInherit(true);

        register(face, new ControlParameters()
                .setName(route)
                .setCost(cost)
                .setOrigin(RouteOrigin.APP.toInteger())
                .setForwardingFlags(flags));
    }

    /**
     * Register a route on a forwarder; this will create a new face on the
     * forwarder to the given URI/route pair. See register(Face,
     * ControlParameters) for more detailed documentation.
     *
     * @param face  only a localhost {@link Face}
     * @param uri   the URI (e.g. "tcp4://10.10.2.2:6363") of the remote node; note
     *              that this must be one of the canonical forms described in the wiki
     *              (http://redmine.named-data.net/projects/nfd/wiki/FaceMgmt#TCP) for NFD to
     *              accept the registration--otherwise you will see 400 errors
     * @param route the {@link Name} prefix of the route
     * @param cost  the numeric cost of forwarding along the route
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     */
    public static void register(final Face face, final String uri, final Name route, final int cost) throws
            ManagementException {
        // create the new face
        int faceId = createFace(face, uri);

        // run base method
        register(face, faceId, route, cost);
    }

    /**
     * Register a route on a forwarder; this will not create a new face since it
     * is provided a faceId. See register(Face, ControlParameters) for full
     * documentation.
     *
     * @param forwarder only a localhost {@link Face}
     * @param faceId    the ID of the {@link Face} to assign to the route
     * @param route     the {@link Name} prefix of the route
     * @param cost      the numeric cost of forwarding along the route
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     */
    public static void register(final Face forwarder, final int faceId, final Name route, final int cost) throws
            ManagementException {
        // build command name
        ControlParameters parameters = new ControlParameters();
        parameters.setName(route);
        parameters.setFaceId(faceId);
        parameters.setCost(cost);
        parameters.setOrigin(RouteOrigin.STATIC.toInteger());
        ForwardingFlags flags = new ForwardingFlags();
        flags.setCapture(false);
        flags.setChildInherit(true);
        parameters.setForwardingFlags(flags);

        // run base method
        register(forwarder, parameters);
    }

    /**
     * Unregister a route on a forwarder
     * <p/>
     * Ensure the forwarding face is on the local machine (management requests are to /localhost/...) and that command
     * signing has been set up (e.g. forwarder.setCommandSigningInfo().
     *
     * @param face              only a localhost {@link Face}
     * @param controlParameters the {@link ControlParameters} command options
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://named-data.net/doc/NFD/current/manpages/nfdc.html">nfdc</a>
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/RibMgmt">RIB Management</a>
     */
    public static void unregister(final Face face, final ControlParameters controlParameters) throws ManagementException {
        // build command name
        Name command = new Name("/localhost/nfd/rib/unregister");
        command.append(controlParameters.wireEncode());

        try {
            sendCommand(face, command);
        } catch (IOException | EncodingException | ManagementException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Unregister a route on a forwarder.
     * <p/>
     * Ensure the forwarding face is on the local machine (management requests are to /localhost/...) and that command
     * signing has been set up (e.g. forwarder.setCommandSigningInfo().
     *
     * @param face  only a localhost {@link Face}
     * @param route the {@link Name} prefix of the route
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://named-data.net/doc/NFD/current/manpages/nfdc.html">nfdc</a>
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/RibMgmt">RIB Management</a>
     */
    public static void unregister(final Face face, final Name route) throws ManagementException {
        // build command name
        ControlParameters controlParameters = new ControlParameters();
        controlParameters.setName(route);

        // send the interest
        unregister(face, controlParameters);
    }

    /**
     * Unregister a route on a forwarder; see
     * <p/>
     * Ensure the forwarding face is on the local machine (management requests are to /localhost/...) and that command
     * signing has been set up (e.g. forwarder.setCommandSigningInfo().
     *
     * @param face   only a localhost {@link Face}
     * @param route  the {@link Name} prefix of the route
     * @param faceId the specific ID of the face to remove (more than one face can
     *               be registered to a route)
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://named-data.net/doc/NFD/current/manpages/nfdc.html">nfdc</a>
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/RibMgmt">RIB Management</a>
     */
    public static void unregister(final Face face, final Name route, final int faceId) throws ManagementException {
        // build command name
        ControlParameters controlParameters = new ControlParameters();
        controlParameters.setName(route);
        controlParameters.setFaceId(faceId);

        // send the interest
        unregister(face, controlParameters);
    }

    /**
     * Unregister a route on a forwarder
     * <p/>
     * Ensure the forwarding face is on the local machine (management requests are to /localhost/...) and that command
     * signing has been set up using forwarder.setCommandSigningInfo().
     *
     * @param face  only a localhost {@link Face}
     * @param route the {@link Name} prefix of the route
     * @param uri   the URI (e.g. "tcp4://some.host.com") of the remote node (more
     *              than one face can be registered to a route)
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://named-data.net/doc/NFD/current/manpages/nfdc.html">nfdc command-line usage</a>
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/RibMgmt">RibMgmt</a>
     */
    public static void unregister(final Face face, final Name route, final String uri) throws ManagementException {
        int faceId = -1;
        for (FaceStatus faceStatus : getFaceList(face)) {
            if (faceStatus.getRemoteUri().matches(uri)) {
                faceId = faceStatus.getFaceId();
                break;
            }
        }

        if (faceId == -1) {
            throw new ManagementException("Face not found: " + uri);
        }

        // send the interest
        unregister(face, route, faceId);
    }

    /**
     * Set a strategy on the forwarder
     * <p/>
     * Ensure the forwarding face is on the local machine (management requests are to /localhost/...) and that command
     * signing has been set up using forwarder.setCommandSigningInfo().
     *
     * @param face     only a localhost {@link Face}
     * @param prefix   the {@link Name} prefix
     * @param strategy the {@link Name} of the strategy to set, e.g.
     *                 /localhost/nfd/strategy/broadcast
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     * @see <a href="http://named-data.net/doc/NFD/current/manpages/nfdc.html">nfdc command-line usage</a>
     * @see <a href="http://redmine.named-data.net/projects/nfd/wiki/StrategyChoice">StrategyChoice</a>
     */
    public static void setStrategy(final Face face, final Name prefix, final Name strategy) throws ManagementException {
        // build command name
        Name command = new Name("/localhost/nfd/strategy-choice/set");
        ControlParameters parameters = new ControlParameters();
        parameters.setName(prefix);
        parameters.setStrategy(strategy);
        command.append(parameters.wireEncode());

        try {
            sendCommand(face, command);
        } catch (IOException | EncodingException | ManagementException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Set a strategy on the forwarder; see
     * {@link #setStrategy(net.named_data.jndn.Face, net.named_data.jndn.Name, net.named_data.jndn.Name)}
     * for more information. Ensure the forwarding face is on the local machine
     * (management requests are to /localhost/...) and that command signing has
     * been set up (e.g. forwarder.setCommandSigningInfo()).
     *
     * @param face   only a localhost {@link Face}
     * @param prefix the {@link Name} prefix
     * @throws ManagementException if the network request failed, the NFD response could not be decoded, or
     *                             the NFD rejected the request
     */
    public static void unsetStrategy(final Face face, final Name prefix) throws ManagementException {
        // build command name
        Name command = new Name("/localhost/nfd/strategy-choice/unset");
        ControlParameters parameters = new ControlParameters();
        parameters.setName(prefix);
        command.append(parameters.wireEncode());

        try {
            sendCommand(face, command);
        } catch (IOException | EncodingException | ManagementException e) {
            throw new ManagementException(e.getMessage(), e);
        }
    }

    /**
     * Send an interest as a command to the forwarder; this method will convert
     * the interest to a command interest and block until a response is received
     * from the forwarder. Ensure the forwarding face is on the local machine
     * (management requests are to /localhost/...) and that command signing has
     * been set up (e.g. forwarder.setCommandSigningInfo()).
     *
     * @param face only a localhost Face, command signing info must be set
     * @param name As described at
     *             <a href="http://redmine.named-data.net/projects/nfd/wiki/ControlCommand">ControlCommand</a>,
     *             the requested interest must have encoded ControlParameters appended to the
     *             interest name
     * @return a {@link ControlResponse}
     * @throws IOException         if the network request failed
     * @throws EncodingException   if the NFD response could not be decoded
     * @throws ManagementException if the NFD rejected the request
     */
    private static ControlResponse sendCommand(final Face face, final Name name) throws IOException, EncodingException,
            ManagementException {
        if (face == null) {
            throw new IllegalArgumentException("Face parameter is null.");
        }

        Interest interest = new Interest(name);

        // forwarder must have command signing info set
        try {
            face.makeCommandInterest(interest);
        } catch (SecurityException e) {
            throw new IllegalArgumentException("Failed to make command interest; ensure command signing info is set on the " +
                    "face.", e);
        }

        // send command packet
        Data data = FetchHelper.getData(face, interest.getName());

        // decode response
        ControlResponse response = new ControlResponse();
        response.wireDecode(data.getContent().buf());

        // check response for success
        if (response.getStatusCode() != OK_STATUS) {
            throw ManagementException.fromResponse(response);
        }

        return response;
    }
}