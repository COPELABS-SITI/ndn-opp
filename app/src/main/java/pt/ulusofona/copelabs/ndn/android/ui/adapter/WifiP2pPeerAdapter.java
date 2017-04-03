/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * The WifiP2pPeerAdapter is used to populate a ListView
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
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.WifiP2pPeer;

public class WifiP2pPeerAdapter extends ArrayAdapter<WifiP2pPeer> {
    private LayoutInflater mInflater;

    public WifiP2pPeerAdapter(Context context) {
        super(context, R.layout.item_wifi_p2p_peer);
        mInflater = LayoutInflater.from(context);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        View entry;
        if (convertView != null)
            entry = convertView;
        else
            entry = mInflater.inflate(R.layout.item_wifi_p2p_peer, null, false);

        WifiP2pPeer peer = getItem(position);

        TextView textDeviceStatus = (TextView) entry.findViewById(R.id.text_peer_status);
        TextView textDeviceGroup = (TextView) entry.findViewById(R.id.text_peer_group);
        TextView textDeviceUuid = (TextView) entry.findViewById(R.id.text_peer_uuid);
        TextView textDeviceMacAddress = (TextView) entry.findViewById(R.id.text_peer_mac_address);

        if(peer != null) {
            textDeviceStatus.setText(peer.getStatus().getSymbol());

            if(peer.isGroupOwner())
                textDeviceGroup.setText(R.string.groupOwner);
            else if(peer.hasGroupOwnerField())
                if(peer.hasGroupOwner())
                    textDeviceGroup.setText(R.string.groupClient);
                else textDeviceGroup.setText("  ");
            else
                textDeviceGroup.setText(R.string.missingGroup);

            textDeviceUuid.setText(peer.getUuid().substring(24));
            textDeviceMacAddress.setText(peer.getMacAddress());
        } else {
            textDeviceStatus.setText(R.string.missingStatus);
            textDeviceGroup.setText(R.string.missingGroup);
            textDeviceUuid.setText(R.string.missingUuid);
            textDeviceMacAddress.setText(R.string.missingMac);
        }

        return entry;
    }
}
