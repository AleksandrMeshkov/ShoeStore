package com.example.shoe_store

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoe_store.models.Product
import com.example.shoe_store.service.RetrofitClient
import kotlinx.coroutines.launch

class HomeViewModel : ViewModel() {
    private val apiService = RetrofitClient.instance
    private val _products = MutableLiveData<List<Product>>()
    private val _isLoading = MutableLiveData<Boolean>()
    private val _error = MutableLiveData<String?>()

    val products: LiveData<List<Product>> = _products
    val isLoading: LiveData<Boolean> = _isLoading
    val error: LiveData<String?> = _error

    fun fetchProducts() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null
            try {
                val response = apiService.getProducts()
                if (response.isSuccessful) {
                    _products.value = response.body()
                } else {
                    _error.value = "Failed to load products: ${response.message()}"
                }
            } catch (e: Exception) {
                _error.value = "Network error: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }
}