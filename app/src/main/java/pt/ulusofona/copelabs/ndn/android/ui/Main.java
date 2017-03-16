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

	private Refreshable mCurrentDisplayedFragment;

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

        setDisplayedFragment(R.id.nav_peerTracking);
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
		switch(item.getItemId()) {
		    case R.id.createFace:
			    CreateFace cf = new CreateFace(mDaemon);
			    cf.setTargetFragment((Fragment) mCurrentDisplayedFragment, 0);
			    cf.show(getSupportFragmentManager(), cf.getTag());
			    break;
            case R.id.addRoute:
                AddRoute ar = new AddRoute(mDaemon);
                ar.setTargetFragment((Fragment) mCurrentDisplayedFragment, 0);
                ar.show(getSupportFragmentManager(), ar.getTag());
                break;
        }
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
        mCurrentDisplayedFragment = null;

        if(id == R.id.nav_peerTracking)
            mCurrentDisplayedFragment = mPeerTracking;
        else if (id == R.id.nav_overview)
            mCurrentDisplayedFragment = mOverview;
        else if (id == R.id.nav_forwarderConfiguration)
            mCurrentDisplayedFragment = mForwarderConfiguration;
        else if (id == R.id.nav_nameTree)
            mCurrentDisplayedFragment = mNametree;
        else if (id == R.id.nav_contentStore)
            mCurrentDisplayedFragment = mContentStore;

        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.container, (Fragment) mCurrentDisplayedFragment)
                .commit();

        if(mDaemonConnected) mCurrentDisplayedFragment.refresh(mDaemon);

        ActionBar actBar = getSupportActionBar();
        if(actBar != null) actBar.setTitle(mCurrentDisplayedFragment.getTitle());
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        setDisplayedFragment(item.getItemId());

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.root);
        drawer.closeDrawer(GravityCompat.START);

        return true;
    }

    // For automatic refreshing of mCurrentDisplayedFragment.
    private static final long STARTUP_DELAY = 0L;
    private static final long UPDATE_PERIOD = 1000L;
    private boolean mUpdaterRunning;
    private Timer mUpdater;

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
                            if (mCurrentDisplayedFragment != null && mDaemonConnected)
                                mCurrentDisplayedFragment.refresh(mDaemon);
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
                mCurrentDisplayedFragment.refresh(mDaemon);
            }
        }
    }
}