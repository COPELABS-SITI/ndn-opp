/*
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-mm-dd
 * A FragmentPagerAdapter for the TabLayout of the MainActivity. Based on official Android example.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

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

    // Fragments
    private final OpportunisticPeerTracking mPeerTracking = new OpportunisticPeerTracking();
    private final PendingInterestTable mPit = new PendingInterestTable();
    private final FaceTable mFaceTable = new FaceTable();
    private final ForwarderConfiguration mFwd = new ForwarderConfiguration();
    private final NameTree mNametree = new NameTree();
    private final ContentStore mContentStore = new ContentStore();

    AppSections(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int id) {
        Fragment mCurrentlyDisplayed = null;
        if(id == 0)
            mCurrentlyDisplayed = mPeerTracking;
        else if (id == 1)
            mCurrentlyDisplayed = mPit;
        else if (id == 2)
            mCurrentlyDisplayed = mFaceTable;
        else if (id == 3)
            mCurrentlyDisplayed = mFwd;
        else if (id == 4)
            mCurrentlyDisplayed = mNametree;
        else if (id == 5)
            mCurrentlyDisplayed = mContentStore;

        return mCurrentlyDisplayed;
    }

    @Override
    public int getCount() {
        return 6;
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
        mPit.clear();
        mFaceTable.clear();
        mFwd.clear();
        mNametree.clear();
        mContentStore.clear();
    }
}
