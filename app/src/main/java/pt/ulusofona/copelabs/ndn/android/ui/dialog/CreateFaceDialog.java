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
import android.util.Log;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.databinding.DialogCreateFaceBinding;

public class CreateFaceDialog extends DialogFragment {
	private static final String TAG = CreateFaceDialog.class.getSimpleName();

	/** Method to be used for creating a new CreateFaceDialog.
	 * @param binder used to access the locally running daemon
	 * @return the CreateFaceDialog object
	 */
	public static CreateFaceDialog create(OpportunisticDaemon.Binder binder) {
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
		final OpportunisticDaemon.Binder fwdDaemon = (OpportunisticDaemon.Binder) getArguments().getBinder("ForwardingDaemon");
		final DialogCreateFaceBinding dialogBinding = DialogCreateFaceBinding.inflate(getActivity().getLayoutInflater());

		return new AlertDialog.Builder(getActivity())
			.setView(dialogBinding.getRoot())
			.setTitle(R.string.createFace)
			.setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					String protocol = dialogBinding.protocolSelector.getSelectedItem().toString();
                    String host = dialogBinding.host.getText().toString();
                    String port = dialogBinding.port.getText().toString();

					Log.v(TAG, "Host:" + host + ", Port:" + port);
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
						dialogBinding.isPermanent.isChecked() ? 2 : 0,
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