package com.soumyajit.cataractdetector

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment

class SwipeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val swipeView = inflater.inflate(R.layout.swipe_fragment, container, false)
        val imageView = swipeView.findViewById<ImageView>(R.id.imageView)
        val bundle = arguments
        val position = bundle?.getInt("position") ?: 0
        val imageFileName = MainActivity.Image_name[position]
        val imgResId = resources.getIdentifier(imageFileName, "drawable", "com.soumyajit.cataractdetector")
        imageView.setImageResource(imgResId)

        return swipeView
    }

    companion object {
        fun newInstance(position: Int): SwipeFragment {
            val swipeFragment = SwipeFragment()
            val bundle = Bundle()
            bundle.putInt("position", position)
            swipeFragment.arguments = bundle
            return swipeFragment
        }
    }
}
