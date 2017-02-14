/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Fragment which allows to display general information about the device and the ForwardingDaemon.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.Context;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import java.util.Locale;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public class Status extends Fragment {
	// Controls
	private TextView mVersion;
	private TextView mUptime;
	private TextView mIpAddress;
	private TextView mMacAddress;

	private WifiManager mWifiMgr;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View status = inflater.inflate(R.layout.fragment_status, parent, false);

		mVersion = (TextView) status.findViewById(R.id.version);
		mUptime = (TextView) status.findViewById(R.id.uptime);
		mIpAddress = (TextView) status.findViewById(R.id.ipAddress);
		mMacAddress = (TextView) status.findViewById(R.id.macAddress);

		return status;
	}

    public void clear() {
		mVersion.setText(R.string.notAvailable);
        mUptime.setText(R.string.notAvailable);
        mIpAddress.setText(R.string.notAvailable);
		mMacAddress.setText(R.string.notAvailable);
	}

    private String formatIPv4(int ipv4Addr, int ipv4Mask) {
        return String.format(Locale.getDefault(), "%d.%d.%d.%d/%d",
                ipv4Addr       & 0xff,
                ipv4Addr >>  8 & 0xff,
                ipv4Addr >> 16 & 0xff,
                ipv4Addr >> 24 & 0xff,
                Integer.bitCount(ipv4Mask)
        );
    }

	public void refresh(String version, long uptimeInMilliseconds) {
        mVersion.setText(version);

        long uptimeInSeconds = uptimeInMilliseconds / 1000L;
        long s = (uptimeInSeconds % 60);
        long m = (uptimeInSeconds / 60) % 60;
        long h = (uptimeInSeconds / 3600) % 60;
        mUptime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));

        int ipv4Addr = mWifiMgr.getConnectionInfo().getIpAddress();
        if (ipv4Addr != 0)
            mIpAddress.setText(formatIPv4(ipv4Addr, mWifiMgr.getDhcpInfo().netmask));
        else mIpAddress.setText(R.string.notAvailable);

		mMacAddress.setText(mWifiMgr.getConnectionInfo().getMacAddress().toLowerCase(Locale.getDefault()));
	}
}