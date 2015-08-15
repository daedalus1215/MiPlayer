package com.example.larry.miplayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;
import com.example.larry.miplayer.MainActivityAlbumListFragmentAlbumAdapter.taskConvertImage;
import android.app.Activity;
import android.app.ActionBar.LayoutParams;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.RecyclerListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AlphabetIndexer;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

/**
 * 
 * 
 * @author larry The Adapter we use for this Fragment is
 *         MainActivityAlbumListFragmentAlbumAdapter We set it in our onPost...
 *         TaskAlbumListCompiled
 */

public class MainActivityAlbumListFragment extends Fragment implements
		OnItemClickListener {

	private static SharedPreferences theSharedPrefs;
	private static SharedPreferences.Editor theEditor;
	final static String MAIN_ACTIVITY_ITEM_SELECTION = "MAIN_ACTIVITY_ITEM_SELECTION";
	private final String[] alphabet = { "#", "A", "B", "C", "D", "E", "F", "G",
			  "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R", "S", "T", "U", "V",
			  "W", "X", "Y", "Z" };
	
	// Adapter stuff
	private MainActivityAlbumListFragmentAlbumAdapter mAdapter;
	private ArrayList<com.example.larry.miplayer.MainActivityAlbumListFragmentAlbumAdapter.taskConvertImage> taskPool;

	// Coming from the SongParcel
	private final static String KEY_FOR_ALBUM_CHOICE_ACTIVITY = "KEY_FOR_ALBUM_CHOICE_ACTIVITY";

	private ListView theListView;
	private Cursor mCursor;
	private Activity mActivity;

	private ArrayList<SongHolder> ALLOFTHESONGS;
	private ArrayList<SongHolder> filteredList;
	private ProgressBar mProgressBar;
	private TextView tvLoading;
	TaskAlbumListCompiled talc = new TaskAlbumListCompiled();

	private void initPreferences() {
		theSharedPrefs = mActivity.getSharedPreferences(
				MainActivity.PREFS_STORED_PROGRESSION, 0);
		theEditor = theSharedPrefs.edit();
	}

	private View mFooterViewCount;
	

	static MainActivityAlbumListFragment newInstance() {
		MainActivityAlbumListFragment frag = new MainActivityAlbumListFragment();
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

	@Override
	public void onStop() {
		super.onStop();
		talc.cancel(true);
		for (taskConvertImage tci : taskPool) {
			tci.cancel(true);
		}
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup view = (ViewGroup) inflater.inflate(
				R.layout.main_activity_album_list_fragment_layout, null, false);
		theListView = (ListView) view.findViewById(R.id.listView1);
		mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
		tvLoading = (TextView) view.findViewById(R.id.tvLoading);
		theListView.setOnItemClickListener(this);

		
		return view;
	}

	class TaskAlbumListCompiled extends AsyncTask<String, Integer, Boolean> {
		String[] mProjection = { MediaStore.Audio.Albums._ID,
				MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM,
				MediaStore.Audio.Albums.ALBUM_KEY,
				MediaStore.Audio.Albums.ALBUM_ART };

		@Override
		protected Boolean doInBackground(String... params) {
			while (true) {
				if (isCancelled())
					break;
				mCursor = getActivity().getContentResolver().query(
						MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI,
						mProjection, null, null, MediaStore.Audio.Albums.ALBUM);
				if (isCancelled())
					break;
				ALLOFTHESONGS = new ArrayList<SongHolder>();
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
			taskPool = new ArrayList<com.example.larry.miplayer.MainActivityAlbumListFragmentAlbumAdapter.taskConvertImage>();

			mAdapter = new MainActivityAlbumListFragmentAlbumAdapter(mActivity,
					R.layout.main_activity_album_list_fragment_adapter_rows,
					filteredList, taskPool);

			ArrayAdapter<String> testAdapter = new ArrayAdapter<String>(
					mActivity, R.layout.test_fast_list_view_row, alphabet);
			

			theListView.setFastScrollEnabled(false);
			theListView.setFastScrollAlwaysVisible(false);
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
		String[] mProjection = { MediaStore.Audio.Albums._ID,
				MediaStore.Audio.Albums.ARTIST, MediaStore.Audio.Albums.ALBUM,
				MediaStore.Audio.Albums.ALBUM_KEY,
				MediaStore.Audio.Albums.ALBUM_ART };

		mCursor = getActivity().getContentResolver().query(
				MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, mProjection,
				null, null, MediaStore.Audio.Albums.ALBUM);
		// Store our Cursor
		mCursor.moveToFirst();
		while (mCursor.moveToNext()) {
			SongHolder s = new SongHolder();
			s.setAlbum(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
			s.setArtist(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Albums.ARTIST)));

			s.setAlbumId(mCursor.getString(mCursor
					.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY)));
			String albumArt = mCursor.getString(mCursor
					.getColumnIndexOrThrow(MediaStore.Audio.Albums.ALBUM_ART));
			s.setAlbumImage(albumArt);

			ALLOFTHESONGS.add(s);
		}

		// Go through our Cursor
		ArrayList<SongHolder> noDuplicateAlbum = new ArrayList<SongHolder>(
				ALLOFTHESONGS.size());

		for (int i = 0; i < ALLOFTHESONGS.size(); i++)
			noDuplicateAlbum.add(ALLOFTHESONGS.get(i));
		mCursor.close();

		return noDuplicateAlbum;
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int position,
			long arg3) {
		theEditor.putString(MAIN_ACTIVITY_ITEM_SELECTION, filteredList.get(position).getAlbumId());
		theEditor.commit();
		Intent intent = new Intent(getActivity(), AlbumActivity.class);
		intent.putExtra(AlbumActivity.KEY_PRIOR_ACTIVITY, "ALBUM");
		mActivity.startActivity(intent);

	}




}

/*
 * private ListView theFastListView;
	private Toast toast;
 * 
 * toast = new Toast(mActivity);
 * 
 * theFastListView.setAdapter(testAdapter);
 * 
 * theFastListView.setVisibility(View.VISIBLE);
 * 
 * public void customToast(int position){ LayoutInflater inflater =
 * LayoutInflater.from(mActivity); View layout =
 * inflater.inflate(R.layout.toast, null); TextView text = (TextView)
 * layout.findViewById(R.id.toast); String letter = alphabet[position];
 * text.setTextSize(100); text.setText(letter); toast.setGravity(Gravity.CENTER,
 * 0, 0); toast.setDuration(Toast.LENGTH_SHORT); toast.setView(layout);
 * toast.show(); }
 * 
 * 
 * 
 * 
 * 
 * theFastListView = (ListView) view.findViewById(R.id.fastListView1);
		theFastListView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {

				if (arg2 != 0) { // conditional is checking for # sign
					int position = mAdapter.evaluateWhereWeAre(arg2);
					if (position != -2)
						theListView.setSelection(position);
					// Toast toast = new Toast(mActivity);
					// toast.setGravity(, xOffset, yOffset)

				} else {
					theListView.setSelection(0);
				}
				customToast(arg2);
			}
		});
		
		
		
		
		
 */