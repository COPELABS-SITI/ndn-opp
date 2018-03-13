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

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.preferences.Configuration;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.databinding.DialogConnectNdnBinding;

/** Dialog for the connection of the running daemon to an NDN node that provides access to the NDN testbed.
 * (cfr. https://named-data.net/ndn-testbed)
 */
public class ConnectToNdnDialog extends DialogFragment {

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
		final OpportunisticDaemon.Binder fwdDaemon = (OpportunisticDaemon.Binder) getArguments().getBinder("ForwardingDaemon");
		final DialogConnectNdnBinding dialogBinding = DialogConnectNdnBinding.inflate(getActivity().getLayoutInflater());

		// Select the COPELABS NDN Node by default (entry 33 of arrays.xml)
		String selectedNdnNodeIp = Configuration.getNdnNodeIp(getContext());
		String[] ndnNodesIps = getContext().getResources().getStringArray(R.array.ndn_nodes_addrs);

		for(int i = 0; i < ndnNodesIps.length; i++) {
			if(ndnNodesIps[i].equals(selectedNdnNodeIp)) {
				dialogBinding.ndnNodeSelector.setSelection(i);
				break;
			}
		}

		return new AlertDialog.Builder(getActivity())
			.setView(dialogBinding.getRoot())
			.setTitle(R.string.selectNdnNode)
			.setPositiveButton(R.string.connect, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					int ndnNodeIndex = dialogBinding.ndnNodeSelector.getSelectedItemPosition();
					String ndnAddr = getContext().getResources().getStringArray(R.array.ndn_nodes_addrs)[ndnNodeIndex];

                    String faceUri = "tcp://" + ndnAddr + ":6363";
					Configuration.setNdnNode(getContext(), faceUri);

					//fwdDaemon.createFace(faceUri, dialogBinding.isPermanent.isChecked() ? 2 : 0,false);
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					dismiss();
				}
			})
			.create();
	}
}