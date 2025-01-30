package com.example.swuni_up

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class PercentAdapter(private val userList: List<UserData>) : RecyclerView.Adapter<PercentAdapter.MyViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.percent, parent, false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val userData = userList[position]
        holder.profileImage.setImageResource(userData.imageResId)
        holder.infoPercent.text = userData.percent
        holder.userNick.text = userData.nick

        holder.itemView.setOnClickListener {
            val intent =
                Intent(holder.itemView.context, ChallengeInfoFeed::class.java).apply {
                    putExtra("imageResId", userData.imageResId)
                    putExtra("percent", userData.percent)
                    putExtra("nick", userData.nick)
                }
            holder.itemView.context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = userList.size

    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val profileImage: ImageView = itemView.findViewById(R.id.profile_image)
        val infoPercent: TextView = itemView.findViewById(R.id.info_percent)
        val userNick: TextView = itemView.findViewById(R.id.user_nick)
    }
}
