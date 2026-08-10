package org.librespeed.speedtest.ui.history

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.librespeed.speedtest.data.HistoryDatabase
import org.librespeed.speedtest.data.HistoryEntry

class HistoryViewModel(application: Application) : AndroidViewModel(application) {

    private val database = HistoryDatabase(application)
    private val _entries = MutableStateFlow<List<HistoryEntry>>(emptyList())
    val entries: StateFlow<List<HistoryEntry>> = _entries

    fun load() {
        viewModelScope.launch {
            _entries.value = withContext(Dispatchers.IO) { database.readAll() }
        }
    }

    fun delete(entry: HistoryEntry) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { database.delete(entry.id) }
            load()
        }
    }

    fun clear() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) { database.clear() }
            load()
        }
    }

}
