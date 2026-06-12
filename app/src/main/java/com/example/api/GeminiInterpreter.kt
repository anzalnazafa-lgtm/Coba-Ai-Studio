package com.example.api

import android.util.Log
import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

object GeminiInterpreter {
    private const val TAG = "GeminiInterpreter"
    private const val MODEL_NAME = "gemini-3.5-flash"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL_NAME:generateContent"

    val apiKey: String
        get() = BuildConfig.GEMINI_API_KEY

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun interpretSurveyData(prompt: String): Result<String> = withContext(Dispatchers.IO) {
        val key = apiKey
        if (key.isEmpty() || key == "MY_GEMINI_API_KEY") {
            return@withContext Result.failure(Exception("API Key belum dikonfigurasi. Silakan tambahkan GEMINI_API_KEY di Panel Secrets AI Studio."))
        }

        val url = "$BASE_URL?key=$key"
        val mediaType = "application/json; charset=utf-8".toMediaType()

        // Build request body using org.json
        val requestJson = JSONObject().apply {
            val contentsArray = JSONArray().apply {
                val contentObj = JSONObject().apply {
                    val partsArray = JSONArray().apply {
                        val partObj = JSONObject().apply {
                            put("text", prompt)
                        }
                        put(partObj)
                    }
                    put("parts", partsArray)
                }
                put(contentObj)
            }
            put("contents", contentsArray)

            // Optional: Include System Instructions
            val systemInstructionObj = JSONObject().apply {
                val partsArray = JSONArray().apply {
                    val partObj = JSONObject().apply {
                        put("text", "Anda adalah Asisten Ahli Geofisika Lapangan profesional dan Dosen Senior Geofisika. Tugas Anda adalah membantu para operator/surveyor di lapangan menginterpretasikan data geofisika (seperti resistivitas/geolistrik, geomagnetik, seismik, gravitasi), menjelaskan litologi bawah permukaan, merekomendasikan spasi elektroda atau koreksi data, serta menjawab pertanyaan teoretis dan praktis. Berikan penjelasan yang akurat, sistematis, ramah, dan gunakan istilah teknis geofisika yang benar dalam Bahasa Indonesia.")
                    }
                    put(partObj)
                }
                put("parts", partsArray)
            }
            put("systemInstruction", systemInstructionObj)
            
            // Optional: Add configuration
            val configObj = JSONObject().apply {
                put("temperature", 0.7)
            }
            put("generationConfig", configObj)
        }

        val body = requestJson.toString().toRequestBody(mediaType)
        val request = Request.Builder()
            .url(url)
            .post(body)
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseStr = response.body?.string()
                if (!response.isSuccessful) {
                    Log.e(TAG, "Gagal memanggil API: ${response.code} - $responseStr")
                    return@withContext Result.failure(Exception("Kesalahan API (${response.code}): Silakan periksa kembali konfigurasi API Key Anda."))
                }

                if (responseStr == null) {
                    return@withContext Result.failure(Exception("Respons kosong dari server."))
                }

                val responseJson = JSONObject(responseStr)
                val candidates = responseJson.optJSONArray("candidates")
                if (candidates == null || candidates.length() == 0) {
                    return@withContext Result.failure(Exception("Tidak ada kandidat jawaban dalam respons."))
                }

                val content = candidates.getJSONObject(0).optJSONObject("content")
                val parts = content?.optJSONArray("parts")
                if (parts == null || parts.length() == 0) {
                    return@withContext Result.failure(Exception("Tidak ada bagian teks dalam konten respons."))
                }

                val text = parts.getJSONObject(0).optString("text")
                if (text.isNotEmpty()) {
                    Result.success(text)
                } else {
                    Result.failure(Exception("Teks respons kosong."))
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error jaringan atau parsing", e)
            Result.failure(e)
        }
    }
}
