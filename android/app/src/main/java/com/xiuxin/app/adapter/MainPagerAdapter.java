package com.xiuxin.app.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.xiuxin.app.fragment.BlessingFragment;
import com.xiuxin.app.fragment.BreathingFragment;
import com.xiuxin.app.fragment.MeditationFragment;
import com.xiuxin.app.fragment.PracticeFragment;
import com.xiuxin.app.fragment.StatsFragment;

public class MainPagerAdapter extends FragmentStateAdapter {

    private final String[] tabTitles = {"正念", "呼吸", "冥想", "功课", "统计"};

    public MainPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0:
                return new BlessingFragment();
            case 1:
                return new BreathingFragment();
            case 2:
                return new MeditationFragment();
            case 3:
                return new PracticeFragment();
            case 4:
            default:
                return new StatsFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }

    public String getTabTitle(int position) {
        return tabTitles[position];
    }
}
