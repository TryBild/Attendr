package com.trybild.attendr.ui.support

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.trybild.attendr.R
import com.trybild.attendr.ui.components.AttendrBackground
import com.trybild.attendr.ui.components.AttendrButton
import com.trybild.attendr.ui.theme.*
import java.io.File

private const val MAX_BYTES = 10 * 1024 * 1024L

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ContactSupportScreen(navController: NavController, prefillIssue: String = "") {
    val vm: SupportViewModel = viewModel()
    val form by vm.form.collectAsStateWithLifecycle()
    val context = LocalContext.current

    var issueTypeExpanded by remember { mutableStateOf(false) }
    var sizeError by remember { mutableStateOf("") }
    var cameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(prefillIssue) {
        if (prefillIssue.isNotBlank() && form.description.isBlank()) {
            vm.setDescription("Issue: $prefillIssue\n\n")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        sizeError = ""
        uris.forEach { uri ->
            val size = context.contentResolver.query(uri, null, null, null, null)?.use { c ->
                val idx = c.getColumnIndex(OpenableColumns.SIZE)
                if (c.moveToFirst() && idx >= 0) c.getLong(idx) else 0L
            } ?: 0L
            if (size > MAX_BYTES) sizeError = context.getString(R.string.support_size_error)
            else vm.addAttachment(uri)
        }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) cameraUri?.let { vm.addAttachment(it) }
        cameraUri = null
    }

    fun launchCamera() {
        val tmpFile = File.createTempFile("cam_", ".jpg", context.cacheDir)
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", tmpFile)
        cameraUri = uri
        cameraLauncher.launch(uri)
    }

    AttendrBackground(modifier = Modifier.fillMaxSize()) {
    Scaffold(
        containerColor = Color.Transparent,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.support_title), style = MaterialTheme.typography.headlineMedium) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.cd_back))
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            Text(stringResource(R.string.support_description_label), style = MaterialTheme.typography.bodyLarge, color = AttendrTextPrimary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.description,
                onValueChange = { vm.setDescription(it) },
                placeholder = { Text(stringResource(R.string.support_description_placeholder), color = AttendrTextSecondary) },
                modifier = Modifier.fillMaxWidth().heightIn(min = 120.dp),
                minLines = 5,
                singleLine = false,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AttendrNavy, unfocusedBorderColor = AttendrBorder)
            )

            Spacer(Modifier.height(20.dp))

            Text(stringResource(R.string.support_issue_type_label), style = MaterialTheme.typography.bodyLarge, color = AttendrTextPrimary)
            Spacer(Modifier.height(8.dp))
            ExposedDropdownMenuBox(
                expanded = issueTypeExpanded,
                onExpandedChange = { issueTypeExpanded = it }
            ) {
                OutlinedTextField(
                    value = form.issueType.label,
                    onValueChange = {},
                    readOnly = true,
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = issueTypeExpanded) },
                    modifier = Modifier.fillMaxWidth().menuAnchor(ExposedDropdownMenuAnchorType.PrimaryNotEditable),
                    shape = RoundedCornerShape(8.dp),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AttendrNavy, unfocusedBorderColor = AttendrBorder)
                )
                ExposedDropdownMenu(expanded = issueTypeExpanded, onDismissRequest = { issueTypeExpanded = false }) {
                    SupportViewModel.IssueType.entries.forEach { type ->
                        DropdownMenuItem(
                            text = { Text(type.label) },
                            onClick = { vm.setIssueType(type); issueTypeExpanded = false }
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(stringResource(R.string.support_attach_label), style = MaterialTheme.typography.bodyMedium, color = AttendrTextSecondary)
            Spacer(Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(BorderStroke(1.dp, AttendrBorder), shape = RoundedCornerShape(8.dp))
                    .padding(24.dp)
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.Center, modifier = Modifier.fillMaxWidth()) {
                        IconButton(onClick = { launchCamera() }) {
                            Icon(Icons.Default.CameraAlt, contentDescription = "Take Photo", tint = AttendrNavy, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(24.dp))
                        IconButton(onClick = {
                            galleryLauncher.launch(arrayOf("image/jpeg", "image/png", "application/pdf"))
                        }) {
                            Icon(Icons.Default.Image, contentDescription = "Pick from Gallery", tint = AttendrNavy, modifier = Modifier.size(32.dp))
                        }
                    }
                    if (sizeError.isNotBlank()) {
                        Spacer(Modifier.height(8.dp))
                        Text(sizeError, color = AttendrError, style = MaterialTheme.typography.labelSmall)
                    }
                    if (form.attachments.isNotEmpty()) {
                        Spacer(Modifier.height(12.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            form.attachments.forEach { uri ->
                                AttachmentChip(uri = uri, context = context, onRemove = { vm.removeAttachment(uri) })
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Text(stringResource(R.string.support_email_label), style = MaterialTheme.typography.bodyLarge, color = AttendrTextPrimary)
            Spacer(Modifier.height(8.dp))
            OutlinedTextField(
                value = form.email,
                onValueChange = { vm.setEmail(it) },
                placeholder = { Text(stringResource(R.string.support_email_placeholder), color = AttendrTextSecondary) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = AttendrNavy, unfocusedBorderColor = AttendrBorder)
            )

            Spacer(Modifier.height(24.dp))

            AttendrButton(
                text = stringResource(R.string.btn_submit_request),
                onClick = { sendSupportEmail(context, form) },
                enabled = form.isValid
            )

            Spacer(Modifier.height(32.dp))
        }
    }
    }
}

@Composable
private fun AttachmentChip(uri: Uri, context: Context, onRemove: () -> Unit) {
    val name = remember(uri) {
        context.contentResolver.query(uri, null, null, null, null)?.use { c ->
            val idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (c.moveToFirst() && idx >= 0) c.getString(idx) else uri.lastPathSegment ?: "file"
        } ?: uri.lastPathSegment ?: "file"
    }
    Surface(shape = RoundedCornerShape(16.dp), color = AttendrNavy.copy(alpha = 0.08f)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(start = 12.dp, end = 4.dp, top = 4.dp, bottom = 4.dp)
        ) {
            Icon(Icons.Default.AttachFile, contentDescription = null, tint = AttendrNavy, modifier = Modifier.size(14.dp))
            Spacer(Modifier.width(6.dp))
            Text(
                if (name.length > 32) name.take(32) + "…" else name,
                style = MaterialTheme.typography.labelSmall,
                color = AttendrNavy
            )
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Remove", tint = AttendrNavy, modifier = Modifier.size(14.dp))
            }
        }
    }
}

private fun sendSupportEmail(context: Context, form: SupportViewModel.FormState) {
    val appVersion = try {
        context.packageManager.getPackageInfo(context.packageName, 0).versionName
    } catch (_: Exception) { "N/A" }

    val body = """
        Issue Type: ${form.issueType.label}
        Description: ${form.description}
        Account Email: ${form.email}
        App Version: $appVersion
        Device: ${Build.MANUFACTURER} ${Build.MODEL} (Android ${Build.VERSION.RELEASE})
    """.trimIndent()

    val intent = Intent(Intent.ACTION_SEND_MULTIPLE).apply {
        type = "message/rfc822"
        putExtra(Intent.EXTRA_EMAIL, arrayOf("support@attendr.in"))
        putExtra(Intent.EXTRA_SUBJECT, "Support Request: ${form.issueType.label}")
        putExtra(Intent.EXTRA_TEXT, body)
        if (form.attachments.isNotEmpty()) {
            putParcelableArrayListExtra(Intent.EXTRA_STREAM, ArrayList(form.attachments))
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
    }
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.send_via_email)))
}

