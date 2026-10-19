package com.proyecto.navi

import android.app.Application
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.proyecto.navi.data.NaviDatabase
import com.proyecto.navi.data.ProfileRepository
import com.proyecto.navi.network.uploadImageToCloudinary
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Estado de la subida a Cloudinary, para que la UI pueda mostrar progreso. */
sealed interface UploadState {
    data object Idle : UploadState
    data object Uploading : UploadState
    data class Error(val message: String) : UploadState
}

/**
 * Todo lo que la tarjeta de perfil necesita, en un objeto. Se expone esto en vez
 * de dos flows sueltos para que los @Preview puedan armarlo a mano.
 */
data class ProfileUiState(
    val name: String = "",
    val subtitle: String = "",
    val avatarUrl: String? = null,
    val uploadState: UploadState = UploadState.Idle
)

/**
 * Guarda el Application (no una Activity ni un Composable) para poder leer el
 * ContentResolver al subir la imagen. Un Application vive tanto como el proceso,
 * así que no hay riesgo de fuga de memoria.
 */
class ProfileViewModel(
    private val application: Application,
    private val repository: ProfileRepository
) : ViewModel() {

    private val _uploadState = MutableStateFlow<UploadState>(UploadState.Idle)

    val uiState: StateFlow<ProfileUiState> =
        combine(repository.profile, _uploadState) { profile, upload ->
            ProfileUiState(
                name = profile?.name.orEmpty(),
                subtitle = profile?.subtitle.orEmpty(),
                avatarUrl = profile?.avatarUrl,
                uploadState = upload
            )
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ProfileUiState()
        )

    /**
     * Sube la foto elegida y guarda la URL resultante en Room.
     *
     * El orden importa: primero sube, después persiste. Si la subida falla no se
     * toca la base, así que el avatar anterior queda intacto en vez de perderse.
     *
     * Corre en viewModelScope: si el usuario rota la pantalla a mitad de la
     * subida, la corrutina sigue viva y el resultado igual llega.
     */
    fun uploadAvatar(uri: Uri) {
        viewModelScope.launch {
            _uploadState.value = UploadState.Uploading
            _uploadState.value = try {
                val url = uploadImageToCloudinary(application, uri)
                repository.setAvatarUrl(url)
                UploadState.Idle
            } catch (e: Exception) {
                UploadState.Error(e.message ?: "Error desconocido")
            }
        }
    }

    fun dismissError() {
        _uploadState.value = UploadState.Idle
    }

    companion object {
        val Factory = viewModelFactory {
            initializer {
                val application = checkNotNull(this[APPLICATION_KEY])
                val dao = NaviDatabase.getInstance(application).profileDao()
                ProfileViewModel(application, ProfileRepository(dao))
            }
        }
    }
}
