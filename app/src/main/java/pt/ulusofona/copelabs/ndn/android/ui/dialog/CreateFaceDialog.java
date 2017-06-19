/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Simple Dialog to Create a new Face in the ForwardingDaemon.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.dialog;

import android.app.Dialog;

import android.content.DialogInterface;

import android.os.Bundle;

import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import android.view.View;

import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public class CreateFaceDialog extends DialogFragment {
	private Spinner mProtocol;
	private EditText mHost;
	private EditText mPort;
	private CheckBox mIsPermanent;

	/** Method to be used for creating a new CreateFaceDialog.
	 * @param binder used to access the locally running daemon
	 * @return the CreateFaceDialog object
	 */
	public static CreateFaceDialog create(ForwardingDaemon.DaemonBinder binder) {
		CreateFaceDialog fragment = new CreateFaceDialog();
		Bundle args = new Bundle();
		args.putBinder("ForwardingDaemon", binder);
		fragment.setArguments(args);
		return fragment;

	}

	/** Constructs a dialog window which enables the creation of Faces. Three parameters are requested through its fields;
	 * the protocol (TCP, UPD, etc.), the target host information (e.g. IPv4), the port number, the permanence state of the resulting Face.
	 * @param savedInstanceState used by Android for restoring the dialog object from a previous instance
	 * @return the Dialog to be displayed for creating a Face.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		ForwardingDaemon.DaemonBinder dBinder = (ForwardingDaemon.DaemonBinder) getArguments().getBinder("ForwardingDaemon");
		final ForwardingDaemon fwdDaemon = dBinder.getService();

		View dialog = View.inflate(getContext(), R.layout.dialog_create_face, null);

		mProtocol = (Spinner) dialog.findViewById(R.id.protocol);
		mHost = (EditText) dialog.findViewById(R.id.host);
		mPort = (EditText) dialog.findViewById(R.id.port);
		mIsPermanent = (CheckBox) dialog.findViewById(R.id.permanent);

		return builder
			.setView(dialog)
			.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					String protocol = mProtocol.getSelectedItem().toString();
                    String host = mHost.getText().toString();
                    String port = mPort.getText().toString();

                    String faceUri;
					if(protocol.equals(R.string.opp))
						faceUri = getString(R.string.opp) + "://[" + host + "]";
                    else {
                        if (host.isEmpty())
                            host = getString(R.string.defaultHost);
                        if (port.isEmpty())
                            port = getString(R.string.defaultPort);
                        faceUri = protocol + "://" + host + ":" + port;
                    }

					fwdDaemon.createFace(
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