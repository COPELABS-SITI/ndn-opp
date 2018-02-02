/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Simple Dialog to Add a route to the ForwardingDaemon's RIB.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui.dialog;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.OpportunisticPeerTracking;
import pt.ulusofona.copelabs.ndn.android.ui.tasks.ExpressInterestTask;
import pt.ulusofona.copelabs.ndn.databinding.DialogExpressInterestBinding;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class ExpressInterestDialog extends DialogFragment {
	private static final String TAG = ExpressInterestDialog.class.getSimpleName();

	private Face mFace;
	private OnData mOnDataReceived;

	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static ExpressInterestDialog create(Face face, OnData odr) {
		ExpressInterestDialog fragment = new ExpressInterestDialog();
		fragment.mFace = face;
		fragment.mOnDataReceived = odr;
		return fragment;
	}

	/** Constructs a dialog window which enables the addition of routes. Three parameters are requested through its fields;
	 * the Name prefix, the Face ID and the Cost.
	 * @param savedInstanceState used by Android for restoring the dialog object from a previous instance
	 * @return the Dialog to be displayed for adding a route.
	 */
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final DialogExpressInterestBinding dialogBinding = DialogExpressInterestBinding.inflate(getActivity().getLayoutInflater());
		return new AlertDialog.Builder(getActivity())
			.setView(dialogBinding.getRoot())
			.setTitle("Express Interest")
			.setPositiveButton(R.string.express, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface di, int id) {
					Name name = new Name(OpportunisticPeerTracking.PREFIX + "/" + dialogBinding.interestName.getText().toString());
					Interest interest = new Interest(name, OpportunisticPeerTracking.INTEREST_LIFETIME);
					interest.setLongLived(dialogBinding.isLongLived.isChecked());
					new ExpressInterestTask(mFace, interest, mOnDataReceived).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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