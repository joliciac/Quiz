package com.example.quiz

//data class Question(
//    val id: String,
//    val text: String,
//    val options: List<String>,
//    val correctOption: String // You might store the correct option directly or just an index.
//)

data class Question(
    val id: String = "",
    val text: String = "",
    val options: List<String> = listOf(),
    val correctOption: String = "",
    var difficulty: String = ""// This could be an index or the text of the correct option
)



