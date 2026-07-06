package com.trybild.attendr.ui.admin

// Multi-year India holiday calendar, keyed by year so it doesn't need a manual
// rewrite every January. Fixed-date holidays (Republic Day, Independence Day,
// Gandhi Jayanti, Christmas, Good Friday) are stable; Islamic holidays (Eid ul-Fitr,
// Eid ul-Adha/Bakrid, Muharram, Milad-un-Nabi) and some Hindu festivals follow lunar
// calendars and are confirmed by moon sighting, so dates beyond the current year are
// best-available estimates and should be re-verified against an official gazette
// closer to each year.
object Holidays {

    private val HOLIDAYS_2026: Map<String, String> = mapOf(
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
        "2026-08-26" to "Milad-un-Nabi",
        "2026-09-04" to "Janmashtami",
        "2026-10-02" to "Gandhi Jayanti",
        "2026-10-20" to "Dussehra",
        "2026-11-08" to "Diwali",
        "2026-11-24" to "Guru Nanak Jayanti",
        "2026-12-25" to "Christmas"
    )

    private val HOLIDAYS_2027: Map<String, String> = mapOf(
        "2027-01-26" to "Republic Day",
        "2027-03-06" to "Maha Shivaratri",
        "2027-03-10" to "Eid ul-Fitr",
        "2027-03-22" to "Holi",
        "2027-03-26" to "Good Friday",
        "2027-04-15" to "Ram Navami",
        "2027-04-19" to "Mahavir Jayanti",
        "2027-05-17" to "Eid ul-Adha",
        "2027-05-20" to "Buddha Purnima",
        "2027-06-15" to "Muharram",
        "2027-08-15" to "Independence Day / Milad-un-Nabi",
        "2027-08-25" to "Janmashtami",
        "2027-10-02" to "Gandhi Jayanti",
        "2027-10-10" to "Dussehra",
        "2027-10-29" to "Diwali",
        "2027-11-14" to "Guru Nanak Jayanti",
        "2027-12-25" to "Christmas"
    )

    private val HOLIDAYS_2028: Map<String, String> = mapOf(
        "2028-01-26" to "Republic Day",
        "2028-02-23" to "Maha Shivaratri",
        "2028-02-27" to "Eid ul-Fitr",
        "2028-03-11" to "Holi",
        "2028-04-04" to "Ram Navami",
        "2028-04-07" to "Mahavir Jayanti",
        "2028-04-14" to "Good Friday",
        "2028-05-05" to "Eid ul-Adha",
        "2028-05-08" to "Buddha Purnima",
        "2028-06-03" to "Muharram",
        "2028-08-03" to "Milad-un-Nabi",
        "2028-08-13" to "Janmashtami",
        "2028-08-15" to "Independence Day",
        "2028-09-27" to "Dussehra",
        "2028-10-02" to "Gandhi Jayanti",
        "2028-10-17" to "Diwali",
        "2028-11-02" to "Guru Nanak Jayanti",
        "2028-12-25" to "Christmas"
    )

    private val BY_YEAR: Map<Int, Map<String, String>> = mapOf(
        2026 to HOLIDAYS_2026,
        2027 to HOLIDAYS_2027,
        2028 to HOLIDAYS_2028
    )

    fun forDate(date: String, year: Int): String? = BY_YEAR[year]?.get(date)
}
