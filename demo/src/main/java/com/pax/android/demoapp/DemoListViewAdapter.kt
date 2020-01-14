package com.pax.android.demoapp

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class DemoListViewAdapter(private val context: Context, private var data: List<Map<String, Any>>, private val resId: Int) : BaseAdapter() {
    private val layoutInflater: LayoutInflater = LayoutInflater.from(context)
    override fun getCount(): Int {
        return data.size
    }

    override fun getItem(position: Int): Any {
        return data[position]
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getView(position: Int, convertView: View, parent: ViewGroup): View {
        var convertView = convertView
        val holder: ViewHolder
        if (convertView == null) {
            holder = ViewHolder()
            convertView = layoutInflater.inflate(resId, null)
            holder.label = convertView.findViewById(R.id.label)
            holder.value = convertView.findViewById(R.id.value)
            convertView.tag = holder
        } else {
            holder = convertView.tag as ViewHolder
        }
        //bind data
        val map = data[position]
        holder.label!!.text = map["label"] as String?
        holder.value!!.text = map["value"].toString()
        return convertView
    }

    fun loadData(data: List<Map<String, Any>>) {
        this.data = data
        // MANDATORY: Notify that the data has changed
        notifyDataSetChanged()
    }

}