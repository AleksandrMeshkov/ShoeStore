package com.example.shoe_store.models

data class UserRegistration(
    val Login: String,
    val Password: String,
    val Name: String,
    val Surname: String,
    val Patronymic: String? = null
)
