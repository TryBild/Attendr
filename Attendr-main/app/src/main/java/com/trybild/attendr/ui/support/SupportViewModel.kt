package com.trybild.attendr.ui.support

import android.net.Uri
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SupportViewModel : ViewModel() {

    enum class IssueType(val label: String) {
        BUG_REPORT("Bug Report"),
        FEATURE_REQUEST("Feature Request"),
        GENERAL_INQUIRY("General Inquiry")
    }

    data class FormState(
        val description: String = "",
        val issueType: IssueType = IssueType.BUG_REPORT,
        val email: String = "",
        val attachments: List<Uri> = emptyList()
    ) {
        val isValid get() = description.isNotBlank() && email.isNotBlank()
    }

    private val _form = MutableStateFlow(FormState())
    val form: StateFlow<FormState> = _form

    fun setDescription(v: String) { _form.value = _form.value.copy(description = v) }
    fun setIssueType(v: IssueType) { _form.value = _form.value.copy(issueType = v) }
    fun setEmail(v: String) { _form.value = _form.value.copy(email = v) }
    fun addAttachment(uri: Uri) {
        if (uri !in _form.value.attachments)
            _form.value = _form.value.copy(attachments = _form.value.attachments + uri)
    }
    fun removeAttachment(uri: Uri) {
        _form.value = _form.value.copy(attachments = _form.value.attachments - uri)
    }
}
