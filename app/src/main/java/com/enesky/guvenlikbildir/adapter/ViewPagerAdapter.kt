package com.enesky.guvenlikbildir.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentStatePagerAdapter

/**
 * Created by Enes Kamil YILMAZ on 02.02.2020
 */

class ViewPagerAdapter(fm: FragmentManager,
                       private val fragmentList: List<Fragment>,
                       private val fragmentListTitles: List<String> = listOf() )
    : FragmentStatePagerAdapter(fm, BEHAVIOR_RESUME_ONLY_CURRENT_FRAGMENT) {

    override fun getItem(position: Int): Fragment = fragmentList[position]

    override fun getCount(): Int = fragmentList.size

    override fun getPageTitle(position: Int): CharSequence? = fragmentListTitles.get(position)

}