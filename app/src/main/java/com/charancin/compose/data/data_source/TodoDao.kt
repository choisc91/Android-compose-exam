package com.charancin.compose.data.data_source

import androidx.room.*
import com.charancin.compose.domain.model.Todo
import kotlinx.coroutines.flow.Flow

// room 에서 사용하기 위해 어노테이션 추가.
@Dao
interface TodoDao {

    @Query("SELECT * FROM todo ORDER BY date DESC")
    fun todos(): Flow<List<Todo>>

    // 동일한 개체가 삽입될 시 자동으로 교체 처리.
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(todo: Todo)

    @Update
    suspend fun update(todo: Todo)

    @Delete
    suspend fun delete(todo: Todo)
}