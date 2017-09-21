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
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.util.Blob;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.tasks.RespondToInterestTask;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class RespondToInterest extends DialogFragment {
	private static final String TAG = RespondToInterest.class.getSimpleName();
	private Face mFace;
	private Interest mInterest;
	private TextView mPrefix;
	private TextView mLifetime;
	private TextView mLongLived;
	private EditText mResponse;

	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static RespondToInterest create(Face face, Interest interest) {
		RespondToInterest fragment = new RespondToInterest();
		fragment.mFace = face;
		fragment.mInterest = interest;
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

		View dialogView = View.inflate(getContext(), R.layout.dialog_respond_to_interest, null);

		mPrefix = (TextView) dialogView.findViewById(R.id.prefix);
		mPrefix.setText(mInterest.getName().toString());
		mLifetime = (TextView) dialogView.findViewById(R.id.lifetime);
		mLifetime.setText(mInterest.getInterestLifetimeMilliseconds() + " ms");
		mLongLived = (TextView) dialogView.findViewById(R.id.is_long_lived);
		mLongLived.setText(Boolean.toString(mInterest.isLongLived()));
		mResponse = (EditText) dialogView.findViewById(R.id.response);

		builder.setView(dialogView)
			.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					getDialog().cancel();
				}
			});

		Dialog dialog;
		if(mInterest.isLongLived()) {
			dialog = builder.setPositiveButton(R.string.respond_to_interest, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface di, int id) {
					Data data = new Data(mInterest.getName());
					Blob blob = new Blob(mResponse.getText().toString());
					Log.v(TAG, "Blob : " + mResponse.getText().toString() + " > " + Base64.encodeToString(blob.getImmutableArray(), Base64.NO_PADDING));
					data.setContent(new Blob(mResponse.getText().toString()));
					new RespondToInterestTask(mFace, data).execute();
				}
			})
							.create();
		} else {
			dialog = builder.setPositiveButton(R.string.respond_to_interest, null)
							.create();
			dialog.setOnShowListener(new DialogInterface.OnShowListener() {
				@Override
				public void onShow(DialogInterface dialogInterface) {
					Button b = ((android.app.AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE);
					b.setOnClickListener(new View.OnClickListener() {
						@Override
						public void onClick(View view) {
							Data data = new Data(mInterest.getName());
							Blob blob = new Blob(mResponse.getText().toString());
							Log.v(TAG, "Blob : " + mResponse.getText().toString() + " > " + Base64.encodeToString(blob.getImmutableArray(), Base64.NO_PADDING));
							data.setContent(new Blob(mResponse.getText().toString()));
							new RespondToInterestTask(mFace, data).execute();
						}
					});
				}
			});
		}

		return dialog;
	}
}