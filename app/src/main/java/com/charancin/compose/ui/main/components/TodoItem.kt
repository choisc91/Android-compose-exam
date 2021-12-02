package com.charancin.compose.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.charancin.compose.domain.model.Todo
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TodoItem(
    todo: Todo,
    onClick: (todo: Todo) -> Unit = {},
    onDelete: (todo: Todo) -> Unit = {},
) {
    val format = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    Row(
        modifier = Modifier.clickable {
            onClick(todo)
        }
    ) {
        Icon(
            imageVector = Icons.Default.Delete,
            contentDescription = "delete",
            tint = Color(0xFFA51212),
            modifier = Modifier
                .padding(8.dp)
                .clickable { onDelete(todo) },
        )
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = format.format(Date(todo.date)),
                color = if (todo.isDone) Color.Gray else Color.Black,
                style = TextStyle(
                    textDecoration = if (todo.isDone) TextDecoration.LineThrough
                    else TextDecoration.None,
                )
            )
            Text(
                text = todo.title,
                color = if (todo.isDone) Color.Gray else Color.Black,
                style = TextStyle(
                    textDecoration = if (todo.isDone) TextDecoration.LineThrough
                    else TextDecoration.None,
                )
            )
        }
        if (todo.isDone) {
            Icon(
                imageVector = Icons.Default.Done,
                contentDescription = "done",
                tint = Color.Green,
            )
        }
    }
}