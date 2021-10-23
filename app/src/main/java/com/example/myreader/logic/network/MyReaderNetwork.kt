package com.example.myreader.logic.network

import com.example.myreader.logic.database.Book
import kotlinx.coroutines.*
import okhttp3.*
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import java.io.File
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import kotlin.math.min

object MyReaderNetwork {

    private const val url = "https://m.shuhaige.tw"

    private val client = OkHttpClient()

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
            CoroutineScope(Dispatchers.IO).launch {
                val chapters = getChapters(pages)
                val articles = getArticles(chapters, listener)
                writeToFile(bookName, fileDir, articles)
                listener.onFinish()
            }
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
        val request = Request.Builder().url(url).build()
        val response =  client.newCall(request).execute()
        val html = response.body?.string()
        if (html != null) {
            return Jsoup.parse(html)
        }
        return null
    }

    private suspend fun getDocumentAsync(url: String): Document? {
        val request = Request.Builder().url(url).build()
        return suspendCoroutine {
            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    it.resumeWithException(e)
                }
                override fun onResponse(call: Call, response: Response) {
                    val html = response.body?.string()
                    if (html != null) {
                        it.resume(Jsoup.parse(html))
                    }
                    response.close()
                }

            })
        }
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

    private suspend fun getArticleAsync(url: String): String {
        val doc = getDocumentAsync(url)
        if (doc != null) {
            val title = getTitle(doc)
            val content = getContent(doc)
            return title + "\n\n\n" + content
        }
        return ""
    }

    fun getArticle(url: String, listener: ArticleDownloadListener) {
        CoroutineScope(Dispatchers.IO).launch {
            val doc = getDocumentAsync(url)
            if (doc != null) {
                val title = getTitle(doc)
                val content = getContent(doc)
                listener.onFinish(title, content)
            }
        }
    }

    private suspend fun getChapters(pages: List<String>) = coroutineScope {
        val chapterList = Array(pages.size) {
            ArrayList<String>()
        }
        val chapters = ArrayList<String>()
        val jobs = ArrayList<Job>()
        var chapterIndex = 0
        for (page in pages) {
            val i = chapterIndex
            chapterIndex += 1
            jobs.add(launch {
                val pageDoc = getDocumentAsync(page)
                if (pageDoc != null) {
                    chapterList[i] = getChapterURLList(pageDoc)
                }
            })
        }
        for (job in jobs) {
            job.join()
        }
        for (list in chapterList) {
            chapters.addAll(list)
        }
        chapters
    }

    private suspend fun getArticles(chapters: ArrayList<String>,
                                    listener: BookDownloadListener)
    = coroutineScope {
        val jobs = ArrayList<Job>()
        var articleIndex = 0
        var progress = 0.0
        val sum = chapters.size.toDouble()
        val articles = Array(chapters.size) {
            ""
        }
        for (chapter in chapters) {
            val index = articleIndex
            articleIndex += 1
            jobs.add(launch {
                articles[index] = getArticleAsync(chapter)
                listener.updateProgress((progress / sum * 100).toInt())
                progress += 1
            })
        }
        for (job in jobs) {
            job.join()
        }
        articles
    }

    private fun writeToFile(bookName: String, fileDir: File,
                            articles: Array<String>) {
        val dir = File(fileDir, bookName)
        if (dir.exists()) {
            dir.delete()
        }
        dir.mkdir()
        val file = File(dir.absolutePath, "$bookName.txt")
        for (article in articles) {
            file.appendText(article)
        }
    }

}