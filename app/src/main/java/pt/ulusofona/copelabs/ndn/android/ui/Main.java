package pt.ulusofona.copelabs.ndn.android.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import android.net.wifi.p2p.WifiP2pManager;

import android.os.Bundle;
import android.os.Handler;

import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.MenuInflater;

import android.widget.Switch;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Toast;

import java.util.List;

import pt.ulusofona.copelabs.ndn.R;

import pt.ulusofona.copelabs.ndn.android.CsEntry;
import pt.ulusofona.copelabs.ndn.android.Face;
import pt.ulusofona.copelabs.ndn.android.Name;

import pt.ulusofona.copelabs.ndn.android.Peer;
import pt.ulusofona.copelabs.ndn.android.service.ContextualManager;
import pt.ulusofona.copelabs.ndn.android.service.ForwardingDaemon;
import pt.ulusofona.copelabs.ndn.android.service.Routing;

import pt.ulusofona.copelabs.ndn.android.ui.dialog.CreateFace;

import pt.ulusofona.copelabs.ndn.android.ui.fragment.Refreshable;
import pt.ulusofona.copelabs.ndn.android.ui.fragment.Table;

public class Main extends AppCompatActivity {
    // BroadcastReceiver
    private IntentFilter mServiceIntents;
    private BroadcastReceiver mServiceTracker;

    private Switch mNfdSwitch;

	// Fragments
	private Table<Name> mNametree;
	private Table<CsEntry> mContentStore;

	private Overview mOverview;
	private ForwarderConfiguration mFwdCfg;

	// Navigation
	private ListView mNavigation;
	private String mNavigationEntries[];

	private Refreshable mCurrent;

	// For automatic refreshing of screen.
	private static final int UPDATE_DELAY = 1000;
	private final Handler mUpdater = new Handler();
	private final Runnable mRefresher = new Runnable() {
		@Override
		public void run() {
            // TODO: this runs all the time. Even when the Daemon is stopped.
			mCurrent.refresh();
			mUpdater.postDelayed(this, UPDATE_DELAY);
        }
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

        mServiceIntents = new IntentFilter();
        mServiceIntents.addAction(ForwardingDaemon.STARTED);
        mServiceIntents.addAction(ForwardingDaemon.STOPPED);
        mServiceTracker = new BroadcastReceiver(this);

        mNametree = new Table<>(R.string.nametree, new Name.Adapter(this), new Table.EntryProvider<Name>() {
			@Override
			public List<Name> getEntries() {
				return ForwardingDaemon.getNameTree();
			}
		});

		mContentStore = new Table<>(R.string.contentstore, new CsEntry.Adapter(this), new Table.EntryProvider<CsEntry>() {
			@Override
			public List<CsEntry> getEntries() {
				return ForwardingDaemon.getContentStore();
			}
		});

		Table<Face> faceTable = new Table<>(R.string.facetable, new Face.Adapter(this), new Table.EntryProvider<Face>() {
			@Override
			public List<Face> getEntries() {
				return ForwardingDaemon.getFaceTable();
			}
		});

        Table<Peer> peerList = new Table<>(R.string.peers, new Peer.Adapter(this), new Table.EntryProvider<Peer>() {
            @Override
            public List<Peer> getEntries() {
                return ForwardingDaemon.getPeers();
            }
        });

		mOverview = new Overview(this, peerList, faceTable);
		mFwdCfg = new ForwarderConfiguration(this, faceTable);

		mNfdSwitch = (Switch) findViewById(R.id.nfdSwitch);
		mNfdSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton button, boolean isOn) {
                Intent target = new Intent(getApplicationContext(), ForwardingDaemon.class);
				if(isOn) startService(target);
				else stopService(target);
			}
		});

		mNavigationEntries = getResources().getStringArray(R.array.navigationEntries);
		mNavigation = (ListView) findViewById(R.id.navigation);
		mNavigation.setAdapter(
			new ArrayAdapter<>(getApplicationContext(), android.R.layout.simple_list_item_1, mNavigationEntries));
		mNavigation.setOnItemClickListener(new ListView.OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				select(position);
				mNavigation.setItemChecked(position, true);
				((DrawerLayout) findViewById(R.id.root)).closeDrawer(mNavigation);
			}
		});

		select(0);
	}
	
	private void select(int position) {
		switch(position) {
		case 0:
			mCurrent = mOverview;
			break;
		case 1:
			mCurrent = mFwdCfg;
			break;
		case 2:
			mCurrent = mNametree;
			break;
		case 3:
			mCurrent = mContentStore;
			break;
		}

		getSupportFragmentManager()
			.beginTransaction()
			.replace(R.id.container, mCurrent)
			.commit();

		getSupportActionBar().setTitle(mNavigationEntries[position]);
	}

	@Override
	protected void onPause() {
		super.onPause();
        unregisterReceiver(mServiceTracker);
		mUpdater.removeCallbacks(mRefresher);
	}

	@Override
	protected void onResume() {
		super.onResume();
        registerReceiver(mServiceTracker, mServiceIntents);
		mUpdater.post(mRefresher);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater mi = getMenuInflater();
		mi.inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		    case R.id.createFace:
			    CreateFace cf = new CreateFace();
			    cf.setTargetFragment(mCurrent, 0);
			    cf.show(getSupportFragmentManager(), cf.getTag());
			    break;
        }
		return true;
	}

    private static class BroadcastReceiver extends android.content.BroadcastReceiver {
        private Main activity;

        public BroadcastReceiver(Main t) {
            activity = t;
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if(action.equals(ForwardingDaemon.STARTED)) {
                activity.mCurrent.update();
                activity.mUpdater.postDelayed(activity.mRefresher, UPDATE_DELAY);
                activity.mNfdSwitch.setChecked(true);
            } else if (action.equals(ForwardingDaemon.STOPPED)) {
                activity.mUpdater.removeCallbacks(activity.mRefresher);
                activity.mCurrent.clear();
                activity.mNfdSwitch.setChecked(false);
            }
        }
    }
}