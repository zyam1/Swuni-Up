package com.example.swuni_up

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class LogAdapter(private val logList: List<DBHelper.LogEntry>) : RecyclerView.Adapter<LogAdapter.LogViewHolder>() {

    class LogViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val logImage: ImageView = view.findViewById(R.id.log_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LogViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_log, parent, false)
        return LogViewHolder(view)
    }

    override fun onBindViewHolder(holder: LogViewHolder, position: Int) {
        val log = logList[position]

        // 이미지 변환 및 설정
        val bitmap = BitmapFactory.decodeByteArray(log.logImage, 0, log.logImage.size)
        holder.logImage.setImageBitmap(bitmap)
    }

    override fun getItemCount() = logList.size
}
