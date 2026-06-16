package com.trybild.attendr.ui.admin

object Holidays {
    val HOLIDAYS_2026: Map<String, String> = mapOf(
        "2026-01-01" to "New Year's Day",
        "2026-01-26" to "Republic Day",
        "2026-02-26" to "Maha Shivaratri",
        "2026-03-22" to "Holi",
        "2026-03-31" to "Ram Navami",
        "2026-04-03" to "Good Friday",
        "2026-04-14" to "Dr. Ambedkar Jayanti",
        "2026-05-12" to "Eid ul-Fitr",
        "2026-05-23" to "Buddha Purnima",
        "2026-07-18" to "Eid ul-Adha",
        "2026-07-27" to "Muharram",
        "2026-08-15" to "Independence Day",
        "2026-08-22" to "Janmashtami",
        "2026-10-02" to "Gandhi Jayanti",
        "2026-10-11" to "Dussehra",
        "2026-10-28" to "Diwali",
        "2026-11-15" to "Guru Nanak Jayanti",
        "2026-12-25" to "Christmas Day"
    )

    private val BY_YEAR: Map<Int, Map<String, String>> = mapOf(
        2026 to HOLIDAYS_2026
    )

    fun forDate(date: String, year: Int): String? = BY_YEAR[year]?.get(date)
}
