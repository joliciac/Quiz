package com.example.quiz

data class Question(
    val id: String = "",
    val text: String = "",
    val options: List<String> = listOf(),
    val correctOption: String = "",
    var difficulty: String = ""
)



