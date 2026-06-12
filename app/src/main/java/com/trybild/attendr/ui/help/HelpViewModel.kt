package com.trybild.attendr.ui.help

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class HelpViewModel : ViewModel() {
    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query

    val filteredArticles: StateFlow<List<HelpListItem>> = _query
        .map { q ->
            if (q.isBlank()) HelpArticles.listItems
            else HelpArticles.listItems.filter { it.title.contains(q, ignoreCase = true) }
        }
        .stateIn(viewModelScope, SharingStarted.Eagerly, HelpArticles.listItems)

    fun setQuery(q: String) { _query.value = q }
}
