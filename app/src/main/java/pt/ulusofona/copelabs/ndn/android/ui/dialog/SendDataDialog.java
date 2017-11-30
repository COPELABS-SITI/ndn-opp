/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Simple Dialog to Add a route to the ForwardingDaemon's RIB.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.Main;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.OpportunisticPeerTracking;
import pt.ulusofona.copelabs.ndn.android.ui.tasks.RespondToInterestTask;
import pt.ulusofona.copelabs.ndn.databinding.DialogSendDataBinding;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class SendDataDialog extends DialogFragment {
	private static final String TAG = SendDataDialog.class.getSimpleName();

	private Face mFace;

	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static SendDataDialog create(Face face) {
		SendDataDialog fragment = new SendDataDialog();
		fragment.mFace = face;
		return fragment;
	}

	/** Constructs a dialog window which enables the addition of routes. Three parameters are requested through its fields;
	 * the Name prefix, the Face ID and the Cost.
	 * @param savedInstanceState used by Android for restoring the dialog object from a previous instance
	 * @return the Dialog to be displayed for adding a route.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final DialogSendDataBinding dialogBinding = DialogSendDataBinding.inflate(getActivity().getLayoutInflater());

		return new AlertDialog.Builder(getActivity())
			.setView(dialogBinding.getRoot())
			.setTitle("Send Push Data")
			.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface di, int id) {
					Name dName = new Name(OpportunisticPeerTracking.PREFIX + "/" + dialogBinding.dataName.getText().toString());
					Data data = new Data(dName);
					data.setPushed(true);
					Blob blob = new Blob(dialogBinding.dataContent.getText().toString());
					Log.v(TAG, "Blob : " + dialogBinding.dataContent.getText().toString() + " > " + Base64.encodeToString(blob.getImmutableArray(), Base64.NO_PADDING));
					data.setContent(blob);
					new RespondToInterestTask(mFace, data).execute();
				}
			})
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialogInterface, int i) {
					dismiss();
				}
			})
			.create();
	}
}