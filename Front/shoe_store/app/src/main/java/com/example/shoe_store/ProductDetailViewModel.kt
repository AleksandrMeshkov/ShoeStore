package com.example.shoe_store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoe_store.models.Product
import com.example.shoe_store.service.RetrofitClient
import kotlinx.coroutines.launch

class ProductDetailViewModel : ViewModel() {
    private val _product = MutableLiveData<Product>()
    val product: LiveData<Product> = _product

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    fun fetchProductById(productId: Int) {
        _isLoading.value = true
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getProductById(productId)
                if (response.isSuccessful) {
                    _product.value = response.body()
                } else {
                    _error.value = "Ошибка при получении данных о товаре: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}

