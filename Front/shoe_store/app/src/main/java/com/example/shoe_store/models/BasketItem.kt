package com.example.shoe_store.models

data class BasketItem(
    val BasketID: Int,
    val ProductID: Int,
    val UsersID: Int,
    val product: Product,
    var quantity: Int = 1
)

