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
import android.content.IntentFilter;

import android.content.ServiceConnection;
import android.os.Bundle;

import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;

import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;

import android.widget.Switch;
import android.widget.CompoundButton;

import java.util.Timer;
import java.util.TimerTask;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.PeerTracking;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

import pt.ulusofona.copelabs.ndn.android.ui.dialog.AddRoute;
import pt.ulusofona.copelabs.ndn.android.ui.dialog.CreateFace;

import pt.ulusofona.copelabs.ndn.android.ui.fragment.ContentStore;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.ForwarderConfiguration;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.NameTree;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Overview;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Refreshable;

public class Main extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private static final String TAG = Main.class.getSimpleName();

    // ForwardingDaemon service
    private Intent mDaemonIntent;
    private final IntentFilter mDaemonBroadcastedIntents = new IntentFilter();
    private final DaemonBroadcastReceiver mDaemonListener = new DaemonBroadcastReceiver();

    private ForwardingDaemon mDaemon;
    private boolean mDaemonConnected = false;

    // Fragments
    private final PeerTracking mPeerTracking = new PeerTracking();
    private final Overview mOverview = new Overview();
	private final ForwarderConfiguration mForwarderConfiguration = new ForwarderConfiguration();
    private final NameTree mNametree = new NameTree();
    private final ContentStore mContentStore = new ContentStore();

    private Fragment mCurrentlyDisplayed;
    private int mCurrentlyDisplayedTitle;

    public Main() {
        mDaemonBroadcastedIntents.addAction(ForwardingDaemon.STARTED);
        mDaemonBroadcastedIntents.addAction(ForwardingDaemon.STOPPING);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        mDaemonIntent = new Intent(getApplicationContext(), ForwardingDaemon.class);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.root);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.navigation);
        navigationView.setNavigationItemSelectedListener(this);

        Switch nfdSwitch = (Switch) findViewById(R.id.nfdSwitch);
		nfdSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isOn) {
				if(isOn) startService(mDaemonIntent);
				else {
                    disconnectDaemon();
                    stopService(mDaemonIntent);
                }
			}
		});

        setDisplayedFragment(R.id.nav_forwarderConfiguration);
	}

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(mDaemonListener, mDaemonBroadcastedIntents);
        if(mDaemonConnected) startUpdater();
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
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.main, menu);
		return true;
	}

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        for(int i = 0; i < menu.size(); i++)
            menu.getItem(i).setEnabled(mDaemonConnected);
        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        DialogFragment dialog = null;
        int itemId = item.getItemId();

        if(item.getItemId() == R.id.createFace)
            dialog = new CreateFace(mDaemon);
        else if (itemId == R.id.addRoute)
            dialog = new AddRoute(mDaemon);

        if(dialog != null) {
            dialog.setTargetFragment(mCurrentlyDisplayed, 0);
            dialog.show(getSupportFragmentManager(), dialog.getTag());
        } else
            Log.w(TAG, "Invalid item ID selected from Options");

		return true;
	}

    @Override
    public void onBackPressed() {
        // TODO: navigate Back out of the App results in crash.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.root);
        if (drawer.isDrawerOpen(GravityCompat.START))
            drawer.closeDrawer(GravityCompat.START);
        else
            super.onBackPressed();
    }

    private void setDisplayedFragment(int id) {
        mCurrentlyDisplayed = null;

        if(id == R.id.nav_peerTracking) {
            mCurrentlyDisplayed = mPeerTracking;
            mCurrentlyDisplayedTitle = R.string.peerTracking;
        } else if (id == R.id.nav_overview) {
            mCurrentlyDisplayed = mOverview;
            mCurrentlyDisplayedTitle = R.string.overview;
        } else if (id == R.id.nav_forwarderConfiguration) {
            mCurrentlyDisplayed = mForwarderConfiguration;
            mCurrentlyDisplayedTitle = R.string.forwarderConfiguration;
        } else if (id == R.id.nav_nameTree) {
            mCurrentlyDisplayed = mNametree;
            mCurrentlyDisplayedTitle = R.string.nametree;
        } else if (id == R.id.nav_contentStore) {
            mCurrentlyDisplayed = mContentStore;
            mCurrentlyDisplayedTitle = R.string.contentstore;
        }

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, mCurrentlyDisplayed)
                .commit();

        if(mDaemonConnected) {
            if(mCurrentlyDisplayed instanceof Refreshable) {
                Refreshable refr = (Refreshable) mCurrentlyDisplayed;
                refr.refresh(mDaemon);
            }
            ActionBar actBar = getSupportActionBar();
            if(actBar != null) actBar.setTitle(mCurrentlyDisplayedTitle);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        setDisplayedFragment(item.getItemId());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.root);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    // For automatic refreshing of mCurrentDisplayedRefreshable.
    private static final long STARTUP_DELAY = 0L;
    private static final long UPDATE_PERIOD = 1000L;
    private boolean mUpdaterRunning;
    private Timer mUpdater;

    private void refreshDisplayed() {
        if (mCurrentlyDisplayed instanceof Refreshable && mDaemonConnected) {
            Refreshable refr = (Refreshable) mCurrentlyDisplayed;
            refr.refresh(mDaemon);
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
        if(mDaemonConnected) {
            unbindService(mConnection);
            mDaemonConnected = false;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName cn, IBinder bndr) {
            Log.d(TAG, "Service Connected");
            mDaemon = ((ForwardingDaemon.DaemonBinder) bndr).getService();
            mDaemonConnected = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName cn) {
            Log.d(TAG, "Service Unexpectedly disconnected");
            mDaemonConnected = false;
            mDaemon = null;
        }
    };

    private class DaemonBroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "ForwardingDaemon : " + action);
			if(action.equals(ForwardingDaemon.STARTED)) {
                startUpdater();
                bindService(mDaemonIntent, mConnection, Context.BIND_AUTO_CREATE);
            } else if (action.equals(ForwardingDaemon.STOPPING)) {
                stopUpdater();
                refreshDisplayed();
            }
        }
    }
}