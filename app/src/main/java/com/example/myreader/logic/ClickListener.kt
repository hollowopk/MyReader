package com.example.myreader.logic

import com.example.myreader.logic.database.Book

interface ClickListener {

    fun onClick(book: Book)
    fun onLongClick(book: Book)

}