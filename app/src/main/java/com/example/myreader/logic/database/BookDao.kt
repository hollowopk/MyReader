package com.example.myreader.logic.database

import androidx.room.*

@Dao
interface BookDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertBook(book: Book): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateBook(book: Book)

    @Query("select * from Book order by lastRead desc")
    fun loadAllBooks(): List<Book>

    @Query("select * from Book where bookName=:bookName and author=:author")
    fun findBook(bookName: String, author: String): List<Book>

    @Query("delete from Book where bookName=:bookName and author=:author")
    fun deleteBook(bookName: String, author: String)

    @Query("update Book set lastReadChap=:lastRead " +
            "where bookName=:bookName and author=:author")
    fun updateLastRead(bookName: String, author: String, lastRead: String)

}