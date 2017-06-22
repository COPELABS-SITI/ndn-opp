/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-03-23
 * The NsdServiceAdapter is used to populate a ListView
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

import java.util.Locale;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.models.NsdService;

/** Adapter class for displaying a list of NsdServices in a View.
 * cfr. https://developer.android.com/reference/android/widget/Adapter.html
 */
public class NsdServiceAdapter extends ArrayAdapter<NsdService> {
    private LayoutInflater mInflater;

    /** Main constructor
     * @param context Android context within which the Adapter should be created
     */
    public NsdServiceAdapter(Context context) {
        super(context, R.layout.item_network_service);
        mInflater = LayoutInflater.from(context);
    }

    /** Used by Android to retrieve the View corresponding to a certain item in the list of NsdServices.
     * @param position position of the NsdService for which the View is requested
     * @param convertView available View to be filled with the NsdService details
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
            entry = mInflater.inflate(R.layout.item_network_service, null, false);

        NsdService svc = getItem(position);

        if (svc != null) {
            TextView textStatus = (TextView) entry.findViewById(R.id.text_service_status);
            TextView textHost = (TextView) entry.findViewById(R.id.text_service_host);
            TextView textPort = (TextView) entry.findViewById(R.id.text_service_port);
            TextView textUuid = (TextView) entry.findViewById(R.id.text_service_uuid);

            textStatus.setText(svc.getStatus().toString());
            textHost.setText(String.format(Locale.getDefault(), "%1$15s", svc.getHost()));
            textPort.setText(String.format(Locale.getDefault(), "%1$5d", svc.getPort()));
            textUuid.setText(svc.getUuid().substring(24));
        }

        return entry;
    }
}
