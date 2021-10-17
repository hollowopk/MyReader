package com.example.myreader.logic

import androidx.lifecycle.liveData
import com.example.myreader.logic.network.MyReaderNetwork
import kotlinx.coroutines.Dispatchers
import java.io.File
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.concurrent.thread

object Repository {

    fun searchBook(query: String) = liveData(Dispatchers.IO) {
        val res = try {
            val bookList = MyReaderNetwork.searchBook(query)
            if (bookList.isNotEmpty()) {
                Result.success(bookList)
            } else {
                Result.failure(RuntimeException("not found"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
        emit(res)
    }

    fun downloadBook(bookName: String, homepage: String, fileDir: File,
                     listener: MyReaderNetwork.BookDownloadListener) = thread {
        MyReaderNetwork.downloadBook(bookName, homepage, fileDir, listener)
    }

    fun downloadCatalog(homepage: String, listener: MyReaderNetwork.CatalogDownloadListener) = thread {
        MyReaderNetwork.downloadCatalog(homepage, listener)
    }

    fun getArticle(url: String,
                   listener: MyReaderNetwork.ArticleDownloadListener) = thread {
        MyReaderNetwork.getArticle(url, listener)
    }

}