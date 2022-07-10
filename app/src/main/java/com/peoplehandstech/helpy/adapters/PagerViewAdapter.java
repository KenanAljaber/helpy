package com.peoplehandstech.helpy.adapters;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.peoplehandstech.helpy.fragments.FragmentMainOtherProfile;
import com.peoplehandstech.helpy.fragments.FragmentRate;

public class PagerViewAdapter extends FragmentPagerAdapter {

    public PagerViewAdapter(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
    }



    @NonNull
    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                FragmentMainOtherProfile askForHelpFragment=new FragmentMainOtherProfile();
                return askForHelpFragment;

            case 1:
                FragmentRate rateFragment=new FragmentRate();
                return rateFragment;


                default:
                    return null;
        }

    }

    @Override
    public int getCount() {
        return 2;
    }
}
