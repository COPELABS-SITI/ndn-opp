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
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.util.Blob;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.tasks.RespondToInterestTask;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class DisplayData extends DialogFragment {
	private static final String TAG = DisplayData.class.getSimpleName();
	private Data mData;
	private TextView mPrefix;
	private TextView mContent;

	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static DisplayData create(Data data) {
		DisplayData fragment = new DisplayData();
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
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View dialog = View.inflate(getContext(), R.layout.dialog_display_data, null);

		mPrefix = (TextView) dialog.findViewById(R.id.prefix);
		mPrefix.setText(mData.getName().toString());
		mContent = (TextView) dialog.findViewById(R.id.content);
		mContent.setText(mData.getContent().toString());

		StringBuilder title = new StringBuilder("Received ");
		if(mData.isPushed())
			title.append("Pushed ");
		title.append("Data");

		return builder
			.setView(dialog)
			.setTitle(title.toString())
			.setPositiveButton("Close", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface di, int id) {}
			})
			.create();
	}
}