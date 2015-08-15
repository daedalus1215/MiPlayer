package com.example.larry.miplayer;


import android.app.Activity;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentTransaction;
import android.os.Bundle;

public class MainActivity extends FragmentActivity {
	// Preferences
	final static String PREFS_STORED_PROGRESSION = "PREFS_STORED_PROGRESSION";

	final public static String TRACK_ALBUM_KEY = "TRACK_ALBUM_KEY";

	final public static String TRACK_ALBUM_ID_KEY = "TRACK_ALBUM_ID_KEY";


    public static final String TAG = "MainActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (savedInstanceState == null) {
            android.support.v4.app.FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            SlidingTabsBasicFragment fragment = new SlidingTabsBasicFragment();
            transaction.replace(R.id.sample_content_fragment, fragment);
            transaction.commit();
            initControlPanel();
        }
    }
    private void initControlPanel() {
        FragmentTransaction fragTrans = getSupportFragmentManager()
                .beginTransaction();
        fragTrans.add(R.id.thirdFragment, new ControlPanelMiniFragment())
                .commit();
    }

}