package com.example.myreader.ui.bookinfo

import android.view.View
import androidx.lifecycle.ViewModel
import com.example.myreader.logic.Repository
import com.example.myreader.logic.network.MyReaderNetwork

class BookInfoViewModel : ViewModel() {

    private val urlList = ArrayList<String>()

    fun setList(list: List<String>) {
        urlList.clear()
        urlList.addAll(list)
    }

    fun getList(): ArrayList<String> {
        return urlList
    }

    fun downloadCatalog(homepage: String, listener: MyReaderNetwork.CatalogDownloadListener) {
        Repository.downloadCatalog(homepage, listener)
    }

}