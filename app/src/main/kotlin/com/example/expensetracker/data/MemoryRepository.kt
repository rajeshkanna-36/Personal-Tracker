package com.example.expensetracker.data

import com.example.expensetracker.data.local.MemoryDao
import com.example.expensetracker.data.local.MemoryEntity
import kotlinx.coroutines.flow.Flow

class MemoryRepository(private val memoryDao: MemoryDao) {
    val allMemories: Flow<List<MemoryEntity>> = memoryDao.getAllMemories()

    suspend fun insert(memory: MemoryEntity) {
        memoryDao.insertMemory(memory)
    }

    suspend fun update(memory: MemoryEntity) {
        memoryDao.updateMemory(memory)
    }

    suspend fun delete(memory: MemoryEntity) {
        memoryDao.deleteMemory(memory)
    }
}
