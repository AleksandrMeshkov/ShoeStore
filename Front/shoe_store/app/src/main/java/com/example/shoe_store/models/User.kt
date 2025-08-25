package com.example.shoe_store.models

data class User(
    val UsersID: Int,
    val Login: String,
    val Password: String,
    val Name: String,
    val Surname: String,
    val Patronymic: String?,
    val Photo: String?
)
