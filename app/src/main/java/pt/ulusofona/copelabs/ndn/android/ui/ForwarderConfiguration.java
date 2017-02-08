package pt.ulusofona.copelabs.ndn.android.ui;

import android.os.Bundle;

import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.View;
import android.view.ViewGroup;
import android.view.LayoutInflater;

import java.util.List;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.FibEntry;
import pt.ulusofona.copelabs.ndn.android.SctEntry;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class ForwarderConfiguration extends Fragment {
	private Table<Face> mFacetable;
	private Table<FibEntry> mFib;
	private Table<SctEntry> mSct;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if(savedInstanceState == null) {
			mFacetable = new Table<>();
			mFacetable.setArguments(Face.TABLE_ARGUMENTS);

            mFib = new Table<>();
            mFib.setArguments(FibEntry.TABLE_ARGUMENTS);

            mSct = new Table<>();
            mSct.setArguments(SctEntry.TABLE_ARGUMENTS);
        }
    }

    @Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
		View contentStore = inflater.inflate(R.layout.fragment_forwarder_configuration, parent, false);

		getChildFragmentManager()
			.beginTransaction()
			.replace(R.id.facetable, mFacetable)
			.replace(R.id.fib, mFib)
			.replace(R.id.sct, mSct)
			.commit();

		return contentStore;
	}

	public void clear() {
        if(mFacetable != null) mFacetable.clear();
        if(mFib != null) mFib.clear();
        if(mSct != null) mSct.clear();
	}

    public void refresh(List<Face> faces, List<FibEntry> fib, List<SctEntry> sct) {
        mFacetable.refresh(faces); mFib.refresh(fib); mSct.refresh(sct);
	}
}