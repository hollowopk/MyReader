package com.example.myreader.ui.bookshelf

import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.myreader.R
import com.example.myreader.logic.ClickListener
import com.example.myreader.logic.database.Book

class BookshelfAdapter(private val fragment: BookshelfFragment,
                       private val bookList: List<Book>,
                       private val clickListener: ClickListener) :
    RecyclerView.Adapter<BookshelfAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookName: TextView = view.findViewById(R.id.bookshelf_bookName)
        val author: TextView = view.findViewById(R.id.bookshelf_author)
        val bookImg: ImageView = view.findViewById(R.id.bookshelf_bookImg)
        val lastChapter: TextView = view.findViewById(R.id.bookshelf_lastChapter)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.saved_book_item,parent,false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val book = bookList[holder.adapterPosition]
            clickListener.onClick(book)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = bookList[position]
        holder.bookName.text = book.bookName
        holder.author.text = book.author
        holder.lastChapter.text = book.lastReadChap
        if (book.bookImg != null) {
            holder.bookImg.setImageBitmap(
                BitmapFactory.decodeByteArray(book.bookImg, 0,
                    book.bookImg!!.size))
        } else {
            holder.bookImg.setImageResource(R.drawable.load)
        }
    }

    override fun getItemCount() = bookList.size


}