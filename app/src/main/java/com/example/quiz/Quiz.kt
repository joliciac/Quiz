package com.example.quiz

data class Quiz(
    val id: String = "", // Optional, depends on if you need to reference the ID later
    val name: String = "",
    val description: String = "",
    val questions: List<Question> = listOf()
)


