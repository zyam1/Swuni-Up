package com.example.swuni_up

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedAdapter(private val items: MutableList<>) :
    RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    inner class FeedViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logImage: ImageView = view.findViewById(R.id.logImage)
        val userNick: TextView = view.findViewById(R.id.user_nick)
        val userMajor: TextView = view.findViewById(R.id.user_major)
        val cheerButton: ImageView = view.findViewById(R.id.cheer_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = items[position]

        holder.logImage.setImageResource(item.imageResId)
        holder.userNick.text = item.nickname
        holder.userMajor.text = item.major
    }

    override fun getItemCount(): Int = items.size

    fun addItem(newItem: UserPost) {
        items.add(newItem)
        notifyItemInserted(items.size - 1)  // UI 갱신
    }
}
