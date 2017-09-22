/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Simple Dialog to Add a route to the ForwardingDaemon's RIB.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.OpportunisticPeerTracking;
import pt.ulusofona.copelabs.ndn.android.ui.tasks.ExpressInterestTask;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class ExpressInterestDialog extends DialogFragment {
	private static final String TAG = ExpressInterestDialog.class.getSimpleName();

	private Face mFace;
	private OnData mOnDataReceived;
	private EditText mInterestName;
	private CheckBox mLongLived;

	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static ExpressInterestDialog create(Face face, OnData odr) {
		ExpressInterestDialog fragment = new ExpressInterestDialog();
		fragment.mFace = face;
		fragment.mOnDataReceived = odr;
		return fragment;
	}

	@Override
	public void onAttach(Context context) {
		super.onAttach(context);
	}

	/** Constructs a dialog window which enables the addition of routes. Three parameters are requested through its fields;
	 * the Name prefix, the Face ID and the Cost.
	 * @param savedInstanceState used by Android for restoring the dialog object from a previous instance
	 * @return the Dialog to be displayed for adding a route.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

		View dialog = View.inflate(getContext(), R.layout.dialog_send_interest, null);

		mInterestName = (EditText) dialog.findViewById(R.id.interestName);
		mLongLived = (CheckBox) dialog.findViewById(R.id.long_lived);

		return builder
			.setView(dialog)
			.setTitle("Express Interest")
			.setPositiveButton(R.string.express, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface di, int id) {
					Name name = new Name(OpportunisticPeerTracking.PREFIX + "/" + mInterestName.getText().toString());
					Interest interest = new Interest(name, OpportunisticPeerTracking.INTEREST_LIFETIME);
					interest.setLongLived(mLongLived.isChecked());
					new ExpressInterestTask(mFace, interest, mOnDataReceived).execute();
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