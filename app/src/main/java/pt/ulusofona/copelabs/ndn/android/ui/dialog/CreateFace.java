package pt.ulusofona.copelabs.ndn.android.ui.dialog;

import android.app.Dialog;

import android.content.Context;
import android.content.DialogInterface;

import android.net.wifi.WifiManager;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import android.view.View;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public class CreateFace extends DialogFragment {
    private ForwardingDaemon mDaemon;
	private View mDialog;

	private Spinner mProtocol;
	private EditText mHost;
	private EditText mPort;
	private CheckBox mIsPermanent;

    public CreateFace(ForwardingDaemon fd) {
        mDaemon = fd;
    }

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		mDialog = View.inflate(getContext(), R.layout.dialog_create_face, null);

		mProtocol = (Spinner) mDialog.findViewById(R.id.protocol);
		mHost = (EditText) mDialog.findViewById(R.id.host);
		mPort = (EditText) mDialog.findViewById(R.id.port);
		mIsPermanent = (CheckBox) mDialog.findViewById(R.id.permanent);

		WifiManager wifiMgr = (WifiManager) getActivity().getSystemService(Context.WIFI_SERVICE);
		int ipAddr = wifiMgr.getConnectionInfo().getIpAddress() & wifiMgr.getDhcpInfo().netmask;
		if(ipAddr != 0)
			mHost.setText(String.format("%d.%d.%d.%d",
				ipAddr & 0xff,
				ipAddr >> 8 & 0xff,
				ipAddr >> 16 & 0xff,
				ipAddr >> 24 & 0xff
			));
		else mHost.setText("");

		return builder
			.setView(mDialog)
			.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					String protocol = mProtocol.getSelectedItem().toString();
                    String host = mHost.getText().toString();
                    String port = mPort.getText().toString();

                    String faceUri;
					if(protocol.equals("wfd"))
						faceUri = "wfd://[" + host + "]";
                    else {
                        if (host.isEmpty())
                            host = getString(R.string.defaultHost);
                        if (port.isEmpty())
                            port = getString(R.string.defaultPort);
                        faceUri = protocol + "://" + host + ":" + port;
                    }

					mDaemon.createFace(
						faceUri,
						mIsPermanent.isChecked() ? 2 : 0,
						host.equals("127.0.0.1")
					);
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					getDialog().cancel();
				}
			})
			.create();
	}
}