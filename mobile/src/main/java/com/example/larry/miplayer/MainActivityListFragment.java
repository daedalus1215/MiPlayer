package com.example.larry.miplayer;

import android.app.Activity;
import android.content.DialogInterface;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.ArrayList;


public class MainActivityListFragment extends Fragment implements OnItemClickListener {

    private ListView theListView;
    private Cursor mCursor;
    private Activity mActivity;

    private ArrayList<SongHolder> ALLOFTHESONGS;
    private ArrayList<SongHolder> filteredList;
    private ProgressBar mProgressBar;
    private TextView tvLoading;

    public MainActivityListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup view = (ViewGroup) inflater.inflate(
                R.layout.main_activity_list_fragments_layout, null, false);
        theListView = (ListView) view.findViewById(R.id.listView1);
        mProgressBar = (ProgressBar) view.findViewById(R.id.progressBar1);
        tvLoading = (TextView) view.findViewById(R.id.tvLoading);
        theListView.setOnItemClickListener(this);


        return view;
    }


    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }
}
