package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import java.util.Locale;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public class Status extends Refreshable {
	// Controls
	private TextView mVersion;
	private TextView mUptime;
	private TextView mIpAddress;

	private WifiManager mWifiMgr;

	@Override
	public void onCreate(@Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mWifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
	}

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View contentStore = inflater.inflate(R.layout.fragment_status, parent, false);

		mVersion = (TextView) contentStore.findViewById(R.id.version);		
		mUptime = (TextView) contentStore.findViewById(R.id.uptime);
		mIpAddress = (TextView) contentStore.findViewById(R.id.ipAddress);

		return contentStore;
	}

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onPause() {

        super.onPause();
    }

    @Override
	public void clear() {
		mVersion.setText(R.string.notAvailable);
        mUptime.setText(R.string.notAvailable);
        mIpAddress.setText(R.string.notAvailable);
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

	@Override
	public void refresh(ForwardingDaemon fd) {
        if(fd != null) {
            mVersion.setText(fd.getVersion());

            long uptimeInSeconds = fd.getUptime() / 1000L;
            long s = (uptimeInSeconds % 60);
            long m = (uptimeInSeconds / 60) % 60;
            long h = (uptimeInSeconds / 3600) % 60;
            mUptime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));

            int ipv4Addr = mWifiMgr.getConnectionInfo().getIpAddress();
            if (ipv4Addr != 0)
                mIpAddress.setText(formatIPv4(ipv4Addr, mWifiMgr.getDhcpInfo().netmask));
            else mIpAddress.setText(R.string.notAvailable);
        }
	}
}