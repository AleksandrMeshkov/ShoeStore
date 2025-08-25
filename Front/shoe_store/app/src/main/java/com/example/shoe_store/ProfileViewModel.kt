package com.example.shoe_store

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.shoe_store.models.User
import com.example.shoe_store.service.RetrofitClient
import com.example.shoe_store.utils.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class ProfileViewModel : ViewModel() {
    private val _user = MutableStateFlow<User?>(null)
    val user: StateFlow<User?> = _user.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun fetchUserById(context: Context, userId: Int) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val response = RetrofitClient.instance.getUserById(userId)
                if (response.isSuccessful) {
                    response.body()?.let { user ->
                        SessionManager.saveUser(context, user)
                        _user.value = user
                    } ?: run {
                        _error.value = "Пустой ответ от сервера"
                    }
                } else {
                    _error.value = "Ошибка загрузки: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun loadUserFromSession(context: Context) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            try {
                val savedUser = SessionManager.getUser(context)
                _user.value = savedUser
                if (savedUser == null) {
                    _error.value = "Пользователь не авторизован"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка загрузки данных: ${e.message}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun updateUserProfile(
        context: Context,
        userId: Int,
        login: String,
        password: String,
        name: String,
        surname: String,
        patronymic: String?,
        photoUri: Uri?
    ) {
        _isUpdating.value = true
        _error.value = null
        _updateSuccess.value = false
        viewModelScope.launch {
            try {
                val response = if (photoUri != null) {
                    val photoFile = photoUri.toMultipartBodyPart(context)
                    RetrofitClient.instance.updateUserWithPhoto(
                        userId = userId,
                        login = login,
                        password = password,
                        name = name,
                        surname = surname,
                        patronymic = patronymic,
                        photoFile = photoFile
                    )
                } else {
                    RetrofitClient.instance.updateUserWithoutPhoto(
                        userId = userId,
                        login = login,
                        password = password,
                        name = name,
                        surname = surname,
                        patronymic = patronymic
                    )
                }

                if (response.isSuccessful) {
                    response.body()?.let { updatedUser ->
                        SessionManager.saveUser(context, updatedUser)
                        _updateSuccess.value = true
                    } ?: run {
                        _error.value = "Пустой ответ от сервера"
                    }
                } else {
                    _error.value = "Ошибка обновления: ${response.code()}"
                }
            } catch (e: Exception) {
                _error.value = "Ошибка сети: ${e.message}"
            } finally {
                _isUpdating.value = false
            }
        }
    }

    private fun Uri.toMultipartBodyPart(context: Context): MultipartBody.Part {
        val file = File(context.cacheDir, "temp_photo_${System.currentTimeMillis()}.jpg")
        context.contentResolver.openInputStream(this)?.use { input ->
            file.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        val requestFile = RequestBody.create(
            context.contentResolver.getType(this)?.toMediaTypeOrNull()
                ?: "image/*".toMediaTypeOrNull(),
            file
        )

        return MultipartBody.Part.createFormData(
            "photo_file",
            file.name,
            requestFile
        )
    }

    fun resetUpdateStatus() {
        _updateSuccess.value = false
    }
}
