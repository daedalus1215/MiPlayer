package com.example.larry.miplayer;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AlbumActivityTitleFragment extends Fragment implements
		OnClickListener {
	private final String TAG = "AlbumActTitFrag";
	private static SongsParcel thePassedParcelAlbum;
	private AlbumActivity mActivity;
	private static SharedPreferences theSharedPrefs;
	private static SharedPreferences.Editor theEditor;
	private SongHolder theAlbumTitle;
	private LinearLayout llMain;
	private String THE_ALBUM_ID;
	private String THE_ARTIST;
	private String THE_ACTIVITY;
	TextView tvTitle;
	ImageButton iv;

	String[] mProjection = { MediaStore.Audio.Albums._ID,
			MediaStore.Audio.Albums.ALBUM, MediaStore.Audio.Albums.ALBUM_KEY,
			MediaStore.Audio.Albums.ALBUM_ART };

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

	}

	private void initPrefs() {
		theSharedPrefs = mActivity.getSharedPreferences(
				MainActivity.PREFS_STORED_PROGRESSION, 0);
		theEditor = theSharedPrefs.edit();
	//	THE_ALBUM_ID = theSharedPrefs.getString(
	//			MainActivityAlbumListFragment.MAIN_ACTIVITY_ITEM_SELECTION,
	//			"The Album Id");
		THE_ALBUM_ID = mActivity.whatAlbumDidWeComeFrom;
	//	THE_ARTIST = theSharedPrefs
	//			.getString("artist_selected", "Jack Johnson");
		THE_ARTIST =  mActivity.whatArtistDidWeComeFrom;
			// set which activity we came from (handle FolderListFragment
		// differently)
		THE_ACTIVITY = mActivity.whatActivityDidWeComeFrom;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = inflater.inflate(R.layout.album_fragment_title, null, false);
		tvTitle = (TextView) v.findViewById(R.id.tvAlbumTitle);
		iv = (ImageButton) v.findViewById(R.id.bRemove);
		llMain = (LinearLayout) v.findViewById(R.id.llMain);
		iv.setOnClickListener(this);
		return v;
	}

	@Override
	public void onResume() {
		super.onResume();
		mActivity = (AlbumActivity) getActivity();
		initPrefs();
		// set the conditional
		if (THE_ACTIVITY.equals("ALBUM"))
			new TaskFindTitleOfSelectedAlbum().execute();
		else if (THE_ACTIVITY.equals("FOLDER") || THE_ACTIVITY.equals("ARTIST"))
			new TaskFindTitleOfSelectedFolder().execute();
		// PIGGY BACKING ON FOLDER ASYNC, it only required 1 conditional for

	}

	/**
	 *
	 */
	class TaskFindTitleOfSelectedAlbum extends
			AsyncTask<Integer, Integer, Boolean> {

		BitmapDrawable bd;

		@Override
		protected Boolean doInBackground(Integer... params) {

			// following will cause this fragment to be tailored for albums
			// specifically, it leaves no room for other choices
			// so All songs and folders will probably need their own custom one.
			Cursor mCursor = mActivity.getContentResolver().query(
					MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, mProjection,
					null, null, MediaStore.Audio.Albums.ALBUM);

			ArrayList<SongHolder> entireSongList = new ArrayList<SongHolder>();

			mCursor.moveToFirst();
			while (mCursor.moveToNext()) {
				SongHolder s = new SongHolder();
				s.setAlbumImage(mCursor.getString(mCursor
						.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
				s.setAlbumId(mCursor.getString(mCursor
						.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY)));
				s.setAlbum(mCursor.getString(mCursor
						.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
				entireSongList.add(s);
			}

			theAlbumTitle = new SongHolder();

			for (int i = 0; i < entireSongList.size(); i++) {
				if (entireSongList.get(i).getAlbumId().equals(THE_ALBUM_ID)) {
					theAlbumTitle = entireSongList.get(i);

				}
			}
			if (theAlbumTitle.getAlbumImage() != null) {
				Bitmap bitmapFact = BitmapFactory.decodeFile(theAlbumTitle
						.getAlbumImage());
				if(bitmapFact != null){
					Bitmap bitmap = Bitmap.createScaledBitmap(bitmapFact, 92, 92,
							false);
					bd = new BitmapDrawable(getResources(), bitmap);
				}
			}
			mCursor.close();
			return null;
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
			tvTitle.setText(theAlbumTitle.getAlbum());
			if (bd != null) {
				iv.setBackground(null);
				llMain.setBackgroundDrawable(bd);
			}
		}

	}

	/**
	 *
	 */
	class TaskFindTitleOfSelectedFolder extends
			AsyncTask<Integer, Integer, Boolean> {

		SongHolder theAlbumTitleImage;

		String[] mProjectionAllAudio = { MediaStore.Audio.Media._ID,
				MediaStore.Audio.Media.ARTIST,
				MediaStore.Audio.Media.ALBUM_KEY, MediaStore.Audio.Media.DATA,
				MediaStore.Audio.Media.ALBUM, MediaStore.Audio.Media.ARTIST };

		BitmapDrawable bd;

		@Override
		protected Boolean doInBackground(Integer... params) {

			// following will cause this fragment to be tailored for albums
			// specifically, it leaves no room for other choices
			// so All songs and folders will probably need their own custom one.
			Cursor mCursor = mActivity.getContentResolver().query(
					MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
					mProjectionAllAudio, null, null, null);

			ArrayList<SongHolder> entireSongList = new ArrayList<SongHolder>();

			mCursor.moveToFirst();
			while (mCursor.moveToNext()) {
				SongHolder s = new SongHolder();
				s.setAlbum(mCursor.getString(mCursor
						.getColumnIndex(MediaStore.Audio.Media.ALBUM)));
				s.setAlbumId(mCursor.getString(mCursor
						.getColumnIndex(MediaStore.Audio.Media.ALBUM_KEY)));
				s.setArtist(mCursor.getString(mCursor
						.getColumnIndex(MediaStore.Audio.Media.ARTIST)));
				entireSongList.add(s);
			}

			theAlbumTitle = new SongHolder();

			for (int i = 0; i < entireSongList.size(); i++) {
				if (entireSongList.get(i).getAlbumId().equals(THE_ALBUM_ID)) {
					theAlbumTitle = entireSongList.get(i);

				}
			}
			// We have the song - theAlbumTitle

			// CHECK AND MAKE SURE THERE IS NO ALBUM IMAGE
			Cursor mECursor = mActivity.getContentResolver().query(
					MediaStore.Audio.Albums.EXTERNAL_CONTENT_URI, mProjection,
					null, null, MediaStore.Audio.Albums.ALBUM);

			ArrayList<SongHolder> mEntireSongList = new ArrayList<SongHolder>();

			mECursor.moveToFirst();
			while (mECursor.moveToNext()) {
				SongHolder s = new SongHolder();
				s.setAlbumImage(mECursor.getString(mECursor
						.getColumnIndex(MediaStore.Audio.Albums.ALBUM_ART)));
				s.setAlbumId(mECursor.getString(mECursor
						.getColumnIndex(MediaStore.Audio.Albums.ALBUM_KEY)));
				s.setAlbum(mECursor.getString(mECursor
						.getColumnIndex(MediaStore.Audio.Albums.ALBUM)));
				mEntireSongList.add(s);
			}

			theAlbumTitleImage = new SongHolder();

			for (int i = 0; i < mEntireSongList.size(); i++) {
				if (mEntireSongList.get(i).getAlbumId().equals(THE_ALBUM_ID)) {
					theAlbumTitleImage = mEntireSongList.get(i);

				}
			}
			if (theAlbumTitleImage.getAlbumImage() != null) {
				Bitmap bitmapFact = BitmapFactory.decodeFile(theAlbumTitleImage
						.getAlbumImage());
				Bitmap bitmap = Bitmap.createScaledBitmap(bitmapFact, 92, 92,
						false);
				bd = new BitmapDrawable(getResources(), bitmap);
			}

			mECursor.close();
			mCursor.close();
			return null;
		}

		@SuppressLint("NewApi")
		@Override
		protected void onPostExecute(Boolean result) { // TODO Auto-generated

			super.onPostExecute(result);
			if (theAlbumTitle.getAlbum() != null) {
				Log.d(TAG + "-onPost", "getAlbum() != null");
				// set the title to the artist name if it is Artist we came from
				if (THE_ACTIVITY.equals("ARTIST")){
					tvTitle.setText(theAlbumTitle.getArtist());
				}
					// set the title to be the album name if it was FOLDER ListView that brought us here
				else{
					tvTitle.setText(theAlbumTitle.getAlbum());
				}
				if (THE_ACTIVITY.equals("FOLDER"))
					if (bd != null) {
						iv.setBackground(null);
						llMain.setBackgroundDrawable(bd);
					}
			}else {
				Log.d(TAG + "-onPost", "getAlbum() == null");
			}
		}

	}



	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.bRemove:
			Log.d("bRemove id",
					"This will be used to go to a NowPlaying Screen with this album, or the first song in the album playing/ saved");
			break;
		}
	}

}
