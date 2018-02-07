/**
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * The PendingInterestAdapter is used to populate a ListView
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

import net.named_data.jndn.Interest;

import pt.ulusofona.copelabs.ndn.R;

public class PendingInterestAdapter extends ArrayAdapter<Interest> {
    private LayoutInflater mInflater;

    /** Main constructor.
     * @param context Android context within which the Adapter should be created
     */
    public PendingInterestAdapter(Context context) {
        super(context, R.layout.item_pending_interest);
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
            entry = mInflater.inflate(R.layout.item_pending_interest, null, false);

        Interest pi = getItem(position);

        if(pi != null) {
            TextView textDeviceStatus = (TextView) entry.findViewById(R.id.text_name);
            TextView textDeviceUuid = (TextView) entry.findViewById(R.id.text_lifetime);

            textDeviceStatus.setText(pi.getName().toString());
            textDeviceUuid.setText(Double.toString(pi.getInterestLifetimeMilliseconds()));
        }

        return entry;
    }
}
