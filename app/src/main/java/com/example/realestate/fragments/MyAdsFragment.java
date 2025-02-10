package com.example.realestate.fragments;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.realestate.databinding.FragmentMyAdsBinding;
import com.google.android.material.tabs.TabLayout;

public class MyAdsFragment extends Fragment {

    private static final String TAG = "MY_ADS_TAG";
    private FragmentMyAdsBinding binding;
    private Context mContext;
    private MyTabsViewPagerAdapter myTabsViewPagerAdapter;

    public MyAdsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onAttach(@NonNull Context context) {

        mContext = context;
        super.onAttach(context);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentMyAdsBinding.inflate(inflater, container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Ads"));
        binding.tabLayout.addTab(binding.tabLayout.newTab().setText("Favorites"));

        FragmentManager fragmentManager = getChildFragmentManager();
        myTabsViewPagerAdapter = new MyTabsViewPagerAdapter(fragmentManager, getLifecycle());
        binding.viewPager.setAdapter(myTabsViewPagerAdapter);

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                binding.viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                binding.tabLayout.selectTab(binding.tabLayout.getTabAt(position));
            }
        });
    }

    public class MyTabsViewPagerAdapter extends FragmentStateAdapter{

        public MyTabsViewPagerAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle) {
            super(fragmentManager, lifecycle);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {

            if (position == 0){
                return new MyAdsAdsFragment();
            } else {
                return new MyAdsFavFragment();
            }
        }

        @Override
        public int getItemCount() {
            return 2;
        }
    }
}