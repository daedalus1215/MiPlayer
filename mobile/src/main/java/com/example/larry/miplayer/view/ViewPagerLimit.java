package com.example.larry.miplayer.view;

import android.content.Context;
import android.support.v4.view.ViewPager;

/**
 * Created by ladam_000 on 5/7/2015.
 */
public class ViewPagerLimit extends ViewPager {
    public ViewPagerLimit(Context context) {
        super(context);
    }

    @Override
    public void setOffscreenPageLimit(int limit) {
        super.setOffscreenPageLimit(limit);
    }
}
