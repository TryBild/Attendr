package com.trybild.attendr.ui.admin

object Holidays {
    val HOLIDAYS_2026: Map<String, String> = mapOf(
        "2026-01-26" to "Republic Day",
        "2026-02-15" to "Maha Shivaratri",
        "2026-03-04" to "Holi",
        "2026-03-21" to "Eid ul-Fitr",
        "2026-03-26" to "Ram Navami",
        "2026-03-31" to "Mahavir Jayanti",
        "2026-04-03" to "Good Friday",
        "2026-05-01" to "Buddha Purnima",
        "2026-05-28" to "Eid ul-Adha",
        "2026-06-26" to "Muharram",
        "2026-08-15" to "Independence Day",
        "2026-08-25" to "Milad-un-Nabi",
        "2026-09-04" to "Janmashtami",
        "2026-10-02" to "Gandhi Jayanti",
        "2026-10-20" to "Dussehra",
        "2026-11-08" to "Diwali",
        "2026-11-24" to "Guru Nanak Jayanti",
        "2026-12-25" to "Christmas"
    )

    private val BY_YEAR: Map<Int, Map<String, String>> = mapOf(
        2026 to HOLIDAYS_2026
    )

    fun forDate(date: String, year: Int): String? = BY_YEAR[year]?.get(date)
}
