package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.os.Bundle;

import android.content.Context;
import android.net.wifi.WifiManager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.view.LayoutInflater;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public class Status extends Refreshable {
	// Controls
	private TextView mVersion;
	private TextView mUptime;
	private TextView mIpAddress;
	private WifiManager mWifiMgr;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View contentStore = inflater.inflate(R.layout.fragment_status, parent, false);

		mVersion = (TextView) contentStore.findViewById(R.id.version);		
		mVersion.setText(ForwardingDaemon.getVersion());

		mUptime = (TextView) contentStore.findViewById(R.id.uptime);
		mIpAddress = (TextView) contentStore.findViewById(R.id.ipAddress);
		mWifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);

		return contentStore;
	}

	@Override
	public void clear() {
		mUptime.setText(R.string.notAvailable);
		mIpAddress.setText(R.string.notAvailable);
	}

	@Override
	public void update() {
		long seconds = ForwardingDaemon.getUptime() / 1000;
		long s = (seconds % 60);
		long m = (seconds / 60) % 60;
		long h = (seconds / 3600) % 60;
		mUptime.setText( String.format("%02d:%02d:%02d", h, m, s));

		int ipAddr = mWifiMgr.getConnectionInfo().getIpAddress();
		if(ipAddr != 0)
			mIpAddress.setText(String.format("%d.%d.%d.%d/%d",
				ipAddr & 0xff,
				ipAddr >> 8 & 0xff,
				ipAddr >> 16 & 0xff,
				ipAddr >> 24 & 0xff,
				Integer.bitCount(mWifiMgr.getDhcpInfo().netmask)
			));
		else mIpAddress.setText(R.string.notAvailable);
	}

	@Override
	public void refresh() {
		clear();
		update();
	}
}