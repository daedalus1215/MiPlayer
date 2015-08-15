package com.example.larry.miplayer;


import android.content.Context;
import android.util.AttributeSet;
import android.widget.ArrayAdapter;
import android.widget.ListView;

public class FastSearchListView extends ListView{
	
	private Context ctx;
	private ArrayAdapter adapter;
	
	public FastSearchListView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}

	public FastSearchListView(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public FastSearchListView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public FastSearchListView(Context context, ArrayAdapter adapter){
		super(context);
		this.adapter = adapter;
	}
	
	public ArrayAdapter getAdapter(){
		return adapter;
	}
	

}
	
	
	
	


