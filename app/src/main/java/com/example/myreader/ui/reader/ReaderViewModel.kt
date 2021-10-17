package com.example.myreader.ui.reader

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.myreader.logic.Repository
import com.example.myreader.logic.network.MyReaderNetwork
import java.util.ArrayList

class ReaderViewModel : ViewModel() {

    var curChapter = 0
    var titles = arrayListOf("", "", "")

    private var _chapterURLList = ArrayList<String>()
    private val _preArticle = MutableLiveData<String>()
    private val _curArticle = MutableLiveData<String>()
    private val _nxtArticle = MutableLiveData<String>()

    private val chapterURLList get() = _chapterURLList
    val preArticle get() = _preArticle
    val curArticle get() = _curArticle
    val nxtArticle get() = _nxtArticle

    fun init(list: ArrayList<String>) {
        _chapterURLList.clear()
        _chapterURLList.addAll(list)
        getArticle(0, 0)
        getArticle(1, 1)
        getArticle(2, 2)
    }

    fun getArticle(chapterNum: Int, target: Int){
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