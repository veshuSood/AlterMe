package com.example.alterme_yourdigitaltwin

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*

class ResultActivity : AppCompatActivity() {
    private lateinit var resultTitle: TextView
    private lateinit var personalityText: TextView
    private lateinit var retryBtn: Button
    private var isRequestInProgress = false
    private var answers: ArrayList<String> = arrayListOf()
    private val maxRetryLimit = 3

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_result_activity)

        resultTitle = findViewById(R.id.resultTitle)
        personalityText = findViewById(R.id.personalityText)
        retryBtn = findViewById(R.id.retryBtn)

        answers = intent.getStringArrayListExtra("ANSWERS") ?: arrayListOf()
        val prompt = buildPrompt(answers)

        generatePersonalityFromOpenRouter(prompt)



        retryBtn.setOnClickListener {
            if (!isRequestInProgress) {
                personalityText.text = "Retrying..."
                generatePersonalityFromOpenRouter(buildPrompt(answers))
            }
        }
    }

    private fun buildPrompt(answers: List<String>): String {
        return buildString {
            append(" genetrate the summary by Analyzing the personality based on the following quiz responses:\n")
            answers.forEachIndexed { index, answer ->
                append("${index + 1}. $answer\n")
            }
            append(
                "\nBased on this, predict the user's personality type or core trait.\n" +
                        "Return:\n1. A title for the personality\n2. A 2-3 line personality summary\n3. A personalized motivational quote."
            )
        }
    }

    private fun generatePersonalityFromOpenRouter(prompt: String, retryCount: Int = 0) {
        if (isRequestInProgress) return
        isRequestInProgress = true

        val messages = listOf(Message("user", prompt))
        val request = ChatRequest(model = "deepseek/deepseek-r1-0528-qwen3-8b:free", messages = messages)

        OpenRouterClient.api.generateChat(request).enqueue(object : Callback<ChatResponse> {
            override fun onResponse(call: Call<ChatResponse>, response: Response<ChatResponse>) {
                isRequestInProgress = false
                if (response.isSuccessful) {
                    val result = response.body()?.choices?.firstOrNull()?.message?.content
                    resultTitle.text = "Your Digital Twin"
                    personalityText.text = result ?: "No response"
                    personalityText.movementMethod = android.text.method.ScrollingMovementMethod()
                } else if (response.code() == 429 && retryCount < maxRetryLimit) {
                    personalityText.text = "Rate limited. Retrying..."
                    Handler(Looper.getMainLooper()).postDelayed({
                        generatePersonalityFromOpenRouter(prompt, retryCount + 1)
                    }, 6000)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: "Unknown Error"
                    personalityText.text = "OpenRouter API error: ${response.code()}"
                    Log.e("OpenRouter", "Error: $errorMsg")
                }
            }

            override fun onFailure(call: Call<ChatResponse>, t: Throwable) {
                isRequestInProgress = false
                personalityText.text = "Failed: ${t.message}"
                Log.e("OpenRouter", "Failure", t)
            }
        })
    }

    // --- DATA MODELS ---
    data class Message(val role: String, val content: String)
    data class ChatRequest(val model: String, val messages: List<Message>)
    data class ChatResponse(val choices: List<Choice>)
    data class Choice(val message: Message, val finish_reason: String?)

    // --- Retrofit Interface ---
    interface OpenRouterApi {
        @Headers(
            "Content-Type: application/json",
            "Accept: application/json", // ✅ Required
            "Authorization: Bearer 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", // 🔑 Replace with your real key
            "HTTP-Referer: https://alterme.digitaltwin", // ✅ Referer domain
            "X-Title: AlterMe App"
        )
        @POST("v1/chat/completions")
        fun generateChat(@Body request: ChatRequest): Call<ChatResponse>
    }

    object OpenRouterClient {
        private const val BASE_URL = "https://openrouter.ai/api/"

        val api: OpenRouterApi by lazy {
            Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(OpenRouterApi::class.java)
        }
    }
}
