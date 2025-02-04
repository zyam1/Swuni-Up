package com.example.swuni_up

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class topRankAdapter(
    private val context: Context,
    private val topChallengers: List<DBHelper.Challenger>
) : RecyclerView.Adapter<topRankAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val profileImage: ImageView = view.findViewById(R.id.profile_image)
        val nickname: TextView = view.findViewById(R.id.nickname)
        val department: TextView = view.findViewById(R.id.department)
        val rank: TextView = view.findViewById(R.id.rank)
        val percentage: TextView = view.findViewById(R.id.percentage)
        val graphLayout: LinearLayout = view.findViewById(R.id.graph_layout)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.top_rank_component, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val challenger = topChallengers[position]

        val dbHelper = DBHelper(holder.itemView.context)
        val nickname = dbHelper.getNicknameById(challenger.userId)
        val major = dbHelper.getMajorById(challenger.userId)
        val bitmap = dbHelper.getUserProfilePhotoById(challenger.userId)

        if (bitmap != null) {
            holder.profileImage.setImageBitmap(bitmap) // 프로필 사진을 ImageView에 설정
        } else {
            Toast.makeText(holder.itemView.context, "프로필 사진을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }

        holder.rank.text = (position + 1).toString()
        holder.nickname.text = nickname ?: "유저 ${challenger.userId}"
        holder.department.text = major ?: "디지털미디어학과"
        holder.percentage.text = "${challenger.percentage}%"

        // 그래프 영역 높이 = 퍼센트 * 2dp
        val graphHeight = (challenger.percentage * 6).toInt()  // 퍼센트 * 2로 높이 계산
        val layoutParams = holder.graphLayout.layoutParams
        layoutParams.height = graphHeight
        holder.graphLayout.layoutParams = layoutParams
    }

    override fun getItemCount() = topChallengers.size
}
