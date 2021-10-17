package com.example.myreader.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myreader.logic.database.AppDatabase

class MainViewModelFactory(private val database: AppDatabase) :
    ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        return MainViewModel(database) as T
    }

}