package com.example.habithealth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.habithealth.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import java.time.LocalDate
import java.time.Instant
import java.time.ZoneId
import android.util.Log
import com.google.firebase.ktx.Firebase
import com.google.firebase.firestore.ktx.firestore
class HomeActivity : AppCompatActivity() {

    private lateinit var cbWater: CheckBox
    private lateinit var cbExercise: CheckBox
    private lateinit var cbSleep: CheckBox
    private lateinit var btnSave: Button
    private lateinit var tvWelcome: TextView
    private lateinit var btnHistory: Button
    private lateinit var btnLogout: Button

    private lateinit var tvWaterStreak: TextView

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var prefs: android.content.SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)

        // Window inset padding (optional)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Firebase setup
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        val user = auth.currentUser
        val uid = user?.uid ?: return

        // View bindings
        cbWater = findViewById(R.id.cbWater)
        cbExercise = findViewById(R.id.cbExercise)
        cbSleep = findViewById(R.id.cbSleep)
        btnSave = findViewById(R.id.btnSaveHabits)
        tvWelcome = findViewById(R.id.tvWelcome)

//        tvWaterStreak = findViewById(R.id.tvWaterStreak)

        //prefs = getSharedPreferences("HabitPrefs", MODE_PRIVATE)

        // Welcome message
        tvWelcome.text = "Welcome, ${user.email}"

        btnHistory = findViewById(R.id.btnHistory)
        btnLogout = findViewById(R.id.btnLogout)

        btnHistory.setOnClickListener {
            startActivity(Intent(this, HistoryActivity::class.java))
        }

        btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        }

        // Save habit data to Firestore
        btnSave.setOnClickListener {
            val habitData = hashMapOf(
                "date" to System.currentTimeMillis(),
                "water" to cbWater.isChecked,
                "exercise" to cbExercise.isChecked,
                "sleep" to cbSleep.isChecked
            )

            firestore.collection("users")
                .document(uid)
                .collection("habits")
                .add(habitData)
                .addOnSuccessListener {
                    Toast.makeText(this, "Habits saved successfully!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(this, "Failed to save: ${it.message}", Toast.LENGTH_LONG).show()
                }
        }
        val streakTextView = findViewById<TextView>(R.id.streakTextView)
        val userId = FirebaseAuth.getInstance().currentUser?.uid
        val db = Firebase.firestore

        db.collection("users").document(userId!!)
            .collection("habits")
            .orderBy("date", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { documents ->
                var streak = 0
                var previousDate = LocalDate.now()
                for (document in documents) {
                    val timestamp = document.getLong("date") ?: continue
                    val date = Instant.ofEpochMilli(timestamp).atZone(ZoneId.systemDefault()).toLocalDate()

                    val water = document.getBoolean("water") ?: false
                    val sleep = document.getBoolean("sleep") ?: false
                    val exercise = document.getBoolean("exercise") ?: false

                    // Check if it's a consecutive day and all habits were done
                    if (date == previousDate || date == previousDate.minusDays(1)) {
                        if (water && sleep && exercise) {
                            streak++
                            previousDate = date
                        } else {
                            break
                        }
                    } else {
                        break
                    }
                }

                streakTextView.text = "Streak: $streak"
            }
            .addOnFailureListener {
                Log.e("STREAK", "Failed to fetch data", it)
            }

//        val waterStreak = prefs.getInt("waterStreak", 0)
//        tvWaterStreak.text = "Streak: $waterStreak"

//        findViewById<Button>(R.id.btnSaveHabits).setOnClickListener {
//            var streak = prefs.getInt("waterStreak", 0)
//            if (cbWater.isChecked) {
//                streak += 1
//            } else {
//                streak = 0
//            }
//            prefs.edit().putInt("waterStreak", streak).apply()
//            tvWaterStreak.text = "Streak: $streak"
//            // Repeat for other habits
//        }
    }
}
