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
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Name;
import net.named_data.jndn.util.Blob;

import java.util.Map;
import java.util.UUID;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.Identity;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.OpportunisticPeerTracking;
import pt.ulusofona.copelabs.ndn.android.ui.tasks.RespondToInterestTask;
import pt.ulusofona.copelabs.ndn.android.umobile.wifip2p.OpportunisticPeerTracker;

/** Dialog for the addition of a new Route to the RIB and FIB of the running daemon. */
public class ScenarioConfigurationDialog extends DialogFragment {
	private static final String TAG = ScenarioConfigurationDialog.class.getSimpleName();

	private static final String mDeviceNames[] = {"R", "K", "U"};
	/** Method to be used for creating a new AddRouteDialog.
	 * @return the AddRouteDialog
	 */
	public static ScenarioConfigurationDialog create() {
		ScenarioConfigurationDialog fragment = new ScenarioConfigurationDialog();
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

		View dialog = View.inflate(getContext(), R.layout.dialog_scenario_configuration, null);
		final Spinner spinner = (Spinner) dialog.findViewById(R.id.scenarioConfiguration);

		final SharedPreferences storage = getContext().getSharedPreferences(Identity.class.getSimpleName(), Context.MODE_PRIVATE);

		return builder
			.setView(dialog)
			.setTitle("Select Scenario Identity")
			.setPositiveButton("Save", new DialogInterface.OnClickListener() {
				@Override public void onClick(DialogInterface di, int id) {
						String assignedIdentity = mDeviceNames[spinner.getSelectedItemPosition()];
						SharedPreferences.Editor editor = storage.edit();
						editor.putString(Identity.PROPERTY_DEMO_KEY, assignedIdentity);
						Toast.makeText(getContext(), "Saving Identity : " + assignedIdentity, Toast.LENGTH_SHORT).show();
						editor.apply();
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