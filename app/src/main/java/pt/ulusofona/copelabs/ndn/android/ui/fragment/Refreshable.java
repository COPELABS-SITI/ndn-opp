package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.support.v4.app.Fragment;

public abstract class Refreshable extends Fragment {
	public abstract void refresh();
	public abstract void update();
	public abstract void clear();
}