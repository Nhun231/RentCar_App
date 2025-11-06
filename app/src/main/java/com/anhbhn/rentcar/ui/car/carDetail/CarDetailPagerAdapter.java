package com.anhbhn.rentcar.ui.car.carDetail;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.anhbhn.rentcar.ui.car.addCar.BasicFragment;
import com.anhbhn.rentcar.ui.car.addCar.DetailsFragment;
import com.anhbhn.rentcar.ui.car.addCar.PricingFragment;

public class CarDetailPagerAdapter extends FragmentStateAdapter {

    private static final int NUM_TABS = 3;

    public CarDetailPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new BasicFragment();
            case 1: return new DetailsFragment();
            case 2: return new PricingFragment();
            default: return new BasicFragment();
        }
    }

    @Override
    public int getItemCount() {
        return NUM_TABS;
    }
}
