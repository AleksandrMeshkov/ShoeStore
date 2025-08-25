package com.example.shoe_store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoe_store.models.BasketItem
import com.example.shoe_store.service.RetrofitClient
import kotlinx.coroutines.launch

class BasketViewModel : ViewModel() {
    private val _basketItems = MutableLiveData<List<BasketItem>>()
    val basketItems: LiveData<List<BasketItem>> = _basketItems

    private val _total = MutableLiveData<Double>()
    val total: LiveData<Double> = _total

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun increaseQuantity(userId: Int, basketItem: BasketItem) {
        viewModelScope.launch {
            try {
                val updatedItems = _basketItems.value?.map {
                    if (it.BasketID == basketItem.BasketID) {
                        it.copy(quantity = it.quantity + 1)
                    } else {
                        it
                    }
                }
                _basketItems.value = updatedItems ?: emptyList()
                calculateTotal()
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            }
        }
    }

    fun decreaseQuantity(userId: Int, basketItem: BasketItem) {
        viewModelScope.launch {
            try {
                if (basketItem.quantity > 1) {
                    val updatedItems = _basketItems.value?.map {
                        if (it.BasketID == basketItem.BasketID) {
                            it.copy(quantity = it.quantity - 1)
                        } else {
                            it
                        }
                    }
                    _basketItems.value = updatedItems ?: emptyList()
                    calculateTotal()
                } else {
                    removeFromBasket(userId, basketItem.ProductID)
                }
            } catch (e: Exception) {
                _error.value = "Ошибка: ${e.message}"
            }
        }
    }

    private fun calculateTotal() {
        _total.value = _basketItems.value?.sumOf {
            it.product.Price * it.quantity
        } ?: 0.0
    }

    fun fetchBasket(userId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getBasket(userId)
                if (response.isSuccessful) {
                    val items = response.body() ?: emptyList()
                    _basketItems.value = items
                    _total.value =
                        items.sumOf { it.product.Price.toDouble() }
                } else {
                    _error.value = "Ошибка при загрузке корзины"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }


    fun addToBasket(userId: Int, productId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.addToBasket(userId, productId)
                if (response.isSuccessful) {
                    fetchBasket(userId)
                } else {
                    try {
                        val errorBody = response.errorBody()?.string()
                        if (errorBody?.contains("Товар уже в корзине") == true) {
                            _error.value = "Этот товар уже в вашей корзине"
                        } else {
                            _error.value = "Ошибка при добавлении в корзину"
                        }
                    } catch (e: Exception) {
                        _error.value = "Ошибка при добавлении в корзину"
                    }
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            }
        }
    }

    fun removeFromBasket(userId: Int, productId: Int) {
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.deleteFromBasket(userId, productId)
                if (response.isSuccessful) {
                    fetchBasket(userId)
                } else {
                    _error.value = "Ошибка при удалении из корзины"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            }
        }
    }
}