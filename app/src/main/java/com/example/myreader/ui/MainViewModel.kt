package com.example.myreader.ui

import android.content.Intent
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myreader.logic.database.AppDatabase
import com.example.myreader.logic.database.Book
import kotlin.concurrent.thread

class MainViewModel(private val database: AppDatabase) : ViewModel() {

    private val _books = MutableLiveData<ArrayList<Book>>()
    val books get() = _books

    var bookList = ArrayList<Book>()

    init {
        _books.value = ArrayList()
        loadAllBooks()
    }

    private fun loadAllBooks() {
        val bookDao = database.BookDao()
        thread {
            bookList = ArrayList(bookDao.loadAllBooks())
            _books.postValue(bookList)
        }
    }

    private fun addBook(book: Book) {
        bookList.add(0, book)
        _books.value = bookList
    }

    private fun deleteBook(bookName: String, author: String) {
        for (book in bookList) {
            if (book.bookName == bookName && book.author == author) {
                bookList.remove(book)
            }
        }
        _books.value = bookList
    }

    private fun updateProgress(newBook: Book) {
        val newList = ArrayList<Book>()
        for (book in bookList) {
            if (book.bookName == newBook.bookName && book.author == newBook.author) {
                book.lastReadChap= newBook.lastReadChap
                book.lastRead = System.currentTimeMillis()
                newList.add(0, book)
            } else {
                newList.add(book)
            }
        }
        bookList.clear()
        bookList.addAll(newList)
        _books.value = bookList
    }

    fun updateByIntent(data: Intent) {
        val action = data.getStringExtra("action")
        if (action == "add") {
            if (data.getSerializableExtra("book") != null) {
                val book = data.getSerializableExtra("book") as Book
                addBook(book)
            }
        } else if (action == "delete") {
            val bookName = data.getStringExtra("bookName")
            val author = data.getStringExtra("author")
            if (bookName != null && author != null) {
                deleteBook(bookName, author)
            }
        } else if (action == "update") {
            if (data.getSerializableExtra("book") != null) {
                val book = data.getSerializableExtra("book") as Book
                updateProgress(book)
            }
        }
    }

}