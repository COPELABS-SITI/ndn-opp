/**
 *  @version 1.0
 * COPYRIGHTS COPELABS/ULHT, LGPLv3.0, 2017-02-14
 * Main Activity of the NDN-Opp app.
 * @author Seweryn Dynerowicz (COPELABS/ULHT)
 */
package pt.ulusofona.copelabs.ndn.android.ui;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.preferences.Configuration;
import pt.ulusofona.copelabs.ndn.android.umobile.connectionless.Identity;
import pt.ulusofona.copelabs.ndn.android.ui.dialog.AddRouteDialog;
import pt.ulusofona.copelabs.ndn.android.ui.dialog.ConnectToNdnDialog;
import pt.ulusofona.copelabs.ndn.android.ui.dialog.CreateFaceDialog;
import pt.ulusofona.copelabs.ndn.android.ui.dialog.ExpressInterestDialog;
import pt.ulusofona.copelabs.ndn.android.ui.dialog.SendDataDialog;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.ContentStore;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.FaceTable;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.ForwarderConfiguration;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.NameTree;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.OpportunisticPeerTracking;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.PendingInterestTable;
import pt.ulusofona.copelabs.ndn.android.umobile.common.OpportunisticDaemon;
import pt.ulusofona.copelabs.ndn.databinding.ActivityMainBinding;

/** Main interface of NDN-Opp. Brings together the various app sections with the connection to the
 * ForwardingDaemon. */
public class Main extends AppCompatActivity implements ServiceConnection {
    private static final String TAG = Main.class.getSimpleName();

    private MainTabListener mTabListener;
    private AppSections mAppSections = new AppSections(getSupportFragmentManager());

    // Fragments
    private final OpportunisticPeerTracking mPeerTracking = new OpportunisticPeerTracking();
    private final PendingInterestTable mPit = new PendingInterestTable();
    private final FaceTable mFaceTable = new FaceTable();
    private final ForwarderConfiguration mFwd = new ForwarderConfiguration();
    private final NameTree mNametree = new NameTree();
    private final ContentStore mContentStore = new ContentStore();

    // ForwardingDaemon service
    private Intent mDaemonIntent;
    private final DaemonBroadcastReceiver mDaemonListener = new DaemonBroadcastReceiver();

    private ActivityMainBinding mBinding;

    private boolean mDaemonBound = false;
    private OpportunisticDaemon.Binder mDaemonBinder;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Identity.initialize(this);
        Log.v(TAG, "Identity : " + Identity.getUuid());

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        Log.v(TAG, "ActivityMainBinding : " + mBinding);

        mDaemonIntent = new Intent(this, OpportunisticDaemon.class);

        mAppSections.addFragment(getString(R.string.peerTracking), mPeerTracking);
        mAppSections.addFragment(getString(R.string.pit), mPit);
        mAppSections.addFragment(getString(R.string.facetable), mFaceTable);
        mAppSections.addFragment(getString(R.string.forwarderConfiguration), mFwd);
        mAppSections.addFragment(getString(R.string.nametree), mNametree);
        mAppSections.addFragment(getString(R.string.contentstore), mContentStore);

        mTabListener = new MainTabListener(mBinding.pager);
        mBinding.pager.setAdapter(mAppSections);

        // Set up the action bar.
        final android.support.v7.app.ActionBar actionBar = getSupportActionBar();
        actionBar.setHomeButtonEnabled(false);
        actionBar.setNavigationMode(android.app.ActionBar.NAVIGATION_MODE_TABS);

        // For each of the sections in the app, add a tab to the action bar.
        for (int i = 0; i < mAppSections.getCount(); i++) {
            actionBar.addTab(
                    actionBar.newTab()
                            .setText(mAppSections.getPageTitle(i))
                            .setTabListener(mTabListener));
        }

        mBinding.pager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

		mBinding.nfdSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isOn) {
                if(isOn) startService(mDaemonIntent);
				else {
                    disconnectDaemon();
                    stopService(mDaemonIntent);
                }
			}
		});

        mBinding.uuid.setText(Identity.getUuid());
	}

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mDaemonListener, OpportunisticDaemon.mIntents);
        if(mDaemonBound) startUpdater();
    }

    @Override
	protected void onPause() {
        unregisterReceiver(mDaemonListener);
        stopUpdater();
        super.onPause();
	}

    @Override
    protected void onDestroy() {
        disconnectDaemon();
        stopService(mDaemonIntent);
        super.onDestroy();
    }

    @Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        menu.getItem(0).setEnabled(mDaemonBound && connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected());
        for(int i = 1; i < menu.size(); i++) {
            menu.getItem(i).setEnabled(mDaemonBound);
            if(menu.getItem(i).isCheckable()) {
                menu.getItem(i).setChecked(Configuration.isBackupOptionEnabled(this));
            }
        }
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        DialogFragment dialog = null;

        switch (item.getItemId()) {
            case R.id.createFace:
                dialog = CreateFaceDialog.create(mDaemonBinder);
                break;
            case R.id.addRoute:
                dialog = AddRouteDialog.create(mDaemonBinder);
                break;
            case R.id.connectNdn:
                dialog = ConnectToNdnDialog.create(mDaemonBinder);
                break;
            case R.id.expressInterest:
                dialog = ExpressInterestDialog.create(mPeerTracking.getFace(), mPeerTracking);
                break;
            case R.id.sendPushedData:
                dialog = SendDataDialog.create(mPeerTracking.getFace());
                break;
            case R.id.sendPacketsConfiguration:
                item.setChecked(!item.isChecked());
                Configuration.setSendOption(this, item.isChecked());
                break;
        }

        if(dialog != null)
            dialog.show(getSupportFragmentManager(), dialog.getTag());
        else
            Log.w(TAG, "Invalid item ID selected from Options");

		return true;
	}

    // For automatic refreshing of mCurrentDisplayedRefreshable.
    private static final long STARTUP_DELAY = 0L;
    private static final long UPDATE_PERIOD = 1000L; // Number of milli-seconds before polling the daemon for its configuration
    private boolean mUpdaterRunning;
    private Timer mUpdater;

    private void clearDisplayed() {
        mBinding.uptime.setText(getString(R.string.notAvailable));
        mAppSections.clear();
    }

    private void refreshDisplayed() {
        if(mDaemonBound) {
            long uptimeInSeconds = mDaemonBinder.getUptime() / 1000L;
            long s = (uptimeInSeconds % 60);
            long m = (uptimeInSeconds / 60) % 60;
            long h = (uptimeInSeconds / 3600) % 60;
            mBinding.uptime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));

            mAppSections.refresh(mDaemonBinder, mTabListener.getCurrentPosition());
        }
    }

    private void startUpdater() {
        if(!mUpdaterRunning) {
            mUpdater = new Timer();
            TimerTask mUpdaterTask = new TimerTask() {
                @Override
                public void run() {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                        // TODO: check this does not run all the time ...
                        refreshDisplayed();
                        }
                    });
                }
            };
            mUpdater.scheduleAtFixedRate(mUpdaterTask, STARTUP_DELAY, UPDATE_PERIOD);
            mUpdaterRunning = true;
        }
    }

    private void stopUpdater() {
        if(mUpdaterRunning) {
            mUpdater.cancel();
            mUpdaterRunning = false;
        }
    }

    private void disconnectDaemon() {
        if(mDaemonBound) {
            stopUpdater();
            unbindService(this);
            clearDisplayed();
            mDaemonBound = false;
            mDaemonBinder = null;
        }
    }

    @Override
    public void onServiceConnected(ComponentName cn, IBinder bndr) {
        Log.d(TAG, "Service Connected");
        mDaemonBinder = (OpportunisticDaemon.Binder) bndr;
        mDaemonBound = true;
        startUpdater();
    }

    @Override
    public void onServiceDisconnected(ComponentName cn) {
        Log.d(TAG, "Service Unexpectedly disconnected");
        stopUpdater();
        clearDisplayed();
        mDaemonBound = false;
        mDaemonBinder = null;
    }

    private class DaemonBroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "ForwardingDaemon : " + action);
            Toast.makeText(Main.this, "Daemon : " + action.substring(action.lastIndexOf('.') + 1, action.length()), Toast.LENGTH_LONG).show();
            if(action.equals(OpportunisticDaemon.STARTED))
                bindService(mDaemonIntent, Main.this, Context.BIND_AUTO_CREATE);
        }
    }
}