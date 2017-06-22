/*
 * @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-mm-dd
 *
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */

package pt.ulusofona.copelabs.ndn.android.ui;

import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;

/** Used to provide a list of Tabs for the Main UI */
class MainTabListener implements ActionBar.TabListener {
    private final ViewPager mPager;
    private int mCurrentPosition = 0;

    MainTabListener(ViewPager pgr) {
        mPager = pgr;
    }

    @Override
    public void onTabSelected(ActionBar.Tab tab, FragmentTransaction ft) {
        if (mPager != null) {
            mCurrentPosition = tab.getPosition();
            mPager.setCurrentItem(tab.getPosition());
        }
    }

    @Override
    public void onTabUnselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    @Override
    public void onTabReselected(ActionBar.Tab tab, FragmentTransaction ft) {

    }

    int getCurrentPosition() {
        return mCurrentPosition;
    }
}
