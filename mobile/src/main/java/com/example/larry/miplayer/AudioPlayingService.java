package com.example.larry.miplayer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.example.larry.miplayer.SongHolder;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.media.AudioManager;
import android.media.AudioManager.OnAudioFocusChangeListener;
import android.media.MediaPlayer;

import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;

import android.os.AsyncTask;
import android.os.Handler;
import android.os.IBinder;
import android.provider.MediaStore;

import android.support.v4.app.NotificationCompat;

import android.util.Log;

import android.widget.Toast;

public class AudioPlayingService extends Service implements
		MediaPlayer.OnPreparedListener, Runnable, OnAudioFocusChangeListener,
		OnCompletionListener, OnErrorListener {
	private final static String TAG = "AudioPlayingService";

	// Preferences - the file we store all our preferences
	final private static String PREFS_STORED_PROGRESSION = "PREFS_STORED_PROGRESSION";
	// PREFERENCE KEY FOR CURRENTLY PLAYING SONGS
	final private static String PREF_SONG_COCKED_NOW = "PREF_SONG_COCKED_NOW";

	/**
	 * ACTION_FOR_BACK : Alerts or sends of forward or backward in audioProgress
	 * FORWARD = boolean; forward(true) or backward(false)
	 */
	final private static String ACTION_FOR_BACK = "ACTION_FOR_BACK";
	final static String ACTION_ASK_SERVICE_ON = "ACTION_ASK_SERVICE_ON";
	/**
	 * 
	 * ACTION_PLAY_STOP : used for FILTER will be accompanied with newSong
	 * (true) or !newSong (false). Will tell this AudioPlayer if we should grab
	 * the new "PREF_SONG_COCKED_NOW" or if we should just play what we have
	 * cocked. boolean KEY = NEW_SONG
	 */
	final public static String ACTION_PLAY_STOP = "ACTION_PLAY_STOP";
	/**
	 * ACTION_SEEKBAR_CHANGE_PROGRESS : is a title we use with our INTENT : will
	 * be accompanied by a integer, to represent the new position of the track.
	 * SEEKBAR_PROGRESS
	 */
	final private static String ACTION_SEEKBAR_CHANGE_PROGRESS = "ACTION_SEEKBAR_CHANGE_PROGRESS";
	final static String ACTION_PLAYER_PAUSE_TWICE = "ACTION_PLAYER_PAUSE_TWICE";
	final static String ACTION_PLAYER_PAUSE = "ACTION_PLAYER_PAUSE";
	/**
	 * ACTION_UNPLUGGED_HEADPHONES : sent from the MusicIntentReceiver telling
	 * us headphones have been unplugged
	 */
	final static String ACTION_UNPLUGGED_HEADPHONES = "ACTION_UNPLUGGED_HEADPHONES";

	/**
	 * TURNED_THE_PAGE : tells service that the page was turned. "Forward"
	 * boolean key: if true then we use nextSong (we set in task) if false then
	 * we use previousSong (we set in task)
	 */
	final static String TURNED_THE_PAGE = "TURNED_THE_PAGE";

	private static final int NOTIFICATION_PLAYING_ID = 1;
	private MediaPlayer mMediaPlayer;
	private AudioManager mAudioManager;
	private boolean wasPlayingWatchForInterruptionsLikePhoneCallsAndTextMessages = false;
	private SharedPreferences theSharedPrefs;
	private SharedPreferences.Editor theEditor;
	private Handler theThreadHandler;
	private Cursor mCursor;
	private String nextSong = null;
	private String previousSong = null;
	private String theCurrentSong;
	private String currentArtistForNotification;
	private String currentSongForNotification;
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {

		initMediaPlayer();
		initThread();
		return START_STICKY;
	}

	private void initThread() {
		theThreadHandler.removeCallbacks(this);
		theThreadHandler.postDelayed(this, 1000);

	}

	private void initPreferences() {
		theSharedPrefs = getSharedPreferences(PREFS_STORED_PROGRESSION, 0);
		theEditor = theSharedPrefs.edit();
	}

	private void initBroadcasts() {
		registerReceiver(receiveIfServiceIsOn, new IntentFilter(
				ACTION_ASK_SERVICE_ON));
		registerReceiver(receivePlayOrStop, new IntentFilter(ACTION_PLAY_STOP));
		registerReceiver(receiveSeekBarChange, new IntentFilter(
				ACTION_SEEKBAR_CHANGE_PROGRESS));
		registerReceiver(receiveBackForwardCommand, new IntentFilter(
				ACTION_FOR_BACK));
		registerReceiver(receiveHeadphoneInput, new IntentFilter(
				ACTION_PLAYER_PAUSE));
		registerReceiver(receiveUnPlugHeadphones, new IntentFilter(
				ACTION_UNPLUGGED_HEADPHONES));
		registerReceiver(receivePageTurn, new IntentFilter(TURNED_THE_PAGE));
		registerReceiver(receiveHeadphoneInputMultipleTimes, new IntentFilter(
				ACTION_PLAYER_PAUSE_TWICE));
	}

	private void initAudioManager() {
		wasPlayingWatchForInterruptionsLikePhoneCallsAndTextMessages = true;
		mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);

		int result = mAudioManager.requestAudioFocus(this,
				AudioManager.STREAM_MUSIC, AudioManager.AUDIOFOCUS_GAIN);
		if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {

			mAudioManager.registerMediaButtonEventReceiver(new ComponentName(
					getPackageName(), RemoteControlReceiver.class.getName()));

		}
	}

	@Override
	public void onCreate() {
		super.onCreate();
		Log.d(TAG, "onCreate()");
		
		//initNotification();
		initBroadcasts();
		initPreferences();
		// mMediaPlayer = new MediaPlayer();

		theThreadHandler = new Handler();
		initAudioManager();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mAudioManager.abandonAudioFocus(this);
		unregisterReceiver(receiveIfServiceIsOn);
		unregisterReceiver(receivePlayOrStop);
		unregisterReceiver(receiveSeekBarChange);
		unregisterReceiver(receiveBackForwardCommand);
		unregisterReceiver(receiveHeadphoneInput);
		unregisterReceiver(receiveUnPlugHeadphones);
		unregisterReceiver(receivePageTurn);
		unregisterReceiver(receiveHeadphoneInputMultipleTimes);
		mAudioManager.unregisterMediaButtonEventReceiver(new ComponentName(
				getPackageName(), RemoteControlReceiver.class.getName()));
		theThreadHandler.removeCallbacks(this);
		// if (mMediaPlayer.isPlaying()) {
		mMediaPlayer.stop();
		mMediaPlayer.release();
		// }
	}
	private void updateNotification(){
		if(mMediaPlayer.isPlaying()){
			initNotificationPlay();
		}else {
			initNotificationStop();
		}
	}
	private void initNotificationStop() {
		Log.d(TAG, "initNotification()");

		Intent intentForPending = new Intent(getApplicationContext(),
				MainActivity.class);
		intentForPending.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_RECEIVER_REPLACE_PENDING);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 2,
				intentForPending, PendingIntent.FLAG_CANCEL_CURRENT);

		// Rewind icon to rewind where we are
		Intent intentForPendingRewind = new Intent(NowPlaying.ACTION_FOR_BACK).putExtra("FORWARD", false);
		PendingIntent pendingIntentRewind = PendingIntent.getBroadcast(this, 12, intentForPendingRewind, 0);
		
		// Play  icon 
		Intent intentForStop = new Intent(NowPlaying.ACTION_PLAY_STOP).putExtra(
				"NEW_SONG", false);
		PendingIntent pendingIntentStop = PendingIntent.getBroadcast(this, 13, intentForStop, 0);
		
		// Forward Icon
		Intent intentForForward = new Intent(NowPlaying.ACTION_FOR_BACK)
		.putExtra("FORWARD", true);
		PendingIntent pendingIntentForward = PendingIntent.getBroadcast(this, 11, intentForForward, 0);
		
		
		
		NotificationManager notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(
				this);

		notiBuilder.setContentText(currentArtistForNotification).setContentTitle(currentSongForNotification)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pendingIntent)
				.addAction(R.drawable.ic_back_noti, "", pendingIntentRewind)
				.addAction(R.drawable.ic_play_noti, "", pendingIntentStop)
				.addAction(R.drawable.ic_for_noti, "", pendingIntentForward);
			

		notiManager.notify(NOTIFICATION_PLAYING_ID, notiBuilder.build());
		final Notification notifcation = notiBuilder.build();
		startForeground(NOTIFICATION_PLAYING_ID, notifcation);			
	}

	private void initNotificationPlay() {
		Log.d(TAG, "initNotification()");

		Intent intentForPending = new Intent(getApplicationContext(),
				MainActivity.class);
		intentForPending.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP
				| Intent.FLAG_RECEIVER_REPLACE_PENDING);
		PendingIntent pendingIntent = PendingIntent.getActivity(this, 5,
				intentForPending, PendingIntent.FLAG_CANCEL_CURRENT);

		// Rewind icon to rewind where we are
		Intent intentForPendingRewind = new Intent(NowPlaying.ACTION_FOR_BACK).putExtra("FORWARD", false);
		PendingIntent pendingIntentRewind = PendingIntent.getBroadcast(this, 12, intentForPendingRewind, 0);
		
		// Play  icon 
		Intent intentForPlaying = new Intent(NowPlaying.ACTION_PLAY_STOP).putExtra(
				"NEW_SONG", false);
		PendingIntent pendingIntentPlay = PendingIntent.getBroadcast(this, 13, intentForPlaying, 0);
		
		// Forward Icon
		Intent intentForForward = new Intent(NowPlaying.ACTION_FOR_BACK)
		.putExtra("FORWARD", true);
		PendingIntent pendingIntentForward = PendingIntent.getBroadcast(this, 11, intentForForward, 0);
		
		
		
		NotificationManager notiManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		NotificationCompat.Builder notiBuilder = new NotificationCompat.Builder(
				this);

		notiBuilder.setContentText(currentArtistForNotification).setContentTitle(currentSongForNotification)
				.setSmallIcon(R.drawable.ic_launcher)
				.setContentIntent(pendingIntent)
				.addAction(R.drawable.ic_back_noti, "", pendingIntentRewind)
				.addAction(R.drawable.ic_pause_noti, "", pendingIntentPlay)
				.addAction(R.drawable.ic_for_noti, "", pendingIntentForward);
			

		notiManager.notify(NOTIFICATION_PLAYING_ID, notiBuilder.build());
		final Notification notifcation = notiBuilder.build();
		startForeground(NOTIFICATION_PLAYING_ID, notifcation);	
	}

	private void initMediaPlayer() {
		
		Log.d(TAG, "initMediaPlayer()");
		if (mMediaPlayer == null)
			mMediaPlayer = new MediaPlayer();
		else
			mMediaPlayer.reset();
		mMediaPlayer.setOnCompletionListener(this);
		mMediaPlayer.setOnPreparedListener(this);
		mMediaPlayer.setOnErrorListener(this);
		theCurrentSong = (theSharedPrefs.getString(PREF_SONG_COCKED_NOW,
				"nothing"));
		sendBroadcast(new Intent(NowPlaying.ACTION_CHANGING_SONG));
		if (!theCurrentSong.equals("nothing")) {

			try {
				new FindAlbumTask().execute();
				mMediaPlayer.setDataSource(theCurrentSong);

			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (SecurityException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalStateException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			mMediaPlayer.prepareAsync();

		} else {
			Toast.makeText(getApplicationContext(), "Not a saved song", 1)
					.show();
		}
	}



	@Override
	public IBinder onBind(Intent intent) {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * receivePlayOrStop : will handle if we initiated a new song or an already
	 * cocked one, if it's not a new song and mediaPlayer is playing we pause.
	 * if its not a new song, and mediaPlayer is not playing, and !null, restart
	 * if it is a new song and mediaPlayer is playing we stop and restart if it
	 * is a new song and mediaPlayer is paused we just end it and restart.
	 */
	BroadcastReceiver receivePlayOrStop = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean isNewSong = intent.getBooleanExtra("NEW_SONG", false);
			if (isNewSong) {
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
					mMediaPlayer.stop();
					mMediaPlayer.reset();
					mMediaPlayer = null;
					initMediaPlayer();
				} else {
					mMediaPlayer.pause();
					mMediaPlayer.stop();
					mMediaPlayer.reset();
					mMediaPlayer = null;
					initMediaPlayer();
				}
			} else { // if its not a new song
				if (mMediaPlayer.isPlaying()) {
					mMediaPlayer.pause();
					wasPlayingWatchForInterruptionsLikePhoneCallsAndTextMessages = false;
					sendBroadcastTellingEveryoneWeArePlayingOrNotForPausePlayButtonDrawableSetting();
				} else {
					mMediaPlayer.start();
					
					sendBroadcastTellingEveryoneWeArePlayingOrNotForPausePlayButtonDrawableSetting();
				}
			}
			updateNotification();
		}

	};
	BroadcastReceiver receiveHeadphoneInputMultipleTimes = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition() + 30000);
				theEditor.putInt(
						theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""),
						mMediaPlayer.getCurrentPosition());
				theEditor.commit();
			}
		}
	};

	BroadcastReceiver receiveHeadphoneInput = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mMediaPlayer.isPlaying()) {
				mMediaPlayer.pause();
				updateNotification();
				wasPlayingWatchForInterruptionsLikePhoneCallsAndTextMessages = false;
				sendBroadcastTellingEveryoneWeArePlayingOrNotForPausePlayButtonDrawableSetting();
			} else {
				initMediaPlayer();
			}
		}
	};

	BroadcastReceiver receiveQuestionWhetherWeArePlaying = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			sendBroadcastTellingEveryoneWeArePlayingOrNotForPausePlayButtonDrawableSetting();
		}
	};

	void sendBroadcastTellingEveryoneWeArePlayingOrNotForPausePlayButtonDrawableSetting() {
		Intent intentSendResponse = new Intent(
				ControlPanelMiniFragment.ACTION_IS_MEDIA_PLAYING);
		if (mMediaPlayer.isPlaying()) {
			intentSendResponse.putExtra("Playing", true);
		} else {
			intentSendResponse.putExtra("Playing", false);
		}
		sendBroadcast(intentSendResponse);
	}

	BroadcastReceiver receiveIfServiceIsOn = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			sendBroadcast(new Intent(NowPlaying.ACTION_IS_SERVICE_ON));
			sendBroadcastTellingEveryoneWeArePlayingOrNotForPausePlayButtonDrawableSetting();
		}
	};
	BroadcastReceiver receiveSeekBarChange = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mMediaPlayer != null) {
				mMediaPlayer
						.seekTo(intent.getIntExtra("SEEKBAR_PROGRESS", 500));
			}
		}

	};

	BroadcastReceiver receiveUnPlugHeadphones = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mMediaPlayer.isPlaying()) {
				sendBroadcast(new Intent(NowPlaying.STOP_SEEKBAR).putExtra(
						"PlayProgress", false));
				savePlayBackStatus();
				wasPlayingWatchForInterruptionsLikePhoneCallsAndTextMessages = false;
				mMediaPlayer.pause();
				updateNotification();
				sendBroadcastTellingEveryoneWeArePlayingOrNotForPausePlayButtonDrawableSetting();
			}
		}
	};

	BroadcastReceiver receivePageTurn = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			boolean forward = intent.getBooleanExtra("Forward", false);
			if (forward) {
				sendBroadcast(new Intent(NowPlaying.STOP_SEEKBAR).putExtra(
						"PlayProgress", false));
				mMediaPlayer.reset();
				mMediaPlayer.release();
				mMediaPlayer = null;
				theEditor.putInt(
						theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""), 0);
				theEditor.commit();
				if (nextSong != null) {
					theEditor.putString(PREF_SONG_COCKED_NOW, nextSong);
					theEditor.commit();
					initMediaPlayer();
				}

			} else if (!forward) {
				sendBroadcast(new Intent(NowPlaying.STOP_SEEKBAR).putExtra(
						"PlayProgress", false));
				mMediaPlayer.reset();
				mMediaPlayer.release();
				mMediaPlayer = null;
				theEditor.putInt(
						theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""), 0);
				theEditor.commit();
				if (previousSong != null) {
					theEditor.putString(PREF_SONG_COCKED_NOW, previousSong);
					theEditor.commit();

					initMediaPlayer();
				}
			}

		}
	};

	BroadcastReceiver receiveBackForwardCommand = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (mMediaPlayer != null) {
				if (!intent.getBooleanExtra("FORWARD", false)) {
					mMediaPlayer
							.seekTo(mMediaPlayer.getCurrentPosition() - 15000);
					theEditor.putInt(
							theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""),
							mMediaPlayer.getCurrentPosition());
					theEditor.commit();
				} else {
					mMediaPlayer
							.seekTo(mMediaPlayer.getCurrentPosition() + 30000);
					theEditor.putInt(
							theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""),
							mMediaPlayer.getCurrentPosition());
					theEditor.commit();
				}
			}
		}

	};

	@Override
	public void onPrepared(MediaPlayer mp) {
		mMediaPlayer.seekTo(theSharedPrefs.getInt(
				theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""), 0));

		wasPlayingWatchForInterruptionsLikePhoneCallsAndTextMessages = true;
		sendBroadcast(new Intent(NowPlaying.STOP_SEEKBAR).putExtra(
				"PlayProgress", true));
		mMediaPlayer.start();
		// mMediaPlayer.setOnCompletionListener(this);
		sendBroadcastTellingEveryoneWeArePlayingOrNotForPausePlayButtonDrawableSetting();
		//String artist = theCurrentSong.
		//currentArtistForNotification
		//currentSongForNotification
		updateNotification();
	}

	protected void savePlayBackStatus() {
		int position = mMediaPlayer.getCurrentPosition();
		// if(position < mMediaPlayer.getDuration()-100){
		theEditor.putInt(theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""),
				position);
		theEditor.commit();
	}

	@Override
	public void run() {
		if (mMediaPlayer.isPlaying()) {
			int position = mMediaPlayer.getCurrentPosition();
			// if(position < mMediaPlayer.getDuration()-100){
			theEditor.putInt(
					theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""),
					position);
			theEditor.commit();
			sendBroadcast(new Intent(NowPlaying.BROADCAST_AUDIO_POSITION)
					.putExtra("PROGRESS", position));

		}
		theThreadHandler.postDelayed(this, 1000);

	}

	@Override
	public void onAudioFocusChange(int focusChange) {
		switch (focusChange) {
		case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
			mMediaPlayer.pause();
			updateNotification();
			break;
		case AudioManager.AUDIOFOCUS_GAIN:
			if (wasPlayingWatchForInterruptionsLikePhoneCallsAndTextMessages)
				mMediaPlayer.start();
			updateNotification();
			break;
		case AudioManager.AUDIOFOCUS_LOSS:
			// am.unregisterMediaButtonEventReceiver(RemoteControlReceiver);
			mMediaPlayer.pause();
			updateNotification();
			break;
		}
	}

	class FindAlbumTask extends AsyncTask<Void, Void, String> {

		final String[] mProjection = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_KEY,
				MediaStore.Audio.Media.DURATION };

		private List<SongHolder> ALLOFTHESONGS;

		@Override
		protected String doInBackground(Void... params) {
			nextSong = null;
			previousSong = null;
			
			mCursor = getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mProjection,
					null, null, null);

			mCursor.moveToFirst();
			ALLOFTHESONGS = new ArrayList<SongHolder>();
			// currentAlbumOfSongs = new ArrayList<SongHolder>();

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

			String theChosenAlbum = "";
			for (int i = 0; i < ALLOFTHESONGS.size(); i++) {
				if (theCurrentSong.equals(ALLOFTHESONGS.get(i).getData())) {
					theChosenAlbum = (ALLOFTHESONGS.get(i).getAlbumId());
				}
			}
			List<SongHolder> theEntireAlbum = new ArrayList<SongHolder>();
			for (int i = 0; i < ALLOFTHESONGS.size(); i++) {
				if (ALLOFTHESONGS.get(i).getAlbumId().equals(theChosenAlbum)) {
					theEntireAlbum.add(ALLOFTHESONGS.get(i));
				}
			}

			int j = 0;
			// set the next song and previous song
			for (int i = 0; i < theEntireAlbum.size(); i++) {
				j = i + 1;
				// if we found the song
				if (theEntireAlbum
						.get(i)
						.getData()
						.equals(theSharedPrefs.getString(PREF_SONG_COCKED_NOW,
								""))) {
					currentArtistForNotification = theEntireAlbum.get(i).getArtist();
					currentSongForNotification = theEntireAlbum.get(i).getTitle();
					// if the next song position is less then the max position
					// on album
					if (j < theEntireAlbum.size()) {
						nextSong = new String(theEntireAlbum.get(j).getData());
					} else
						nextSong = new String(theEntireAlbum.get(0).getData());

					if (i == 0)
						previousSong = new String(theEntireAlbum.get(
								theEntireAlbum.size() - 1).getData());
					else
						previousSong = new String(theEntireAlbum.get(i - 1)
								.getData());

				}
			}
			
			return null;
		}

		@Override
		protected void onPostExecute(String result) {
			super.onPostExecute(result);
			updateNotification();
		}
		

	}

	@Override
	public void onCompletion(MediaPlayer mp) {
		sendBroadcast(new Intent(NowPlaying.STOP_SEEKBAR).putExtra(
				"PlayProgress", false));
		// mp.stop();
		Log.d("THIS IS ONCOMPLETION", "onSTOP CAUSED THIS!!!!!!!!!!!!");
		mp.reset();
		mp.release();
		mMediaPlayer = null;
		theEditor.putInt(theSharedPrefs.getString(PREF_SONG_COCKED_NOW, ""), 0);
		theEditor.commit();
		if (nextSong != null) {
			theEditor.putString(PREF_SONG_COCKED_NOW, nextSong);
			theEditor.commit();

			initMediaPlayer();
		} else {
		}
	}

	@Override
	public boolean onError(MediaPlayer mp, int what, int extra) {
		Log.e(getPackageName(), String.format("Error(%s%s)", what, extra));
		return false;
	}
}
