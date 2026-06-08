package com.trybild.attendr.ui.welcome

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.ClickableText
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.trybild.attendr.R
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.components.AttendrCard
import com.trybild.attendr.ui.components.LogoIcon
import com.trybild.attendr.ui.theme.*

@Composable
fun WelcomeScreen(
    onContinue: () -> Unit,
    onNavigateToSupport: () -> Unit,
    onNavigateToHelp: () -> Unit,
    onNavigateToTerms: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
    onNavigateToDataUsage: () -> Unit
) {
    val vm: WelcomeViewModel = viewModel()
    val selectedLang by vm.language.collectAsStateWithLifecycle()
    var menuExpanded by remember { mutableStateOf(false) }
    var langDialogOpen by remember { mutableStateOf(false) }

    if (langDialogOpen) {
        LanguagePickerDialog(
            selected = selectedLang,
            onSelect = { vm.setLanguage(it); langDialogOpen = false },
            onDismiss = { langDialogOpen = false }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Box(modifier = Modifier.align(Alignment.TopEnd).padding(top = 12.dp, end = 8.dp)) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = stringResource(R.string.cd_menu), tint = AttendrTextPrimary)
            }
            DropdownMenu(expanded = menuExpanded, onDismissRequest = { menuExpanded = false }) {
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_contact_support)) }, onClick = { menuExpanded = false; onNavigateToSupport() })
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_help_center)) }, onClick = { menuExpanded = false; onNavigateToHelp() })
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_terms)) }, onClick = { menuExpanded = false; onNavigateToTerms() })
                DropdownMenuItem(text = { Text(stringResource(R.string.menu_privacy)) }, onClick = { menuExpanded = false; onNavigateToPrivacy() })
            }
        }

        Column(
            modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(80.dp))

            LogoIcon(size = 96.dp)

            Spacer(Modifier.height(24.dp))

            Text(
                stringResource(R.string.welcome_title),
                style = MaterialTheme.typography.displayLarge,
                textAlign = TextAlign.Center,
                color = AttendrTextPrimary
            )
            Spacer(Modifier.height(8.dp))
            Text(
                stringResource(R.string.welcome_subtitle),
                style = MaterialTheme.typography.bodyLarge,
                color = AttendrTextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(32.dp))

            PrivacyLinksText(
                onPrivacy = onNavigateToPrivacy,
                onTerms = onNavigateToTerms,
                onDataUsage = onNavigateToDataUsage
            )

            Spacer(Modifier.height(24.dp))

            LanguageSelectorCard(selectedLang = selectedLang, onClick = { langDialogOpen = true })

            Spacer(Modifier.weight(1f))

            AttendrButton(text = stringResource(R.string.btn_agree_continue), onClick = onContinue)

            Spacer(Modifier.height(24.dp))
        }
    }
}

@Composable
private fun PrivacyLinksText(onPrivacy: () -> Unit, onTerms: () -> Unit, onDataUsage: () -> Unit) {
    val privacyLabel = stringResource(R.string.privacy_link_policy)
    val termsLabel = stringResource(R.string.privacy_link_terms)
    val dataLabel = stringResource(R.string.privacy_link_data)

    val annotated = buildAnnotatedString {
        append("Read our ")
        pushStringAnnotation("PRIVACY", "privacy")
        withStyle(SpanStyle(color = AttendrNavy, textDecoration = TextDecoration.Underline)) { append(privacyLabel) }
        pop()
        append(", ")
        pushStringAnnotation("TERMS", "terms")
        withStyle(SpanStyle(color = AttendrNavy, textDecoration = TextDecoration.Underline)) { append(termsLabel) }
        pop()
        append(", and ")
        pushStringAnnotation("DATA", "data")
        withStyle(SpanStyle(color = AttendrNavy, textDecoration = TextDecoration.Underline)) { append(dataLabel) }
        pop()
        append(" before continuing.")
    }

    ClickableText(
        text = annotated,
        style = MaterialTheme.typography.bodyLarge.copy(textAlign = TextAlign.Center),
        modifier = Modifier.padding(horizontal = 16.dp),
        onClick = { offset ->
            annotated.getStringAnnotations("PRIVACY", offset, offset).firstOrNull()?.let { onPrivacy() }
            annotated.getStringAnnotations("TERMS", offset, offset).firstOrNull()?.let { onTerms() }
            annotated.getStringAnnotations("DATA", offset, offset).firstOrNull()?.let { onDataUsage() }
        }
    )
}

@Composable
private fun LanguageSelectorCard(selectedLang: String, onClick: () -> Unit) {
    AttendrCard(modifier = Modifier.fillMaxWidth().clickable { onClick() }) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("🌐", style = MaterialTheme.typography.bodyLarge)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(stringResource(R.string.lang_selection_label), style = MaterialTheme.typography.labelSmall, color = AttendrTextSecondary)
                Text(selectedLang, style = MaterialTheme.typography.bodyLarge, color = AttendrTextPrimary)
            }
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = AttendrTextSecondary)
        }
    }
}

@Composable
private fun LanguagePickerDialog(selected: String, onSelect: (String) -> Unit, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.lang_dialog_title), style = MaterialTheme.typography.headlineMedium) },
        text = {
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                items(SUPPORTED_LANGUAGES) { lang ->
                    val isSelected = lang == selected
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (isSelected) AttendrNavy else AttendrBackground,
                        modifier = Modifier.clickable { onSelect(lang) }
                    ) {
                        Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(8.dp)) {
                            Text(
                                lang,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isSelected) Color.White else AttendrTextPrimary,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.btn_close)) } }
    )
}
