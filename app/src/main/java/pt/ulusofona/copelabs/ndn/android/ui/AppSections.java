/*
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-mm-dd
 * A FragmentPagerAdapter for the TabLayout of the MainActivity. Based on official Android example.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui;

import android.support.annotation.IdRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;
import java.util.List;

import pt.ulusofona.copelabs.ndn.android.ui.fragment.ContentStore;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.FaceTable;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.ForwarderConfiguration;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.NameTree;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.OpportunisticPeerTracking;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.PendingInterestTable;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Refreshable;
import pt.ulusofona.copelabs.ndn.android.umobile.OpportunisticDaemon;

class AppSections extends FragmentPagerAdapter {
    private static final String TAG = AppSections.class.getSimpleName();

    private List<String> mFragmentTitles = new ArrayList<>();
    private List<Fragment> mFragments = new ArrayList<>();

    AppSections(FragmentManager fm) {
        super(fm);
    }

    public void addFragment(String fragTitle, Fragment frag) {
        mFragmentTitles.add(fragTitle);
        mFragments.add(frag);
    }

    @Override
    public Fragment getItem(int id) {
        return mFragments.get(id);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Override
    public CharSequence getPageTitle(int id) {
        CharSequence mCurrentlyDisplayedTitle = null;

        if(id == 0)
            mCurrentlyDisplayedTitle = "Peers";
        else if (id == 1) {
            mCurrentlyDisplayedTitle = "PIT";
        } else if (id == 2) {
            mCurrentlyDisplayedTitle = "Faces";
        } else if (id == 3) {
            mCurrentlyDisplayedTitle = "FWD";
        } else if (id == 4) {
            mCurrentlyDisplayedTitle = "Names";
        } else if (id == 5) {
            mCurrentlyDisplayedTitle = "Content";
        }

        return mCurrentlyDisplayedTitle;
    }

    public void refresh(OpportunisticDaemon.Binder daemon, int currentPosition) {
        Fragment current = getItem(currentPosition);
        if(current instanceof Refreshable) {
            Refreshable refr = (Refreshable) current;
            refr.refresh(daemon);
        }
    }

    void clear() {
        for(Fragment current : mFragments)
            if(current instanceof Refreshable)
                ((Refreshable) current).clear();
    }
}
