package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.support.annotation.NonNull;

import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

public interface Refreshable {
    int getTitle();
    void refresh(@NonNull ForwardingDaemon daemon);
}
