package pt.ulusofona.copelabs.oi_ndn.fragments;

import android.app.DialogFragment;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;

import com.example.copelabs.oi_ndn.R;

import net.named_data.jndn.Data;
import net.named_data.jndn.Face;
import net.named_data.jndn.Interest;
import net.named_data.jndn.InterestFilter;
import net.named_data.jndn.Name;
import net.named_data.jndn.OnData;
import net.named_data.jndn.OnInterestCallback;
import net.named_data.jndn.OnPushedDataCallback;

import java.util.ArrayList;
import java.util.Arrays;

import pt.ulusofona.copelabs.oi_ndn.interfaces.EmergencyDialogFragmentInterface;
import pt.ulusofona.copelabs.oi_ndn.models.Message;

import static android.app.Activity.RESULT_OK;

/**
 * Created by copelabs on 10/08/2017.
 */

public class EmergencyDialogFragment extends DialogFragment implements OnInterestCallback, OnData ,OnPushedDataCallback{

    private EditText mMessageText;
    private String TAG = getClass().getSimpleName();
    private EmergencyDialogFragmentInterface mInterface;

    static final int REQUEST_IMAGE_CAPTURE = 1;
    private ImageView mImageView;
    private ImageButton mImageButton;

    /**
     * Create a new instance of EmergencyDialogFragment, providing "num"
     * as an argument.
     */
    public static EmergencyDialogFragment newInstance(int num) {

        EmergencyDialogFragment f = new EmergencyDialogFragment();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_emergency_message, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        mMessageText  = (EditText) v.findViewById(R.id.editText);
        mImageButton = (ImageButton) v.findViewById(R.id.imageButton);

        ArrayList<String> mInterestsSelected = new ArrayList<>(Arrays.asList(this.getActivity().getResources().getStringArray(R.array.emergency)));

        Spinner mSpinner = (Spinner) v.findViewById(R.id.spinner);

        ArrayAdapter adapter = new ArrayAdapter<>(this.getActivity(), android.R.layout.simple_spinner_item, mInterestsSelected);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        mSpinner.setAdapter(adapter);
        mSpinner.setOnItemSelectedListener(
                new AdapterView.OnItemSelectedListener() {
                    public void onItemSelected(AdapterView<?> parent,
                                               View v, int position, long id) {
                        Log.d(TAG, parent.getItemAtPosition(position).toString()) ;

                    }
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        mImageButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });
        // Watch for button clicks.
        Button button = (Button)v.findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {

                // When button is clicked, call up to owning activity.
                //((FragmentDialog)getActivity()).showDialog();
                Message message = new Message("XX",mMessageText.getText().toString(),"Emergency");
                Log.d(TAG, message.getContent()+"");

                mInterface.SendMessage(message.getContent());

                //getDialog().cancel();
            }
        });

        return v;
    }


    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            mImageView.setImageBitmap(imageBitmap);
        }
    }
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            mInterface = (EmergencyDialogFragmentInterface) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnHeadlineSelectedListener");
        }
    }

    @Override
    public void onInterest(Name name, Interest interest, Face face, long l, InterestFilter interestFilter) {
        Log.d(TAG, "on Interest");
    }

    @Override
    public void onData(Interest interest, Data data) {
        Log.d(TAG, "on data");
    }

    @Override
    public void onPushedData(Data data) {
        Log.d(TAG, "on pushed");
    }
}
