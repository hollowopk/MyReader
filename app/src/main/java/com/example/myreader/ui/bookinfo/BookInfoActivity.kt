package com.example.myreader.ui.bookinfo

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.view.View
import androidx.lifecycle.ViewModelProvider
import com.example.myreader.R
import com.example.myreader.databinding.ActivityBookInfoBinding
import com.example.myreader.logic.Repository
import com.example.myreader.logic.database.AppDatabase
import com.example.myreader.logic.database.Book
import com.example.myreader.logic.network.MyReaderNetwork
import com.example.myreader.logic.services.DownloadService
import com.example.myreader.logic.utils.showToast
import com.example.myreader.ui.MainActivity
import com.example.myreader.ui.reader.ReaderActivity
import java.io.ByteArrayOutputStream
import java.util.ArrayList
import kotlin.concurrent.thread

class BookInfoActivity : AppCompatActivity() {

    private lateinit var downloadBinder: DownloadService.DownloadBinder

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            downloadBinder = service as DownloadService.DownloadBinder
            downloadBinder.startDownloadBook(book.bookName, book.homepage, cacheDir)
        }

        override fun onServiceDisconnected(name: ComponentName?) {}

    }

    private lateinit var binding: ActivityBookInfoBinding
    private lateinit var viewModel: BookInfoViewModel
    private val bookDao = AppDatabase
        .getDatabase(this).BookDao()
    private lateinit var from: String

    private lateinit var book: Book

    private val readerReqCode = 2

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookInfoBinding.inflate(layoutInflater)
        viewModel = ViewModelProvider(this).get(BookInfoViewModel::class.java)
        setContentView(binding.root)
        supportActionBar?.hide()

        var isInShelf = false

        val bookNameView = binding.infoBookName
        val authorView = binding.infoAuthor
        val bookImgView = binding.infoBookImg
        val introView = binding.infoIntro
        val lastOrNewest = binding.infoLastOrNewest
        val chapterName = binding.infoChapterName
        val addToShelfBtn = binding.infoAddToShelfBtn
        val readBtn = binding.infoStartReadingBtn
        val downloadBtn = binding.infoDownlaodBtn
        val progressBar = binding.infoDownloadProgressBar

        from = intent.getStringExtra("from") ?: ""
        book = intent.getSerializableExtra("book") as Book

        if (from == "shelf") {
            lastOrNewest.text = "上次阅读到："
            chapterName.text = book.lastReadChap
        } else if (from == "search") {
            lastOrNewest.text = "最新章节："
            chapterName.text = book.newestChap
        }
        bookNameView.text = book.bookName
        authorView.text = book.author
        introView.text = book.intro
        bookImgView.setImageResource(R.drawable.load)
        if (book.bookImg != null) {
            bookImgView.setImageBitmap(BitmapFactory
                .decodeByteArray(book.bookImg, 0, book.bookImg!!.size))
        }

        thread {
            val sameBooks = bookDao.findBook(book.bookName, book.author)
            if (sameBooks.isNotEmpty()) {
                isInShelf = true
                runOnUiThread {
                    addToShelfBtn.text = "从书架中移除"
                }
            }
        }

        addToShelfBtn.setOnClickListener {
            if (!isInShelf) {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("book", book)
                intent.putExtra("action", "add")
                setResult(RESULT_OK, intent)
                thread {
                    bookDao.insertBook(book)
                }
                finish()
            } else {
                val intent = Intent(this, MainActivity::class.java)
                intent.putExtra("book", book)
                intent.putExtra("action", "delete")
                setResult(RESULT_OK, intent)
                thread {
                    bookDao.deleteBook(book.bookName, book.author)
                }
                finish()
            }
        }

        readBtn.setOnClickListener {
            if (viewModel.getList().isEmpty()) {
                "正在获取章节信息".showToast(this)
                progressBar.visibility = View.VISIBLE
                Repository.downloadCatalog(book.homepage, object : MyReaderNetwork.CatalogDownloadListener {
                    override fun updateProgress(progress: Int) {
                        runOnUiThread {
                            progressBar.progress = progress
                        }
                    }

                    override fun onFinish(urlList: List<String>) {
                        runOnUiThread {
                            progressBar.visibility = View.GONE
                        }
                        viewModel.setList(urlList)
                        startReadWithList(urlList)
                    }
                })

            } else {
                startReadWithList(viewModel.getList())
            }
        }

        downloadBtn.setOnClickListener {
            val serviceIntent = Intent(this, DownloadService::class.java)
            bindService(serviceIntent, connection, Context.BIND_AUTO_CREATE)
        }

    }

    private fun startReadWithList(urlList: List<String>) {
        if (urlList.isEmpty()) {
            Looper.prepare()
            "没有章节信息！".showToast(this)
            Looper.loop()
        } else {
            val intent = Intent(
                this,
                ReaderActivity::class.java
            )
            intent.putStringArrayListExtra(
                "urlList",
                ArrayList(urlList)
            )
            startActivityForResult(intent, readerReqCode)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            readerReqCode -> {
                if (data != null) {
                    book.lastReadChap = data.getStringExtra("lastReadChapter") ?: ""
                    book.lastRead = System.currentTimeMillis()
                    if (from == "shelf") {
                        binding.infoChapterName.text = book.lastReadChap
                    }
                    thread {
                        bookDao.updateBook(book)
                    }
                }
            }
        }
    }

    override fun onBackPressed() {
        val intent = Intent()
        intent.putExtra("action", "update")
        intent.putExtra("book", book)
        setResult(RESULT_OK, intent)
        finish()
    }

}