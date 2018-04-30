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
import android.util.Base64;
import android.util.Log;

import net.named_data.jndn.util1.Blob;
import net.named_data.jndn1.Data;
import net.named_data.jndn1.Face;
import net.named_data.jndn1.Interest;
import net.named_data.jndn1.Name;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.OpportunisticPeerTracking;
import pt.ulusofona.copelabs.ndn.android.ui.tasks.RespondToInterestTask;
import pt.ulusofona.copelabs.ndn.databinding.DialogRespondToInterestBinding;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class RespondToInterestDialog extends DialogFragment {

	/** This variable is used to debug RespondToInterestDialog */
	private static final String TAG = RespondToInterestDialog.class.getSimpleName();

	/** This object is used to remove the interest from incoming interests */
	private OpportunisticPeerTracking mPeerTracking;

	/** This object is used to reference which interest are we responding */
	private Interest mInterest;

	/** This object is used to set which face the interest will be responded. */
	private Face mFace;

	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static RespondToInterestDialog create(OpportunisticPeerTracking opt, Face face, Interest interest) {
		RespondToInterestDialog fragment = new RespondToInterestDialog();
		fragment.mInterest = interest;
		fragment.mPeerTracking = opt;
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
		final DialogRespondToInterestBinding dialogBinding = DialogRespondToInterestBinding.inflate(getActivity().getLayoutInflater());
		dialogBinding.setInterestPacket(mInterest);

		return new AlertDialog.Builder(getActivity())
			.setView(dialogBinding.getRoot())
			.setTitle("Send WifiP2pCache")
			.setPositiveButton(R.string.send, new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface di, int id) {
					Name dName = new Name(dialogBinding.dataName.getText().toString());
					Data data = new Data(dName);
					Blob blob = new Blob(dialogBinding.dataContent.getText().toString());
					Log.v(TAG, "Blob : " + dialogBinding.dataContent.getText().toString() + " > " + Base64.encodeToString(blob.getImmutableArray(), Base64.NO_PADDING));
					data.setContent(blob);
					new RespondToInterestTask(mFace, data).executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
					mPeerTracking.respondedToInterest(mInterest);
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