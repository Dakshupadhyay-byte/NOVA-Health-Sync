package com.hcwebhook.app

import android.content.Context
import android.net.Uri
import android.os.Build
import android.provider.Settings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException
import java.util.concurrent.TimeUnit

sealed class NovaPairingResult {
    data class Success(val token: String) : NovaPairingResult()
    data class Error(val message: String) : NovaPairingResult()
}

object NovaPairingService {
    const val NOVA_WEBHOOK_URL = "https://nova-backend-eta.vercel.app/api/health/webhook"
    private const val PAIRING_BASE_URL = "http://10.77.76.150:5000"
    private const val CLAIM_ENDPOINT = "$PAIRING_BASE_URL/api/health/pairing/claim"
    private const val DEFAULT_DEVICE_NAME = "NOVA Health Sync"

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(15, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun extractPairingCode(scannedContent: String): String? {
        val trimmed = scannedContent.trim()
        if (trimmed.isEmpty()) return null

        if (trimmed.startsWith("http://") || trimmed.startsWith("https://")) {
            try {
                val uri = Uri.parse(trimmed)
                val codeParam = uri.getQueryParameter("code")
                if (!codeParam.isNullOrBlank()) {
                    return codeParam.trim()
                }
            } catch (_: Exception) {
                // Ignore parse errors and fallback to regex
            }

            val codeRegex = Regex("[?&]code=([^&]+)")
            val match = codeRegex.find(trimmed)
            if (match != null && match.groupValues.size > 1) {
                return match.groupValues[1].trim()
            }
        }

        // Return raw trimmed content if it doesn't look like an unparsed non-pairing URL
        return trimmed
    }

    fun getDeviceId(context: Context): String {
        return try {
            Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
                ?: "android_${Build.FINGERPRINT.hashCode()}"
        } catch (_: Exception) {
            "android_${Build.FINGERPRINT.hashCode()}"
        }
    }

    suspend fun claimPairing(
        context: Context,
        pairingCode: String
    ): NovaPairingResult = withContext(Dispatchers.IO) {
        val cleanCode = extractPairingCode(pairingCode)
        if (cleanCode.isNullOrBlank()) {
            return@withContext NovaPairingResult.Error("Invalid NOVA pairing QR code.")
        }

        val deviceId = getDeviceId(context)
        val payload = JSONObject().apply {
            put("pairingCode", cleanCode)
            put("deviceId", deviceId)
            put("deviceName", DEFAULT_DEVICE_NAME)
        }.toString()

        val request = Request.Builder()
            .url(CLAIM_ENDPOINT)
            .post(payload.toRequestBody(jsonMediaType))
            .header("Accept", "application/json")
            .build()

        try {
            client.newCall(request).execute().use { response ->
                val responseBody = response.body?.string().orEmpty()
                val statusCode = response.code

                if (response.isSuccessful) {
                    val token = extractTokenFromResponse(responseBody)
                    if (token != null && token.isNotBlank()) {
                        NovaPairingResult.Success(token)
                    } else {
                        NovaPairingResult.Error("Could not connect to NOVA. Please try again.")
                    }
                } else {
                    val bodyLower = responseBody.lowercase()
                    val isExpired = statusCode == 410 || bodyLower.contains("expire")
                    val isClaimed = statusCode == 409 || bodyLower.contains("already") ||
                            bodyLower.contains("claimed") || bodyLower.contains("used")
                    val isInvalid = statusCode == 400 || statusCode == 404 ||
                            bodyLower.contains("invalid") || bodyLower.contains("not found")

                    val errorMessage = when {
                        isExpired -> "QR code expired. Generate a new QR code on NOVA."
                        isClaimed -> "QR code already used. Generate a new QR code on NOVA."
                        isInvalid -> "Invalid NOVA pairing QR code."
                        else -> "Could not connect to NOVA. Please try again."
                    }
                    NovaPairingResult.Error(errorMessage)
                }
            }
        } catch (_: IOException) {
            NovaPairingResult.Error("Could not connect to NOVA. Please try again.")
        } catch (_: Exception) {
            NovaPairingResult.Error("Could not connect to NOVA. Please try again.")
        }
    }

    private fun extractTokenFromResponse(responseBody: String): String? {
        val trimmed = responseBody.trim()
        if (trimmed.startsWith("nova_hk_")) {
            return trimmed
        }

        try {
            val json = JSONObject(trimmed)

            // Direct candidate keys
            val candidates = listOf("token", "credential", "apiKey", "healthKey", "key", "bearerToken")
            for (key in candidates) {
                if (json.has(key)) {
                    val value = json.optString(key)
                    if (value.isNotBlank()) return value.trim()
                }
            }

            // Check nested "data" object if present
            if (json.has("data") && json.optJSONObject("data") != null) {
                val dataObj = json.getJSONObject("data")
                for (key in candidates) {
                    if (dataObj.has(key)) {
                        val value = dataObj.optString(key)
                        if (value.isNotBlank()) return value.trim()
                    }
                }
            }

            // Fallback: search for any string value starting with nova_hk_
            findNovaTokenInJson(json)?.let { return it }
        } catch (_: Exception) {
            // Not a JSON object or parsing failed
        }

        return null
    }

    private fun findNovaTokenInJson(json: JSONObject): String? {
        val keys = json.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = json.opt(key)
            if (value is String && value.startsWith("nova_hk_")) {
                return value.trim()
            } else if (value is JSONObject) {
                val nested = findNovaTokenInJson(value)
                if (nested != null) return nested
            }
        }
        return null
    }
}
