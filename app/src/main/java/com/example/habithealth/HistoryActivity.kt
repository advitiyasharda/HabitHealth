package com.example.habithealth

import android.os.Bundle
import android.util.Log

import androidx.appcompat.app.AppCompatActivity

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



    private lateinit var chart: BarChart
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: HabitHistoryAdapter

    private val habitList = mutableListOf<HabitRecord>()

    private val firestore by lazy { FirebaseFirestore.getInstance() }
    private val userId: String by lazy { FirebaseAuth.getInstance().currentUser?.uid ?: "" }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)

        chart = findViewById(R.id.barChart)
        recyclerView = findViewById(R.id.historyRecycler)

        setupRecyclerView()
        setupChart()
        fetchHabitHistory()
    }

    private fun setupRecyclerView() {
        recyclerView.layoutManager = LinearLayoutManager(this)
        adapter = HabitHistoryAdapter(habitList)
        recyclerView.adapter = adapter
    }


    private fun setupChart() {
        chart.apply {
            description.isEnabled = false
            setDrawGridBackground(false)
            setDrawBorders(false)
            legend.isEnabled = false
            animateY(1000)
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                granularity = 1f
                setDrawLabels(true)
                textSize = 12f
                setDrawGridLines(false)
            }
            axisLeft.textSize = 12f
            axisRight.isEnabled = false
        }
    }

    private fun fetchHabitHistory() {
        if (userId.isEmpty()) {
            Log.e("HistoryActivity", "User not logged in")
            return
        }
        firestore.collection("users").document(userId).collection("habits")

            .get()
            .addOnSuccessListener { result ->
                habitList.clear()
                val barEntries = mutableListOf<BarEntry>()
                val dateLabels = mutableListOf<String>()

                result.forEachIndexed { index, doc ->

                    val timestamp = doc.getLong("date") ?: return@forEachIndexed
                    val formattedDate = formatDate(timestamp)

                    val water = doc.getBoolean("water") ?: false
                    val exercise = doc.getBoolean("exercise") ?: false
                    val sleep = doc.getBoolean("sleep") ?: false

                    habitList.add(HabitRecord(formattedDate, water, exercise, sleep))

                    val completed = listOf(water, exercise, sleep).count { it }
                    val percent = (completed / 3f) * 100
                    barEntries.add(BarEntry(index.toFloat(), percent))
                    dateLabels.add(formattedDate)

                }

                updateChart(barEntries, dateLabels)
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {

                Log.e("HistoryActivity", "Failed to fetch habits", it)
            }
    }

    private fun updateChart(barEntries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(barEntries, "Daily Completion %").apply {
            color = getColor(R.color.teal_700)
            valueTextSize = 12f
            valueTextColor = getColor(R.color.black)
        }
        chart.apply {
            data = BarData(dataSet)
            xAxis.valueFormatter = IndexAxisValueFormatter(labels)
            invalidate()
        }
    }

    private fun formatDate(timestamp: Long): String {
        val sdf = java.text.SimpleDateFormat("dd MMM", java.util.Locale.getDefault())
        return sdf.format(java.util.Date(timestamp))
    }
}

