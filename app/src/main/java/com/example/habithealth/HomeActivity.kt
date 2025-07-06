package com.example.habithealth

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.habithealth.R.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class HomeActivity : AppCompatActivity() {

    private lateinit var cbWater: CheckBox
    private lateinit var cbExercise: CheckBox
    private lateinit var cbSleep: CheckBox
    private lateinit var btnSave: Button
    private lateinit var tvWelcome: TextView
    private lateinit var btnHistory: Button
    private lateinit var btnLogout: Button


    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(layout.activity_home)

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
        cbWater = findViewById(id.cbWater)
        cbExercise = findViewById(id.cbExercise)
        cbSleep = findViewById(id.cbSleep)
        btnSave = findViewById(id.btnSaveHabits)
        tvWelcome = findViewById(id.tvWelcome)

        // Welcome message
        tvWelcome.text = "Welcome, ${user.email}"

        btnHistory = findViewById(id.btnHistory)
        btnLogout = findViewById(id.btnLogout)

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
    }
}
