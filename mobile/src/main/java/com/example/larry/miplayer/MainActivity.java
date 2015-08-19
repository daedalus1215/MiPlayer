package com.example.larry.miplayer;


import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;

public class MainActivity extends FragmentActivity {
	// Preferences
	final static String PREFS_STORED_PROGRESSION = "PREFS_STORED_PROGRESSION";

	final public static String TRACK_ALBUM_KEY = "TRACK_ALBUM_KEY";

	final public static String TRACK_ALBUM_ID_KEY = "TRACK_ALBUM_ID_KEY";



    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // remove title
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {

            initSlidingTabFragment();
            initControlPanel();
        }
    }

    private void initSlidingTabFragment() {
        FragmentTransaction transaction = getSupportFragmentManager()
                .beginTransaction();
        SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
        transaction.replace(R.id.sliding_tab_basic_fragment, fragment);
        transaction.commit();
    }

    private void initControlPanel() {
        FragmentTransaction fragTrans = getSupportFragmentManager()
                .beginTransaction();
        fragTrans.add(R.id.thirdFragment, new ControlPanelMiniFragment())
                .commit();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(new Intent(MainActivity.this, AudioPlayingService.class));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }
}