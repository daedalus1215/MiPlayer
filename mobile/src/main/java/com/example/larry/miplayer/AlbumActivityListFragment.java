package com.example.larry.miplayer;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView.OnItemClickListener;
import com.example.larry.miplayer.SongHolder;
import com.example.larry.miplayer.ViewHolder;
import com.example.larry.miplayer.MainActivityFolderListFragmentFolderAdapter.taskConvertImage;

public class AlbumActivityListFragment extends Fragment implements
		OnItemClickListener {

	final private static String MAIN_ACTIVITY_ITEM_SELECTION = "MAIN_ACTIVITY_ITEM_SELECTION";
	private String ALBUM_ID;
	// CONSTANTS
	// Preferences
	final private static String PREFS_STORED_PROGRESSION = "PREFS_STORED_PROGRESSION";
	// PREFERENCE KEY FOR CURRENTLY PLAYING SONGS
	final private static String PREF_SONG_COCKED_NOW = "PREF_SONG_COCKED_NOW";
	// For packing into a Parcel
	final public static String TRACK_ID_KEY = "track_id_key";
	final public static String TRACK_ARTIST_KEY = "track_artist_key";
	final public static String TRACK_TITLE_KEY = "track_title_key";
	final public static String TRACK_ALBUM_KEY = "TRACK_ALBUM_KEY";
	final public static String TRACK_DATA_KEY = "TRACK_DATA_KEY";
	final public static String TRACK_ALBUM_ID_KEY = "TRACK_ALBUM_ID_KEY";
	final public static String TRACK_DURATION_KEY = "TRACK_DURATION_KEY";
	// Coming from
	private final static String KEY_FOR_ALBUM_CHOICE_ACTIVITY = "KEY_FOR_ALBUM_CHOICE_ACTIVITY";
	// Sending to
	private final static String SONG_CHOSEN = "SONG_CHOSEN";
	// private static SongsParcel thePassedParcelAlbum;

	private static SharedPreferences theSharedPrefs;
	private static SharedPreferences.Editor theEditor;
	private static boolean isServiceOn = false;
	private String THE_ARTIST;
	/**
	 * ACTION_IS_SERVICE_ON : is a title we use for our FILTER to receive
	 * whether or not AudioPlayingService is actually on. There is a
	 * BroadcastReceiver that receives ACTION_ASK_SERVICE_ON intent and will
	 * broadcast out if the service is actually on.
	 */
	final private static String ACTION_IS_SERVICE_ON = "ACTION_IS_SERVICE_ON";
	/**
	 * ACTION_ASK_SERVICE_ON : is a title we use with out INTENT to find out if
	 * the AudioPlayingService is actually on. There is a Receiver in that class
	 * that is listening for this intent and it will broadcast out if it is on
	 * with ACTION_IS_SERVICE_ON.
	 */
	final private static String ACTION_ASK_SERVICE_ON = "ACTION_ASK_SERVICE_ON";
	/**
	 * ACTION_PLAY_STOP : used for FILTER will be accompanied with newSong
	 * (true) or !newSong (false). Will tell this AudioPlayer if we should grab
	 * the new "PREF_SONG_COCKED_NOW" or if we should just play what we have
	 * cocked. boolean KEY = NEW_SONG
	 */
	final private static String ACTION_PLAY_STOP = "ACTION_PLAY_STOP";

	private AlbumActivity mActivity;

	private static final String[] mProjection = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM_KEY, MediaStore.Audio.Media.DURATION };

	private static final String[] newProjectionForAlbumArt = {
			MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_KEY,
			MediaStore.Audio.Albums.ALBUM_ART };

	final String[] mProjectionArtist = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM_KEY, MediaStore.Audio.Media.DURATION };

	private static final int LOADER_ID = 1;
	ArrayList<SongHolder> theFilteredSongList;
	private ListAdapter mAdapter;
	private ArrayList<SongsParcel> allSongs = new ArrayList<SongsParcel>();
	private Cursor mCursor;
	private ListView mListView;
	private TextView mLoading;
	ProgressBar mProgressBar;
	// last activity we were from, Different task for different origins
	private String activityAlbumActivityCameFrom;

	private ArrayList<SongHolder> ALLOFTHESONGS;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = (AlbumActivity) getActivity();
		isServiceOn = false;

		initPreferences();
		initBroadcastReceivers();

		// set which Activity was prior
		activityAlbumActivityCameFrom = mActivity.whatActivityDidWeComeFrom;
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		mActivity.unregisterReceiver(receiveIsServiceIsOn);
	}

	private void initPreferences() {
		theSharedPrefs = mActivity.getSharedPreferences(
				PREFS_STORED_PROGRESSION, 0);
		theEditor = theSharedPrefs.edit();
		// if it came from album?? ??? ??
		ALBUM_ID = theSharedPrefs.getString(MAIN_ACTIVITY_ITEM_SELECTION, "");
		// if it came from artist
		THE_ARTIST = theSharedPrefs
				.getString("artist_selected", "Jack Johnson");

	}

	private void initBroadcastReceivers() {
		mActivity.registerReceiver(receiveIsServiceIsOn, new IntentFilter(
				ACTION_IS_SERVICE_ON));
	}

	@Override
	public void onStart() {
		super.onStart();
		isServiceOn = false;
		mActivity.sendBroadcast(new Intent(ACTION_ASK_SERVICE_ON));
	}

	@Override
	public void onResume() {
		super.onResume();
		if (activityAlbumActivityCameFrom.equals("ALBUM")
				|| activityAlbumActivityCameFrom.equals("FOLDER"))
			new Task().execute();
		// else if (activityAlbumActivityCameFrom.equals("FOLDER"))
		// new TaskFolderListCompiled().execute();
		else if (activityAlbumActivityCameFrom.equals("ARTIST"))
			new TaskArtist().execute();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		View v = inflater.inflate(
				R.layout.album_activity_list_fragment_listview_progress_layout,
				null, false);
		mProgressBar = (ProgressBar) v.findViewById(R.id.progressBar1);
		mListView = (ListView) v.findViewById(R.id.listView1);
		mLoading = (TextView) v.findViewById(R.id.tvLoading);
		mListView.setOnItemClickListener(this);
		return v;
	}

	// ARRAY ADAPTER for the ListView
	class ListAdapter extends ArrayAdapter<SongHolder> {
		private LayoutInflater mInflater;
		private Context mContext;
		private int mTextViewResourceId;
		private List<SongHolder> theAlbum;

		public ListAdapter(Context context, int textViewResourceId,
				List<SongHolder> objects) {
			super(context, textViewResourceId, objects);
			this.mContext = context;
			this.mTextViewResourceId = textViewResourceId;
			this.theAlbum = objects;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			ViewHolder viewHolder;

			// TextView songTitle, sdHr, sdMin, sdSec;
			long duration = theAlbum.get(position).getDuration();

			if (convertView == null) {
				mInflater = LayoutInflater.from(mContext);
				convertView = mInflater.inflate(mTextViewResourceId, parent,
						false);
				viewHolder = new ViewHolder();
				viewHolder.songTitle = (TextView) convertView
						.findViewById(R.id.tvSongTitleAlbumLayout);
				viewHolder.sdHr = (TextView) convertView
						.findViewById(R.id.tvDurationHr);
				viewHolder.sdMin = (TextView) convertView
						.findViewById(R.id.tvDurationMin);
				viewHolder.sdSec = (TextView) convertView
						.findViewById(R.id.tvDurationSec);
				convertView.setTag(viewHolder);
			} else {
				viewHolder = (ViewHolder) convertView.getTag();
			}

			processDuration(duration, viewHolder.sdHr, viewHolder.sdMin,
					viewHolder.sdSec);
			viewHolder.songTitle.setText(theAlbum.get(position).getTitle());

			return convertView;
		}

		private void processDuration(long duration, TextView sdHr,
				TextView sdMin, TextView sdSec) {
			int hour = (int) duration / 1000 / 60 / 60;

			if (hour > 0)
				sdHr.setText(Integer.toString(hour) + ":");
			else
				sdHr.setText("");

			int minute = (int) duration / 1000 / 60 % 60;
			if (minute > 10)
				sdMin.setText(Integer.toString(minute) + ":");
			else if (minute < 10 && minute > 0)
				sdMin.setText("0" + Integer.toString(minute) + ":");
			else
				sdMin.setText("00:");

			int second = (int) duration / 1000 % 60;
			if (second >= 10 && second <= 59)
				sdSec.setText(Integer.toString(second));
			else if (second < 10 && second > 0)
				sdSec.setText("0" + Integer.toString(second));
			else
				sdSec.setText("00");
		}

	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {

		// Save selection
		theEditor.putString(PREF_SONG_COCKED_NOW,
				theFilteredSongList.get(position).getData());
		theEditor.commit();
		// Go to NOWPlaying
		Intent letsGo = new Intent(getActivity(), NowPlaying.class);
		if (isServiceOn) {
			mActivity.sendBroadcast(new Intent(ACTION_PLAY_STOP).putExtra(
					"NEW_SONG", true));
		} else {
			mActivity.startService(new Intent(mActivity,
					AudioPlayingService.class));
		}

		startActivity(letsGo);
	}

	BroadcastReceiver receiveIsServiceIsOn = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			isServiceOn = true;
		}
	};
	
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////

	// 
	// 

	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// not actually using
	// ///////////////////////////////////////////////////////
	
	
	
	
	
	
	

	class Task extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mAdapter = new ListAdapter(getActivity(),
					R.layout.album_activity_list_fragment_adapter_row,
					theFilteredSongList);
			mListView.setAdapter(mAdapter);
			mLoading.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			ALLOFTHESONGS = new ArrayList<SongHolder>();
			theFilteredSongList = new ArrayList<SongHolder>();
			theFilteredSongList = updateLists();

			return null;
		}

	}

	private ArrayList<SongHolder> updateLists() {
		// get our Cursor
		String[] mProjection = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_KEY,
				MediaStore.Audio.Media.DURATION };

		mCursor = getActivity().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mProjection, null,
				null, null);

		// Store our Cursor
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
		ArrayList<SongHolder> theChosenAlbum = new ArrayList<SongHolder>();
		for (int i = 0; i < ALLOFTHESONGS.size(); i++) {
			if (ALBUM_ID.equals(ALLOFTHESONGS.get(i).getAlbumId())) {
				theChosenAlbum.add(ALLOFTHESONGS.get(i));
			}
		}
		return theChosenAlbum;
	}

	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////

	// THIS IS THE TASK AND THE METHOD THAT IS USED BY IT TO SETUP A FILTER FOR
	// WHICH FOLDER AND THE ALBUM ART SHOULD BE ASSOCIATED WITH THAT CHOICE

	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// not actually using
	// ///////////////////////////////////////////////////////

	class TaskFolderListCompiled extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			ALLOFTHESONGS = new ArrayList<SongHolder>();

			theFilteredSongList = new ArrayList<SongHolder>();

			theFilteredSongList = updateFolderList();

			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);

			mAdapter = new ListAdapter(getActivity(),
					R.layout.album_activity_list_fragment_adapter_row,
					theFilteredSongList);
			mListView.setAdapter(mAdapter);
			mLoading.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);

		}
	}

	private ArrayList<SongHolder> updateFolderList() {
		Cursor mCursorAllAlbumArt;

		mCursor = getActivity().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mProjection,
				MediaStore.Audio.Media.DURATION + ">= 60000", null,
				MediaStore.Audio.Media.ALBUM);

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

		// Go through our Cursor
		ArrayList<SongHolder> noDuplicateAlbum = new ArrayList<SongHolder>(
				ALLOFTHESONGS.size());
		int counter = 0;
		for (int i = 0; i < ALLOFTHESONGS.size(); i++) {
			for (int j = 0; j < noDuplicateAlbum.size(); j++) {
				if (noDuplicateAlbum.get(j).getAlbumId()
						.equals(ALLOFTHESONGS.get(i).getAlbumId())
						|| ALLOFTHESONGS.get(i).getAlbum().equals("unknown")) {
					counter = 1;
				} else {
					counter = 0;
				}
			}

			if (counter == 0) {
				noDuplicateAlbum.add(ALLOFTHESONGS.get(i));
				counter = 0;
			}

		}

		mCursorAllAlbumArt = getActivity().getContentResolver().query(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
				newProjectionForAlbumArt, null, null,
				MediaStore.Audio.Albums.ALBUM_ART);

		ArrayList<SongHolder> SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch = new ArrayList<SongHolder>(
				noDuplicateAlbum.size());

		mCursorAllAlbumArt.moveToFirst();
		while (mCursorAllAlbumArt.moveToNext()) {
			AllSongHolder s = new AllSongHolder();
			s.setArtist(mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));

			s.setAlbumId(mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY)));

			String albumArt = mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			s.setAlbumImage(albumArt);

			SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch.add(s);

		}

		for (int i = 0; i < noDuplicateAlbum.size(); i++) {
			for (int j = 0; j < SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch
					.size(); j++) {
				if (noDuplicateAlbum
						.get(i)
						.getAlbumId()
						.toString()
						.equals(SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch
								.get(j).getAlbumId())) {
					String albumArt = SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch
							.get(j).getAlbumImage();
					noDuplicateAlbum.get(i).setAlbumImage(albumArt);

				}
			}
		}
		// Go through our Cursor
		mCursor.close();
		mCursorAllAlbumArt.close();
		return noDuplicateAlbum;

	}

	//

	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////

	// THIS IS THE TASK AND THE METHOD THAT IS USED BY IT TO SETUP A FILTER FOR
	// WHICH ARTIST AND THE ALBUM ART SHOULD BE ASSOCIATED WITH THAT CHOICE

	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////
	// ///////////////////////////////////////////////////////

	// ///////////////////////////////////////////////////////

	class TaskArtist extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			mAdapter = new ListAdapter(getActivity(),
					R.layout.album_activity_list_fragment_adapter_row,
					theFilteredSongList);
			mListView.setAdapter(mAdapter);
			mLoading.setVisibility(View.GONE);
			mProgressBar.setVisibility(View.GONE);
			mListView.setVisibility(View.VISIBLE);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			ALLOFTHESONGS = new ArrayList<SongHolder>();
			theFilteredSongList = new ArrayList<SongHolder>();
			theFilteredSongList = updateArtistLists();
			return null;
		}

	}

	private ArrayList<SongHolder> updateArtistLists() {

		// String artist = thePassedParcelAlbum.getArtist();

		ArrayList<SongHolder> filteringThroughArtists = new ArrayList<SongHolder>();

		String[] mProjection = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM_KEY,
				MediaStore.Audio.Media.DURATION };

		mCursor = getActivity().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mProjection, null,
				null, MediaStore.Audio.Media.ARTIST);

		// Store our Cursor
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
			filteringThroughArtists.add(s);
		}
		mCursor.close();

		ArrayList<SongHolder> theChosenArtist = new ArrayList<SongHolder>();

		for (int i = 0; i < filteringThroughArtists.size(); i++) {
			if (filteringThroughArtists.get(i).getArtist().equals(THE_ARTIST)) {
				theChosenArtist.add(filteringThroughArtists.get(i));
			}
		}

		return theChosenArtist;
	}

}
