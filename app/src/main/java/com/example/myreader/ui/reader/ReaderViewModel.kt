package com.example.myreader.ui.reader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myreader.logic.Repository
import com.example.myreader.logic.network.MyReaderNetwork
import java.io.File
import java.util.ArrayList
import kotlin.concurrent.thread

class ReaderViewModel : ViewModel() {

    var curChapter = 0
    var titles = arrayListOf("", "", "")

    private lateinit var chapterURLList: ArrayList<String>
    private lateinit var file: File
    private val contentList = ArrayList<String>()

    private var isDownload = false

    private val _preArticle = MutableLiveData<String>()
    private val _curArticle = MutableLiveData<String>()
    private val _nxtArticle = MutableLiveData<String>()

    val preArticle get() = _preArticle
    val curArticle get() = _curArticle
    val nxtArticle get() = _nxtArticle

    fun initWithList(list: ArrayList<String>) {
        chapterURLList = list
        getArticle(0, 0)
        getArticle(1, 1)
        getArticle(2, 2)
    }

    fun initWithFile(f: File) {
        file = f
        isDownload = true
        var str = ""
        var count = 0
        file.useLines {
                it.forEach { line ->
                    str += line
                    str += "\n"
                    count += 1
                    if (count == 17) {
                        contentList.add(str)
                        str = ""
                        count = 0
                    }
                }
            }
        getArticle(0, 0)
        getArticle(1, 1)
        getArticle(2, 2)
    }

    fun getArticle(chapterNum: Int, target: Int) {
        if (!isDownload) {
            getArticleFromInternet(chapterNum, target)
        } else {
            getArticleFromLocal(chapterNum, target)
        }
    }

    private fun getArticleFromInternet(chapterNum: Int, target: Int) {
        if (chapterNum < chapterURLList.size) {
            Repository.getArticle(
                chapterURLList[chapterNum],
                object : MyReaderNetwork.ArticleDownloadListener {
                    override fun onFinish(title: String, content: String) {
                        curChapter = chapterNum - target
                        titles[target] = title
                        when (target) {
                            0 -> {
                                _preArticle.postValue(title + "\n\n" + content)
                            }
                            1 -> {
                                _curArticle.postValue(title + "\n\n" + content)
                            }
                            2 -> {
                                _nxtArticle.postValue(title + "\n\n" + content)
                            }
                        }
                    }
                })
        }
    }

    private fun getArticleFromLocal(chapterNum: Int, target: Int) {
        if (chapterNum >= 0 && chapterNum < contentList.size) {
            val content = contentList[chapterNum]
            curChapter = chapterNum - target
            when (target) {
                0 -> {
                    _preArticle.value = content
                }
                1 -> {
                    _curArticle.value = content
                }
                2 -> {
                    _nxtArticle.value = content
                }
            }
        }
    }

    fun getNext() {
        _preArticle.value = curArticle.value
        _curArticle.value = nxtArticle.value
        _nxtArticle.value = ""
        titles[0] = titles[1]
        titles[1] = titles[2]
        titles[2] = ""
    }

    fun getPre() {
        _nxtArticle.value = curArticle.value
        _curArticle.value = preArticle.value
        _preArticle.value = ""
        titles[2] = titles[1]
        titles[1] = titles[0]
        titles[0] = ""
    }

}