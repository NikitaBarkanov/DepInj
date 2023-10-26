package ru.netology.nmedia.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.repository.PostRepository
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    fun saveIdAndToken(id: String, token: String) {
        viewModelScope.launch {
            try {
                repository.setIdAndTokenToAuth(id, token)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}