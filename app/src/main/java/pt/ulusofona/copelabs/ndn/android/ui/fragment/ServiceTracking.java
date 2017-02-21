package pt.ulusofona.copelabs.ndn.android.ui.fragment;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.UmobileService;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;

public class ServiceTracking extends Fragment implements Refreshable {
    private Table<UmobileService> mServicePeers;

    public ServiceTracking() {
        mServicePeers = Table.newInstance(R.string.servicePeers, R.layout.item_service);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View frag = inflater.inflate(R.layout.fragment_wifi, container, false);

        getChildFragmentManager()
                .beginTransaction()
                .replace(R.id.servicePeers, mServicePeers)
                .commit();

        return frag;
    }

    @Override
    public int getTitle() {
        return R.string.serviceTracker;
    }

    public void refresh(@NonNull ForwardingDaemon daemon) {
        mServicePeers.refresh(daemon.getUmobileServices());
    }
}
