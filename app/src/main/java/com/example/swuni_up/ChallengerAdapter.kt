package com.example.swuni_up

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChallengerAdapter(private val context: Context, private val challengers: List<ChallengerDBHelper.Challenger>) :
    RecyclerView.Adapter<ChallengerAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val rankNumber: TextView = view.findViewById(R.id.rank_number)
        val rankNick: TextView = view.findViewById(R.id.rank_nick)
        val rankPercentage: TextView = view.findViewById(R.id.rank_percentage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.rank_component, parent, false)
        return ViewHolder(view)

    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val challenger = challengers[position]

        val dbHelper = UserDBHelper(holder.itemView.context)
        val nickname = dbHelper.getNicknameById(challenger.userId)

        holder.rankNumber.text = String.format("%02d", position + 4) // 랭킹 번호
        holder.rankNick.text = "${nickname ?: challenger.userId}"  // 예제: 유저 ID를 닉네임처럼 표시
        holder.rankPercentage.text = "${challenger.percentage}%"
    }

    override fun getItemCount() = challengers.size
}
