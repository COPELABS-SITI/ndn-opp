/*
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-mm-dd
 *
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
import android.widget.Spinner;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;

/** Dialog for the connection of the running daemon to an NDN node that provides access to the NDN testbed.
 * (cfr. https://named-data.net/ndn-testbed)
 */
public class ConnectToNdnDialog extends DialogFragment {
	private Spinner mNdnNodes;
	private CheckBox mIsPermanent;

	/** Method to be used for creating a new ConnectToNdnDialog.
	 * @param binder used to access the locally running daemon
	 * @return the ConnectToNdnDialog
	 */
	public static ConnectToNdnDialog create(OpportunisticDaemon.Binder binder) {
		ConnectToNdnDialog fragment = new ConnectToNdnDialog();
		Bundle args = new Bundle();
		args.putBinder("ForwardingDaemon", binder);
		fragment.setArguments(args);
		return fragment;
	}

	/** Constructs a dialog window which enables the connection to an NDN Node. Two parameters are requested through its fields;
	 * the NDN node to connect to, whether the connection should be set to Permanent.
	 * @param savedInstanceState used by Android for restoring the dialog object from a previous instance
	 * @return the Dialog to be displayed for connecting to a NDN node
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		final OpportunisticDaemon.Binder fwdDaemon = (OpportunisticDaemon.Binder) getArguments().getBinder("ForwardingDaemon");

		View dialog = View.inflate(getContext(), R.layout.dialog_connect_ndn, null);

		mNdnNodes = (Spinner) dialog.findViewById(R.id.ndn_nodes);
		// Select the COPELABS NDN Node by default (entry 33 of arrays.xml)
		mNdnNodes.setSelection(33);

		mIsPermanent = (CheckBox) dialog.findViewById(R.id.permanent);

		return builder
			.setView(dialog)
			.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					int ndnNodeIndex = mNdnNodes.getSelectedItemPosition();
					String ndnAddr = getResources().getStringArray(R.array.ndn_nodes_addrs)[ndnNodeIndex];

                    String faceUri = "tcp://" + ndnAddr + ":6363";

					fwdDaemon.createFace(
						faceUri,
						mIsPermanent.isChecked() ? 2 : 0,
						false
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