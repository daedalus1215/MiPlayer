package com.example.larry.miplayer;

import java.util.ArrayList;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.TextView;
import com.example.larry.miplayer.SongHolder;

public class ControlPanelMiniFragment extends Fragment implements
		OnClickListener, Runnable {

	// Preferences - the file we store all our preferences
	final private static String PREFS_STORED_PROGRESSION = "PREFS_STORED_PROGRESSION";
	// PREFERENCE KEY FOR CURRENTLY PLAYING SONGS
	final private static String PREF_SONG_COCKED_NOW = "PREF_SONG_COCKED_NOW";

	/**
	 * This will help us set whether we have a pause icon or a play icon.
	 * "Playing" is the extra boolean key, it will tell us if audioPlayer is
	 * playing (true) or if it is paused (false)
	 */
	final static String ACTION_IS_MEDIA_PLAYING = "ACTION_IS_MEDIA_PLAYING";
	private SharedPreferences theSharedPrefs;
	private SharedPreferences.Editor theEditor;
	private SeekBar seekBar1;
	private ImageButton bPlay, bBack, bFor, bRemove;
	private TextView tvArtist, tvTitle;
	private Activity activity;
	private boolean isServiceOn = false;
	private int currentPositionInAudioPlayBack = 0;
	private boolean audioPlayBackKeepProgressBarGoing = false;
	private Handler theThreaderHandler;
	private TextView tvTotalT, tvCurrentT;

	ArrayList<SongHolder> theAlbumListOfSongHolders;
	ArrayList<SongHolder> ALLOFTHESONGS = new ArrayList<SongHolder>();
	SongHolder currentSong;
	private Cursor mCursor;
	private Cursor mCursorAllAlbumArt;
	private String isthereASongSaved;
	private static final String[] newProjectionForAlbumArt = {
			MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_KEY,
			MediaStore.Audio.Albums.ALBUM_ART };

	private static final String[] mProjection = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM_KEY, MediaStore.Audio.Media.DURATION };

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

	@Override
	public void onDestroyView() {
		super.onDestroyView();

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

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		isServiceOn = false;
		activity = getActivity();
		currentSong = new SongHolder();
		initPrefs();
		initReceivers();
		initInfo();
		activity.sendBroadcast(new Intent(
				AudioPlayingService.ACTION_ASK_SERVICE_ON));
		audioPlayBackKeepProgressBarGoing = true;
		theThreaderHandler = new Handler();
	}

	private void initReceivers() {
		activity.registerReceiver(receiveSongChangeFromAudioPlayingService,
				new IntentFilter(NowPlaying.ACTION_CHANGING_SONG));
		activity.registerReceiver(receiveWhetherButtonIsPlayOrPause,
				new IntentFilter(ACTION_IS_MEDIA_PLAYING));
		activity.registerReceiver(receiveAudioProgressForSeekBar,
				new IntentFilter(NowPlaying.BROADCAST_AUDIO_POSITION));
		activity.registerReceiver(receiveSongChangeFromAudioPlayingService,
				new IntentFilter(NowPlaying.ACTION_CHANGING_SONG));
		activity.registerReceiver(receiveIsServiceIsOn, new IntentFilter(
				NowPlaying.ACTION_IS_SERVICE_ON));

	}

	private void initInfo() {
		// new TaskFindSong().execute();
	}

	private void initPrefs() {
		theSharedPrefs = activity.getSharedPreferences(
				PREFS_STORED_PROGRESSION, 0);
		theEditor = theSharedPrefs.edit();
		isthereASongSaved = theSharedPrefs.getString(PREF_SONG_COCKED_NOW, "NO");
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.control_center_mini, null, false);

		bPlay = (ImageButton) v.findViewById(R.id.bPlayStop);
		bPlay.setOnClickListener(this);

		bBack = (ImageButton) v.findViewById(R.id.bRew);
		bBack.setOnClickListener(this);

		bFor = (ImageButton) v.findViewById(R.id.bFor);
		bFor.setOnClickListener(this);

		bRemove = (ImageButton) v.findViewById(R.id.bRemove);
		bRemove.setOnClickListener(this);

		tvTitle = (TextView) v.findViewById(R.id.tvTitleccm);

		tvArtist = (TextView) v.findViewById(R.id.tvArtistccm);
		seekBar1 = (SeekBar) v.findViewById(R.id.seekBar1);

		tvCurrentT = (TextView) v.findViewById(R.id.tvCurrentTime);
		tvTotalT = (TextView) v.findViewById(R.id.tvTotalTime);
		return v;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		activity.unregisterReceiver(receiveSongChangeFromAudioPlayingService);
		activity.unregisterReceiver(receiveWhetherButtonIsPlayOrPause);
		activity.unregisterReceiver(receiveAudioProgressForSeekBar);
		activity.unregisterReceiver(receiveIsServiceIsOn);

		try {
			activity.unregisterReceiver(receiveSongChangeFromAudioPlayingService);

		} catch (IllegalArgumentException e) {

		}
		audioPlayBackKeepProgressBarGoing = false;
		theThreaderHandler.removeCallbacks(this);
	}

	@Override
	public void onResume() {
		super.onResume();
		initReceivers();
		initSeekBar();
		initHandler();
		activity.sendBroadcast(new Intent(
				AudioPlayingService.ACTION_ASK_SERVICE_ON));
		if(!isthereASongSaved.equals("NO"))
			new TaskFindSong().execute();
	}

	@Override
	public void onClick(View v) {
		activity.sendBroadcast(new Intent(
				AudioPlayingService.ACTION_ASK_SERVICE_ON));
		switch (v.getId()) {

		case R.id.bPlayStop:
			if (isServiceOn) {
				activity.sendBroadcast(new Intent(
						AudioPlayingService.ACTION_PLAY_STOP).putExtra(
						"NEW_SONG", false));
			} else {
				activity.startService(new Intent(activity,
						AudioPlayingService.class));
				isServiceOn = true;
			}
			break;

		case R.id.bFor:
			activity.sendBroadcast(new Intent(NowPlaying.ACTION_FOR_BACK)
					.putExtra("FORWARD", true));
			break;

		case R.id.bRew:
			activity.sendBroadcast(new Intent(NowPlaying.ACTION_FOR_BACK)
					.putExtra("FORWARD", false));
			break;

		case R.id.bRemove:
			// Go to NOWPlaying
			Activity activity = getActivity();
			Intent letsGo = new Intent(activity, NowPlaying.class);
			if (isServiceOn) {
				//activity.sendBroadcast(new Intent(NowPlaying.ACTION_PLAY_STOP)
						//.putExtra("NEW_SONG", true));
			} else {
				activity.startService(new Intent(activity,
						AudioPlayingService.class));
			}
			startActivity(letsGo);
			break;
		}
	}

	// this will come from the activity
	void notifyFragmentIfServiceIsOnOrOff(boolean answer) {
		isServiceOn = answer;
	}

	class TaskFindSong extends AsyncTask<Void, Void, Integer> {

		@Override
		protected Integer doInBackground(Void... params) {
			currentSong = updateList();
			return null;
		}

		@Override
		protected void onPostExecute(Integer result) {
			super.onPostExecute(result);
			tvTitle.setText(currentSong.getTitle() + " - ");
			tvArtist.setText(currentSong.getArtist());
			if (currentSong.getAlbumImage() == null) {

				bRemove.setBackgroundResource(R.drawable.capture);
			} else {
				Drawable img = Drawable.createFromPath(currentSong
						.getAlbumImage());

				bRemove.setBackgroundDrawable(img);
			}
		}

	}

	public SongHolder updateList() {
		mCursor = activity.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mProjection, null,
				null, null);

		ALLOFTHESONGS = new ArrayList<SongHolder>();

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

		theAlbumListOfSongHolders = new ArrayList<SongHolder>(
				ALLOFTHESONGS.size());

		mCursorAllAlbumArt = getActivity().getContentResolver().query(
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

		SongHolder filteredArraySongs = new SongHolder();

		for (int i = 0; i < ALLOFTHESONGS.size(); i++) {
			if (ALLOFTHESONGS.get(i).getData()
					.equals(theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""))) {
				filteredArraySongs = ALLOFTHESONGS.get(i);
			}
		}
		

			for (int j = 0; j < theAlbumListOfSongHolders.size(); j++) {
				if (filteredArraySongs.getAlbumId().toString()
						.equals(theAlbumListOfSongHolders.get(j).getAlbumId())) {
					filteredArraySongs.setAlbumImage(theAlbumListOfSongHolders
							.get(j).getAlbumImage());
				}
			}
		
		return filteredArraySongs;
	}

	BroadcastReceiver receiveSongChangeFromAudioPlayingService = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			new TaskFindSong().execute();
		}
	};

	BroadcastReceiver receiveWhetherButtonIsPlayOrPause = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getBooleanExtra("Playing", false))
				bPlay.setBackgroundResource(R.drawable.btn_pause);
			else
				bPlay.setBackgroundResource(R.drawable.btn_play);
		}
	};

	// BELOW RECEIVERS DEAL WITH SEEKBAR ALERTS
	BroadcastReceiver receiveAudioProgressForSeekBar = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			currentPositionInAudioPlayBack = (intent.getIntExtra("PROGRESS",
					1000));
			// Log.d("GETTING CURRENT POSITION", "GETTING CURRENT POSITION");

		}
	};
	BroadcastReceiver receiveIsServiceIsOn = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			isServiceOn = true;

		}
	};

	@Override
	public void run() {
		if (audioPlayBackKeepProgressBarGoing) {
			seekBar1.setMax((int) currentSong.getDuration());
			seekBar1.setProgress(theSharedPrefs.getInt(
					theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""),
					seekBar1.getMax() / 50));
			updatePlayBackTimeDisplays();
			updatePlayBackTimeDisplaysTotalTime();
		} else {
			seekBar1.setMax(100);
			seekBar1.setProgress(0);
		}
		theThreaderHandler.postDelayed(this, 1000);
	}

	private void initSeekBar() {
		seekBar1.setMax((int) currentSong.getDuration());
		seekBar1.setProgress((int) theSharedPrefs.getInt(
				theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""), 0));
	}

	private void initHandler() {

		theThreaderHandler.removeCallbacks(this);
		theThreaderHandler.postDelayed(this, 1000);
	}
}
