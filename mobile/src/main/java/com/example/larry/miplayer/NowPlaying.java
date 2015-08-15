package com.example.larry.miplayer;

/*
 * TROUBLING ISSUE I FEEL
 * 
 */
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothHeadset;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.ParcelUuid;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.example.larry.miplayer.SongHolder;

public class NowPlaying extends FragmentActivity implements
		OnSeekBarChangeListener, Runnable, OnClickListener {
	// CONSTANTS

	// Preferences
	// PREFERENCE KEY this is where all my preferences are located, in this
	// source file.
	final private static String PREFS_STORED_PROGRESSION = "PREFS_STORED_PROGRESSION";
	// PREFERENCE KEY FOR CURRENTLY PLAYING SONG
	final private static String PREF_SONG_COCKED_NOW = "PREF_SONG_COCKED_NOW";

	// Coming from MainActivity
	private final static String SONG_CHOSEN = "SONG_CHOSEN";

	// Intents
	/*
	 * BROADCAST_AUDIO_POSITION : Intent to send out where we are audio, NOW
	 * PLAYING WILL RECEIVE this has a intExtra that is called PROGRESS for
	 * getting the progress value
	 */
	final static String BROADCAST_AUDIO_POSITION = "BROADCAST_AUDIO_POSITION";
	/**
	 * ACTION_SEEKBAR_CHANGE_PROGRESS : is a title we use with our INTENT : will
	 * be accompanied by a integer, to represent the new position of the track.
	 * SEEKBAR_PROGRESS
	 */
	final private static String ACTION_SEEKBAR_CHANGE_PROGRESS = "ACTION_SEEKBAR_CHANGE_PROGRESS";
	/**
	 * ACTION_ASK_SERVICE_ON : is a title we use with out INTENT to find out if
	 * the AudioPlayingService is actually on. There is a Receiver in that class
	 * that is listening for this intent and it will broadcast out if it is on
	 * with ACTION_IS_SERVICE_ON.
	 */
	final static String ACTION_ASK_SERVICE_ON = "ACTION_ASK_SERVICE_ON";
	/**
	 * ACTION_PLAY_STOP : used for FILTER will be accompanied with newSong
	 * (true) or !newSong (false). Will tell this AudioPlayer if we should grab
	 * the new "PREF_SONG_COCKED_NOW" or if we should just play what we have
	 * cocked. boolean KEY = NEW_SONG
	 */
	final static String ACTION_PLAY_STOP = "ACTION_PLAY_STOP";
	/**
	 * ACTION_IS_SERVICE_ON : is a title we use for our FILTER to receive
	 * whether or not AudioPlayingService is actually on. There is a
	 * BroadcastReceiver that receives ACTION_ASK_SERVICE_ON intent and will
	 * broadcast out if the service is actually on.
	 */
	final static String ACTION_IS_SERVICE_ON = "ACTION_IS_SERVICE_ON";
	/**
	 * ACTION_FOR_BACK : Alerts or sends of forward or backward in audioProgress
	 * FORWARD = boolean; forward(true) or backward(false)
	 */
	final static String ACTION_FOR_BACK = "ACTION_FOR_BACK";
	/*
	 * STOP_SEEKBAR is used to tell this Activity page to not to keep adjusting
	 * progress bar. boolean associative "PlayProgress" (false) stop the thread
	 * from updating progressbar (true) have thread continue to update progress
	 * bar
	 */
	final public static String STOP_SEEKBAR = "STOP_SEEKBAR";
	/**
	 * ACTION_CHANGING_SONG will be sent from the AudioPlayingService and it
	 * will essentially run a Task that will figure out duration of the SeekBar
	 * because when we get to the end of one song, our duration was all messy
	 */
	final public static String ACTION_CHANGING_SONG = "ACTION_CHANGING_SONG";
	/**
	 * This will help us set whether we have a pause icon or a play icon.
	 * "Playing" is the extra boolean key, it will tell us if audioPlayer is
	 * playing (true) or if it is paused (false)
	 */
	final static String ACTION_IS_MEDIA_PLAYING = "ACTION_IS_MEDIA_PLAYING";

	/**
	 * this will be used only by this ACTIVITY in a way to let the
	 * AudioPlayingService to stop what it is doing, and start from the new
	 * song.
	 */

	// DECLARING GLOBAL VARIABLES/FIELDS/OBJECTS
	private ImageButton bFor, bBack, bPlayStop;
	private Button bVol, bBlueTooth;
	private TextView tvAlbum, tvSong, tvArtist;
	private SeekBar mSeekBar;
	private ListView mListView;
	private boolean isServiceOn = false;
	private SharedPreferences theSharedPrefs;
	private SharedPreferences.Editor theEditor;
	private SongsParcel theSong;
	private int currentPositionInAudioPlayBack = 0;
	private Handler theThreaderHandler;
	private AudioManager mAudioManager;
	private boolean audioPlayBackKeepProgressBarGoing;
	private SongHolder currentSong;
	private TextView tvCurrentT, tvTotalT;
	private ArrayList<SongHolder> entireAlbum;
	private ImageView imView;
	Cursor mCursor;
	Cursor mCursorAllAlbumArt;
	private ViewPager mViewPager;
	private ViewPagerAdapter mViewPagerAdapter;
	FragmentManager fmg;

	private static final String[] newProjectionForAlbumArt = {
			MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_KEY,
			MediaStore.Audio.Albums.ALBUM_ART };

	private static final String[] mProjection = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM_KEY, MediaStore.Audio.Media.DURATION };
	private static final int REQUEST_ENABLE_BT = 2;

	// Picking up which way we just went on the page

	private int pageThreshold;
	private int didwegoforward;

	// bluetooth related stuff
	BluetoothAdapter mBluetoothAdapter;
	ArrayAdapter<String> bluetoothArrayAdapter;
	HashMap<String, String> hashBluetoothList;
	ParcelUuid[] UUID;
	HandlerSocket mHandlerSocket;
	ConnectedThread mConnectedThread;

	// SETTING SHIT UP
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
		fmg = getSupportFragmentManager();
		setContentView(R.layout.now_playing_layout);
		theThreaderHandler = new Handler();
		mHandlerSocket = new HandlerSocket(this);
		mConnectedThread = null;
		currentSong = new SongHolder();
		initPreferences();
		initParcelAccessories();
		initButtonsBars();
		initBroadcastReceivers();
		audioPlayBackKeepProgressBarGoing = true;
		// / FINSIH UP HANDLING THE PARCEL, maybe use method to break it up
		sendBroadcast(new Intent(ACTION_ASK_SERVICE_ON));

	}

	@Override
	protected void onResume() {
		super.onResume();
		sendBroadcast(new Intent(ACTION_ASK_SERVICE_ON));

		initSeekBar();
		initHandler();
	}

	private void initPage() {
		mViewPager.setAdapter(mViewPagerAdapter);
		mViewPager.setCurrentItem(entireAlbum.indexOf(currentSong));

		pageThreshold = mViewPager.getCurrentItem();
		mViewPager.setOnPageChangeListener(new OnPageChangeListener() {

			@Override
			public void onPageSelected(int position) {
				if (position > mViewPagerAdapter.getCount()) {
					// mViewPager.setCurrentItem(0);
				}
			}

			@Override
			public void onPageScrolled(int arg0, float arg1, int arg2) {
			}

			@Override
			public void onPageScrollStateChanged(int state) {
				if (state == ViewPager.SCROLL_STATE_SETTLING) {
					if (mViewPager.getCurrentItem() < pageThreshold) {
						didwegoforward = 0;
						Log.d("ONPAGESELECTED", "Backwards");

					} else if (mViewPager.getCurrentItem() > pageThreshold) {
						didwegoforward = 2;
						Log.d("ONPAGESELECTED", "Forwards");
					}

				}

				if (state == ViewPager.SCROLL_STATE_IDLE) {
					Log.d("ONPAGESELECTED", Integer.toString(didwegoforward));
					turnPage(didwegoforward);
					pageThreshold = mViewPager.getCurrentItem();
					Log.d("ONPAGESELECTED", "Idle");
				}

			}

			private void turnPage(int pos) {
				boolean forward = false;
				if (pos == 0) {
					forward = false;
				} else if (pos == 2) {
					forward = true;
				}
				sendBroadcast(new Intent(AudioPlayingService.TURNED_THE_PAGE)
						.putExtra("Forward", forward));
			}
		});
	}

	private void initHandler() {

		theThreaderHandler.removeCallbacks(this);
		theThreaderHandler.postDelayed(this, 1000);
	}

	private void initButtonsBars() {
		mSeekBar = (SeekBar) findViewById(R.id.mainSeekBar);
		initSeekBar();

		mSeekBar.setOnSeekBarChangeListener(this);

	}

	private void initSeekBar() {
		mSeekBar.setMax((int) currentSong.getDuration());
		mSeekBar.setProgress((int) theSharedPrefs.getInt(
				theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""), 0));
	}

	private void initPreferences() {
		theSharedPrefs = getSharedPreferences(PREFS_STORED_PROGRESSION, 0);
		theEditor = theSharedPrefs.edit();

	}

	private void initParcelAccessories() {
		new TaskToFindNewSong().execute();
		// this.theSong = getIntent().getExtras().getParcelable(SONG_CHOSEN);
		mViewPager = (ViewPager) findViewById(R.id.llMain);
		mViewPagerAdapter = new ViewPagerAdapter(getSupportFragmentManager());
		// imView = (ImageView) findViewById(R.id.imImage);

		tvAlbum = (TextView) findViewById(R.id.tvNowPlayingAlbum);

		tvSong = (TextView) findViewById(R.id.tvNowPlayingSong);

		tvArtist = (TextView) findViewById(R.id.tvNowPlayingArtist);

		bPlayStop = (ImageButton) findViewById(R.id.bPlayStop);
		bPlayStop.setBackgroundResource(R.drawable.btn_pause);
		bPlayStop.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

				sendBroadcast(new Intent(ACTION_PLAY_STOP).putExtra("NEW_SONG",
						false));

			}
		});

		bFor = (ImageButton) findViewById(R.id.bFor);
		bFor.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendBroadcast(new Intent(ACTION_FOR_BACK).putExtra("FORWARD",
						true));
			}
		});

		bBack = (ImageButton) findViewById(R.id.bRew);
		bBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				sendBroadcast(new Intent(ACTION_FOR_BACK).putExtra("FORWARD",
						false));
			}
		});

		bVol = (Button) findViewById(R.id.ivVolume);
		bVol.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				mAudioManager.adjustVolume(AudioManager.ADJUST_SAME,
						AudioManager.FLAG_SHOW_UI);
			}
		});

		tvCurrentT = (TextView) findViewById(R.id.tvCurrentTime);
		tvTotalT = (TextView) findViewById(R.id.tvTotalTime);
		bBlueTooth = (Button) findViewById(R.id.bBluetoothButton);
		bBlueTooth.setOnClickListener(this);

	}

	@Override
	protected void onPause() {
		super.onPause();
		theThreaderHandler.removeCallbacks(this);
	}

	private void initBroadcastReceivers() {
		registerReceiver(receiveAudioProgressForSeekBar, new IntentFilter(
				BROADCAST_AUDIO_POSITION));
		registerReceiver(receiveIsServiceIsOn, new IntentFilter(
				ACTION_IS_SERVICE_ON));
		registerReceiver(receiveSongChangeFromAudioPlayingService,
				new IntentFilter(ACTION_CHANGING_SONG));
		registerReceiver(receiveWhetherButtonIsPlayOrPause, new IntentFilter(
				ACTION_IS_MEDIA_PLAYING));
		 registerReceiver(receiveBluetoothBroadcasts, new IntentFilter(
				BluetoothDevice.ACTION_FOUND));
	}

	@Override
	protected void onStop() {
		super.onStop();
		mHandlerSocket.removeCallbacks(mConnectedThread);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		unregisterReceiver(receiveBluetoothBroadcasts);
		unregisterReceiver(receiveAudioProgressForSeekBar);
		unregisterReceiver(receiveIsServiceIsOn);
		unregisterReceiver(receiveSongChangeFromAudioPlayingService);
		// unregisterReceiver(receiveBluetoothBroadcasts);
		theThreaderHandler.removeCallbacks(this);

	}

	BroadcastReceiver receiveSongChangeFromAudioPlayingService = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			new TaskToFindNewSong().execute();

		}
	};
	BroadcastReceiver receiveIsServiceIsOn = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			isServiceOn = true;

		}
	};

	BroadcastReceiver receiveAudioProgressForSeekBar = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			currentPositionInAudioPlayBack = (intent.getIntExtra("PROGRESS",
					1000));
			// Log.d("GETTING CURRENT POSITION", "GETTING CURRENT POSITION");

		}
	};

	BroadcastReceiver receiveFromAudioPlayingServiceToStopProgress = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getBooleanExtra("PlayProgress", false)) {
				audioPlayBackKeepProgressBarGoing = false;
			} else {
				audioPlayBackKeepProgressBarGoing = true;
			}
		}
	};

	BroadcastReceiver receiveWhetherButtonIsPlayOrPause = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getBooleanExtra("Playing", false))
				bPlayStop.setBackgroundResource(R.drawable.btn_pause);
			else
				bPlayStop.setBackgroundResource(R.drawable.btn_play);
		}
	};
	BroadcastReceiver receiveBluetoothBroadcasts = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent intent) {
			 String action = intent.getAction();
		        // When discovery finds a device
		        if (BluetoothDevice.ACTION_FOUND.equals(action)) {
		            // Get the BluetoothDevice object from the Intent
		            BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
		            // Add the name and address to an array adapter to show in a ListView
		           Toast.makeText(getApplicationContext(), device.getName() + "\n" + device.getAddress(), Toast.LENGTH_LONG).show();
		           Toast.makeText(getApplicationContext(), device.getUuids().toString(), Toast.LENGTH_LONG).show();
		        }

		}

	};

	@Override
	public void onProgressChanged(SeekBar seekBar, int progress,
			boolean fromUser) {
		if (fromUser) {
			sendBroadcast(new Intent(ACTION_SEEKBAR_CHANGE_PROGRESS).putExtra(
					"SEEKBAR_PROGRESS", mSeekBar.getProgress()));
		}
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// TODO Auto-generated method stub

	}

	@Override
	public void run() {

		if (audioPlayBackKeepProgressBarGoing) {
			mSeekBar.setMax((int) currentSong.getDuration());
			mSeekBar.setProgress(theSharedPrefs.getInt(
					theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""),
					mSeekBar.getMax() / 50));
			updatePlayBackTimeDisplays();
			updatePlayBackTimeDisplaysTotalTime();
		} else {
			mSeekBar.setMax(100);
			mSeekBar.setProgress(0);
		}

		theThreaderHandler.postDelayed(this, 1000);
	}

	private void updatePlayBackTimeDisplaysTotalTime() {
		int totalTime = (int) currentSong.getDuration();
		if (totalTime == 0) {

		} else {
			String secs;
			int seconds = totalTime / 1000 % 60;
			if (seconds < 10) {
				secs = "0" + Integer.toString(seconds);
			} else {
				secs = Integer.toString(seconds);
			}

			int minutes = totalTime / 1000 / 60 % 60;
			String min;
			if (minutes < 10) {
				min = "0" + Integer.toString(minutes);
			} else {
				min = Integer.toString(minutes);
			}

			int hours = totalTime / 1000 / 60 / 60;
			String hr;
			if (hours < 10) {
				hr = "0" + Integer.toString(hours);
			} else {
				hr = Integer.toString(hours);
			}
			tvTotalT.setText(hr + ":" + min + ":" + secs);

		}

	}

	private void updatePlayBackTimeDisplays() {
		int totalTime = theSharedPrefs.getInt(
				theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""), 0);

		if (totalTime == 0) {

		} else {

			int seconds = totalTime / 1000 % 60;
			String secs;

			if (seconds < 10) {
				secs = "0" + Integer.toString(seconds);
			} else {
				secs = Integer.toString(seconds);
			}

			int minutes = totalTime / 1000 / 60 % 60;
			String min;
			if (minutes < 10) {
				min = "0" + Integer.toString(minutes);
			} else {
				min = Integer.toString(minutes);
			}

			int hours = totalTime / 1000 / 60 / 60;
			String hr;
			if (hours < 10) {
				hr = "0" + Integer.toString(hours);
			} else {
				hr = Integer.toString(hours);
			}
			tvCurrentT.setText(hr + ":" + min + ":" + secs);

		}
	}

	class TaskToFindNewSong extends AsyncTask<Void, Void, Integer> {
		final String[] mProjection = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_KEY,
				MediaStore.Audio.Media.DURATION };

		private List<SongHolder> ALLOFTHESONGS;

		@Override
		protected Integer doInBackground(Void... params) {
			currentSong = updateList();

			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			initPage();
			tvAlbum.setText(currentSong.getAlbum());
			tvSong.setText(currentSong.getTitle());
			tvArtist.setText(currentSong.getArtist());

			/*
			 * if (currentSong.getAlbumImage() == null) {
			 * 
			 * imView.setBackgroundResource(R.drawable.capture); } else {
			 * Drawable img = Drawable.createFromPath(currentSong
			 * .getAlbumImage());
			 * 
			 * imView.setBackgroundDrawable(img); }
			 */
		}
	}

	public SongHolder updateList() {
		mCursor = getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mProjection, null,
				null, null);

		ArrayList<SongHolder> ALLOFTHESONGS = new ArrayList<SongHolder>();

		mCursor.moveToFirst();

		while (mCursor.moveToNext()) {
			SongHolder s = new SongHolder();
			s.setAlbum(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
			s.setArtist(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
			s.setData(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Media.DATA)));
			s.setId(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Media._ID)));
			s.setTitle(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Media.TITLE)));
			s.setAlbumId(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY)));
			s.setDuration(mCursor.getLong(mCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
			ALLOFTHESONGS.add(s);
		}

		ArrayList<SongHolder> theAlbumListOfSongHolders;
		theAlbumListOfSongHolders = new ArrayList<SongHolder>(
				ALLOFTHESONGS.size());

		mCursorAllAlbumArt = getContentResolver().query(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
				newProjectionForAlbumArt, null, null,
				MediaStore.Audio.Albums._ID);

		mCursorAllAlbumArt.moveToFirst();
		while (mCursorAllAlbumArt.moveToNext()) {
			SongHolder s = new SongHolder();
			s.setArtist(mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));

			s.setAlbumId(mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY)));

			s.setAlbumImage(mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)));
			theAlbumListOfSongHolders.add(s);
		}

		SongHolder filteredArraySong = new SongHolder();

		for (int i = 0; i < ALLOFTHESONGS.size(); i++) {
			if (ALLOFTHESONGS.get(i).getData()
					.equals(theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""))) {
				filteredArraySong = ALLOFTHESONGS.get(i);
			}
		}

		entireAlbum = new ArrayList<SongHolder>();

		// Get the album

		for (int i = 0; i < ALLOFTHESONGS.size(); i++) {
			if (ALLOFTHESONGS.get(i).getAlbumId()
					.equals(filteredArraySong.getAlbumId())) {
				entireAlbum.add(ALLOFTHESONGS.get(i));
			}
		}

		for (int j = 0; j < theAlbumListOfSongHolders.size(); j++) {
			if (filteredArraySong.getAlbumId().toString()
					.equals(theAlbumListOfSongHolders.get(j).getAlbumId())) {
				filteredArraySong.setAlbumImage(theAlbumListOfSongHolders
						.get(j).getAlbumImage());
			}
		}
		for (int i = 0; i < entireAlbum.size(); i++) {
			entireAlbum.get(i).setAlbumImage(filteredArraySong.getAlbumImage());
		}
		return filteredArraySong;
	}

	class ViewPagerAdapter extends FragmentStatePagerAdapter {

		public ViewPagerAdapter(FragmentManager fm) {
			super(fm);
		}

		@Override
		public Fragment getItem(int arg0) {

			return new NowPlayingViewPagerFragments().newInstance(arg0,
					entireAlbum.get(arg0).getAlbumImage());
		}

		@Override
		public int getCount() {
			return entireAlbum.size();
		}

	}

	// CLICKS ON BLUETOOTH - Check to see if device has bluetooth capabilities,
	// if it does then we go ahead and attempt to enable it,
	// if it's enabled refer to onActivityResult
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bBluetoothButton:

			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

			if (mBluetoothAdapter == null) {
			}

			else {
				if (!mBluetoothAdapter.isEnabled()) {
					Intent enableBtIntent = new Intent(
							BluetoothAdapter.ACTION_REQUEST_ENABLE);
					startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);

				}else {
					Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
							.getBondedDevices();
					hashBluetoothList = new HashMap<String, String>();
					List<String> bluetoothDevices = new ArrayList<String>();
					if (pairedDevices.size() > 0) {

						for (BluetoothDevice device : pairedDevices) {
							hashBluetoothList
									.put(device.getName(), device.getAddress());

							bluetoothDevices.add(device.getName());

						}

					}
					alert_dialog_bluetooth_paired_devices(bluetoothDevices,
							pairedDevices);
				}
			}

			break;
		}
	}

	// onActivityResult - We come back after bluetooth is Enabled,
	// Grab all previously bonded Devices (pairedDevices),
	// store their name and addresses in a Hash (hashBluetoothList),
	// and store their names in a List(bluetoothDevices)
	// kick off a AlertDialog to display Names and Addresses
	// (alert_dialog_bluetooth_paired_devices)
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		// if user said yes and Bluetooth is now on
		if (resultCode == RESULT_OK) {

			Set<BluetoothDevice> pairedDevices = mBluetoothAdapter
					.getBondedDevices();
			hashBluetoothList = new HashMap<String, String>();
			List<String> bluetoothDevices = new ArrayList<String>();
			if (pairedDevices.size() > 0) {

				for (BluetoothDevice device : pairedDevices) {
					hashBluetoothList
							.put(device.getName(), device.getAddress());

					bluetoothDevices.add(device.getName());

				}

			}
			alert_dialog_bluetooth_paired_devices(bluetoothDevices,
					pairedDevices);
		} else if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(), "Must enable Bluetooth",
					Toast.LENGTH_LONG).show();
			if(mConnectedThread != null)
				mHandlerSocket.removeCallbacks(mConnectedThread);
			

		}
	}

	// We have stored pairedDevices, hashBluetoothList, and bluetoothDevices,
	// it's time for the user to select one of the devices.
	private void alert_dialog_bluetooth_paired_devices(
			final List<String> listOfDevices, Set<BluetoothDevice> pairDevices) {
		// Need it to be a list so we can get exact entry, but API requires we
		// get Devices with a Set
		final List<BluetoothDevice> pairedDevices = new ArrayList<BluetoothDevice>();

		for (Object entry : pairDevices) {
			pairedDevices.add((BluetoothDevice) entry);
		}
		if (hashBluetoothList.size() > 0) {

			final AlertDialog.Builder alertDialog = new AlertDialog.Builder(
					NowPlaying.this);
			LayoutInflater inflater = getLayoutInflater();
			View convertView = (View) inflater.inflate(R.layout.toast, null);
			alertDialog.setView(convertView);
			alertDialog.setTitle("List");
			ListView lv = (ListView) convertView.findViewById(R.id.listView1);
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
					android.R.layout.simple_list_item_1, listOfDevices);
			lv.setAdapter(adapter);
			// We have selected a Device
			lv.setOnItemClickListener(new OnItemClickListener() {

				@Override
				public void onItemClick(AdapterView<?> arg0, View arg1,
						int position, long arg3) {
					String s = listOfDevices.get(position);
					// Display the Mac address of the device selected
					Toast.makeText(getApplicationContext(),
							hashBluetoothList.get(s), Toast.LENGTH_LONG).show();
					// grab the UUID for the device selected
					UUID = pairedDevices.get(position).getUuids();
					// kick off
					// --------------------------------------------------------------------------------------------
					// new TaskFirstBluetoothAttempt(hashBluetoothList.get(s),
					// pairedDevices.get(position), alertDialog).execute();
					new TaskBluetoothServerSocket(pairedDevices.get(position))
							.execute();

				}
			});
			alertDialog.show();
		}
	}

	// ServerSocket route. Because my car always initiates it, so I am sure my
	// phone receives it.
	// I always hit 'Connect' on my car.
	// #1 Get BluetoothServerSocket
	// #2 Start listening for connection requests by calling accept()
	// #3 Close connection
	// #4
	class TaskBluetoothServerSocket extends
			AsyncTask<Boolean, Integer, Boolean> {
		private final BluetoothServerSocket mServerSocket;
		private BluetoothDevice mBDevice;
		// Use this as a way of keeping track of a failure to get a ServerSocket
		// and listen for a Connection
		private Boolean send_toast_failure = false;
		private Boolean isSocketConnected = false;
		private Boolean somethingWentWrongWithServerSocketAcceptMethod = false;

		public TaskBluetoothServerSocket(BluetoothDevice device) {
			// Need temporary because mServerSocket is final
			BluetoothServerSocket tempSocket = null;
			// Passing the device down
			mBDevice = device;
			// setting the Booleans
			somethingWentWrongWithServerSocketAcceptMethod = false;
			isSocketConnected = false;
			send_toast_failure = false;

			try {
				tempSocket = mBluetoothAdapter
						.listenUsingInsecureRfcommWithServiceRecord(
								"AudioPlayerApp",
								mBDevice.getUuids()[0].getUuid());
			} catch (IOException e) {
				tempSocket = null;
				send_toast_failure = true;
				e.printStackTrace();
			}
			mServerSocket = tempSocket;
		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			BluetoothSocket socket = null;
			// Keep listening until exception occurs or a socket is returned
			while (true) {
				try {
					socket = mServerSocket.accept();
					// this will tell us if we are connected
					if (socket.isConnected()) {
						isSocketConnected = true;
					}
				} catch (IOException e) {
					somethingWentWrongWithServerSocketAcceptMethod = true;
					break;
				}

				// If a connection was accepted
				if (socket != null) {
					// Do work to manage the connection (in a seperate thread)
					manageConnectedSocket(socket);
					try {
						mServerSocket.close();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}

			return null;
		}

		public void cancel() {
			try {
				mServerSocket.close();
			} catch (IOException e) {
			}
		}

		// need to kick off the connection with this socket
		private void manageConnectedSocket(BluetoothSocket socket) {
			mConnectedThread = new ConnectedThread(socket);
			mHandlerSocket.removeCallbacks(mConnectedThread);
			mHandlerSocket.postDelayed(mConnectedThread, 100);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (isSocketConnected)
				Toast.makeText(getApplicationContext(), "Connected",
						Toast.LENGTH_LONG).show();

		}

	}

	private class ConnectedThread extends Thread {
		private final BluetoothSocket mmSocket;
		private final InputStream mmInStream;

		public ConnectedThread(BluetoothSocket socket) {
			mmSocket = socket;
			InputStream tmpIn = null;

			// Get the input and output streams, using temp objects because
			// member streams are final
			try {
				tmpIn = socket.getInputStream();

			} catch (IOException e) {
			}

			mmInStream = tmpIn;

		}

		public void run() {
			byte[] buffer = new byte[1024]; // buffer store for the stream
			int bytes; // bytes returned from read()

			// Keep listening to the InputStream until an exception occurs
			while (true) {
				try {
					// Read from the InputStream
					bytes = mmInStream.read(buffer);
					// Send the obtained bytes to the UI activity
					Message msg = new Message();
					msg.arg1 = bytes; // for integer data
					mHandlerSocket.obtainMessage(bytes).sendToTarget();
				} catch (IOException e) {
					break;
				}
			}
		}

		/* Call this from the main activity to shutdown the connection */
		public void cancel() {
			try {
				mmSocket.close();
			} catch (IOException e) {
			}
		}
	}

	static class HandlerSocket extends Handler {
		private Context context;
		public HandlerSocket(Context context){
			this.context = context;
		}
		@Override
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			int value = msg.arg1;
			Log.d("whats", Integer.toString(value));
			Toast.makeText(context, Integer.toString(value), Toast.LENGTH_LONG).show();
		}

	};
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	

	// If we are to Connect as a Server - we will hold the
	// BluetoothServerSocket,
	// and listen for incoming connection requests and when one is accepted
	// provide a connected bluetoothSocket

	class TaskFirstBluetoothAttempt extends
			AsyncTask<Boolean, Integer, Boolean> {

		private BluetoothSocket mmSocket;
		private BluetoothDevice mmDevice;
		private String deviceMacAddress;
		Boolean failed = false;
		Boolean justConnected = false;
		boolean justDisconnected = false;

		public TaskFirstBluetoothAttempt(String deviceMacAddress,
				BluetoothDevice device, AlertDialog.Builder alertDialog) {
			this.deviceMacAddress = deviceMacAddress;
			BluetoothSocket tmp = null;
			mmDevice = device;
			// Get a BluetoothSocket to connect with the given BluetoothDevice
			try {
				// MY_UUID is the app's UUID string, also used by the server
				// code
				tmp = device.createInsecureRfcommSocketToServiceRecord(device
						.getUuids()[0].getUuid());
			} catch (IOException e) {
			}
			mmSocket = tmp;

		}

		@Override
		protected Boolean doInBackground(Boolean... params) {
			// Cancel discovery because it will slow down the connection
			mBluetoothAdapter.cancelDiscovery();

			try {
				// Connect the device through the socket. This will block
				// until it succeeds or throws an exception
				mmSocket.connect();
				failed = false;
			} catch (IOException connectException) {
				// Unable to connect; close the socket and get out
				failed = true;
				try {
					mmSocket.close();
				} catch (IOException closeException) {
				}
			}

			// Do work to manage the connection (in a separate thread)
			// TEMPORARILY NOT DOING THIS CALL
			// manageConnectedSocket(mmSocket);

			return null;
		}

		private void manageConnectedSocket(BluetoothSocket mmSocket2) {
			// TESTING STUFF

			// Get the default adapter

			// Establish connection to the proxy.
			mBluetoothAdapter.getProfileProxy(getApplicationContext(),
					mProfileListener, BluetoothProfile.HEADSET);

			// ... call functions on mBluetoothHeadset

			// Close proxy connection after use.
			// mBluetoothAdapter.closeProfileProxy(mBluetoothHeadset);
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			if (failed)
				Toast.makeText(getApplicationContext(), "Unable to Connect",
						Toast.LENGTH_LONG).show();
			else if (!failed)
				Toast.makeText(getApplicationContext(), "Connected",
						Toast.LENGTH_LONG).show();
			if (justConnected)
				Toast.makeText(getApplicationContext(),
						"Profile does equal Headset Class.", Toast.LENGTH_LONG)
						.show();
			if (justDisconnected)
				Toast.makeText(getApplicationContext(),
						"Profile does equal headset and dissconnected.",
						Toast.LENGTH_LONG).show();

		}

		private BluetoothProfile.ServiceListener mProfileListener = new BluetoothProfile.ServiceListener() {
			BluetoothHeadset mBluetoothHeadset;

			public void onServiceConnected(int profile, BluetoothProfile proxy) {
				if (profile == BluetoothProfile.HEADSET) {
					mBluetoothHeadset = (BluetoothHeadset) proxy;
					justConnected = true;
				}
			}

			public void onServiceDisconnected(int profile) {
				if (profile == BluetoothProfile.HEADSET) {
					mBluetoothHeadset = null;
					justDisconnected = true;
				}
			}
		};

	}
}
