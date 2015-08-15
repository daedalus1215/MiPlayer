package com.example.larry.miplayer;

import android.app.ActionBar.Tab;
import android.app.ActionBar;
import android.content.Intent;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.widget.Toolbar;

public class MainActivity2 extends FragmentActivity {
	private ActionBar actionBar;

	@Override
	protected void onCreate(Bundle arg0) {
		super.onCreate(arg0);
		setContentView(R.layout.activity_main_2);
        initActionBar();
		initControlPanel();

	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		stopService(new Intent(MainActivity2.this, AudioPlayingService.class));
	}

	private void initActionBar() {
		actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
        actionBar.setDisplayShowTitleEnabled(false);
		actionBar.setDisplayShowHomeEnabled(false);

		Tab albumTab = actionBar
				.newTab()
				.setText("Albums")
				.setTabListener(
						new TabListener(new MainActivityAlbumListFragment()));
		Tab audioFolder = actionBar
				.newTab()
				.setText("Folders")
				.setTabListener(
						new TabListener(new MainActivityFolderListFragment()));

		Tab allFolder = actionBar
				.newTab()
				.setText("Songs")
				.setTabListener(
						new TabListener(new MainActivityAllSongsListFragment()));

		Tab artistTab = actionBar
				.newTab()
				.setText("Artists")
				.setTabListener(
						new TabListener(new MainActivityArtistsListFragment()));

		actionBar.addTab(albumTab);
		actionBar.addTab(audioFolder);
		actionBar.addTab(allFolder);
		actionBar.addTab(artistTab);
	}

	private void initControlPanel() {
		FragmentTransaction fragTrans = getSupportFragmentManager()
				.beginTransaction();
		fragTrans.add(R.id.thirdFragment, new ControlPanelMiniFragment())
				.commit();
	}

	class TabListener implements ActionBar.TabListener {
		Fragment fragment;

                                                                                                                                                                                            public TabListener(Fragment frag) {
			fragment = frag;
		}

		@Override
		public void onTabReselected(Tab tab, android.app.FragmentTransaction ft) {
			FragmentTransaction fragmentTrans = getSupportFragmentManager()
					.beginTransaction();
			fragmentTrans.add(R.id.frame1, fragment).commit();

		}

		@Override
		public void onTabSelected(Tab tab, android.app.FragmentTransaction ft) {
			FragmentTransaction fragmentTrans = getSupportFragmentManager()
					.beginTransaction();
			fragmentTrans.add(R.id.frame1, fragment).commit();
		}

		@Override
		public void onTabUnselected(Tab tab, android.app.FragmentTransaction ft) {
			FragmentTransaction fragmentTrans = getSupportFragmentManager()
					.beginTransaction();
			fragmentTrans.remove(fragment).commit();
		}

	}

}
