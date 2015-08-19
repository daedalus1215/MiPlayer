package com.example.larry.miplayer;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

public class NowPlayingViewPagerFragments extends Fragment{
	private TextView position;
	private ImageView albumArt;
	private int pos;
	private String albumImage;
	
	public static NowPlayingViewPagerFragments newInstance(int pos, String albumImage){
		NowPlayingViewPagerFragments npvpf = new NowPlayingViewPagerFragments();
		Bundle args = new Bundle();
		args.putInt("pos", pos);
		args.putString("albumImage", albumImage);
		npvpf.setArguments(args);
		return npvpf;
		
	}

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		albumImage = getArguments().getString("albumImage", null);
		pos = getArguments().getInt("pos", 0);
		
	}
	

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		
	}



	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		
		ViewGroup viewGroup = (ViewGroup) inflater.inflate(R.layout.now_playing_view_pager_fragment_layout, null, false);
		albumArt = (ImageView) viewGroup.findViewById(R.id.imageView1);
		if(albumImage!= null){
			Drawable img = Drawable.createFromPath(albumImage);
			albumArt.setBackgroundDrawable(img);
		}else {
			albumArt.setBackgroundResource(R.mipmap.capture);
		}


			
		return viewGroup;
	}



	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
	
	}
	

	
	
	
}
