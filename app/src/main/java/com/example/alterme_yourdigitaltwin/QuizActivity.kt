package com.example.alterme_yourdigitaltwin

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ProgressBar
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class QuizActivity : AppCompatActivity() {
    private lateinit var QNo: TextView
    private lateinit var Question: TextView
    private lateinit var Next: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var progressBar: ProgressBar

    private var currentQuestionIndex = 0
    private var score = 0
 //   private val selectedOptions = mutableListOf<Int>()
    //for gpt storing answers
 private val selectedAnswers = ArrayList<String>()
    data class Ques(
        val questionText: String,
        val options: List<String>
    )

    private val questions = listOf(
        Ques("How do you react to stress?", listOf("Stay calm", "Get anxious", "Avoid it")),
        Ques("What motivates you the most?", listOf("Growth", "Recognition", "Security")),
        Ques("How do you make decisions?", listOf("Logical thinking", "Emotions", "Impulsively")),
        Ques("How do you prefer to work?", listOf("Alone", "In teams", "Flexible")),
        Ques("Which describes you best?", listOf("Creative", "Practical", "Empathetic")),
        Ques("What’s your ideal weekend?", listOf("Exploring new hobbies", "Hanging out with friends", "Relaxing at home")),
        Ques("How do you handle failure?", listOf("Learn from it", "Feel demotivated", "Blame circumstances")),
        Ques("What type of content do you prefer?", listOf("Self-help and productivity", "Comedy and memes", "Motivational and emotional")),
        Ques("In a group project, your role is usually…", listOf("Leader", "Supporter", "Idea generator")),
        Ques("What’s your strongest trait?", listOf("Resilience", "Compassion", "Adaptability"))
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz)

        QNo = findViewById(R.id.QNo)
        Question = findViewById(R.id.Question)
        Next = findViewById(R.id.Next)
        radioGroup = findViewById(R.id.radioGroup)
        progressBar = findViewById(R.id.progressBar)

        loadQuestion()

        Next.setOnClickListener {
            if (radioGroup.checkedRadioButtonId != -1) {
               // val selectedAnswerIndex = radioGroup.indexOfChild(findViewById(radioGroup.checkedRadioButtonId))
                //selectedOptions.add(selectedAnswerIndex) // Add selection instead of scoring
                // to get options in txt
                val selectedRadioButton = findViewById<RadioButton>(radioGroup.checkedRadioButtonId)
                val selectedText = selectedRadioButton.text.toString()
                selectedAnswers.add(selectedText)
                currentQuestionIndex++
                if (currentQuestionIndex < questions.size) {
                    loadQuestion()
                } else {
                    goToResult()
                }
            } else {
                Toast.makeText(this, "Please select an option", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadQuestion() {
        val question = questions[currentQuestionIndex]
        val fadeIn = AnimationUtils.loadAnimation(this, R.anim.fade_in)
        Question.startAnimation(fadeIn)

        QNo.text = "QNo ${currentQuestionIndex + 1}"
        Question.text = question.questionText

        radioGroup.removeAllViews()
        for (option in question.options) {
            val rb = RadioButton(this)
            rb.text = option
            rb.textSize = 16f
            rb.setTextColor(Color.parseColor("#333333"))
            rb.setBackgroundResource(R.drawable.option_selector_bg)
            rb.setPadding(20, 20, 20, 20)
            radioGroup.addView(rb)
        }

        val progress = ((currentQuestionIndex + 1).toDouble() / questions.size * 100).toInt()
        progressBar.progress = progress
    }

    private fun goToResult() {
        val intent = Intent(this, ResultActivity::class.java)
     //   intent.putIntegerArrayListExtra("ANSWERS", ArrayList(selectedOptions))
        //for gpt dynamic answers
        intent.putStringArrayListExtra("ANSWERS", selectedAnswers)
        startActivity(intent)

        finish()
    }
}
