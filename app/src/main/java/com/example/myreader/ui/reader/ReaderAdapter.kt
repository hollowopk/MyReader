package com.example.myreader.ui.reader

import android.view.View
import android.view.ViewGroup
import androidx.viewpager.widget.PagerAdapter

class ReaderAdapter(private val views: ArrayList<View>) : PagerAdapter() {

    override fun getCount() = views.size

    override fun isViewFromObject(view: View, `object`: Any) = view == `object`

    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        container.addView(views[position])
        return views[position]
    }

    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(views[position])
    }

}