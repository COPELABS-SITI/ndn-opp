/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Fragment which allows to display general information about the device and the ForwardingDaemon.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.os.Bundle;

import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import java.util.Locale;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public class Status extends Fragment implements Refreshable {
	// Controls
	private TextView mVersion;
	private TextView mUptime;
	private TextView mUmobileUuid;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View status = inflater.inflate(R.layout.fragment_status, parent, false);

		mVersion = (TextView) status.findViewById(R.id.version);
		mUptime = (TextView) status.findViewById(R.id.uptime);
		mUmobileUuid = (TextView) status.findViewById(R.id.umobileUuid);

		return status;
	}

    public void clear() {
		mVersion.setText(R.string.notAvailable);
        mUptime.setText(R.string.notAvailable);
		mUmobileUuid.setText(R.string.notAvailable);
	}

	@Override
	public int getTitle() {
		return R.string.status;
	}

	@Override
	public void refresh(@NonNull ForwardingDaemon daemon) {
		mVersion.setText(daemon.getVersion());

		long uptimeInSeconds = daemon.getUptime() / 1000L;
		long s = (uptimeInSeconds % 60);
		long m = (uptimeInSeconds / 60) % 60;
		long h = (uptimeInSeconds / 3600) % 60;
		mUptime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));

		mUmobileUuid.setText(daemon.getUmobileUuid());
	}
}