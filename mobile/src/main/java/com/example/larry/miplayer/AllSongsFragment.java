package com.example.larry.miplayer;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class AllSongsFragment extends Fragment{

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		ViewGroup row = (ViewGroup) inflater.inflate(R.layout.all_songs_layout, container, false);
		TextView tv = (TextView) row.findViewById(R.id.textView1);
		return row;
	}

}
