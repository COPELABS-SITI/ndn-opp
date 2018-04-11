/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Simple Dialog to Add a route to the ForwardingDaemon's RIB.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.widget.TextView;

import net.named_data.jndn1.Data;

import pt.ulusofona.copelabs.ndn.databinding.DialogDisplayDataBinding;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class DisplayDataDialog extends DialogFragment {
	private static final String TAG = DisplayDataDialog.class.getSimpleName();
	private Data mData;
	private TextView mPrefix;
	private TextView mContent;

	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static DisplayDataDialog create(Data data) {
		DisplayDataDialog fragment = new DisplayDataDialog();
		fragment.mData = data;
		return fragment;
	}

	/** Constructs a dialog window which enables the addition of routes. Three parameters are requested through its fields;
	 * the Name prefix, the Face ID and the Cost.
	 * @param savedInstanceState used by Android for restoring the dialog object from a previous instance
	 * @return the Dialog to be displayed for adding a route.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		DialogDisplayDataBinding dialogBinding = DialogDisplayDataBinding.inflate(getActivity().getLayoutInflater());
		dialogBinding.setDataPacket(mData);

		StringBuilder title = new StringBuilder("Received ");
		if(mData.isPushed())
			title.append("Pushed ");
		title.append("WifiP2pCache");

		return new AlertDialog.Builder(getActivity())
			.setView(dialogBinding.getRoot())
			.setTitle(title.toString())
			.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface di, int id) {}
			})
			.create();
	}
}