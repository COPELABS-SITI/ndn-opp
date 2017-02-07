package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.support.v4.app.Fragment;

import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public abstract class Refreshable extends Fragment {
    public abstract void clear();
    public abstract void refresh(ForwardingDaemon fd);
}
