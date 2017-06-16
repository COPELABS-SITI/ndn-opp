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
import android.net.ConnectivityManager;
import android.os.Bundle;

import android.os.IBinder;
import android.support.v4.app.DialogFragment;
import android.support.v4.view.ViewPager;

import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuInflater;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;

import pt.ulusofona.copelabs.ndn.R;
import pt.ulusofona.copelabs.ndn.android.ui.dialog.ConnectToNdnDialog;
import pt.ulusofona.copelabs.ndn.android.umobile.ForwardingDaemon;

import pt.ulusofona.copelabs.ndn.android.ui.dialog.AddRouteDialog;
import pt.ulusofona.copelabs.ndn.android.ui.dialog.CreateFaceDialog;
import pt.ulusofona.copelabs.ndn.android.umobile.Utilities;

public class Main extends AppCompatActivity {
    private static final String TAG = Main.class.getSimpleName();

    private ViewPager mPager;
    private MainTabListener mTabListener;
    private AppSections mAppSections = new AppSections(getSupportFragmentManager());

    // ForwardingDaemon service
    private Intent mDaemonIntent;
    private final IntentFilter mDaemonBroadcastedIntents = new IntentFilter();
    private final DaemonBroadcastReceiver mDaemonListener = new DaemonBroadcastReceiver();

    private TextView mUptime;
    private ForwardingDaemon mDaemon;
    private ForwardingDaemon.DaemonBinder mDaemonBinder;
    private boolean mDaemonConnected = false;

    public Main() {
        mDaemonBroadcastedIntents.addAction(ForwardingDaemon.STARTED);
        mDaemonBroadcastedIntents.addAction(ForwardingDaemon.STOPPING);
    }

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        mDaemonIntent = new Intent(getApplicationContext(), ForwardingDaemon.class);

        mUptime = (TextView) findViewById(R.id.uptime);

        mPager = (ViewPager) findViewById(R.id.root);
        mTabListener = new MainTabListener(mPager);
        mPager.setAdapter(mAppSections);

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

        mPager.setOnPageChangeListener(new ViewPager.SimpleOnPageChangeListener() {
            @Override
            public void onPageSelected(int position) {
                actionBar.setSelectedNavigationItem(position);
            }
        });

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

        TextView uuid = (TextView) findViewById(R.id.umobileUuid);
        uuid.setText(Utilities.obtainUuid(this));
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
        ConnectivityManager connMgr = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);

        for(int i = 0; i < menu.size(); i++)
            if(i == 0)
                menu.getItem(i).setEnabled(mDaemonConnected && connMgr.getNetworkInfo(ConnectivityManager.TYPE_WIFI).isConnected());
            else
                menu.getItem(i).setEnabled(mDaemonConnected);

        return true;
    }

    @Override
	public boolean onOptionsItemSelected(MenuItem item) {
        DialogFragment dialog = null;

        int itemId = item.getItemId();

        if(item.getItemId() == R.id.createFace)
            dialog = CreateFaceDialog.create(mDaemonBinder);
        else if (itemId == R.id.addRoute)
            dialog = AddRouteDialog.create(mDaemonBinder);
        else if (itemId == R.id.connectNdn)
            dialog = ConnectToNdnDialog.create(mDaemonBinder);

        if(dialog != null)
            dialog.show(getSupportFragmentManager(), dialog.getTag());
        else
            Log.w(TAG, "Invalid item ID selected from Options");

		return true;
	}

    // For automatic refreshing of mCurrentDisplayedRefreshable.
    private static final long STARTUP_DELAY = 0L;
    private static final long UPDATE_PERIOD = 1000L;
    private boolean mUpdaterRunning;
    private Timer mUpdater;

    private void clearDisplayed() {
        if(mUptime != null)
            mUptime.setText(getString(R.string.notAvailable));

        mAppSections.clear();
    }

    private void refreshDisplayed() {
        if(mDaemonConnected) {
            if (mUptime != null) {
                long uptimeInSeconds = mDaemon.getUptime() / 1000L;
                long s = (uptimeInSeconds % 60);
                long m = (uptimeInSeconds / 60) % 60;
                long h = (uptimeInSeconds / 3600) % 60;
                mUptime.setText(String.format(Locale.getDefault(), "%02d:%02d:%02d", h, m, s));
            }

            mAppSections.refresh(mDaemon, mTabListener.getCurrentPosition());
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
            stopUpdater();
            unbindService(mConnection);
            clearDisplayed();
            mDaemonConnected = false;
            mDaemon = null;
        }
    }

    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName cn, IBinder bndr) {
            Log.d(TAG, "Service Connected");
            mDaemonBinder = (ForwardingDaemon.DaemonBinder) bndr;
            mDaemon = mDaemonBinder.getService();
            mDaemonConnected = true;
            startUpdater();
        }

        @Override
        public void onServiceDisconnected(ComponentName cn) {
            Log.d(TAG, "Service Unexpectedly disconnected");
            stopUpdater();
            clearDisplayed();
            mDaemonConnected = false;
            mDaemon = null;
        }
    };

    private class DaemonBroadcastReceiver extends android.content.BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            Log.d(TAG, "ForwardingDaemon : " + action);
            Toast.makeText(Main.this, "Daemon : " + nicefy(action), Toast.LENGTH_LONG).show();
            if(action.equals(ForwardingDaemon.STARTED))
                bindService(mDaemonIntent, mConnection, Context.BIND_AUTO_CREATE);
        }

        private String nicefy(String action) {
            String result = null;

            if(action.equals(ForwardingDaemon.STARTED))
                result = "STARTED";
            else if (action.equals(ForwardingDaemon.STOPPING))
                result = "STOPPING";

            return result;
        }
    }
}