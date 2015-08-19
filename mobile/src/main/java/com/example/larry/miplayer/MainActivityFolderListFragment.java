package com.example.larry.miplayer;

import java.util.ArrayList;

import com.example.larry.miplayer.MainActivityFolderListFragmentFolderAdapter.taskConvertImage;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.AdapterView.OnItemClickListener;

public class MainActivityFolderListFragment extends Fragment implements
		OnItemClickListener {

	private static SharedPreferences theSharedPrefs;
	private static SharedPreferences.Editor theEditor;
	final private static String MAIN_ACTIVITY_ITEM_SELECTION = "MAIN_ACTIVITY_ITEM_SELECTION";
	
	
	// this will be an argument sent from activity
	private MainActivityFolderListFragmentFolderAdapter mAdapter;
	private final static String KEY_FOR_ALBUM_CHOICE_ACTIVITY = "KEY_FOR_ALBUM_CHOICE_ACTIVITY";
	private ListView theListView;
	private Activity mActivity;
	private ArrayList<SongHolder> ALLOFTHESONGS;
	private ArrayList<SongHolder> filteredList;
	private ProgressBar mProgressBar;
	private TextView tvLoading;

	private Cursor mCursor;
	private Cursor mCursorAllAlbumArt;
	TaskAlbumListCompiled talc = new TaskAlbumListCompiled();

	private ArrayList<taskConvertImage> taskPool;

	private View mFooterViewCount;

	private static final String[] newProjectionForAlbumArt = {
			MediaStore.Audio.Albums._ID, MediaStore.Audio.Albums.ARTIST,
			MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_KEY,
			MediaStore.Audio.Albums.ALBUM_ART };

	String[] mProjection = { MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.ARTIST, MediaStore.Audio.Media.TITLE,
			MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.ALBUM_KEY, MediaStore.Audio.Media.DURATION };

	static MainActivityFolderListFragment newInstance() {
		MainActivityFolderListFragment frag = new MainActivityFolderListFragment();
		return frag;

	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mActivity = getActivity();
		initPreferences();
		talc = new TaskAlbumListCompiled();
		talc.execute();
	}
	private void initPreferences() {
		theSharedPrefs = mActivity.getSharedPreferences(
				MainActivity.PREFS_STORED_PROGRESSION, 0);
		theEditor = theSharedPrefs.edit();
	}
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	@Override
	public void onDestroy() {
		super.onStop();
		talc.cancel(true);
		for (taskConvertImage tci : taskPool) {
			tci.cancel(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater
				.inflate(R.layout.main_activity_list_fragments_layout,
						null, false);
		theListView = (ListView) view.findViewById(R.id.listView1);

		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
		theListView.setOnItemClickListener(this);
		tvLoading = (TextView) view.findViewById(R.id.tvLoading);
		return view;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {

		Intent intent = new Intent(getActivity(), AlbumActivity.class);
		intent.putExtra(AlbumActivity.KEY_PRIOR_ACTIVITY, "FOLDER");
        intent.putExtra(AlbumActivity.MAIN_ACTIVITY_ALBUM_ID,  filteredList.get(position).getAlbumId());
		intent.putExtra(AlbumActivity.MAIN_ACTIVITY_ARTIST_ID,  filteredList.get(position).getArtist());

		mActivity.startActivity(intent);

	}


	class TaskAlbumListCompiled extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected Boolean doInBackground(String... params) {
			while (true) {
				if (isCancelled())
					break;
				ALLOFTHESONGS = new ArrayList<SongHolder>();
				if (isCancelled())
					break;
				taskPool = new ArrayList<taskConvertImage>();
				if (isCancelled())
					break;
				filteredList = new ArrayList<SongHolder>();
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

			mAdapter = null;

			mAdapter = new MainActivityFolderListFragmentFolderAdapter(
					getActivity(),
					R.layout.main_activity_folder_list_fragment_adapter_rows,
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
			tvLoading.setVisibility(View.GONE);
			theListView.setVisibility(View.VISIBLE);

		}
	}

	private ArrayList<SongHolder> updateAlbumList() {

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
}
