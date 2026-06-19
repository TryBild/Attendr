package com.trybild.attendr.utils

import java.text.SimpleDateFormat
import java.util.*

fun formatIsoTime(isoString: String?): String? {
    if (isoString.isNullOrBlank()) return null
    return try {
        val inFmt = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }
        val date = inFmt.parse(isoString) ?: return isoString.take(16)
        SimpleDateFormat("h:mm a", Locale.ENGLISH).apply {
            timeZone = TimeZone.getTimeZone("Asia/Kolkata")
        }.format(date)
    } catch (_: Exception) { isoString.take(16) }
}

fun isoToEpochMs(isoString: String?): Long? {
    if (isoString.isNullOrBlank()) return null
    return try {
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse(isoString)?.time
    } catch (_: Exception) { null }
}

fun formatShortDate(date: String?): String {
    if (date.isNullOrBlank()) return "—"
    return try {
        val d = SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(date) ?: return date
        SimpleDateFormat("EEE, d MMM", Locale.ENGLISH).format(d)
    } catch (_: Exception) { date }
}

fun formatDuration(elapsedMs: Long): String {
    val h = elapsedMs / 3_600_000
    val m = (elapsedMs % 3_600_000) / 60_000
    return if (h > 0) "${h}h ${m}m" else "${m}m"
}
