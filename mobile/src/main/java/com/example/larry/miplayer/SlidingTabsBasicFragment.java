/*
 * Copyright (C) 2013 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.larry.miplayer;

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.example.larry.miplayer.view.SlidingTabLayout;

import java.util.List;

/**
 * A basic sample which shows how to use {@link com.example.larry.miplayer.view.SlidingTabLayout}
 * to display a custom {@link android.support.v4.view.ViewPager} title strip which gives continuous feedback to the user
 * when scrolling.
 */
public class SlidingTabsBasicFragment extends Fragment {

    static final String LOG_TAG = "SlidingTabsBasicFragment";

    /**
     * A custom {@link android.support.v4.view.ViewPager} title strip which looks much like Tabs present in Android v4.0 and
     * above, but is designed to give continuous feedback to the user when scrolling.
     */
    private SlidingTabLayout mSlidingTabLayout;

    /**
     * A {@link android.support.v4.view.ViewPager} which will be used in conjunction with the {@link SlidingTabLayout} above.
     */
    private ViewPager mViewPager;


    /**
     * Inflates the {@link android.view.View} which will be displayed by this {@link android.support.v4.app.Fragment}, from the app's
     * resources.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        return inflater.inflate(R.layout.sliding_tab_fragment_layout, container, false);
    }

    // BEGIN_INCLUDE (fragment_onviewcreated)
    /**
     * This is called after the {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)} has finished.
     * Here we can pick out the {@link android.view.View}s we need to configure from the content view.
     *
     * We set the {@link android.support.v4.view.ViewPager}'s adapter to be an instance of {@link ViewPagerAdapter}. The
     * {@link SlidingTabLayout} is then given the {@link android.support.v4.view.ViewPager} so that it can populate itself.
     *
     * @param view View created in {@link #onCreateView(android.view.LayoutInflater, android.view.ViewGroup, android.os.Bundle)}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        // BEGIN_INCLUDE (setup_viewpager)
        // Get the ViewPager and set it's PagerAdapter so that it can display items
        mViewPager = (ViewPager) view.findViewById(R.id.viewpager);
        mViewPager.setAdapter(new ViewPagerAdapter(getChildFragmentManager()));
        mViewPager.setOffscreenPageLimit(0);
        // END_INCLUDE (setup_viewpager)
        // BEGIN_INCLUDE (setup_slidingtablayout)
        // Give the SlidingTabLayout the ViewPager, this must be done AFTER the ViewPager has had
        // it's PagerAdapter set.
        mSlidingTabLayout = (SlidingTabLayout) view.findViewById(R.id.sliding_tabs);

        /***************** BEGIN MY CODE */
        mSlidingTabLayout.setDividerColors(getActivity().getResources().getColor(R.color.main_blue));
        mSlidingTabLayout.setSelectedIndicatorColors(getActivity().getResources().getColor(R.color.main_yellow));
        /****************** END OF MY CODE */

        mSlidingTabLayout.setViewPager(mViewPager);
        // END_INCLUDE (setup_slidingtablayout)
    }
    // END_INCLUDE (fragment_onviewcreated)

    /**
     * The {@link android.support.v4.view.PagerAdapter} used to display pages in this sample.
     * The individual pages are simple and just display two lines of text. The important section of
     * this class is the {@link #getPageTitle(int)} method which controls what is displayed in the
     * {@link SlidingTabLayout}.
     */
    class ViewPagerAdapter extends FragmentStatePagerAdapter {

        String[] listOfTitles = new String[5];
        Fragment[] fragments = new Fragment[5];
        FragmentManager fragmentManager;


        ViewPagerAdapter(FragmentManager fm) {
            super(fm);
            fragmentManager = fm;
            /* Populate the list I will use */
            this.listOfTitles[0] = "Playlist";
            this.listOfTitles[1] = "Albums";
            this.listOfTitles[2] = "Folders";
            this.listOfTitles[3] = "Artists";
            this.listOfTitles[4] = "Songs";
            /* Populate the fragments I will use */
            this.fragments[0] = new MainActivityFolderListFragment();
            this.fragments[1] = new MainActivityAlbumListFragment();
            this.fragments[2] = new MainActivityFolderListFragment();
            this.fragments[3] = new MainActivityArtistsListFragment();
            this.fragments[4] = new MainActivityAllSongsListFragment();
        }
        /* Standard stuff */
        @Override
        public Fragment getItem(int position) {
            //Log.i("GetItem from SLidingTab", Integer.toString(position));
            /* Get fragment based off of position */
            return fragments[position];

        }
        /* Standard stuff */
        @Override
        public int getCount() {
            return listOfTitles.length;
        }

        /* They demand I use this and tell us why */
        // BEGIN_INCLUDE (pageradapter_getpagetitle)
        @Override
        public CharSequence getPageTitle(int position) {
            return listOfTitles[position];
        }
        // END_INCLUDE (pageradapter_getpagetitle)

    }
}
