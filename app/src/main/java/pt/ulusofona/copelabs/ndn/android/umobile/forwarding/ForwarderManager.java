package pt.ulusofona.copelabs.ndn.android.umobile.forwarding;

import android.util.Log;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.models.Face;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;

public class ForwarderManager {

    private static final String TAG = ForwarderManager.class.getSimpleName();
    private OpportunisticDaemon.Binder mDaemonBinder;

    public ForwarderManager(OpportunisticDaemon.Binder daemonBinder) {
        mDaemonBinder = daemonBinder;
    }

    public ArrayList<Long> getDestinationFace(long targetFaceId) {
        Log.i(TAG, "Ask for a packet to deliver on face " + targetFaceId);
        List<Face> faces = mDaemonBinder.getFaceTable();
        return isFaceUp(faces, targetFaceId) ? new ArrayList<>(Arrays.asList(targetFaceId)) : getAllFacesIds(faces);
    }

    private boolean isFaceUp(List<Face> faces, long targetFaceId) {
        for(Face face : faces) {
            if(face.getFaceId() == targetFaceId) {
                Log.i(TAG, "Target face found!");
                Log.i(TAG, "Target face status is " + face.state());
                return face.isFaceUp();
            }
        }
        Log.i(TAG, "Target face not found!");
        return false;
    }

    private ArrayList<Long> getAllFacesIds(List<Face> faces) {
        ArrayList<Long> targetFaces = new ArrayList<>();
        for(Face face : faces)
            targetFaces.add(face.getFaceId());
        Log.i(TAG, "Since the face is down, let's flood!");
        Log.i(TAG, "Target faces " + targetFaces);
        return targetFaces;
    }

}
