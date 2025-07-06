package com.example.habithealth

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var chart: BarChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitHistoryAdapter

    private val habitList = mutableListOf<HabitRecord>()
    private val userId = FirebaseAuth.getInstance().currentUser?.uid ?: "unknown"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_history)
        setupWindowInsets()

        chart = findViewById(R.id.barChart)
        recyclerView = findViewById(R.id.historyRecycler)
        setupRecyclerView()

        db = FirebaseFirestore.getInstance()
        fetchHabitHistory()
    }

    private fun setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HabitHistoryAdapter(habitList)
        recyclerView.adapter = adapter
    }

    private fun fetchHabitHistory() {
        db.collection("users").document(userId).collection("habits")
            .get()
            .addOnSuccessListener { result ->
                habitList.clear()
                val barEntries = mutableListOf<BarEntry>()
                val dateLabels = mutableListOf<String>()

                result.forEachIndexed { index, doc ->
                    val record = parseHabitRecord(doc.getString("date"),
                        doc.getBoolean("water"),
                        doc.getBoolean("exercise"),
                        doc.getBoolean("sleep")) ?: return@forEachIndexed

                    habitList.add(record)
                    barEntries.add(createBarEntry(index, record))
                    dateLabels.add(record.date)
                }

                updateChart(barEntries, dateLabels)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                Log.e("HistoryActivity", "Error fetching history", it)
            }
    }

    private fun parseHabitRecord(
        date: String?,
        water: Boolean?,
        exercise: Boolean?,
        sleep: Boolean?
    ): HabitRecord? {
        if (date == null) return null
        return HabitRecord(
            date,
            water ?: false,
            exercise ?: false,
            sleep ?: false,

            habitsCompleted = TODO(),
            totalHabits = TODO()
        )
    }

    private fun createBarEntry(index: Int, record: HabitRecord): BarEntry {
        val completedCount = listOf(record.water, record.exercise, record.sleep).count { it }
        val percent = (completedCount / 3f) * 100
        return BarEntry(index.toFloat(), percent)
    }

    private fun updateChart(barEntries: List<BarEntry>, dateLabels: List<String>) {
        val dataSet = BarDataSet(barEntries, "Daily Completion %")
        chart.apply {
            data = BarData(dataSet)
            description.isEnabled = false
            animateY(1000)
            xAxis.apply {
                valueFormatter = IndexAxisValueFormatter(dateLabels)
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
            }
            invalidate()
        }
    }
}


