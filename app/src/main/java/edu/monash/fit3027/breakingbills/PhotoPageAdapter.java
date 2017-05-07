package edu.monash.fit3027.breakingbills;

/**
 * Created by Callistus on 5/5/2017.
 */

import java.util.List;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

public class PhotoPageAdapter extends FragmentPagerAdapter {

    private List<Fragment> mFragments;

    public PhotoPageAdapter(FragmentManager fm, List<Fragment> fragments) {
        super(fm);
        mFragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return this.mFragments.get(position);
    }

    @Override
    public int getCount() {
        return this.mFragments.size();
    }

}
