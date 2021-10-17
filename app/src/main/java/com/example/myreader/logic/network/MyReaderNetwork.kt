package com.example.myreader.logic.network

import com.example.myreader.logic.database.Book
import kotlinx.coroutines.*
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import kotlin.math.min

object MyReaderNetwork {

    private const val url = "https://m.shuhaige.tw"

    interface CatalogDownloadListener {
        fun updateProgress(progress: Int)
        fun onFinish(urlList: List<String>)
    }

    interface ArticleDownloadListener {
        fun onFinish(title: String, content: String)
    }

    interface BookDownloadListener {
        fun updateProgress(progress: Int)
        fun onFinish()
    }

    fun searchBook(query: String): MutableList<Book> {
        val resHTML = getSearchResult(query)
        val bookList = mutableListOf<Book>()
        if (resHTML != null) {
            val doc = Jsoup.parse(resHTML)
            val elements = doc.select("ul[class=list]")
            if (!elements.isNullOrEmpty()) {
                val listDoc = Jsoup.parse(elements[0].html())
                val bookElements = listDoc.select("li")
                for (bookElement in bookElements.subList(0, min(10, bookElements.size))) {
                    val imgURL = "https:" + bookElement.select("img").attr("src")
                    val bookName = bookElement.select("p[class=bookname]").select("a").text()
                    val author = bookElement.select("a[class=layui-btn layui-btn-xs layui-bg-cyan]").text()
                    val intro = bookElement.select("p[class=intro]").text()
                    val homepage = url + bookElement.select("p[class=bookname]")
                        .select("a").attr("href")
                    val newestChap = bookElement.select("p[class=data]").select("a")[1].text()
                    bookList.add(
                        Book(bookName, author, intro, imgURL, null, 0,
                            "", newestChap, homepage, 0, false)
                    )
                }
                return bookList
            }
        }
        return bookList
    }

    fun downloadCatalog(homepage: String, listener: CatalogDownloadListener) {
        val doc = getDocument(homepage)
        if (doc != null) {
            val pages = getPageURLList(doc)
            var progress = 0.0
            val count = pages.size.toDouble()
            val chapters = Array(pages.size) {
                ArrayList<String>()
            }
            var index = 0
            val jobs = ArrayList<Deferred<Unit>>()
            CoroutineScope(Dispatchers.IO).launch {
                for (page in pages) {
                    val i = index
                    val job = async {
                        val pageDoc = getDocument(page)
                        if (pageDoc != null) {
                            val chapterURLList = getChapterURLList(pageDoc)
                            val size = chapters.size
                            if (i < size) {
                                chapters[i] = chapterURLList
                            }
                            listener.updateProgress((progress / count * 100).toInt())
                            progress += 1
                        }
                    }
                    jobs.add(job)
                    index += 1
                }
                for (job in jobs) {
                    job.await()
                }
                val chapterList = ArrayList<String>()
                for (list in chapters) {
                    chapterList.addAll(list)
                }
                listener.onFinish(chapterList)
            }
        }
    }

    fun downloadBook(bookName: String, homepage: String, fileDir: File,
                     listener: BookDownloadListener) {
        val doc = getDocument(homepage)
        if (doc != null) {
            val pages = getPageURLList(doc)
            val chapters = ArrayList<String>()
            for (page in pages) {
                val pageDoc = getDocument(page)
                if (pageDoc != null) {
                    chapters.addAll(getChapterURLList(pageDoc))
                }
            }
            val count = chapters.size.toDouble()
            var progress = 0.0
            for (chapter in chapters) {
                val article = getArticle(chapter)
                val dir = File(fileDir, bookName)
                dir.mkdir()
                File(dir.absolutePath, "$bookName.txt").appendText(article)
                listener.updateProgress((progress / count * 100).toInt())
                progress += 1
            }
            listener.onFinish()
        }
    }

    private fun getSearchResult(query: String): String? {
        val searchURL = "$url/search.html"
        val client = OkHttpClient()
        val requestBody = FormBody.Builder()
            .add("searchkey", query)
            .build()
        val request = Request.Builder()
            .url(searchURL)
            .addHeader("cookie","Hm_lvt_0118616aa8ef038e464abd9f469319ac=1632843730")
            .post(requestBody)
            .build()
        val response = client.newCall(request).execute()
        return response.body?.string()
    }

    private fun getDocument(url: String): Document? {
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val response =  client.newCall(request).execute()
        val html = response.body?.string()
        if (html != null) {
            return Jsoup.parse(html)
        }
        return null
    }

    private fun getChapterURLList(doc: Document): ArrayList<String> {
        val elements = doc.select("ul[class=read]")
        val sectionURLList = ArrayList<String>()
        if (!elements.isNullOrEmpty()) {
            val readDoc = Jsoup.parse(elements[0].html())
            val sectionElements = readDoc.select("a")
            for (sectionElement in sectionElements) {
                sectionURLList.add(url+sectionElement.attr("href"))
            }
        }
        return sectionURLList
    }

    private fun getPageURLList(doc: Document): ArrayList<String> {
        val pageList = ArrayList<String>()
        val elements = doc.select("option")
        for (element in elements) {
            val pageURL = url+element.`val`()
            pageList.add(pageURL)
        }
        return pageList
    }

    private fun getTitle(doc: Document): String {
        return doc.select("h1[class=headline]").text()
    }

    private fun getContent(doc: Document): String {
        val elements = doc.select("div[class=content]")
        if (!elements.isNullOrEmpty()) {
            var content = ""
            val contentList = elements[0].html().split("<p>")
            for (i in 0..contentList.size-2) {
                content += contentList[i].replace("</p>", "\n")
            }
            return content
        }
        return ""
    }

    fun getArticle(url: String): String {
        val doc = getDocument(url)
        if (doc != null) {
            val title = getTitle(doc)
            val content = getContent(doc)
            return title + "\n\n" + content
        }
        return ""
    }

    fun getArticle(url: String, listener: ArticleDownloadListener) {
        val doc = getDocument(url)
        if (doc != null) {
            val title = getTitle(doc)
            val content = getContent(doc)
            listener.onFinish(title, content)
        }
    }

}