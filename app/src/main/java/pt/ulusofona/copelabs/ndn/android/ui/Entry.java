package pt.ulusofona.copelabs.ndn.android.ui;

import android.view.LayoutInflater;
import android.view.View;

public interface Entry {
    View getView(LayoutInflater infl);
    int getItemViewType();
    void setViewContents(View entry);
}
