package com.example.quiz

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.icu.util.Calendar
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.quiz.databinding.ActivityAdminPanelBinding
import com.google.firebase.database.FirebaseDatabase
import java.util.UUID

class AdminPanelActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAdminPanelBinding
    private lateinit var database: FirebaseDatabase
    private val calendar = Calendar.getInstance()
    private lateinit var questionsAdapter: QuestionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAdminPanelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        database = FirebaseDatabase.getInstance()

        setupRecyclerView()
        setupListeners()
        setupDifficultySpinner()

        binding.btnSaveQuiz.setOnClickListener {
            val questionText = binding.edtQuestion.text.toString().trim()
            val options = listOf(
                binding.edtOption1.text.toString().trim(),
                binding.edtOption2.text.toString().trim(),
                binding.edtOption3.text.toString().trim(),
                binding.edtOption4.text.toString().trim()
            )

            val radioGroup = findViewById<RadioGroup>(R.id.correctOptionRadioGroup)
            val selectedOptionId = radioGroup.checkedRadioButtonId
            val correctOption = when (selectedOptionId) {
                R.id.option1RadioButton -> options[0]
                R.id.option2RadioButton -> options[1]
                R.id.option3RadioButton -> options[2]
                R.id.option4RadioButton -> options[3]
                else -> ""
            }
            val difficulty = binding.difficultySpinner.selectedItem.toString()
            if (questionText.isNotEmpty() && options.none { it.isBlank() } && selectedOptionId != -1) {
                val newQuestion = Question(UUID.randomUUID().toString(), questionText, options, correctOption, difficulty)
                saveQuestionToFirebase(newQuestion)
            } else {
                Toast.makeText(this, "Please ensure all fields are filled and a correct option is selected.", Toast.LENGTH_LONG).show()
            }
        }

        binding.btnAddQuestion.setOnClickListener{
            val intent = Intent(this, AdminPanelActivity::class.java)
            startActivity(intent)
        }

        binding.btnLogoutAdmin.setOnClickListener {
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
        }

    }

    private fun setupDifficultySpinner() {
        val difficultyLevels = arrayOf("Easy", "Medium", "Hard")
        val difficultyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, difficultyLevels)
        difficultyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.difficultySpinner.adapter = difficultyAdapter
    }

    private fun setupRecyclerView() {
        questionsAdapter = QuestionsAdapter(mutableListOf())
        binding.questionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(this@AdminPanelActivity)
            adapter = questionsAdapter
        }
    }

    private fun setupListeners() {
        binding.btnPickDate.setOnClickListener {
            showDatePicker()
        }
        binding.btnPickTime.setOnClickListener {
            showTimePicker()
        }
//        binding.btnSaveQuiz.setOnClickListener {
//            saveQuiz()
//        }
    }

    private fun showDatePicker() {
        DatePickerDialog(
            this@AdminPanelActivity,
            { _, year, month, dayOfMonth ->
                calendar.set(Calendar.YEAR, year)
                calendar.set(Calendar.MONTH, month)
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun showTimePicker() {
        TimePickerDialog(this@AdminPanelActivity, { _, hourOfDay, minute ->
            calendar.set(Calendar.HOUR_OF_DAY, hourOfDay)
            calendar.set(Calendar.MINUTE, minute)
        }, calendar.get(Calendar.HOUR_OF_DAY), calendar.get(Calendar.MINUTE), false).show()
    }

    private fun saveQuiz() {
        val quizName = binding.edtQuizName.text.toString()
        val difficulty = binding.difficultySpinner.selectedItem.toString()
//        val duration = binding.edtDuration.text.toString().toInt()
        val scheduledDate = calendar.time

        val quizMap = mapOf(
            "name" to quizName,
            "difficulty" to difficulty,
            "scheduledDate" to scheduledDate.toString(),
//            "duration" to duration
        )

        database.reference.child("quizzes").push().setValue(quizMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Quiz saved successfully.", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to save quiz: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun saveQuestionToFirebase(question: Question) {
        val questionMap = mapOf(
            "id" to question.id,
            "text" to question.text,
            "options" to question.options,
            "correctOption" to question.correctOption,
            "difficulty" to question.difficulty
        )

        database.reference.child("questions").child(question.id).setValue(questionMap)
            .addOnSuccessListener {
                Toast.makeText(this, "Question saved successfully.", Toast.LENGTH_SHORT).show()
                questionsAdapter.addQuestion(question) // Update RecyclerView with new question
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Error saving question: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}