package com.example.habithealth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

// Example data class for a Habit History item
data class HabitHistoryItem(
    val date: String,
    val habitsCompleted: Int,
    val totalHabits: Int
)

class HabitHistoryAdapter(private val historyList: MutableList<HabitRecord>) :
    RecyclerView.Adapter<HabitHistoryAdapter.HabitViewHolder>() {

    inner class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateTextView: TextView = itemView.findViewById(R.id.tvDate)
        val progressTextView: TextView = itemView.findViewById(R.id.tvProgress)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_history, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {
        val item = historyList[position]
        holder.dateTextView.text = item.date
        holder.progressTextView.text = "${item.habitsCompleted} / ${item.totalHabits} habits"
    }

    override fun getItemCount(): Int = historyList.size
}
