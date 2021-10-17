package com.example.myreader.logic.database

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.io.Serializable

@Entity
data class Book(var bookName: String, var author: String,
                     var intro: String, var bookImgURL: String,
                     var bookImg: ByteArray?, var progress: Int,
                     var lastReadChap: String, var newestChap: String,
                     var homepage: String, var lastRead: Long,
                    var isDownload: Boolean) : Serializable{

    @PrimaryKey(autoGenerate = true)
    var id: Long = 0

}