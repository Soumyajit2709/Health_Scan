package com.soumyajit.cataractdetector

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter

class ImageFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm) {

    override fun getCount(): Int {
        return MainActivity.number_item
    }

    override fun getItem(position: Int): Fragment {
        return SwipeFragment.newInstance(position)
    }
}
