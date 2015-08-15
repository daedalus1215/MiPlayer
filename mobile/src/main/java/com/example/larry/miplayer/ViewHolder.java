package com.example.larry.miplayer;

import android.widget.ImageView;
import android.widget.TextView;

public class ViewHolder {

	// AlbumActivityListFragment
	TextView songTitle, sdHr, sdMin, sdSec;

	// MainActivityAlbumListFragment
	TextView artist, album;
	ImageView albumArt;

	// MainActivityAllSongsListFragmentAllSongsAdapter
	TextView title;

	// MainActivityArtistsListFragmentAdapter
	TextView artistId, artistKey;

	// MainActivityFolderListFragmentFolderAdapter
	int position;

	public ViewHolder() {
		// TODO Auto-generated constructor stub
	}

}
