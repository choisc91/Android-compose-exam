package com.charancin.compose.ui.main

import android.app.Application
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.charancin.compose.domain.model.Todo
import com.charancin.compose.domain.repository.TodoRepository
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class MainViewModel(application: Application, private val repository: TodoRepository) :
    AndroidViewModel(application) {

    private val _items = mutableStateOf(emptyList<Todo>())

    val items: State<List<Todo>> = _items

    private var recentlyDeleteTodo: Todo? = null

    init {
        viewModelScope.launch {
            repository.observeTodos().collect { todos ->
                _items.value = todos
            }
        }
    }


    fun addTodo(text: String) {
        viewModelScope.launch {
            repository.addTodo(Todo(title = text))
        }
    }

    fun toggle(uid: Int) {
        val target = _items.value.find { it.uid == uid }
        target?.let {
            viewModelScope.launch {
                repository.updateTodo(it.copy(isDone = !it.isDone).apply {
                    this.uid = it.uid
                })
            }
        }
    }

    fun delete(uid: Int) {
        val target = _items.value.find { it.uid == uid }
        target?.let {
            viewModelScope.launch {
                recentlyDeleteTodo = it
                repository.deleteTodo(it)
            }
        }
    }

    fun restoreTodo() {
        viewModelScope.launch {
            repository.addTodo(recentlyDeleteTodo ?: return@launch)
            recentlyDeleteTodo = null
        }
    }
}