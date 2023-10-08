package com.soumyajit.cataractdetector

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class MyListAdapter(private val context: Activity, private val maintitle: Array<String>, private val subtitle: Array<String>, private val imgid: Array<Int>) :
    ArrayAdapter<String>(context, R.layout.activity_my_list_adapter, maintitle) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val inflater = context.layoutInflater
        val rowView = inflater.inflate(R.layout.activity_my_list_adapter, null, true)

        val titleText = rowView.findViewById<TextView>(R.id.title)
        val imageView = rowView.findViewById<ImageView>(R.id.icon)
        val subtitleText = rowView.findViewById<TextView>(R.id.subtitle)

        titleText.text = maintitle[position]
        imageView.setImageResource(imgid[position])
        subtitleText.text = subtitle[position]

        return rowView
    }
}
