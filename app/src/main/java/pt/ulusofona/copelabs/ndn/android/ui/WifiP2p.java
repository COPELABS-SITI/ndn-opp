/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * This class manages the Fragment which displays the UMobile Peers and the Group to which this
 * device is connected.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.Peer;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class WifiP2p extends Fragment {
    private Table<Peer> mPeers;
    private Table<Face> mFaces;
    private Table<Peer> mGroup;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
            mPeers = new Table<>();
            mPeers.setArguments(Peer.TABLE_ARGUMENTS);

            mFaces = new Table<>();
            mFaces.setArguments(Face.TABLE_ARGUMENTS);

            mGroup = new Table<>();
            Bundle args = (Bundle) Peer.TABLE_ARGUMENTS.clone();
            args.putInt(Table.TITLE, R.string.group);
            mGroup.setArguments(args);
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View contentStore = inflater.inflate(R.layout.fragment_wifi_p2p, parent, false);

		getChildFragmentManager()
            .beginTransaction()
            .replace(R.id.peers, mPeers)
            .replace(R.id.faces, mFaces)
            .replace(R.id.group, mGroup)
			.commit();

		return contentStore;
	}

	public void clear() {
        if(mPeers != null) mPeers.clear();
        if(mFaces != null) mFaces.clear();
	}

	public void refresh(List<Peer> peers, List<Face> faces) {
        mPeers.refresh(peers);

        // Filter out non-opportunistic faces from the input list.
        List<Face> oppFaces = new ArrayList<>();
        for(Face current : faces)
            if(current.getRemoteURI().startsWith(getString(R.string.opp)))
                oppFaces.add(current);
        mFaces.refresh(oppFaces);
	}
}