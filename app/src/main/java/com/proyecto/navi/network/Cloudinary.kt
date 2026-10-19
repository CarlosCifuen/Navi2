package com.proyecto.navi.network

import android.content.Context
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path

/**
 * Credenciales de Cloudinary.
 *
 * IMPORTANTE sobre seguridad:
 * - El cloud name y un upload preset *unsigned* NO son secretos. Cualquier sitio
 *   web que suba desde el navegador los expone igual; Cloudinary los diseñó para
 *   ser públicos. Por eso está bien tenerlos acá.
 * - El API SECRET nunca debe estar en la app. Firmar uploads requiere un backend
 *   que genere la firma; el secreto en un APK se extrae en minutos.
 *
 * El preset debe estar creado en el dashboard de Cloudinary como "Unsigned"
 * (Settings → Upload → Upload presets). El nombre es sensible a mayúsculas.
 */
object CloudinaryConfig {
    const val CLOUD_NAME = "tv2ayucc"
    const val UPLOAD_PRESET = "navi_avatars"
}

/**
 * La respuesta de Cloudinary trae ~25 campos; acá solo se declaran los dos que
 * se usan. `ignoreUnknownKeys = true` en el Json es lo que evita que kotlinx
 * explote por todos los demás.
 */
@Serializable
data class CloudinaryUploadResponse(
    @SerialName("secure_url") val secureUrl: String,
    @SerialName("public_id") val publicId: String
)

interface CloudinaryApi {
    /**
     * POST https://api.cloudinary.com/v1_1/{cloudName}/image/upload
     * con dos campos multipart: `file` (los bytes) y `upload_preset`.
     */
    @Multipart
    @POST("v1_1/{cloudName}/image/upload")
    suspend fun uploadImage(
        @Path("cloudName") cloudName: String,
        @Part file: MultipartBody.Part,
        @Part("upload_preset") uploadPreset: RequestBody
    ): CloudinaryUploadResponse
}

object CloudinaryClient {
    private val json = Json { ignoreUnknownKeys = true }

    val api: CloudinaryApi by lazy {
        Retrofit.Builder()
            .baseUrl("https://api.cloudinary.com/")
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(CloudinaryApi::class.java)
    }
}

/**
 * Sube la imagen apuntada por [uri] y devuelve su `secure_url`.
 *
 * El photo picker entrega un content:// Uri, no una ruta de archivo, así que hay
 * que leerlo por el ContentResolver. Para una foto de perfil cargar los bytes en
 * memoria está bien; para archivos grandes convendría streamear con un
 * RequestBody propio en vez de un ByteArray.
 *
 * Lanza excepción si falla (sin red, preset mal escrito, imagen ilegible); el
 * ViewModel la atrapa y la convierte en estado de error.
 */
suspend fun uploadImageToCloudinary(context: Context, uri: Uri): String =
    withContext(Dispatchers.IO) {
        val bytes = context.contentResolver.openInputStream(uri)?.use { it.readBytes() }
            ?: error("No se pudo leer la imagen seleccionada")

        val mimeType = context.contentResolver.getType(uri) ?: "image/*"

        val filePart = MultipartBody.Part.createFormData(
            name = "file",
            filename = "avatar",
            body = bytes.toRequestBody(mimeType.toMediaType())
        )
        val presetPart = CloudinaryConfig.UPLOAD_PRESET
            .toRequestBody("text/plain".toMediaType())

        CloudinaryClient.api
            .uploadImage(CloudinaryConfig.CLOUD_NAME, filePart, presetPart)
            .secureUrl
    }
