package com.example.myreader.ui.bookinfo

import android.view.View
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myreader.logic.Repository
import com.example.myreader.logic.network.MyReaderNetwork
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlin.concurrent.thread

class BookInfoViewModel : ViewModel() {

    private val urlList = ArrayList<String>()

    fun setList(list: List<String>) {
        urlList.clear()
        urlList.addAll(list)
    }

    fun getList(): ArrayList<String> {
        return urlList
    }

    fun downloadCatalog(homepage: String,
                        listener: MyReaderNetwork.CatalogDownloadListener) = thread {
        Repository.downloadCatalog(homepage, listener)
    }


}