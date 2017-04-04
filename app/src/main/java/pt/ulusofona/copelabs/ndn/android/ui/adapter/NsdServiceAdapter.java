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

public class NsdServiceAdapter extends ArrayAdapter<NsdService> {
    private LayoutInflater mInflater;

    public NsdServiceAdapter(Context context) {
        super(context, R.layout.item_network_service);
        mInflater = LayoutInflater.from(context);
    }

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
