package com.example.myreader.logic.services

import android.app.*
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.widget.RemoteViews
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import com.example.myreader.R
import com.example.myreader.logic.Repository
import com.example.myreader.logic.database.AppDatabase
import com.example.myreader.logic.database.Book
import com.example.myreader.logic.network.MyReaderNetwork
import com.example.myreader.logic.utils.showToast
import com.example.myreader.ui.bookinfo.BookInfoActivity
import java.io.File
import kotlin.concurrent.thread

class DownloadService : Service() {

    private lateinit var database: AppDatabase

    private val binder = DownloadBinder()
    private lateinit var manager: NotificationManager
    private lateinit var notification: Notification
    private lateinit var layout: RemoteViews

    init {
        database = AppDatabase.getDatabase(this)
    }

    inner class DownloadBinder : Binder() {

        fun startDownloadBook(book: Book, fileDir: File) {
            Repository.downloadBook(book.bookName, book.homepage, fileDir,
                object : MyReaderNetwork.BookDownloadListener {
                    override fun updateProgress(progress: Int) {
                        layout.setProgressBar(R.id.notification_progress, 100,
                            progress, false)
                        manager.notify(1, notification)
                    }

                    override fun onFinish() {
                        stopForeground(true)
                        book.isDownload = true
                        val bookDao = database.BookDao()
                        thread {
                            bookDao.updateBook(book)
                        }
                    }
                })
        }

    }

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        manager = getSystemService(Context.NOTIFICATION_SERVICE) as
                NotificationManager
        val channel = NotificationChannel("download_service", "下载",
            NotificationManager.IMPORTANCE_HIGH)
        manager.createNotificationChannel(channel)
        layout = RemoteViews(packageName, R.layout.download_notification)
        notification = NotificationCompat.Builder(this,
            "download_service")
            .setSmallIcon(R.drawable.small_icon)
            .setLargeIcon(BitmapFactory.decodeResource(resources, R.drawable.large_icon))
            .setCustomContentView(layout)
            .build()
        startForeground(1, notification)
    }

}