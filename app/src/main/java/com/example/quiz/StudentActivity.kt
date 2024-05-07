package com.example.quiz

import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.quiz.databinding.ActivityStudentBinding
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class StudentActivity : AppCompatActivity() {

    private lateinit var binding: ActivityStudentBinding
    private lateinit var timer: CountDownTimer
    private var questionsList = mutableListOf<Question>()
    private var allQuestions = mutableListOf<Question>()
    private var currentQuestionIndex = 0
    private var score = 0
    private var selectedAnswers: MutableList<Int?> = mutableListOf()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityStudentBinding.inflate(layoutInflater)
        setContentView(binding.root)

        fetchQuestions()
        setupNavigationButtons()

    }

    private fun fetchQuestions() {
        val databaseReference = FirebaseDatabase.getInstance().getReference("questions")
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    allQuestions.clear()
                    for (snapshot in dataSnapshot.children) {
                        val question = snapshot.getValue(Question::class.java)
                        question?.let { allQuestions.add(it) }
                    }
                    selectRandomQuestions(5)
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Toast.makeText(
                    this@StudentActivity,
                    "Error loading questions: ${databaseError.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun selectRandomQuestions(count: Int) {
        questionsList.clear()
        selectedAnswers.clear()
        allQuestions.shuffle()
        questionsList.addAll(allQuestions.take(count))  // Take the first 'count' elements after shuffling
        questionsList.forEach { _ -> selectedAnswers.add(null) }

        if (questionsList.isNotEmpty()) {
            displayQuestion(0)
            startTimer(2 * 60000)
        }
    }

    private fun displayQuestion(index: Int) {
        if (index in questionsList.indices) {
            val question = questionsList[index]
            binding.txtQuestion.text = question.text
            binding.txtDifficulty.text = "Difficulty: ${question.difficulty}"
            binding.optionsRadioGroup.removeAllViews()
            Log.d("StudentActivity", "Displaying question at index $index: ${question.text}")

            question.options.forEachIndexed { idx, option ->
                val radioButton = RadioButton(this).apply {
                    text = option
                    tag = idx
                    layoutParams = RadioGroup.LayoutParams(
                        RadioGroup.LayoutParams.MATCH_PARENT,
                        RadioGroup.LayoutParams.WRAP_CONTENT
                    )
                }
                binding.optionsRadioGroup.addView(radioButton)
            }
        }
    }

    private fun setupNavigationButtons() {
        binding.btnNext.setOnClickListener {
            checkCurrentAnswer()
            if (currentQuestionIndex < questionsList.size - 1) {
                currentQuestionIndex++
                displayQuestion(currentQuestionIndex)
                Log.d("StudentActivity", "Next Question: New Index=$currentQuestionIndex")
            } else {
                Log.d("StudentActivity", "End of Quiz reached on Next button")
                finishQuiz()
            }
        }

        binding.btnPrev.setOnClickListener {
            if (currentQuestionIndex > 0) {
                currentQuestionIndex--
                displayQuestion(currentQuestionIndex)
                Log.d("StudentActivity", "Previous Question: New Index=$currentQuestionIndex")
            } else {
                Log.d("StudentActivity", "Already at the first question, can't go back further")
            }
        }
    }

    private fun startTimer(duration: Long) {
        timer = object : CountDownTimer(duration, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val minutes = (millisUntilFinished / 1000) / 60
                val seconds = (millisUntilFinished / 1000) % 60
                binding.txtTimer.text = String.format("Time Left: %02d:%02d", minutes, seconds)
            }

            override fun onFinish() {
                Log.d("StudentActivity", "Timer finished")
                checkCurrentAnswer()
                finishQuiz()
            }
        }.start()
    }

    private fun checkCurrentAnswer() {
        val selectedRadioButtonId = binding.optionsRadioGroup.checkedRadioButtonId
        if (selectedRadioButtonId != -1) {
            val radioButton = findViewById<RadioButton>(selectedRadioButtonId)
            if (radioButton != null) {  // Check if the radioButton is not null
                val selectedIndex = radioButton.tag as? Int  // Safe cast to Int
                if (selectedIndex != null) {
                    selectedAnswers[currentQuestionIndex] = selectedIndex
                    val correctOption = questionsList[currentQuestionIndex].correctOption
                    val correctOptionIndex = questionsList[currentQuestionIndex].options.indexOf(correctOption)

                    Log.d("StudentActivity", "Checking answer: Selected Index=$selectedIndex, Correct Index=$correctOptionIndex, Selected Option='${radioButton.text}', Correct Option='$correctOption'")
                    if (selectedIndex == correctOptionIndex) {
                        score++
                    }
                } else {
                    Log.d("StudentActivity", "RadioButton tag is not an integer")
                }
            } else {
                Log.d("StudentActivity", "No RadioButton found for ID: $selectedRadioButtonId")
            }
        } else {
            Log.d("StudentActivity", "No RadioButton selected")
        }
    }

    private fun finishQuiz() {
        Log.d("StudentActivity", "Finishing quiz")
        timer.cancel()

        val feedbackBuilder = StringBuilder()
        feedbackBuilder.append("Your score is $score out of ${questionsList.size}.\n\n")

        questionsList.forEachIndexed { index, question ->
            val selectedIndex = selectedAnswers[index]
            feedbackBuilder.append("Question: ${question.text}\n")
            if (selectedIndex != null) {
                val selectedOption = question.options[selectedIndex]
                feedbackBuilder.append("Your Answer: $selectedOption\n")
                feedbackBuilder.append("Correct Answer: ${question.correctOption}\n")
            } else {
                feedbackBuilder.append("No answer selected\n")
                feedbackBuilder.append("Correct Answer: ${question.correctOption}\n")
            }
            feedbackBuilder.append("\n")
        }

        AlertDialog.Builder(this)
            .setTitle("Quiz Complete")
            .setMessage(feedbackBuilder.toString())
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss(); finish() }
            .show()
    }
}

