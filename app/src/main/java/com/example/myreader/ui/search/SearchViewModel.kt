package com.example.myreader.ui.search

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.myreader.logic.Repository
import com.example.myreader.logic.database.Book

class SearchViewModel : ViewModel() {

    private val searchLiveData = MutableLiveData<String>()

    val bookList = ArrayList<Book>()

    val bookLiveData = Transformations.switchMap(searchLiveData) { query ->
        Repository.searchBook(query)
    }

    fun searchBook(query: String) {
        searchLiveData.value = query
    }

}