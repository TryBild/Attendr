package com.trybild.attendr.utils

import android.util.Base64
import org.json.JSONObject

object JwtUtils {

    fun decodeClaim(token: String, claim: String): String? {
        return try {
            val payload = token.split(".")[1]
            val decoded = String(
                Base64.decode(payload, Base64.URL_SAFE or Base64.NO_PADDING or Base64.NO_WRAP)
            )
            val json = JSONObject(decoded)
            if (json.has(claim)) json.getString(claim) else null
        } catch (e: Exception) {
            null
        }
    }

    fun decodeTokenKind(token: String): String? = decodeClaim(token, "kind")

    fun decodeTokenName(token: String): String? = decodeClaim(token, "name")
}
