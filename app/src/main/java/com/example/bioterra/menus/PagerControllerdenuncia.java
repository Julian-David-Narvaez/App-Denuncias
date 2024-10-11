package com.example.bioterra.menus;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

public class PagerControllerdenuncia extends FragmentPagerAdapter {

    int numoftabs2;

    public PagerControllerdenuncia(@NonNull FragmentManager fm, int behavior) {
        super(fm, behavior);
        this.numoftabs2 = behavior;
    }


    @NonNull
    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case 0:
                return new realizar_denuncias() ;
            case 1:
                return new Mis_denuncias();

            default:
                break;
        }
        return null;
    }

    @Override
    public int getCount() {
        return numoftabs2;
    }
}