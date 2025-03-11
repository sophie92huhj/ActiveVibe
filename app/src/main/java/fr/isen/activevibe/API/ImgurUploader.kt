package fr.isen.activevibe.API

import android.content.Context
import android.net.Uri
import android.widget.Toast
import fr.isen.activevibe.network.ImgurResponse
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object ImgurUploader {
    private const val BASE_URL = "https://api.imgur.com/"
    private const val CLIENT_ID = "792eec281e7cd90"  // Remplacez par votre Client-ID Imgur

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val service: ImgurApiService = retrofit.create(ImgurApiService::class.java)

    fun uploadToImgur(context: Context, imageUri: Uri, onSuccess: (String) -> Unit, onFailure: () -> Unit) {
        val file = createTempFileFromUri(context, imageUri) ?: run {
            Toast.makeText(context, "Impossible de charger l'image", Toast.LENGTH_SHORT).show()
            return
        }

        val requestFile = file.asRequestBody("image/*".toMediaTypeOrNull())
        val body = MultipartBody.Part.createFormData("image", file.name, requestFile)

        service.uploadImage("Client-ID $CLIENT_ID", body).enqueue(object : Callback<ImgurResponse> {
            override fun onResponse(call: Call<ImgurResponse>, response: Response<ImgurResponse>) {
                if (response.isSuccessful) {
                    onSuccess(response.body()?.data?.link ?: "")
                } else {
                    onFailure()
                }
            }

            override fun onFailure(call: Call<ImgurResponse>, t: Throwable) {
                onFailure()
            }
        })
    }

    private fun createTempFileFromUri(context: Context, uri: Uri): File? {
        return try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val file = File(context.cacheDir, "temp_image.jpg")
            val outputStream = FileOutputStream(file)

            inputStream?.copyTo(outputStream)
            inputStream?.close()
            outputStream.close()

            file
        } catch (e: Exception) {
            null
        }
    }
}