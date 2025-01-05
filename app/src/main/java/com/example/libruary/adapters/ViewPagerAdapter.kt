package com.example.libruary.adapters

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.appcompat.app.AppCompatActivity
import com.example.libruary.UserInfoFragment
import com.example.libruary.UserReviewsFragment

class ViewPagerAdapter(activity: AppCompatActivity) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int {
        return 2
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> UserInfoFragment()
            1 -> UserReviewsFragment()
            else -> Fragment()
        }
    }
}
