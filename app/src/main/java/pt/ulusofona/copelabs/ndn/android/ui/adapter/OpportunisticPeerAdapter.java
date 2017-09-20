/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The OpportunisticPeerAdapter is used to populate a ListView
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui.adapter;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.OpportunisticPeer;

/** Adapter class for displaying a list of OpportunisticPeer in a View.
 * cfr. https://developer.android.com/reference/android/widget/Adapter.html
 */
public class OpportunisticPeerAdapter extends ArrayAdapter<OpportunisticPeer> {
    private LayoutInflater mInflater;

    /** Main constructor.
     * @param context Android context within which the Adapter should be created
     */
    public OpportunisticPeerAdapter(Context context) {
        super(context, R.layout.item_ndn_opp_peer);
        mInflater = LayoutInflater.from(context);
    }

    /** Used by Android to retrieve the View corresponding to a certain item in the list of WifiP2pPeers.
     * @param position position of the WifiP2pPeer for which the View is requested
     * @param convertView available View that can be recycled by filling it with the WifiP2pPeer details
     * @param parent parent View in the hierarchy
     * @return the View to be used
     */
    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View entry;
        if (convertView != null)
            entry = convertView;
        else
            entry = mInflater.inflate(R.layout.item_ndn_opp_peer, null, false);

        OpportunisticPeer peer = getItem(position);

        TextView textDeviceStatus = (TextView) entry.findViewById(R.id.text_peer_status);
        TextView textDeviceUuid = (TextView) entry.findViewById(R.id.text_peer_uuid);

        if(peer != null) {
            textDeviceStatus.setText(peer.getStatus().getSymbol());
            textDeviceUuid.setText(peer.getUuid());
        } else {
            textDeviceStatus.setText(R.string.missingStatus);
            textDeviceUuid.setText(R.string.missingUuid);
        }

        return entry;
    }
}
