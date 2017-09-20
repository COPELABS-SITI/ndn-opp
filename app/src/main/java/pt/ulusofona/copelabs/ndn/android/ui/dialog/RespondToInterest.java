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
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import java.io.IOException;

import pt.ulusofona.copelabs.ndn.R;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class RespondToInterest extends DialogFragment {
	private Face mFace;
	private Name mName;
	private TextView mPrefix;
	private EditText mResponse;

	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static RespondToInterest create(Face face, Name name) {
		RespondToInterest fragment = new RespondToInterest();
		fragment.mFace = face;
		fragment.mName = name;
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

		View dialog = View.inflate(getContext(), R.layout.dialog_add_route, null);

		mPrefix = (EditText) dialog.findViewById(R.id.prefix);
		mPrefix.setText(mName.toString());
		mResponse = (EditText) dialog.findViewById(R.id.response);

		return builder
			.setView(dialog)
			.setPositiveButton(R.string.respond_to_interest, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
				Data data = new Data(mName);
				data.setContent(new Blob(mResponse.toString()));
					try {
						mFace.putData(data);
					} catch (IOException e) {
						e.printStackTrace();
					}
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