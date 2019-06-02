package com.zeerooo.anikumii.adapters;

import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.zeerooo.anikumii.fragments.AnikumiiMainFragment;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by ZeeRooo on 24/02/18
 */

public class ViewPagerAdapter extends FragmentPagerAdapter {
    private final List<Fragment> mFragmentList = new ArrayList<>();
    private final List<String> mFragmentTitleList = new ArrayList<>();
    private AnikumiiMainFragment anikumiiMainFragment;

    public ViewPagerAdapter(FragmentManager manager, int behavior) {
        super(manager, behavior);
    }

    @Override
    @NonNull
    public Fragment getItem(int position) {
        return mFragmentList.get(position);
    }

    @Override
    public int getCount() {
        return mFragmentList.size();
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return mFragmentTitleList.get(position);
    }

    @Override
    public void setPrimaryItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
        if ((object.toString().startsWith("TioHentaiFragment") || object.toString().startsWith("TioAnimeFragment")) && getCurrentFragment() != object) {
            anikumiiMainFragment = ((AnikumiiMainFragment) object);
        }
        super.setPrimaryItem(container, position, object);
    }

    public AnikumiiMainFragment getCurrentFragment() {
        return anikumiiMainFragment;
    }

    public void addFragment(Fragment fragment, String title) {
        mFragmentList.add(fragment);
        mFragmentTitleList.add(title);
    }
}
