package com.example.larry.miplayer;

// # MAKING SECOND CURSOR FOR THE ART, GOING TO NEED TO IMPLEMENT ALL THE OTHER STUFF TOO

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import com.example.larry.miplayer.MainActivityArtistsListFragmentAdapter.taskConvertImage;

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
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivityArtistsListFragment extends Fragment implements
		OnItemClickListener {

	// CONSTANTS
	// Preferences
	final static String PREFS_STORED_PROGRESSION = "PREFS_STORED_PROGRESSION";
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
	private ArrayList<ArtistHolder> filteredList;
	private Cursor mCursorArtists;
	private Cursor mCursorArt;
	private MainActivityArtistsListFragmentAdapter mAdapter;
	private Boolean isServiceOn = false;
	private View mFooterViewCount;
	private ArrayList<taskConvertImage> taskPool;

	TaskGrabAndSetSongList tgassl = new TaskGrabAndSetSongList();

	private static final String[] mProjectionArt = {
			MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_KEY,
			MediaStore.Audio.Albums.ALBUM_ART };

	private static final String[] mProjection = { MediaStore.Audio.Artists._ID,
			MediaStore.Audio.Artists.ARTIST,
			MediaStore.Audio.Artists.ARTIST_KEY };

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater
				.inflate(R.layout.main_activity_artist_list_fragment_layout,
						null, false);
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
				filteredList = new ArrayList<ArtistHolder>();
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
			mAdapter = new MainActivityArtistsListFragmentAdapter(
					getActivity(),
					R.layout.main_activity_artist_list_fragment_adapter_rows,
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

	private ArrayList<ArtistHolder> updateAlbumList() {
		// 1#LIST artistArrayList
		ArrayList<ArtistHolder> artistArrayList = new ArrayList<ArtistHolder>();

		// 1#CURSOR mCursorArtists
		mCursorArtists = getActivity().getContentResolver().query(
				MediaStore.Audio.Artists.EXTERNAL_CONTENT_URI, mProjection,
				null, null, MediaStore.Audio.Artists.ARTIST);

		mCursorArtists.moveToFirst();

		while (mCursorArtists.moveToNext()) {
			ArtistHolder s = new ArtistHolder();

			s.setArtist(mCursorArtists.getString(mCursorArtists
					.getColumnIndex(MediaStore.Audio.Artists.ARTIST)));

			s.setId(mCursorArtists.getString(mCursorArtists
					.getColumnIndex(MediaStore.Audio.Artists._ID)));

			s.setArtistId(mCursorArtists.getString(mCursorArtists
					.getColumnIndex(MediaStore.Audio.Artists.ARTIST_KEY)));

			artistArrayList.add(s);
		}

		// 2#LIST artistArtList
		ArrayList<SongHolder> artistArtList = new ArrayList<SongHolder>();

		// 2#CURSOR mCursorArt
		mCursorArt = mActivity.getContentResolver().query(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, mProjectionArt,
				null, null, null);

		mCursorArt.moveToFirst();
		while (mCursorArt.moveToNext()) {
			AllSongHolder s = new AllSongHolder();
			s.setArtist(mCursorArt.getString(mCursorArt
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ARTIST)));
			s.setAlbumImage(mCursorArt.getString(mCursorArt
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART)));
			artistArtList.add(s);
		}

		// Add album Image to artistArrayList
		for (int i = 0; i < artistArrayList.size(); i++) {
			for (int j = 0; j < artistArtList.size(); j++) {
				if (artistArrayList.get(i).getArtist()
						.equals(artistArtList.get(j).getArtist())) {
					artistArrayList.get(i).setAlbumImage(
							artistArtList.get(j).getAlbumImage());
				}
			}
		}

		return artistArrayList;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		//Intent newIntent = new Intent(mActivity, ArtistActivity.class);
		//SongsParcel sp = getNewSongParcel(filteredList.get(position));
		
		//newIntent.putExtra(MainActivity.KEY_FOR_ALBUM_CHOICE_ACTIVITY, sp);
		
		theEditor.putString("artist_selected", filteredList.get(position).getArtist());
		
		theEditor.commit();
		Intent intent = new Intent(getActivity(), AlbumActivity.class);
		intent.putExtra(AlbumActivity.KEY_PRIOR_ACTIVITY, "ARTIST");
		mActivity.startActivity(intent);
	}

	/*private SongsParcel getNewSongParcel(SongHolder songHolding) {
		JSONObject jsonObject = new JSONObject();
		try {

			jsonObject.put(MainActivityArtistsListFragment.TRACK_ARTIST_KEY, songHolding.getArtist());
			return new SongsParcel(jsonObject);
		} catch (JSONException e) {
			throw new RuntimeException(e);
		}
	}*/

	BroadcastReceiver receiveIsServiceIsOn = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			isServiceOn = true;
		}
	};

}
