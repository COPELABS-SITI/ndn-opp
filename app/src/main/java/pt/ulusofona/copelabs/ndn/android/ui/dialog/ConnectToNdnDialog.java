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
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public class ConnectToNdnDialog extends DialogFragment {
	private Spinner mNdnNodes;
	private CheckBox mIsPermanent;

	public static ConnectToNdnDialog create(ForwardingDaemon.DaemonBinder binder) {
		ConnectToNdnDialog fragment = new ConnectToNdnDialog();
		Bundle args = new Bundle();
		args.putBinder("ForwardingDaemon", binder);
		fragment.setArguments(args);
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		ForwardingDaemon.DaemonBinder dBinder = (ForwardingDaemon.DaemonBinder) getArguments().getBinder("ForwardingDaemon");
		final ForwardingDaemon fwdDaemon = dBinder.getService();

		View dialog = View.inflate(getContext(), R.layout.dialog_connect_ndn, null);

		mNdnNodes = (Spinner) dialog.findViewById(R.id.ndn_nodes);
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