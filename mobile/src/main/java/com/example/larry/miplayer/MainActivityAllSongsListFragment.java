package com.example.larry.miplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.example.larry.miplayer.MainActivityAllSongsListFragmentAllSongsAdapter.taskConvertImage;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import android.os.AsyncTask;
import android.os.Bundle;

import android.provider.MediaStore;

import android.support.v4.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.AdapterView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

public class MainActivityAllSongsListFragment extends Fragment implements
		OnItemClickListener {

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

	private static SharedPreferences theSharedPrefs;
	private static SharedPreferences.Editor theEditor;
	private ProgressBar mProgressBar;
	private TextView mLoading;
	private ListView theListView;
	private Activity mActivity;
	private ArrayList<AllSongHolder> ALLOFTHESONGS;
	private ArrayList<AllSongHolder> filteredList;
	private Cursor mCursorAllSongs;
	private Cursor mCursorAllAlbumArt;
	private MainActivityAllSongsListFragmentAllSongsAdapter mAdapter;
	private Boolean isServiceOn = false;
	private View mFooterViewCount;
	private ArrayList<taskConvertImage> taskPool;
	
	TaskGrabAndSetSongList tgassl = new TaskGrabAndSetSongList();

	private static final String[] newProjectionForAlbumArt = {
			MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_KEY,
			MediaStore.Audio.Albums.ALBUM_ART };

	private static final String[] mProjection = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM_KEY, MediaStore.Audio.Media.DURATION };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(
				R.layout.main_activity_all_songs_list_fragment_layout, null,
				false);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
		mLoading = (TextView) view.findViewById(R.id.tvLoading);
		theListView = (ListView) view.findViewById(R.id.listView1);
		theListView.setOnItemClickListener(this);
		return view;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
		isServiceOn = false;
		initPreferences();
		initBroadcastReceivers();
		mActivity.sendBroadcast(new Intent(NowPlaying.ACTION_ASK_SERVICE_ON));
		mActivity.sendBroadcast(new Intent(NowPlaying.ACTION_ASK_SERVICE_ON));
		tgassl = new TaskGrabAndSetSongList();
		tgassl.execute();
	}

	@Override
	public void onStop() {
		super.onStop();
		tgassl.cancel(true);
		for (taskConvertImage task : taskPool)
			task.cancel(true);
		
		
		
	}


	private void initPreferences() {
		theSharedPrefs = mActivity.getSharedPreferences(
				PREFS_STORED_PROGRESSION, 0);
		theEditor = theSharedPrefs.edit();
	}

	private void initBroadcastReceivers() {
		mActivity.registerReceiver(receiveIsServiceIsOn, new IntentFilter(
				NowPlaying.ACTION_IS_SERVICE_ON));
	}

	class TaskGrabAndSetSongList extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			while (true) {

				if (isCancelled())
					break;
				ALLOFTHESONGS = new ArrayList<AllSongHolder>();
				if (isCancelled())
					break;
				filteredList = new ArrayList<AllSongHolder>();
				if (isCancelled())
					break;
				filteredList = updateAlbumList();
				break;
			}
			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			taskPool = new ArrayList<taskConvertImage>();
			mAdapter = new MainActivityAllSongsListFragmentAllSongsAdapter(
					getActivity(),
					R.layout.main_activity_all_songs_list_fragment_adapter_rows,
					filteredList, taskPool);
			
			mFooterViewCount = LayoutInflater.from(mActivity).inflate(
					R.layout.main_activity_fragment_list_count_footer, null);
			TextView textView = (TextView) mFooterViewCount
					.findViewById(R.id.tvCount);
			textView.setText("Total: " + filteredList.size());
			theListView.addFooterView(mFooterViewCount);
			theListView.setAdapter(mAdapter);
			theListView.setFastScrollEnabled(true);
			theListView.setRecyclerListener(new RecyclerListener() {

				@Override
				public void onMovedToScrapHeap(View view) {

					for (taskConvertImage task : taskPool)
						task.viewRecycled(view);
				}
			});
			mProgressBar.setVisibility(View.GONE);
			mLoading.setVisibility(View.GONE);
			theListView.setVisibility(View.VISIBLE);
		}

	}

	



	private ArrayList<AllSongHolder> updateAlbumList() {
		// 1#LIST ALLOFTHESONGS

		// 1#CURSOR
		mCursorAllSongs = getActivity().getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, mProjection,
				MediaStore.Audio.Media.DURATION + ">= 60000", null,
				MediaStore.Audio.Media.TITLE);

		// 2#CURSOR
		// into SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch
		mCursorAllAlbumArt = getActivity().getContentResolver().query(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
				newProjectionForAlbumArt, null, null,
				MediaStore.Audio.Albums.ALBUM_ART);

		// Store our 1#CURSOR
		// Into ALLOFTHESONGS
		mCursorAllSongs.moveToFirst();

		while (mCursorAllSongs.moveToNext()) {
			AllSongHolder s = new AllSongHolder();
			s.setAlbum(mCursorAllSongs.getString(mCursorAllSongs
					.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
			s.setArtist(mCursorAllSongs.getString(mCursorAllSongs
					.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
			s.setData(mCursorAllSongs.getString(mCursorAllSongs
					.getColumnIndex(MediaStore.Audio.Media.DATA)));
			s.setId(mCursorAllSongs.getString(mCursorAllSongs
					.getColumnIndex(MediaStore.Audio.Media._ID)));
			s.setTitle(mCursorAllSongs.getString(mCursorAllSongs
					.getColumnIndex(MediaStore.Audio.Media.TITLE)));
			s.setAlbumId(mCursorAllSongs.getString(mCursorAllSongs
					.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY)));
			s.setDuration(mCursorAllSongs.getLong(mCursorAllSongs
					.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)));
			ALLOFTHESONGS.add(s);
		}

		// 3#LIST SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch
		ArrayList<SongHolder> SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch = new ArrayList<SongHolder>(
				ALLOFTHESONGS.size());

		mCursorAllAlbumArt.moveToFirst();

		while (mCursorAllAlbumArt.moveToNext()) {
			SongHolder s = new SongHolder();
			s.setArtist(mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));

			s.setAlbumId(mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY)));

			String albumArt = mCursorAllAlbumArt.getString(mCursorAllAlbumArt
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));

			s.setAlbumImage(albumArt);
			SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch.add(s);

		}

		// 1#LIST is getting the actual Drawables from 2#LIST, doesnt worry
		// about paths like last one
		for (int i = 0; i < ALLOFTHESONGS.size(); i++) {
			for (int j = 0; j < SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch
					.size(); j++) {
				if (ALLOFTHESONGS
						.get(i)
						.getAlbumId()
						.toString()
						.equals(SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch
								.get(j).getAlbumId())) {
					String albumArt = SomeReasonFrantiNotPickingUpSoWeDoublingDownSearch
							.get(j).getAlbumImage();
					ALLOFTHESONGS.get(i).setAlbumImage(albumArt);
					
				}
			}
		}

		mCursorAllSongs.close();
		mCursorAllAlbumArt.close();
		return ALLOFTHESONGS;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		mActivity.sendBroadcast(new Intent(NowPlaying.ACTION_ASK_SERVICE_ON));
		// Save selection
		theEditor.putString(PREF_SONG_COCKED_NOW, filteredList.get(position)
				.getData());
		theEditor.commit();
		// Go to NOWPlaying
		Intent letsGo = new Intent(getActivity(), NowPlaying.class);
		if (isServiceOn) {
			mActivity.sendBroadcast(new Intent(NowPlaying.ACTION_PLAY_STOP)
					.putExtra("NEW_SONG", true));
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

}
