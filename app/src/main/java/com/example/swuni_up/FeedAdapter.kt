package com.example.swuni_up

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class FeedAdapter(private val feedList: List<FeedItem>) :
    RecyclerView.Adapter<FeedAdapter.FeedViewHolder>() {

    class FeedViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val logImage: ImageView = itemView.findViewById(R.id.logImage)
        val userNickTextView: TextView = itemView.findViewById(R.id.user_nick)
        val userMajorTextView: TextView = itemView.findViewById(R.id.user_major)
        val cheerButton: ImageView = itemView.findViewById(R.id.cheer_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FeedViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.feed, parent, false)
        return FeedViewHolder(view)
    }

    override fun onBindViewHolder(holder: FeedViewHolder, position: Int) {
        val item = feedList[position]
        val bitmap = BitmapFactory.decodeByteArray(item.image, 0, item.image.size)
        holder.logImage.setImageBitmap(bitmap)
        holder.userNickTextView.text = item.nickname
        holder.userMajorTextView.text = item.department

        // Optional: Handle click for cheer button if needed
        holder.cheerButton.setOnClickListener {
            // Example: Showing a toast or implementing cheer logic
        }
    }

    override fun getItemCount() = feedList.size
}
