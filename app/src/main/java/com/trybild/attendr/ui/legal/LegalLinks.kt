package com.trybild.attendr.ui.legal

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import com.trybild.attendr.ui.theme.AttendrError
import com.trybild.attendr.ui.theme.AttendrNavy
import com.trybild.attendr.ui.theme.AttendrTextPrimary
import com.trybild.attendr.ui.theme.AttendrTextSecondary

object AttendrUrls {
    const val PRIVACY = "https://trybild.com/attendr/privacy/"
    const val TERMS = "https://trybild.com/attendr/terms/"
    const val REFUND = "https://trybild.com/attendr/refund/"
    const val PRICING = "https://trybild.com/attendr/pricing/"
    const val DELETE_ACCOUNT = "https://trybild.com/attendr/delete-account/"
    const val CONTACT = "https://trybild.com/contact"
}

fun Context.openUrl(url: String) {
    val uri = Uri.parse(url)
    try {
        CustomTabsIntent.Builder().build().launchUrl(this, uri)
    } catch (e: ActivityNotFoundException) {
        try {
            startActivity(Intent(Intent.ACTION_VIEW, uri))
        } catch (e: ActivityNotFoundException) {
            // No browser available on the device — nothing more we can do.
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun LegalFooter(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val links = listOf(
        "Terms" to AttendrUrls.TERMS,
        "Privacy" to AttendrUrls.PRIVACY,
        "Refund" to AttendrUrls.REFUND,
        "Delete Account" to AttendrUrls.DELETE_ACCOUNT,
        "Contact" to AttendrUrls.CONTACT
    )

    FlowRow(
        modifier = modifier,
        horizontalArrangement = Arrangement.Center
    ) {
        links.forEachIndexed { index, (label, url) ->
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = AttendrNavy,
                textDecoration = TextDecoration.Underline,
                modifier = Modifier.clickable { context.openUrl(url) }
            )
            if (index != links.lastIndex) {
                Text(
                    text = " · ",
                    style = MaterialTheme.typography.labelSmall,
                    color = AttendrTextSecondary
                )
            }
        }
    }
}

@Composable
fun LegalConsentText(modifier: Modifier = Modifier) {
    val context = LocalContext.current

    val linkStyle = SpanStyle(color = AttendrNavy, textDecoration = TextDecoration.Underline)

    val annotatedText = buildAnnotatedString {
        withStyle(SpanStyle(color = AttendrTextSecondary)) {
            append("By continuing, you agree to our ")
        }
        withLink(LinkAnnotation.Clickable("terms", styles = TextLinkStyles(style = linkStyle)) {
            context.openUrl(AttendrUrls.TERMS)
        }) {
            append("Terms of Service")
        }
        withStyle(SpanStyle(color = AttendrTextSecondary)) {
            append(" and ")
        }
        withLink(LinkAnnotation.Clickable("privacy", styles = TextLinkStyles(style = linkStyle)) {
            context.openUrl(AttendrUrls.PRIVACY)
        }) {
            append("Privacy Policy")
        }
        withStyle(SpanStyle(color = AttendrTextSecondary)) {
            append(".")
        }
    }

    Text(
        text = annotatedText,
        style = MaterialTheme.typography.labelSmall,
        textAlign = TextAlign.Center,
        modifier = modifier
    )
}

/**
 * A single tappable legal/support row for use in Settings-style screens,
 * e.g. "Privacy Policy", "Delete Account".
 */
@Composable
fun LegalMenuRow(label: String, url: String, modifier: Modifier = Modifier, destructive: Boolean = false) {
    val context = LocalContext.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clickable { context.openUrl(url) }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            label,
            style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
            color = if (destructive) AttendrError else AttendrTextPrimary
        )
    }
}
