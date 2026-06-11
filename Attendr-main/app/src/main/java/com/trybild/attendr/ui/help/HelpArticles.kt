package com.trybild.attendr.ui.help

data class HelpSection(val heading: String, val bullets: List<String>)
data class HelpArticle(
    val slug: String,
    val title: String,
    val sections: List<HelpSection>,
    val relatedSlugs: List<String>
)

object HelpArticles {
    val all = listOf(
        HelpArticle(
            slug = "otp-not-received",
            title = "OTP Not Received",
            sections = listOf(
                HelpSection("Common Reasons", listOf(
                    "Poor network signal at your location",
                    "SMS delivery delay from your carrier",
                    "Phone number entered incorrectly",
                    "SMS blocked by device DND settings"
                )),
                HelpSection("Steps to Fix", listOf(
                    "Wait 60 seconds before requesting a resend",
                    "Check your network connection and try again",
                    "Verify the phone number is exactly 10 digits",
                    "Disable Do Not Disturb mode and retry",
                    "Check if your inbox is full"
                )),
                HelpSection("Still Not Working?", listOf(
                    "Contact your organization administrator",
                    "Use the Contact Support form below"
                ))
            ),
            relatedSlugs = listOf("invalid-team-id", "app-not-working")
        ),
        HelpArticle(
            slug = "invalid-team-id",
            title = "Invalid Team ID",
            sections = listOf(
                HelpSection("What is a Team ID?", listOf(
                    "Your Team ID (also called Organization ID) is provided by your employer",
                    "It is case-sensitive — enter it exactly as given",
                    "Examples: ABC001, TRYBILD101, ATTENDR500"
                )),
                HelpSection("How to Fix", listOf(
                    "Double-check the ID with your HR or manager",
                    "Make sure there are no extra spaces before or after",
                    "Try uppercase letters if lowercase does not work"
                ))
            ),
            relatedSlugs = listOf("unable-to-join-org", "otp-not-received")
        ),
        HelpArticle(
            slug = "unable-to-join-org",
            title = "Unable to Join Organization",
            sections = listOf(
                HelpSection("Possible Causes", listOf(
                    "Your phone number has not been added to the organization by admin",
                    "The organization account may be inactive or suspended",
                    "You may be using the wrong Team ID"
                )),
                HelpSection("What to Do", listOf(
                    "Ask your administrator to verify your phone number is registered",
                    "Ask admin to confirm the correct Team ID",
                    "If recently onboarded, wait 10 minutes and try again"
                ))
            ),
            relatedSlugs = listOf("invalid-team-id", "changed-phone")
        ),
        HelpArticle(
            slug = "changed-phone",
            title = "Changed Phone / New Device",
            sections = listOf(
                HelpSection("What Happens When You Change Phones", listOf(
                    "Your old session is automatically invalidated for security",
                    "You need to re-register on the new device",
                    "Your attendance history is preserved"
                )),
                HelpSection("Steps to Re-Register", listOf(
                    "Open Attendr on your new device",
                    "Enter your registered phone number",
                    "Enter the OTP received on your number",
                    "Enter your Organization ID and continue"
                ))
            ),
            relatedSlugs = listOf("otp-not-received", "unable-to-join-org")
        ),
        HelpArticle(
            slug = "location-permission",
            title = "Location Permission Required",
            sections = listOf(
                HelpSection("Why Location is Needed", listOf(
                    "Attendr uses GPS to verify you are at the correct work location",
                    "Location is only captured at the moment of check-in or check-out",
                    "No background location tracking is performed"
                )),
                HelpSection("How to Enable Location", listOf(
                    "Go to your phone Settings → Apps → Attendr → Permissions",
                    "Enable Location permission",
                    "Return to Attendr and try checking in again"
                )),
                HelpSection("Mock Location Detected", listOf(
                    "Attendr detects and flags mock/fake GPS apps",
                    "Disable any GPS spoofing or mock location apps",
                    "Turn off Developer Options mock location setting"
                ))
            ),
            relatedSlugs = listOf("app-not-working")
        ),
        HelpArticle(
            slug = "app-not-working",
            title = "App Not Working Properly",
            sections = listOf(
                HelpSection("Quick Fixes", listOf(
                    "Close the app completely and reopen it",
                    "Check your internet connection",
                    "Ensure your phone's date and time are set to automatic"
                )),
                HelpSection("If the Problem Persists", listOf(
                    "Clear the app's cache from Settings → Apps → Attendr → Storage",
                    "Uninstall and reinstall the app from the Play Store",
                    "Make sure your Android version is 7.0 or above"
                ))
            ),
            relatedSlugs = listOf("location-permission", "otp-not-received")
        )
    )

    val bySlug: Map<String, HelpArticle> = all.associateBy { it.slug }

    val listItems = listOf(
        HelpListItem("otp-not-received", "OTP Not Received", "lock"),
        HelpListItem("invalid-team-id", "Invalid Team ID", "group"),
        HelpListItem("unable-to-join-org", "Unable to Join Organization", "business"),
        HelpListItem("changed-phone", "Changed Phone / New Device", "smartphone"),
        HelpListItem("location-permission", "Location Permission Required", "location_on"),
        HelpListItem("app-not-working", "App Not Working Properly", "bug_report")
    )
}

data class HelpListItem(val slug: String, val title: String, val iconKey: String)
