package com.example.expensetracker.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.expensetracker.ExpenseTrackerApplication
import com.example.expensetracker.data.MemoryRepository
import com.example.expensetracker.data.local.MemoryEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class MemoryViewModel(private val repository: MemoryRepository) : ViewModel() {

    private val _memories = MutableStateFlow<List<MemoryEntity>>(emptyList())
    val memories: StateFlow<List<MemoryEntity>> = _memories.asStateFlow()

    init {
        viewModelScope.launch {
            repository.allMemories.collect {
                _memories.value = it
            }
        }
    }

    fun addNote(content: String) {
        viewModelScope.launch {
            val memory = MemoryEntity(
                type = "NOTE",
                title = content,
                timestamp = System.currentTimeMillis()
            )
            repository.insert(memory)
        }
    }

    fun addPassword(title: String, username: String, pass: String) {
        viewModelScope.launch {
            val memory = MemoryEntity(
                type = "PASSWORD",
                title = title,
                username = username,
                password = pass,
                timestamp = System.currentTimeMillis()
            )
            repository.insert(memory)
        }
    }

    fun deleteMemory(memory: MemoryEntity) {
        viewModelScope.launch {
            repository.delete(memory)
        }
    }

    companion object {
        val Factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras
            ): T {
                val application = checkNotNull(extras[ViewModelProvider.AndroidViewModelFactory.APPLICATION_KEY]) as ExpenseTrackerApplication
                return MemoryViewModel(application.container.memoryRepository) as T
            }
        }
    }
}
