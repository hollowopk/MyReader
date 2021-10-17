package com.example.myreader.ui.search

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.animation.GlideAnimation
import com.bumptech.glide.request.target.SimpleTarget
import com.example.myreader.R
import com.example.myreader.logic.ClickListener
import com.example.myreader.logic.database.Book
import java.io.ByteArrayOutputStream

class SearchAdapter(private val fragment: SearchFragment,
                    private val bookList: List<Book>,
                    private val clickListener: ClickListener) :
    RecyclerView.Adapter<SearchAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val bookIMG: ImageView = view.findViewById(R.id.result_bookImg)
        val bookName: TextView = view.findViewById(R.id.result_bookName)
        val author: TextView = view.findViewById(R.id.result_author)
        val intro: TextView = view.findViewById(R.id.result_intro)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.result_item,parent,false)
        val holder = ViewHolder(view)
        holder.itemView.setOnClickListener {
            val book = bookList[holder.adapterPosition]
            clickListener.onClick(book)
        }
        return holder
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val book = bookList[position]
        if (book.bookImg != null) {
            holder.bookIMG.setImageBitmap(BitmapFactory
                .decodeByteArray(book.bookImg, 0, book.bookImg!!.size))
        } else {
            holder.bookIMG.setImageResource(R.drawable.load)
            Glide.with(fragment.activity)
                .load(book.bookImgURL)
                .asBitmap()
                .into(object : SimpleTarget<Bitmap>() {
                    override fun onResourceReady(
                        resource: Bitmap?,
                        glideAnimation: GlideAnimation<in Bitmap>?
                    ) {
                        if (resource == null) {
                            book.bookImg = null
                        } else {
                            val baos = ByteArrayOutputStream()
                            resource.compress(Bitmap.CompressFormat.PNG, 100, baos)
                            val bytes = baos.toByteArray()
                            book.bookImg = bytes
                            holder.bookIMG.setImageBitmap(resource)
                        }
                    }
                })
        }
        holder.bookName.text = book.bookName
        holder.author.text = book.author
        holder.intro.text = book.intro
    }

    override fun getItemCount() = bookList.size

}