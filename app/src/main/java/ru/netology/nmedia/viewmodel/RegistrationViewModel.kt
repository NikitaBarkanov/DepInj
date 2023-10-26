package ru.netology.nmedia.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import ru.netology.nmedia.dto.MediaUpload
import ru.netology.nmedia.repository.PostRepository
import java.io.File
import javax.inject.Inject

@HiltViewModel
class RegistrationViewModel @Inject constructor(
    private val repository: PostRepository
) : ViewModel() {
    private val _registerImage = MutableLiveData<MediaUpload>()
    val registerImage: LiveData<MediaUpload>
        get() = _registerImage

    fun setRegisterImage(media: MediaUpload) {
        _registerImage.value = media
    }
    fun saveUserWithRegister(login: String, password: String, name: String, file: File) {
        viewModelScope.launch {
            try {
                repository.registerWithAvatar(login, password, name, file)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}