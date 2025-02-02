package com.example.swuni_up

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MyChallengeAdapter(
    private val context: Context,
    private var challengeList: List<DBHelper.Challenge>
) : RecyclerView.Adapter<MyChallengeAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val challengeTitle: TextView = view.findViewById(R.id.my_title)
        val button: TextView = view.findViewById(R.id.certification_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.my_challenge_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val challenge = challengeList[position]

        holder.challengeTitle.text = challenge.title


    }

    override fun getItemCount(): Int = challengeList.size


    fun updateData(newList: List<DBHelper.Challenge>) {
        challengeList = newList
        notifyDataSetChanged()
    }
}
