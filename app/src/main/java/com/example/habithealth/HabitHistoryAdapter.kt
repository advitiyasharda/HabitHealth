package com.example.habithealth

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.CheckBox
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class HabitHistoryAdapter(private val habits: List<HabitRecord>) :
    RecyclerView.Adapter<HabitHistoryAdapter.HabitViewHolder>() {

    class HabitViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dateText: TextView = itemView.findViewById(R.id.textDate)
        val waterCheck: CheckBox = itemView.findViewById(R.id.checkWater)
        val exerciseCheck: CheckBox = itemView.findViewById(R.id.checkExercise)
        val sleepCheck: CheckBox = itemView.findViewById(R.id.checkSleep)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HabitViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_habit_history, parent, false)
        return HabitViewHolder(view)
    }

    override fun onBindViewHolder(holder: HabitViewHolder, position: Int) {

        val habit = habits[position]
        holder.dateText.text = habit.date
        holder.waterCheck.isChecked = habit.water
        holder.exerciseCheck.isChecked = habit.exercise
        holder.sleepCheck.isChecked = habit.sleep
    }

    override fun getItemCount(): Int = habits.size

}
