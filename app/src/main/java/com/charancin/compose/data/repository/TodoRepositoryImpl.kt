package com.charancin.compose.data.repository

import android.app.Application
import androidx.room.Room
import com.charancin.compose.data.data_source.TodoDatabase
import com.charancin.compose.domain.model.Todo
import com.charancin.compose.domain.repository.TodoRepository
import kotlinx.coroutines.flow.Flow

class TodoRepositoryImpl(application: Application) : TodoRepository {

    private val database = Room.databaseBuilder(
        application,
        TodoDatabase::class.java,
        "todo-database"
    ).build()


    override fun observeTodos(): Flow<List<Todo>> {
        return database.todoDao().todos()
    }

    override suspend fun addTodo(todo: Todo) {
        return database.todoDao().insert(todo)
    }

    override suspend fun updateTodo(todo: Todo) {
        return database.todoDao().update(todo)
    }

    override suspend fun deleteTodo(todo: Todo) {
        return database.todoDao().delete(todo)
    }

}