package com.google.android.gms.location.sample.locationupdatesbackgroundkotlin

import androidx.recyclerview.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.google.android.gms.location.sample.locationupdatesbackgroundkotlin.dummy.DummyContent.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem].
 * TODO: Replace the implementation with code for your data type.
 */
class MyPersonRecyclerViewAdapter(
    private val values: List<DummyItem>
) : RecyclerView.Adapter<MyPersonRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.fragment_person, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = values[position]
        holder.idView.text = item.id
        holder.usernameView.text = item.username
        holder.distanceView.text = item.distance
    }

    override fun getItemCount(): Int = values.size

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val idView: TextView = view.findViewById(R.id.item_number)
        val usernameView: TextView = view.findViewById(R.id.personUsername)
        val distanceView: TextView = view.findViewById(R.id.personDistance)

        override fun toString(): String {
            return super.toString() + " '" + usernameView.text + "'"
        }
    }
}